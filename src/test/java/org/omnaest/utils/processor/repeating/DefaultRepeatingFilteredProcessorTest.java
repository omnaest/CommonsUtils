package org.omnaest.utils.processor.repeating;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor.AggregatedElement;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor.ProcessedElement;

/**
 * @see DefaultRepeatingFilteredProcessor
 * @author omnaest
 */
public class DefaultRepeatingFilteredProcessorTest
{
    private RepeatingFilteredProcessor<String> processor = RepeatingFilteredProcessor.newInstance(String.class)
                                                                                     .withDistributionFactor(0.2, 10);

    @Test
    public void testProcessingWithDistinctKeys() throws Exception
    {
        int numberOfElements = 100;
        List<ProcessedElement<Integer, String>> results = this.processor.process(() -> IntStream.range(0, numberOfElements)
                                                                                                .boxed())
                                                                        .withOperation(element -> "key" + element, element -> "value" + element)
                                                                        .collect(Collectors.toList());
        assertEquals(IntStream.range(0, numberOfElements)
                              .mapToObj(index -> "key" + index)
                              .collect(Collectors.toSet()),
                     results.stream()
                            .map(pe -> pe.getId())
                            .collect(Collectors.toSet()));
        assertEquals(IntStream.range(0, numberOfElements)
                              .mapToObj(index -> "value" + index)
                              .collect(Collectors.toSet()),
                     results.stream()
                            .map(pe -> pe.getResult())
                            .collect(Collectors.toSet()));
    }

    @Test
    public void testProcessWithMerging() throws Exception
    {
        int numberOfElements = 1;
        List<AggregatedElement<String>> results = this.processor.process(() -> Stream.concat(IntStream.range(0, numberOfElements)
                                                                                                      .boxed(),
                                                                                             IntStream.range(0, numberOfElements)
                                                                                                      .boxed()))
                                                                .withAggregatingOperation(element -> "key" + element, element -> "value" + element,
                                                                                          (a, b) -> a + b)
                                                                .collect(Collectors.toList());
        assertEquals(IntStream.range(0, numberOfElements)
                              .mapToObj(index -> "key" + index)
                              .collect(Collectors.toSet()),
                     results.stream()
                            .map(pe -> pe.getId())
                            .collect(Collectors.toSet()));
        assertEquals(IntStream.range(0, numberOfElements)
                              .boxed()
                              .map(index -> "value" + index + "value" + index)
                              .collect(Collectors.toSet()),
                     results.stream()
                            .map(pe -> pe.getResult())
                            .collect(Collectors.toSet()));
    }

}
