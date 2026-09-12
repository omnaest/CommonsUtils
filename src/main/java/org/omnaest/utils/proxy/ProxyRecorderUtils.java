package org.omnaest.utils.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.omnaest.utils.ClassUtils;
import org.omnaest.utils.JsonUtils;
import org.omnaest.utils.ObjectUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.ProxyUtils;
import org.omnaest.utils.ProxyUtils.MethodInvocationHandler;
import org.omnaest.utils.ReflectionUtils;
import org.omnaest.utils.ReflectionUtils.Method;
import org.omnaest.utils.StreamUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Utility to record and interpret proxy interactions
 * 
 * @author omnaest
 */
@NoArgsConstructor
public class ProxyRecorderUtils
{
    public static ProxyRecorder recorder()
    {
        return new ProxyRecorder() {
            @Override
            public <P, R> ProxyInteractionRecording<P, R> recordInteraction(Class<P> proxyType, Function<P, R> interaction)
            {
                List<MethodInvocationRecording> methodInvocationRecordings = new ArrayList<>();
                AtomicReference<Class<?>> returnTypeHolder = new AtomicReference<>();
                interaction.apply(this.createRecorderProxy(proxyType, methodInvocationRecordings::add,
                                                           this.createReturnTypeCapturerAndDefaultValueProvider(returnTypeHolder)));

                return this.createProxyInteractionRecording(proxyType, methodInvocationRecordings, returnTypeHolder.get());
            }

            private Function<Class<?>, Object> createReturnTypeCapturerAndDefaultValueProvider(AtomicReference<Class<?>> returnTypeHolder)
            {
                return returnType ->
                {
                    returnTypeHolder.set(returnType);
                    return ObjectUtils.getPrimitiveDefault(returnType);
                };
            }

            private <R, P> ProxyInteractionRecording<P, R> createProxyInteractionRecording(Class<P> proxyType, List<MethodInvocationRecording> methodInvocationRecordings, Class<?> returnType)
            {
                return new ProxyInteractionRecording<>() {

                    @Override
                    public R andRemotelyInvoke(Function<ProxyRecording, R> recording)
                    {
                        return recording.apply(ProxyRecording.builder()
                                                             .type(proxyType.getName())
                                                             .methodInvocationRecordings(methodInvocationRecordings)
                                                             .build());
                    }

                    @Override
                    public R andInvokeOn(P instance)
                    {
                        return this.andRemotelyInvoke(recording -> ProxyRecorderUtils.interpreter()
                                                                                     .useRecording(recording)
                                                                                     .andInvokeOn(instance));
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public R andRemotelyInvokeTyped(BiFunction<ProxyRecording, Class<R>, R> recordingConsumer)
                    {
                        return this.andRemotelyInvoke(recording -> recordingConsumer.apply(recording, (Class<R>) returnType));
                    }
                };
            }

            private <P> P createRecorderProxy(Class<P> proxyType, Consumer<MethodInvocationRecording> methodInvocationRecordingConsumer, Function<Class<?>, Object> terminatingValueProvider)
            {
                MethodInvocationHandler methodInvocationHandler = (method, arguments) ->
                {
                    List<MethodInvocationRecording> returnTypeMethodInvocationRecordings = new ArrayList<>();
                    methodInvocationRecordingConsumer.accept(MethodInvocationRecording.builder()
                                                                                      .methodName(method.getName())
                                                                                      .parameters(arguments.stream()
                                                                                                           .map(argument -> MethodInvocationRecording.Parameter.builder()
                                                                                                                                                               .parameterType(argument.getType()
                                                                                                                                                                                      .getName())
                                                                                                                                                               .value(JsonUtils.toJsonNode(argument.get()))
                                                                                                                                                               .build())
                                                                                                           .toList())
                                                                                      .returnTypeMethodInvocationRecordings(returnTypeMethodInvocationRecordings)
                                                                                      .build());

                    Class<?> returnType = method.getReturnType();
                    if (returnType.isInterface())
                    {
                        return this.createRecorderProxy(returnType, returnTypeMethodInvocationRecordings::add, terminatingValueProvider);
                    }
                    else if (org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(returnType) || String.class.equals(returnType))
                    {
                        return terminatingValueProvider.apply(returnType);
                    }
                    else
                    {
                        return null;
                    }
                };
                return ProxyUtils.builder()
                                 .of(proxyType)
                                 .withHandler(PredicateUtils.allMatching(), methodInvocationHandler)
                                 .build();
            }

            @Override
            public <P> ProxyVoidInteractionRecording<P> recordVoidInteraction(Class<P> proxyType, Consumer<P> interaction)
            {
                ProxyInteractionRecording<P, Class<Void>> proxyInteractionRecording = this.recordInteraction(proxyType, proxy ->
                {
                    if (interaction != null)
                    {
                        interaction.accept(proxy);
                    }
                    return Void.class;
                });
                return new ProxyVoidInteractionRecording<P>() {
                    @Override
                    public void andInvokeOn(P instance)
                    {
                        proxyInteractionRecording.andInvokeOn(instance);
                    }

                    @Override
                    public void andRemotelyInvoke(Consumer<ProxyRecording> recordingConsumer)
                    {
                        proxyInteractionRecording.andRemotelyInvoke(recording ->
                        {
                            recordingConsumer.accept(recording);
                            return Void.class;
                        });
                    }
                };
            }

            @Override
            public <P, R> P createRecordingProxy(Class<P> proxyType, Function<ProxyInteractionRecording<P, ?>, R> recording)
            {
                List<MethodInvocationRecording> methodInvocationRecordings = new ArrayList<>();
                return this.createRecorderProxy(proxyType, methodInvocationRecordings::add,
                                                returnType -> recording.apply(this.createProxyInteractionRecording(proxyType, methodInvocationRecordings,
                                                                                                                   returnType)));
            }
        };
    }

    @Data
    @Builder
    private static class RawMethodParameter
    {
        private final Class<?> parameterType;
        private final Object   value;
    }

    public static ProxyRecordingInterpreter interpreter()
    {
        return new ProxyRecordingInterpreter() {

            @Override
            public LoadedInterpreter useRecording(ProxyRecording recording)
            {
                return new LoadedInterpreter() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <R, P> R andInvokeOn(P instance)
                    {
                        List<MethodInvocationRecording> methodInvocationRecordings = recording.getMethodInvocationRecordings();
                        return StreamUtils.reduceWithPrevious(methodInvocationRecordings.stream(),
                                                              (previous, recording) -> this.invokeRecordingOnInstance(recording, Optional.ofNullable(previous)
                                                                                                                                         .orElse(instance)))
                                          .map(result -> (R) result)
                                          .orElse(null);
                    }

                    private Object invokeRecordingOnInstance(MethodInvocationRecording recording, Object currentInstance)
                    {
                        String methodName = recording.getMethodName();
                        List<RawMethodParameter> parameters = recording.getParameters()
                                                                       .stream()
                                                                       .map(parameter -> ClassUtils.findClassByName(parameter.getParameterType())
                                                                                                   .map(parameterType -> RawMethodParameter.builder()
                                                                                                                                           .parameterType(parameterType)
                                                                                                                                           .value(JsonUtils.toObjectWithType(parameter.getValue(),
                                                                                                                                                                             parameterType))
                                                                                                                                           .build()))
                                                                       .filter(Optional::isPresent)
                                                                       .map(Optional::get)
                                                                       .toList();

                        Object result = Optional.ofNullable(ReflectionUtils.of(currentInstance.getClass())
                                                                           .getMethod(methodName, parameters.stream()
                                                                                                            .map(RawMethodParameter::getParameterType)
                                                                                                            .toList())
                                                                           .orElseThrow(() -> new IllegalStateException("Unabled to find method: "
                                                                                                                        + methodName)))
                                                .map(Method::getRawMethod)
                                                .map(rawMethod ->
                                                {
                                                    try
                                                    {
                                                        return rawMethod.invoke(currentInstance, parameters.stream()
                                                                                                           .map(RawMethodParameter::getValue)
                                                                                                           .toArray());
                                                    }
                                                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                                                    {
                                                        throw new IllegalStateException("Unabled to invoke recording on " + methodName, e);
                                                    }
                                                })
                                                .orElse(null);

                        List<MethodInvocationRecording> returnTypeMethodInvocationRecordings = recording.getReturnTypeMethodInvocationRecordings();
                        if (!returnTypeMethodInvocationRecordings.isEmpty())
                        {
                            return StreamUtils.reduceWithPrevious(recording.getReturnTypeMethodInvocationRecordings()
                                                                           .stream(),
                                                                  (previous, recording2) -> this.invokeRecordingOnInstance(recording2,
                                                                                                                           Optional.ofNullable(previous)
                                                                                                                                   .orElse(result)))
                                              .orElse(null);
                        }
                        else
                        {
                            return result;
                        }
                    }
                };
            }
        };
    }

