package org.omnaest.utils.processor.repeating;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.ProcessorUtils;
import org.omnaest.utils.ProcessorUtils.CacheContent;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor.AggregatedElement;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor.ProcessedElement;
import org.omnaest.utils.repository.ElementRepositoryUtils;
import org.omnaest.utils.repository.MapElementRepository;

/**
 * @see DefaultRepeatingFilteredProcessor
 * @author omnaest
 */
public class DefaultRepeatingFilteredProcessorTest
{
    private RepeatingFilteredProcessor<String> processor = RepeatingFilteredProcessor.newInstance(String.class)
                                                                                     .withDistributionFactor(0.2, 100);

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
        int numberOfElements = 100;
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

    @Test
    public void testProcessWithMemoryAndContentFileCache() throws Exception
    {
        int numberOfElements = 20;
        List<AggregatedElement<String>> results = ProcessorUtils.newRepeatingFilteredProcessorWithInMemoryCacheAndContentRepository(ElementRepositoryUtils.newJsonHashFileIndexRepository(FileUtils.createRandomTempDirectory(),
                                                                                                                                                                                          10,
                                                                                                                                                                                          1,
                                                                                                                                                                                          Integer.class,
                                                                                                                                                                                          CacheContent.class),
                                                                                                                                    String.class)
                                                                .withDistributionFactor(0.1, numberOfElements)
                                                                .process(() -> Stream.concat(IntStream.range(0, numberOfElements)
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

    @Test
    public void testProcessWithMemoryAndFileCache() throws Exception
    {
        int numberOfElements = 20;
        MapElementRepository<String, String> elementRepository = ElementRepositoryUtils.newJsonHashFileIndexRepository(FileUtils.createRandomTempDirectory(),
                                                                                                                       10, 1, String.class, String.class);
        List<AggregatedElement<String>> results = ProcessorUtils.newRepeatingFilteredProcessorWithInMemoryCacheAndRepository(elementRepository, String.class)
                                                                .withDistributionFactor(0.1, numberOfElements)
                                                                .process(() -> Stream.concat(IntStream.range(0, numberOfElements)
                                                                                                      .boxed(),
                                                                                             IntStream.range(0, numberOfElements)
                                                                                                      .boxed()))
                                                                .withAggregatingOperation(element -> "key" + element, element -> "value" + element,
                                                                                          (a, b) -> a + b)
                                                                .collect(Collectors.toList());

        // validate the result stream
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

        // validate the content of the repository
        assertEquals(IntStream.range(0, numberOfElements)
                              .mapToObj(index -> "key" + index)
                              .collect(Collectors.toSet()),
                     elementRepository.ids()
                                      .collect(Collectors.toSet()));
        assertEquals(IntStream.range(0, numberOfElements)
                              .boxed()
                              .map(index -> "value" + index + "value" + index)
                              .collect(Collectors.toSet()),
                     elementRepository.values()
                                      .collect(Collectors.toSet()));

    }

}
