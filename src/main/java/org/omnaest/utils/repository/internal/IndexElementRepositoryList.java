package org.omnaest.utils.repository.internal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.list.AbstractList;
import org.omnaest.utils.repository.IndexElementRepository;

/**
 * {@link List} wrapper around a {@link IndexElementRepository}
 * 
 * @author omnaest
 * @param <D>
 */
public class IndexElementRepositoryList<D> extends AbstractList<D>
{
    private IndexElementRepository<D> elementRepository;

    public IndexElementRepositoryList(IndexElementRepository<D> elementRepository)
    {
        super();
        this.elementRepository = elementRepository;
    }

    @Override
    public int size()
    {
        return (int) this.elementRepository.size();
    }

    @Override
    public D get(int index)
    {
        return this.elementRepository.getValue((long) index);
    }

    @Override
    public D set(int index, D element)
    {
        D previousElement = this.elementRepository.getValue((long) index);
        this.elementRepository.put((long) index, element);
        return previousElement;
    }

    @Override
    public void add(int index, D element)
    {
        List<Long> ids = this.elementRepository.ids()
                                               .filter(id -> id >= index)
                                               .sorted()
                                               .collect(Collectors.toList());
        Collections.reverse(ids);
        ids.forEach(id -> this.elementRepository.put(id + 1, this.elementRepository.getValue(id)));
        this.elementRepository.put((long) index, element);
    }

    @Override
    public D remove(int index)
    {

        // delete the element
        D previousElement = this.elementRepository.getValue((long) index);
        this.elementRepository.remove((long) index);

        // correct other index positions
        int size = this.size();
        StreamUtils.concat(Stream.of((long) index), this.elementRepository.ids()
                                                                          .filter(id -> id >= index)
                                                                          .sorted())
                   .collect(Collectors.toList())
                   .forEach(id ->
                   {
                       long nextId = id + 1;
                       if (nextId < size + 1)
                       {
                           this.elementRepository.put(id, this.elementRepository.getValue(nextId));
                       }
                       else
                       {
                           this.elementRepository.remove(id);
                       }
                   });
        return previousElement;
    }

}
