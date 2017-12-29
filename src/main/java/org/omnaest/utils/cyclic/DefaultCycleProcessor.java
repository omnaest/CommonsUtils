package org.omnaest.utils.cyclic;

import java.util.Map;
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
import java.util.stream.Collectors;

import org.omnaest.utils.RetryHelper;
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

    private Map<I, WindowLockManager<W>> windowIndexToLock      = new ConcurrentHashMap<>();
    private NewOperationsSignaling       newOperationsSignaling = new NewOperationsSignaling();

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

    private static class WindowLockManager<W>
    {
        private Lock      windowLock;
        private Condition windowReadyCondition;
        private Condition operationsFinishedCondition;
        private Condition windowWrittenCondition;

        private AtomicInteger waitingOperationsCounter = new AtomicInteger();

        private AtomicReference<W> window = new AtomicReference<>();

        public WindowLockManager()
        {
            super();
            this.windowLock = new ReentrantLock();
            this.windowReadyCondition = this.windowLock.newCondition();
            this.operationsFinishedCondition = this.windowLock.newCondition();
            this.windowWrittenCondition = this.windowLock.newCondition();
        }

        public W getWindow()
        {
            return this.window.get();
        }

        public void setWindow(W window)
        {
            this.window.set(window);
        }

        public AtomicInteger getWaitingOperationsCounter()
        {
            return this.waitingOperationsCounter;
        }

        public Condition getWindowReadyCondition()
        {
            return this.windowReadyCondition;
        }

        public Condition getOperationsFinishedCondition()
        {
            return this.operationsFinishedCondition;
        }

        public void markOneMoreOperationAsFinished()
        {
            int counter = this.waitingOperationsCounter.decrementAndGet();
            if (counter <= 0)
            {
                LOG.info("Sent all operations finished signal");
                this.windowLock.lock();
                this.operationsFinishedCondition.signal();
                this.windowLock.unlock();
            }
        }

        @Override
        public String toString()
        {
            return "LockAndCondition [waitingOperationsCounter=" + this.waitingOperationsCounter + "]";
        }

        public W executeReadOperation(NewOperationsSignaling newOperationsSignaling)
        {
            Lock lock = this.windowLock;
            lock.lock();
            try
            {
                //
                newOperationsSignaling.signal();

                //
                return this.window.getAndUpdate(w ->
                {
                    if (w == null)
                    {
                        this.windowReadyCondition.awaitUninterruptibly();
                    }
                    return w;
                });
            }
            finally
            {
                lock.unlock();
            }
        }

        public void executeWriteOperation(Runnable operation)
        {
            Lock lock = this.windowLock;
            lock.lock();
            try
            {
                operation.run();

                this.windowWrittenCondition.signalAll();
            }
            finally
            {
                lock.unlock();
            }

        }

        public void awaitWindowWriteFinished()
        {
            Lock lock = this.windowLock;
            lock.lock();
            try
            {
                this.windowWrittenCondition.awaitUninterruptibly();
            }
            finally
            {
                lock.unlock();
            }
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
        WindowLockManager<W> lockAndCondition = this.windowIndexToLock.computeIfAbsent(index, i -> new WindowLockManager<>());

        LOG.info("Operation waiting for window: " + index);

        lockAndCondition.getWaitingOperationsCounter()
                        .incrementAndGet();

        W window = lockAndCondition.executeReadOperation(this.newOperationsSignaling);
        if (window != null)
        {
            LOG.info("Execution operation for window: " + index);
            operation.accept(lockAndCondition.getWindow());
            LOG.info("Operation finished for window: " + index);
        }

        //
        lockAndCondition.markOneMoreOperationAsFinished();

        lockAndCondition.awaitWindowWriteFinished();
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
                if (this.windowIndexToLock.isEmpty())
                {
                    this.newOperationsSignaling.awaitSignal();
                }
                return this.windowIndexToLock.keySet()
                                             .stream()
                                             .collect(Collectors.toList())
                                             .stream();
            })
                       .forEach(index ->
                       {
                           //
                           WindowLockManager<W> lockAndCondition = this.windowIndexToLock.remove(index);

                           lockAndCondition.executeWriteOperation(() ->
                           {
                               // read window
                               this.readWindow(index, lockAndCondition);

                               // signal all waiting operations and wait for operations to finish
                               this.signalWaitingOperationsToRun(index, lockAndCondition);

                               // write window
                               this.writeWindow(index, lockAndCondition);

                           });
                       });
        });
    }

    private void signalWaitingOperationsToRun(I index, WindowLockManager<W> lockAndCondition)
    {
        LOG.info("Signal operations that window " + index + " is ready");
        while (lockAndCondition.getWaitingOperationsCounter()
                               .get() > 0)
        {
            LOG.info("Number of waiting operations: " + lockAndCondition.getWaitingOperationsCounter()
                                                                        .get());
            //
            lockAndCondition.getWindowReadyCondition()
                            .signalAll();
            try
            {
                lockAndCondition.getOperationsFinishedCondition()
                                .await(10, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                LOG.info("Waiting for too long...");
            }
        }
    }

    private void writeWindow(I index, WindowLockManager<W> lockAndCondition)
    {
        LOG.info("Writing window: " + index);

        RetryHelper.retryUnlimited(100, TimeUnit.MILLISECONDS, () ->
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

    private void readWindow(I index, WindowLockManager<W> lockAndCondition)
    {
        LOG.info("Reading window: " + index);
        RetryHelper.retryUnlimited(100, TimeUnit.MILLISECONDS, () ->
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
