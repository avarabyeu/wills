package com.github.avarabyeu.wills;

import com.google.common.util.concurrent.ListeningExecutorService;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * Executor service which uses {@link com.github.avarabyeu.wills.Will} instead of Guava's {@link com.google.common.util.concurrent.ListenableFuture}
 *
 * @author Andrei Varabyeu
 */
public interface WillExecutorService extends ListeningExecutorService {

    @Override
    Will<?> submit(@Nonnull Runnable task);

    @Override
    <T> Will<T> submit(@Nonnull Callable<T> task);

    @Override
    <T> Will<T> submit(@Nonnull Runnable task, T result);
}
