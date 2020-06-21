package org.omnaest.utils.repository.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.ListUtils;
import org.omnaest.utils.repository.ElementAggregationRepository;
import org.omnaest.utils.repository.ElementAggregationRepository.Entry;
import org.omnaest.utils.repository.ElementAggregationRepository.LoggedEntry.State;
import org.omnaest.utils.repository.ElementRepository.Factory;

public class ElementAggregationRepositoryImplTest
{
    private ElementAggregationRepository<String, Data, Meta> elementRepository = ElementAggregationRepository.of(Factory.of(new HashMap<>()), String.class,
                                                                                                                 Data.class, Meta.class);

    @Test
    public void testAddAndGet() throws Exception
    {
        IntStream.range(0, 10)
                 .forEach(ii ->
                 {
                     String id = this.elementRepository.add(Entry.of(new Data().setValue("data" + ii), new Meta().setUserId(ii)));

                     Entry<Data, Meta> entry = this.elementRepository.get(id)
                                                                     .get();
                     assertEquals("data" + ii, entry.get()
                                                    .getValue());
                     assertEquals(ii, entry.getMeta()
                                           .getUserId());
                 });

        assertTrue(this.elementRepository.ids()
                                         .allMatch(id -> this.elementRepository.getLog(id)
                                                                               .count() == 1));
    }

    @Test
    public void testPutAndGet() throws Exception
    {
        IntStream.range(0, 10)
                 .forEach(ii ->
                 {
                     String id = "" + ii;
                     this.elementRepository.put(id, Entry.of(new Data().setValue("data" + ii), new Meta().setUserId(ii)));

                     Entry<Data, Meta> entry = this.elementRepository.get(id)
                                                                     .get();
                     assertEquals("data" + ii, entry.get()
                                                    .getValue());
                     assertEquals(ii, entry.getMeta()
                                           .getUserId());
                 });

        assertTrue(this.elementRepository.ids()
                                         .allMatch(id -> this.elementRepository.getLog(id)
                                                                               .count() == 1));
    }

    @Test
    public void testMultiplePutAndGet() throws Exception
    {
        String id = "0";
        IntStream.range(0, 10)
                 .forEach(ii ->
                 {
                     this.elementRepository.put(id, Entry.of(new Data().setValue("data" + ii), new Meta().setUserId(ii)));

                     Entry<Data, Meta> entry = this.elementRepository.get(id)
                                                                     .get();
                     assertEquals("data" + ii, entry.get()
                                                    .getValue());
                     assertEquals(ii, entry.getMeta()
                                           .getUserId());
                 });

        assertEquals(10l, this.elementRepository.getLog(id)
                                                .count());
    }

    @Test
    public void testRemove() throws Exception
    {
        String id = "0";
        this.elementRepository.put(id, Entry.of(new Data().setValue("data" + 0), new Meta().setUserId(0)));
        this.elementRepository.put(id, Entry.of(new Data().setValue("data" + 1), new Meta().setUserId(1)));
        assertTrue(this.elementRepository.containsId(id));
        assertEquals(Arrays.asList(State.CREATED, State.UPDATED), ListUtils.inverse(this.elementRepository.getLog(id)
                                                                                                          .map(entry -> entry.getState())
                                                                                                          .collect(Collectors.toList())));

        this.elementRepository.remove(id, new Meta().setUserId(2));
        assertFalse(this.elementRepository.containsId(id));
        assertEquals(Arrays.asList(State.CREATED, State.UPDATED, State.DELETED), ListUtils.inverse(this.elementRepository.getLog(id)
                                                                                                                         .map(entry -> entry.getState())
                                                                                                                         .collect(Collectors.toList())));

        this.elementRepository.put(id, Entry.of(new Data().setValue("data" + 3), new Meta().setUserId(3)));
        assertTrue(this.elementRepository.containsId(id));
        assertEquals(Arrays.asList(State.CREATED, State.UPDATED, State.DELETED, State.CREATED), ListUtils.inverse(this.elementRepository.getLog(id)
                                                                                                                                        .map(entry -> entry.getState())
                                                                                                                                        .collect(Collectors.toList())));

        this.elementRepository.remove(id, new Meta().setUserId(4));
        assertFalse(this.elementRepository.containsId(id));
        assertEquals(Arrays.asList(State.CREATED, State.UPDATED, State.DELETED, State.CREATED, State.DELETED),
                     ListUtils.inverse(this.elementRepository.getLog(id)
                                                             .map(entry -> entry.getState())
                                                             .collect(Collectors.toList())));
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), ListUtils.inverse(this.elementRepository.getLog(id)
                                                                                           .map(entry -> entry.getMeta()
                                                                                                              .getUserId())
                                                                                           .collect(Collectors.toList())));
    }

    private static class Meta
    {
        private int userId;

        public int getUserId()
        {
            return this.userId;
        }

        public Meta setUserId(int userId)
        {
            this.userId = userId;
            return this;
        }

    }

    private static class Data
    {
        private String value;

        public String getValue()
        {
            return this.value;
        }

        public Data setValue(String value)
        {
            this.value = value;
            return this;
        }

    }

}
