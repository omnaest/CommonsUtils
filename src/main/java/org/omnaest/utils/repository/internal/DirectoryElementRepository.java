package org.omnaest.utils.repository.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.omnaest.utils.EnumUtils;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.MapperUtils;
import org.omnaest.utils.NumberUtils;
import org.omnaest.utils.ObjectUtils;
import org.omnaest.utils.functional.Accessor;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.IndexElementRepository;

/**
 * {@link IndexElementRepository} based on a {@link File} directory structure and json serializer/deserializer
 * 
 * @author omnaest
 * @param <D>
 */
public class DirectoryElementRepository<D> implements IndexElementRepository<D>
{
    private AtomicLong     counter;
    private File           directory;
    private Class<D>       type;
    private boolean        deleteFilesOnExit = false;
    private Accessor<Long> counterFileAccessor;

    public DirectoryElementRepository(File directory, Class<D> type)
    {
        super();
        this.directory = directory;
        this.type = type;

        this.counterFileAccessor = FileUtils.toAccessor(new File(this.directory, "counter.json"))
                                            .with(JSONHelper.serializer(), JSONHelper.deserializer(Long.class));

        this.counter = new AtomicLong(ObjectUtils.defaultIfNull(this.counterFileAccessor.get(), 0l));
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
    public void put(Long id, D element)
    {
        File file = this.determineFile(id);
        FileUtils.toConsumer(file)
                 .accept(JSONHelper.serializer()
                                   .apply(element));

        if (this.deleteFilesOnExit)
        {
            file.deleteOnExit();
        }
    }

    @Override
    public void remove(Long id)
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
        long fileIndex = this.counter.getAndIncrement();
        this.counterFileAccessor.accept(fileIndex);

        File file = this.determineFile(fileIndex);
        FileUtils.toConsumer(file)
                 .accept(JSONHelper.serializer()
                                   .apply(element));

        if (this.deleteFilesOnExit)
        {
            file.deleteOnExit();
        }

        return fileIndex;
    }

    @Override
    public long size()
    {
        return this.counter.get() + 1;
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
    public IndexElementRepository<D> clear()
    {
        try
        {
            org.apache.commons.io.FileUtils.deleteDirectory(this.directory);
            this.counter.set(0);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        return this;
    }

    @Override
    public NullOptional<D> get(Long id)
    {
        return NullOptional.ofNullable(FileUtils.toSupplier(this.determineFile(id)))
                           .map(supplier -> supplier.get())
                           .mapToNullable(content -> JSONHelper.deserializer(this.type)
                                                               .apply(content));
    }

    @Override
    public Stream<Long> ids(IdOrder order)
    {
        LongStream idStream = EnumUtils.decideOn(order)
                                       .ifEqualTo(IdOrder.FROM_OLDEST_TO_NEWEST, () -> LongStream.range(0, this.size()))
                                       .orElse(LongStream.range(0, this.size()));
        return idStream.filter(id -> this.determineFile(id)
                                         .exists())
                       .mapToObj(MapperUtils.identityForLongAsBoxed());
    }

    @Override
    public String toString()
    {
        return "DirectoryElementRepository [counter=" + this.counter + ", directory=" + this.directory + ", type=" + this.type + ", deleteFilesOnExit="
                + this.deleteFilesOnExit + "]";
    }

}
