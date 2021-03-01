/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils.processor.cyclic;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.omnaest.utils.processor.cyclic.CycleProcessor.CycleProcessorBuilder.TypedCycleProcessorBuilder.TypedCycleProcessorBuilderLoaded;

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
