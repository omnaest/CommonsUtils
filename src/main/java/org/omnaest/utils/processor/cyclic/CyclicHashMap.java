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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omnaest.utils.MapUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.map.CRUDMap;
import org.omnaest.utils.map.MapDecorator;

public class CyclicHashMap<K, V> extends MapDecorator<K, V>
{
    private CycleProcessor<Integer, Map<K, V>> processor;
    private int                                numberOfCycleBlocks;

    @SuppressWarnings("unchecked")
    public CyclicHashMap(int numberOfCycleBlocks, Cache cache)
    {
        this(numberOfCycleBlocks, index -> cache.computeIfAbsent(String.valueOf(index), () -> new LinkedHashMap<>(), Map.class), (index, window) ->
        {
            cache.put(String.valueOf(index), window);
        });
    }

    public CyclicHashMap(int numberOfCycleBlocks, Function<Integer, Map<K, V>> windowReaderFunction, BiConsumer<Integer, Map<K, V>> windowWriter)
    {
        super(() -> null);
        this.numberOfCycleBlocks = numberOfCycleBlocks;

        this.processor = CycleProcessor.builder()
                                       .<Integer, Map<K, V>>withWindowReader(windowReaderFunction)
                                       .andWindowWriter(windowWriter)
                                       .build();

        CRUDMap<K, V> crudMap = this.newCRUDMapInstance();
        Map<K, V> map = MapUtils.toMap(crudMap);
        this.map = () -> map;

    }

    private CRUDMap<K, V> newCRUDMapInstance()
    {

        return new CRUDMap<K, V>()
        {

            @Override
            public int size()
            {
                return IntStream.range(0, CyclicHashMap.this.numberOfCycleBlocks)
                                .map(index -> CyclicHashMap.this.processor.execute(index, map -> map.size()))
                                .sum();
            }

            @Override
            public boolean containsKey(K key)
            {
                boolean retval = false;

                if (key != null)
                {
                    int windowIndex = this.determineWindowIndexFromKey(key);
                    retval = CyclicHashMap.this.processor.execute(windowIndex, map -> map.containsKey(key));
                }

                return retval;
            }

            private int determineWindowIndexFromKey(K key)
            {
                return key.hashCode() % CyclicHashMap.this.numberOfCycleBlocks;
            }

            @Override
            public V get(K key)
            {
                V retval = null;

                if (key != null)
                {
                    int windowIndex = this.determineWindowIndexFromKey(key);
                    retval = CyclicHashMap.this.processor.execute(windowIndex, map -> map.get(key));
                }

                return retval;
            }

            @Override
            public V put(K key, V value)
            {
                V retval = null;

                if (key != null)
                {
                    int windowIndex = this.determineWindowIndexFromKey(key);
                    retval = CyclicHashMap.this.processor.execute(windowIndex, map -> map.put(key, value));
                }

                return retval;
            }

            @Override
            public V remove(K key)
            {
                V retval = null;

                if (key != null)
                {
                    int windowIndex = this.determineWindowIndexFromKey(key);
                    retval = CyclicHashMap.this.processor.execute(windowIndex, map -> map.remove(key));
                }

                return retval;
            }

            @Override
            public void clear()
            {
                IntStream.range(0, CyclicHashMap.this.numberOfCycleBlocks)
                         .forEach(index -> CyclicHashMap.this.processor.execute(index, map ->
                         {
                             map.clear();
                             return null;
                         }));

            }

            @Override
            public Set<K> keySet()
            {
                return Collections.unmodifiableSet(IntStream.range(0, CyclicHashMap.this.numberOfCycleBlocks)
                                                            .mapToObj(index -> CyclicHashMap.this.processor.execute(index, map -> map.keySet()))
                                                            .flatMap(set -> set.stream())
                                                            .collect(Collectors.toSet()));
            }
        };
    }

}
