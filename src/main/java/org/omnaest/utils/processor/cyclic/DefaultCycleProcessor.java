package org.omnaest.utils.processor.cyclic;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.omnaest.utils.RetryUtils;
import org.omnaest.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCycleProcessor<I, W> implements CycleProcessor<I, W>
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCycleProcessor.class);

    private Function<I, W>   windowReaderFunction;
    private BiConsumer<I, W> windowWriter;

    private ExecutorService operationsExecutorService = Executors.newCachedThreadPool();
    private ExecutorService mainExecutorService       = Executors.newSingleThreadExecutor();

    private KeyLockingRepository<I, WindowCollector<W>> windowIndexToCollector = new KeyLockingRepository<>();

    private static class NewOperationsSignaling
    {
        private Lock      lock      = new ReentrantLock();
        private Condition condition = this.lock.newCondition();

        public void awaitSignal()
        {
            this.lock.lock();
            try
            {
                this.condition.await(1000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                //nothing to do
            }
            finally
            {
                this.lock.unlock();
            }
        }

        public void signal()
        {
            this.lock.lock();
            try
            {
                this.condition.signal();
            }
            finally
            {
                this.lock.unlock();
            }
        }

    }

    private static class KeyLockingRepository<K, V>
    {
        private Map<K, Lock>          keyToLock          = new ConcurrentHashMap<>();
        private Map<K, Condition>     keyToReadSignal    = new ConcurrentHashMap<>();
        private Map<K, Condition>     keyToLoadedSignal  = new ConcurrentHashMap<>();
        private Map<K, Condition>     keyToRemovedSignal = new ConcurrentHashMap<>();
        private Map<K, AtomicInteger> keyToReadCounter   = new ConcurrentHashMap<>();
        private Map<K, V>             keyToValue         = new ConcurrentHashMap<>();

        private NewOperationsSignaling newOperationsSignaling = new NewOperationsSignaling();

        public void computeIfAbsentOperation(K key, Consumer<V> consumer, Supplier<V> supplier)
        {
            //
            this.newOperationsSignaling.signal();

            boolean consumed = false;
            do
            {
                Lock lock = this.keyToLock.computeIfAbsent(key, k -> new ReentrantLock());
                lock.lock();
                try
                {
                    if (lock.equals(this.keyToLock.get(key))) //ensure the removal operation has not yet been run
                    {

                        //
                        this.keyToReadCounter.computeIfAbsent(key, k -> new AtomicInteger())
                                             .incrementAndGet();

                        this.keyToLoadedSignal.computeIfAbsent(key, k -> lock.newCondition())
                                              .awaitUninterruptibly();

                        consumer.accept(this.keyToValue.computeIfAbsent(key, k -> supplier.get()));

                        //signal the window write
                        int counter = this.keyToReadCounter.computeIfAbsent(key, k -> new AtomicInteger())
                                                           .decrementAndGet();
                        if (counter <= 0)
                        {
                            this.keyToReadSignal.computeIfAbsent(key, k -> lock.newCondition())
                                                .signal();
                        }

                        //wait for window to write
                        this.keyToRemovedSignal.computeIfAbsent(key, k -> lock.newCondition())
                                               .awaitUninterruptibly();

                        //
                        consumed = true;
                    }
                }
                finally
                {
                    lock.unlock();
                }
            } while (!consumed);
        }

        public void removalOperation(K key, Consumer<V> preConsumer, Consumer<V> postConsumer)
        {
            boolean consumed = false;
            do
            {
                Lock lock = this.keyToLock.computeIfAbsent(key, k -> new ReentrantLock());
                lock.lock();
                try
                {
                    if (lock.equals(this.keyToLock.get(key))) //ensure the removal operation has not yet been run
                    {
                        //
                        V value = this.keyToValue.remove(key);
                        preConsumer.accept(value);

                        //wait for read threads to stage
                        while (this.keyToReadCounter.computeIfAbsent(key, k -> new AtomicInteger())
                                                    .get() > 0)
                        {
                            //
                            this.keyToLoadedSignal.computeIfAbsent(key, k -> lock.newCondition())
                                                  .signalAll();

                            //
                            try
                            {
                                this.keyToReadSignal.computeIfAbsent(key, k -> lock.newCondition())
                                                    .await(100, TimeUnit.MILLISECONDS);
                            }
                            catch (InterruptedException e)
                            {
                                //do nothing
                            }
                        }

                        //
                        postConsumer.accept(value);

                        //Release read threads
                        this.keyToRemovedSignal.computeIfAbsent(key, k -> lock.newCondition())
                                               .signalAll();

                        //
                        consumed = true;
                    }
                }
                finally
                {
                    if (consumed)
                    {
                        this.keyToLock.remove(key);
                    }
                    lock.unlock();
                }
            } while (!consumed);
        }

        public Set<K> keySet()
        {
            this.newOperationsSignaling.awaitSignal();
            return this.keyToValue.keySet();
        }

    }

    private static class WindowCollector<W>
    {

        private AtomicReference<W> window = new AtomicReference<>();

        public WindowCollector()
        {
            super();

        }

        public W getWindow()
        {
            return this.window.get();
        }

        public void setWindow(W window)
        {
            this.window.set(window);
        }

        @Override
        public String toString()
        {
            return "WindowCollector [window=" + this.window + "]";
        }

    }

    public DefaultCycleProcessor(Function<I, W> windowReaderFunction, BiConsumer<I, W> windowWriter)
    {
        this.windowReaderFunction = windowReaderFunction;
        this.windowWriter = windowWriter;

        this.start();
    }

    private void executeWithWindowIndexLock(I index, Consumer<W> operation)
    {
        LOG.info("Operation waiting for window: " + index);
        this.windowIndexToCollector.computeIfAbsentOperation(index, windowCollector ->
        {
            LOG.info("Execution operation for window: " + index);
            operation.accept(windowCollector.getWindow());
            LOG.info("Operation finished for window: " + index);
        }, () -> new WindowCollector<>());
    }

    @Override
    public <R> R execute(I windowIndex, CyclicWindowOperation<W, R> operation)
    {
        //
        Future<R> future = this.operationsExecutorService.submit(() ->
        {
            AtomicReference<R> retval = new AtomicReference<>();

            this.executeWithWindowIndexLock(windowIndex, window ->
            {
                retval.set(operation.accept(window));
            });

            return retval.get();
        });

        //
        try
        {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private void start()
    {
        if (this.windowReaderFunction == null)
        {
            throw new IllegalArgumentException("window reader function must not be null");
        }

        this.mainExecutorService.submit(() ->
        {
            StreamUtils.fromStreamSupplier(() ->
            {
                return this.windowIndexToCollector.keySet()
                                                  .stream()
                                                  .collect(Collectors.toList())
                                                  .stream();
            })
                       .forEach(index ->
                       {
                           //
                           this.windowIndexToCollector.removalOperation(index, lockAndCondition -> this.readWindow(index, lockAndCondition),
                                                                        lockAndCondition -> this.writeWindow(index, lockAndCondition));
                       });
        });
    }

    private void writeWindow(I index, WindowCollector<W> lockAndCondition)
    {
        LOG.info("Writing window: " + index);

        RetryUtils.retryUnlimited(100, TimeUnit.MILLISECONDS, () ->
        {
            if (this.windowWriter != null)
            {
                try
                {
                    this.windowWriter.accept(index, lockAndCondition.getWindow());
                }
                catch (Exception e)
                {
                    LOG.error("Retrying after fail to write window " + index + " ...", e);
                    throw e;
                }
            }
        });
    }

    private void readWindow(I index, WindowCollector<W> lockAndCondition)
    {
        LOG.info("Reading window: " + index);
        RetryUtils.retryUnlimited(100, TimeUnit.MILLISECONDS, () ->
        {
            try
            {
                W window = this.windowReaderFunction.apply(index);
                lockAndCondition.setWindow(window);
            }
            catch (Exception e)
            {
                LOG.error("Retrying after fail to read window " + index + " ...", e);
                throw e;
            }
        });
    }

}
