package org.omnaest.utils.repository.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.omnaest.utils.ThreadUtils;
import org.omnaest.utils.repository.ElementRepository;

public class BufferedUpdateElementRepository<I, D> extends ElementRepositoryDecorator<I, D>
{
    private Map<I, D>       cache = new ConcurrentHashMap<>();
    private ExecutorService executorService;

    private int      delay         = 1;
    private TimeUnit delayTimeUnit = TimeUnit.SECONDS;
    private int      maxCacheSize  = 100;

    public BufferedUpdateElementRepository(ElementRepository<I, D> elementRepository)
    {
        super(elementRepository);

        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                ThreadUtils.sleepSilently(BufferedUpdateElementRepository.this.delay, BufferedUpdateElementRepository.this.delayTimeUnit);
                BufferedUpdateElementRepository.this.flush();
                BufferedUpdateElementRepository.this.executorService.submit(this);
            }
        });
    }

    public BufferedUpdateElementRepository<I, D> setDelay(int delay, TimeUnit timeUnit)
    {
        this.delay = delay;
        this.delayTimeUnit = timeUnit;
        return this;
    }

    public BufferedUpdateElementRepository<I, D> setMaxBufferSize(int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    @Override
    public void put(I id, D element)
    {
        this.cache.put(id, element);
        if (this.cache.size() > this.maxCacheSize)
        {
            this.flush();
        }
    }

    public void flush()
    {
        Map<I, D> flushMap = new HashMap<>();
        this.cache.keySet()
                  .stream()
                  .collect(Collectors.toList())
                  .forEach(key -> flushMap.put(key, this.cache.remove(key)));

        this.elementRepository.putAll(flushMap);
    }

    @Override
    public D getValue(I id)
    {
        D retval = this.cache.get(id);
        if (retval == null)
        {
            retval = this.elementRepository.getValue(id);
        }
        return retval;
    }

    @Override
    public void close()
    {
        this.flush();
        this.elementRepository.close();
        this.executorService.shutdown();
    }

}
