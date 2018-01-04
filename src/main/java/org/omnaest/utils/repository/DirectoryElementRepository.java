package org.omnaest.utils.repository;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;

public class DirectoryElementRepository<D> implements ElementRepository<Long, D>
{
    private AtomicLong counter = new AtomicLong();
    private File       directory;
    private Class<D>   type;

    public DirectoryElementRepository(File directory, Class<D> type)
    {
        super();
        this.directory = directory;
        this.type = type;
    }

    @Override
    public Long put(D element)
    {
        long fileIndex = this.counter.incrementAndGet();

        FileUtils.toConsumer(this.determineFileName(fileIndex))
                 .accept(JSONHelper.serializer(this.type)
                                   .apply(element));

        return fileIndex;
    }

    private File determineFileName(long fileIndex)
    {
        File subDirectory = new File(this.directory, String.valueOf(fileIndex / 10000));
        return new File(subDirectory, fileIndex + ".json");
    }

    @Override
    public D get(Long id)
    {
        D element = JSONHelper.deserializer(this.type)
                              .apply(FileUtils.toSupplier(this.determineFileName(id))
                                              .get());
        return element;
    }

}