    public static interface ProxyRecorder
    {

        public <P, R> ProxyInteractionRecording<P, R> recordInteraction(Class<P> proxyType, Function<P, R> interaction);

        public <P> ProxyVoidInteractionRecording<P> recordVoidInteraction(Class<P> proxyType, Consumer<P> interaction);

        public <P, R> P createRecordingProxy(Class<P> proxyType, Function<ProxyInteractionRecording<P, ?>, R> recording);

    }

    public static interface ProxyInteractionRecording<P, R>
    {

        public R andInvokeOn(P instance);

        public R andRemotelyInvoke(Function<ProxyRecording, R> recording);

        public R andRemotelyInvokeTyped(BiFunction<ProxyRecording, Class<R>, R> recording);

    }

    public static interface ProxyVoidInteractionRecording<P>
    {

        public void andInvokeOn(P instance);

        public void andRemotelyInvoke(Consumer<ProxyRecording> recording);

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter(value = AccessLevel.PRIVATE)
    public static class ProxyRecording
    {
        @JsonProperty
        private String                          type;

        @JsonProperty
        @Default
        private List<MethodInvocationRecording> methodInvocationRecordings = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter(value = AccessLevel.PRIVATE)
    public static class MethodInvocationRecording
    {
        private String                          methodName;
        private List<Parameter>                 parameters;

        @Default
        private List<MethodInvocationRecording> returnTypeMethodInvocationRecordings = new ArrayList<>();

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @Setter(value = AccessLevel.PRIVATE)
        public static class Parameter
        {
            private String   parameterType;
            private JsonNode value;
        }
    }

    public static interface ProxyRecordingInterpreter
    {
        public LoadedInterpreter useRecording(ProxyRecording recording);
    }

    public static interface LoadedInterpreter
    {

        public <R, P> R andInvokeOn(P instance);

    }
}
