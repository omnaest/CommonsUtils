package org.omnaest.utils.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;

/**
 * {@link ElementRepository} based on a {@link File} directory structure and json serializer/deserializer
 * 
 * @author omnaest
 * @param <D>
 */
public class DirectoryElementRepository<D> implements ElementRepository<Long, D>
{
    private AtomicLong counter           = new AtomicLong();
    private File       directory;
    private Class<D>   type;
    private boolean    deleteFilesOnExit = false;

    public DirectoryElementRepository(File directory, Class<D> type)
    {
        super();
        this.directory = directory;
        this.type = type;
    }

    /**
     * If set to true, files created with call {@link File#deleteOnExit()}, so that they are deleted on JVM exit
     * 
     * @param deleteFilesOnExit
     * @return
     */
    public DirectoryElementRepository<D> setDeleteFilesOnExit(boolean deleteFilesOnExit)
    {
        this.deleteFilesOnExit = deleteFilesOnExit;
        return this;
    }

    @Override
    public Long put(D element)
    {
        long fileIndex = this.counter.incrementAndGet();

        File file = this.determineFile(fileIndex);
        FileUtils.toConsumer(file)
                 .accept(JSONHelper.serializer(this.type)
                                   .apply(element));

        if (this.deleteFilesOnExit)
        {
            file.deleteOnExit();
        }

        return fileIndex;
    }

    protected File determineFile(long fileIndex)
    {
        File subDirectory = this.directory;

        List<String> subDirectoryTokens = new ArrayList<>();
        int divider = 10000;
        long counter = Long.MAX_VALUE;
        long fileIndexCounter = fileIndex;
        while (counter > divider)
        {
            fileIndexCounter /= divider;
            counter /= divider;
            subDirectoryTokens.add(String.valueOf(fileIndexCounter));
        }
        Collections.reverse(subDirectoryTokens);
        for (String token : subDirectoryTokens)
        {
            subDirectory = new File(subDirectory, token);
        }

        return new File(subDirectory, fileIndex + ".json");
    }

    @Override
    public D get(Long id)
    {
        D element = JSONHelper.deserializer(this.type)
                              .apply(FileUtils.toSupplier(this.determineFile(id))
                                              .get());
        return element;
    }

}
