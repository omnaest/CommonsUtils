package org.omnaest.utils.cyclic;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.omnaest.utils.cyclic.CycleProcessor.CycleProcessorBuilder.TypedCycleProcessorBuilder.TypedCycleProcessorBuilderLoaded;

public interface CycleProcessor<I, W>
{

    /**
     * A operation on a cyclic read and written window
     * 
     * @author omnaest
     * @param <W>
     * @param <R>
     */
    public static interface CyclicWindowOperation<W, R>
    {
        public R accept(W w);
    }

    /**
     * Executes the given {@link CyclicWindowOperation} at the point when the respective window is read
     * 
     * @param index
     *            of the window that the {@link CyclicWindowOperation} should operate on
     * @param operation
     * @return
     */
    public <R> R execute(I windowIndex, CyclicWindowOperation<W, R> operation);

    /**
     * Builder for a {@link CycleProcessor}
     * 
     * @author omnaest
     */
    public static interface CycleProcessorBuilder
    {
        public <I, W> TypedCycleProcessorBuilderLoaded<I, W> withWindowReader(Function<I, W> windowReaderFunction);

        public static interface TypedCycleProcessorBuilder<I, W>
        {
            public TypedCycleProcessorBuilderLoaded<I, W> andWindowWriter(BiConsumer<I, W> windowWriter);

            public static interface TypedCycleProcessorBuilderLoaded<I, W> extends TypedCycleProcessorBuilder<I, W>
            {
                public CycleProcessor<I, W> build();
            }
        }

    }

    /**
     * Builder for a {@link CycleProcessor}
     * 
     * @return
     */
    public static CycleProcessorBuilder builder()
    {
        return new CycleProcessorBuilder()
        {

            @Override
            public <I, W> TypedCycleProcessorBuilderLoaded<I, W> withWindowReader(Function<I, W> windowReaderFunction)
            {

                return new TypedCycleProcessorBuilderLoaded<I, W>()
                {
                    private BiConsumer<I, W> windowWriter = (i, w) ->
                    {
                    };

                    @Override
                    public TypedCycleProcessorBuilderLoaded<I, W> andWindowWriter(BiConsumer<I, W> windowWriter)
                    {
                        this.windowWriter = windowWriter;
                        return this;
                    }

                    @Override
                    public CycleProcessor<I, W> build()
                    {
                        return new DefaultCycleProcessor<I, W>(windowReaderFunction, this.windowWriter);
                    }

                };
            }

        };
    }
}
