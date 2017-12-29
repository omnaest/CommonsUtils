package org.omnaest.utils.cyclic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class DefaultCycleProcessorTest
{
    private CycleProcessor<Integer, List<String>> processor = CycleProcessor.builder()
                                                                            .withWindowReader((Integer index) ->
                                                                            {
                                                                                List<String> retlist = new ArrayList<>();
                                                                                retlist.add("" + index);
                                                                                retlist.add("" + index);
                                                                                return retlist;
                                                                            })
                                                                            .build();

    @Test
    public void testExecute() throws Exception
    {
        IntStream.range(0, 1000)
                 .parallel()
                 .forEach(ii ->
                 {
                     int index = ii % 5;
                     String result = this.processor.execute(index, window -> window.stream()
                                                                                   .collect(Collectors.joining()));

                     assertEquals("" + index + "" + index, result);
                 });

    }

}
