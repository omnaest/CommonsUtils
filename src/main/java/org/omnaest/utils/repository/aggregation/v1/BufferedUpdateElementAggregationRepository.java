package org.omnaest.utils.repository.aggregation.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.ThreadUtils;
import org.omnaest.utils.element.bi.BiElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedUpdateElementAggregationRepository<I, D> extends ElementAggregationRepositoryDecorator<I, D>
{
    private static final Logger LOG = LoggerFactory.getLogger(BufferedUpdateElementAggregationRepository.class);

    private Queue<BiElement<I, D>> cache = new ConcurrentLinkedQueue<>();
    private ExecutorService        executorService;

    private int      delay         = 5;
    private TimeUnit delayTimeUnit = TimeUnit.SECONDS;
    private int      maxCacheSize  = 100;

    public BufferedUpdateElementAggregationRepository(ElementAggregationRepositoryV1<I, D> repository)
    {
        super(repository);

        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ThreadUtils.sleepSilently(BufferedUpdateElementAggregationRepository.this.delay,
                                              BufferedUpdateElementAggregationRepository.this.delayTimeUnit);
                    BufferedUpdateElementAggregationRepository.this.flush();
                    BufferedUpdateElementAggregationRepository.this.executorService.submit(this);
                }
                catch (Exception e)
                {
                    LOG.error("Exception in flush thread", e);
                }
            }
        });
    }

    public BufferedUpdateElementAggregationRepository<I, D> setDelay(int delay, TimeUnit timeUnit)
    {
        this.delay = delay;
        this.delayTimeUnit = timeUnit;
        return this;
    }

    public BufferedUpdateElementAggregationRepository<I, D> setMaxBufferSize(int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    @Override
    public void add(I id, D element)
    {
        this.cache.add(BiElement.of(id, element));
        if (this.cache.size() > this.maxCacheSize)
        {
            this.flush();
        }
    }

    public void flush()
    {

        if (!this.cache.isEmpty())
        {
            List<BiElement<I, D>> flushList = new ArrayList<>();
            for (int ii = this.cache.size() - 1; ii >= 0; ii--)
            {
                BiElement<I, D> removed = this.cache.poll();
                if (removed != null)
                {
                    flushList.add(removed);
                }
            }
            this.repository.addAll(flushList);
        }

    }

    @Override
    public Stream<D> get(I id)
    {
        return Stream.concat(this.cache.stream()
                                       .filter(PredicateUtils.notNull())
                                       .map(bi -> bi.getSecond()),
                             this.repository.get(id));
    }

    @Override
    public void close()
    {
        this.flush();
        this.repository.close();
        this.executorService.shutdown();
    }

}
