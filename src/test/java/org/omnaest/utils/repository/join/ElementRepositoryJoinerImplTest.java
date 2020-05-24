package org.omnaest.utils.repository.join;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.repository.ElementRepositoryUtils;
import org.omnaest.utils.repository.ImmutableElementRepository;
import org.omnaest.utils.repository.IndexElementRepository;

public class ElementRepositoryJoinerImplTest
{

    @Test
    public void testInner() throws Exception
    {
        IndexElementRepository<String> repositoryLeft = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();
        IndexElementRepository<String> repositoryRight = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();

        ImmutableElementRepository<Long, BiElement<String, String>> result = repositoryLeft.join(repositoryRight)
                                                                                           .inner();

        repositoryLeft.put(0l, "l0");
        repositoryLeft.put(1l, "l1");

        repositoryRight.put(1l, "r1");
        repositoryRight.put(2l, "r2");

        assertEquals(1, result.size());
        assertEquals(BiElement.of("l1", "r1"), result.get(1l));

        assertEquals(Arrays.asList(1l), result.ids()
                                              .collect(Collectors.toList()));
    }

    @Test
    public void testOuter() throws Exception
    {
        IndexElementRepository<String> repositoryLeft = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();
        IndexElementRepository<String> repositoryRight = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();

        ImmutableElementRepository<Long, BiElement<String, String>> result = repositoryLeft.join(repositoryRight)
                                                                                           .outer();

        repositoryLeft.put(0l, "l0");
        repositoryLeft.put(1l, "l1");

        repositoryRight.put(1l, "r1");
        repositoryRight.put(2l, "r2");

        assertEquals(3, result.size());
        assertEquals(BiElement.of("l0", null), result.get(0l));
        assertEquals(BiElement.of("l1", "r1"), result.get(1l));
        assertEquals(BiElement.of(null, "r2"), result.get(2l));

        assertEquals(Arrays.asList(0l, 1l, 2l), result.ids()
                                                      .collect(Collectors.toList()));
    }

    @Test
    public void testLeftOuter() throws Exception
    {
        IndexElementRepository<String> repositoryLeft = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();
        IndexElementRepository<String> repositoryRight = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();

        ImmutableElementRepository<Long, BiElement<String, String>> result = repositoryLeft.join(repositoryRight)
                                                                                           .leftOuter();
        repositoryLeft.put(0l, "l0");
        repositoryLeft.put(1l, "l1");

        repositoryRight.put(1l, "r1");
        repositoryRight.put(2l, "r2");

        assertEquals(2, result.size());
        assertEquals(BiElement.of("l0", null), result.get(0l));
        assertEquals(BiElement.of("l1", "r1"), result.get(1l));

        assertEquals(Arrays.asList(0l, 1l), result.ids()
                                                  .collect(Collectors.toList()));
    }

    @Test
    public void testRightOuter() throws Exception
    {
        IndexElementRepository<String> repositoryLeft = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();
        IndexElementRepository<String> repositoryRight = ElementRepositoryUtils.newConcurrentHashMapIndexElementRepository();

        ImmutableElementRepository<Long, BiElement<String, String>> result = repositoryLeft.join(repositoryRight)
                                                                                           .rightOuter();

        repositoryLeft.put(0l, "l0");
        repositoryLeft.put(1l, "l1");

        repositoryRight.put(1l, "r1");
        repositoryRight.put(2l, "r2");

        assertEquals(2, result.size());
        assertEquals(BiElement.of("l1", "r1"), result.get(1l));
        assertEquals(BiElement.of(null, "r2"), result.get(2l));

        assertEquals(Arrays.asList(1l, 2l), result.ids()
                                                  .collect(Collectors.toList()));
    }

}
