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
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

public class ClassUtilsTest
{

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testLoadResource() throws Exception
    {
        Map map = ClassUtils.loadResource(ClassUtilsTest.class, "test_load.txt")
                            .orElseThrow(() -> new IllegalStateException("no resource found"))
                            .asJson()
                            .as(HashMap.class);
        assertEquals(Arrays.asList("field")
                           .stream()
                           .collect(Collectors.toSet()),
                     map.keySet());
        assertEquals(Arrays.asList("value")
                           .stream()
                           .collect(Collectors.toSet()),
                     map.values()
                        .stream()
                        .collect(Collectors.toSet()));
    }

}
