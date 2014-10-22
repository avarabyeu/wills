package com.github.avarabyeu.wills;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Set of utility method for decorating Guava's and default JDK executor services
 *
 * @author Andrei Varabyeu
 */
public class WillExecutors {

    /**
     * Decorates Guava's {@link com.google.common.util.concurrent.ListeningExecutorService}
     *
     * @param delegate Guava's {@link com.google.common.util.concurrent.ListeningExecutorService}
     * @return WillExecutor service
     */
    public static WillExecutorService willDecorator(ListeningExecutorService delegate) {
        return new WillDecorator(delegate);
    }

    /**
     * Decorates JDK's {@link java.util.concurrent.ExecutorService}
     *
     * @param delegate JDK's {@link java.util.concurrent.ExecutorService}
     * @return WillExecutor service
     */
    public static WillExecutorService willDecorator(ExecutorService delegate) {
        return new WillDecorator(MoreExecutors.listeningDecorator(delegate));
    }


    /**
     * Decorates Guava's Executor service. Make all submit method return {@link com.github.avarabyeu.wills.Will} instead of default {@link com.google.common.util.concurrent.ListenableFuture}
     * Delegates all another methods to provided executor service
     */
    private static class WillDecorator extends AbstractListeningExecutorService implements WillExecutorService {

        private ListeningExecutorService delegate;

        private WillDecorator(ListeningExecutorService delegate) {
            this.delegate = Preconditions.checkNotNull(delegate, "Delegate shouldn't be null");
        }

        @Override
        public Will<?> submit(Runnable task) {
            return Wills.forListenableFuture(super.submit(task));
        }

        @Override
        public <T> Will<T> submit(Runnable task, @Nullable T result) {
            return Wills.forListenableFuture(super.submit(task, result));
        }

        @Override
        public <T> Will<T> submit(Callable<T> task) {
            return Wills.forListenableFuture(super.submit(task));
        }

        @Override
        public void shutdown() {
            this.delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return this.delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return this.delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return this.delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return this.delegate.awaitTermination(timeout, unit);
        }

        @Override
        public void execute(@Nonnull Runnable command) {
            this.delegate.execute(command);
        }
    }
}
