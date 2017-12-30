package org.omnaest.utils.cyclic;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockingQueueChannel<K, V>
{
    private Map<K, BlockingDeque<V>> keyToQueue = new ConcurrentHashMap<>();
    private Lock                     lock       = new ReentrantLock();

    public Supplier<V> producer(K key)
    {
        return () -> this.executeWithLock(() ->
        {
            try
            {
                BlockingDeque<V> queue = this.getOrCreateQueue(key);
                V take = queue.take();
                if (queue.isEmpty())
                {
                    this.keyToQueue.remove(key);
                }
                return take;
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException(e);
            }
        });
    }

    private BlockingDeque<V> getOrCreateQueue(K key)
    {
        return this.keyToQueue.computeIfAbsent(key, k -> new LinkedBlockingDeque<>());

    }

    public Consumer<V> consumer(K key)
    {
        return t ->
        {
            this.executeWithLock(() ->
            {
                try
                {
                    this.getOrCreateQueue(key)
                        .put(t);
                }
                catch (InterruptedException e)
                {
                    throw new IllegalStateException(e);
                }
                return null;
            });
        };
    }

    private static interface LockOperation<V> extends Supplier<V>
    {

    }

    private V executeWithLock(LockOperation<V> operation)
    {
        V retval = null;

        this.lock.lock();
        try
        {
            retval = operation.get();
        }
        finally
        {
            this.lock.unlock();
        }

        return retval;
    }

}
