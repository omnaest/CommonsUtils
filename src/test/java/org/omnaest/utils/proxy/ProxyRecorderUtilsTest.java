package org.omnaest.utils.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.omnaest.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProxyRecorderUtilsTest
{

    @Test
    public void testRecorderDirectInvocation()
    {
        String result = ProxyRecorderUtils.recorder()
                                          .recordInteraction(SomeService.class, service -> service.withParameter(10)
                                                                                                  .getSomething())
                                          .andInvokeOn(this.createDummyServiceInstance());
        assertEquals("10", result);
    }

    @Test
    public void testRecorderTransferInvocation()
    {
        String result = ProxyRecorderUtils.recorder()
                                          .recordInteraction(SomeService.class, service -> service.withParameter(Parameter.of(10))
                                                                                                  .getSomething())
                                          .andRemotelyInvoke(recording -> ProxyRecorderUtils.interpreter()
                                                                                            .useRecording(JsonUtils.clone(recording))
                                                                                            .andInvokeOn(this.createDummyServiceInstance()));
        assertEquals("10", result);
    }

    @Test
    public void testRecorderStandaloneProxyDirectInvocation()
    {
        String result = ProxyRecorderUtils.recorder()
                                          .createRecordingProxy(SomeService.class, recording -> recording.andInvokeOn(this.createDummyServiceInstance()))
                                          .withParameter(10)
                                          .getSomething();
        assertEquals("10", result);
    }

    @Test
    public void testRecorderDirectVoidInvocation()
    {
        SomeService instance = this.createDummyServiceInstance();
        ProxyRecorderUtils.recorder()
                          .recordVoidInteraction(SomeService.class, service -> service.withParameter(10)
                                                                                      .doSomething())
                          .andInvokeOn(instance);
        assertEquals("11", instance.getSomething());
    }

    @Test
    public void testRecorderTransferVoidInvocation()
    {
        SomeService instance = this.createDummyServiceInstance();
        ProxyRecorderUtils.recorder()
                          .recordVoidInteraction(SomeService.class, service -> service.withParameter(10)
                                                                                      .doSomething())
                          .andRemotelyInvoke(recording -> ProxyRecorderUtils.interpreter()
                                                                            .useRecording(JsonUtils.clone(recording))
                                                                            .andInvokeOn(instance));
        assertEquals("11", instance.getSomething());
    }

    private SomeService createDummyServiceInstance()
    {
        return new SomeServiceImpl();
    }

    private static class SomeServiceImpl implements SomeService
    {
        private Parameter value;

        @Override
        public SomeService withParameter(Parameter value)
        {
            this.value = value;
            return this;
        }

        @Override
        public String getSomething()
        {
            return "" + Optional.ofNullable(this.value)
                                .map(Parameter::getValue)
                                .map(String::valueOf)
                                .orElse("");
        }

        @Override
        public SomeService withParameter(int value)
        {
            return this.withParameter(Parameter.of(value));
        }

        @Override
        public void doSomething()
        {
            this.value = Parameter.of(this.value.getValue() + 1);
        }
    }

    protected static interface SomeService
    {
        public SomeService withParameter(int value);

        public SomeService withParameter(Parameter value);

        public String getSomething();

        public void doSomething();
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    protected static class Parameter
    {
        @JsonProperty
        private int value;
    }

}
