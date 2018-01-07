package org.omnaest.utils.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.NumberUtils;

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

        if (this.deleteFilesOnExit)
        {
            try
            {
                org.apache.commons.io.FileUtils.forceDeleteOnExit(this.directory);
            }
            catch (IOException e)
            {
                //do nothing
            }
        }

        return this;
    }

    @Override
    public void update(Long id, D element)
    {
        File file = this.determineFile(id);
        FileUtils.toConsumer(file)
                 .accept(JSONHelper.serializer(this.type)
                                   .apply(element));

        if (this.deleteFilesOnExit)
        {
            file.deleteOnExit();
        }
    }

    @Override
    public void delete(Long id)
    {
        try
        {
            File file = this.determineFile(id);
            if (file.exists())
            {
                org.apache.commons.io.FileUtils.forceDelete(file);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public Long add(D element)
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

    protected File determineFile(long id)
    {
        File subDirectory = this.directory;

        List<String> subDirectoryTokens = new ArrayList<>();
        int divider = 10000;
        int digits = (int) Math.round(Math.log10(Long.MAX_VALUE));
        long counter = Long.MAX_VALUE;
        long fileIndexCounter = id;
        while (counter > divider)
        {
            digits -= (int) Math.round(Math.log10(divider));
            fileIndexCounter /= divider;
            counter /= divider;
            subDirectoryTokens.add(NumberUtils.formatter()
                                              .withMinimumIntegerDigits(digits)
                                              .withMaximumFractionDigits(0)
                                              .format(fileIndexCounter));
        }
        Collections.reverse(subDirectoryTokens);
        for (String token : subDirectoryTokens)
        {
            subDirectory = new File(subDirectory, token);
        }

        return new File(subDirectory, id + ".json");
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
