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
