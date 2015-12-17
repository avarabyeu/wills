/*
 * Copyright (C) 2014 Andrei Varabyeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.avarabyeu.wills;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nonnull;

/**
 * Useful wrapper around Google's {@link com.google.common.util.concurrent.ListenableFuture}
 *
 * @param <T> Type of object to be returned
 * @author Andrey Varabyeu
 * @see {@link Wills}
 */
public interface Will<T> extends ListenableFuture<T> {

    /**
     * Blocks current thread until future object is availible or some exception thrown
     * Signature-free version of {@link java.util.concurrent.Future#get()},
     * propagates checked exceptions as runtume exceptions
     *
     * @return future result
     * @see {@link com.google.common.util.concurrent.ListenableFuture#isDone()}
     */
    T obtain();

    /**
     * Adds callback to future object. Will be executed if future is successful
     *
     * @param action Action to be performed on future result
     * @return This object
     */
    Will<T> whenSuccessful(Action<T> action);

    /**
     * Adds callback to future object. Will be executed if some exception is thrown
     *
     * @param action Action to be performed on future exception
     * @return This object
     */
    Will<T> whenFailed(Action<Throwable> action);

    /**
     * Adds callback to the future object. Will be executed once future is completed
     *
     * @param action Some action with Boolean type. TRUE in case future is successful
     * @return This object
     */
    Will<T> whenDone(@Nonnull Action<Boolean> action);

    /**
     * Adds {@link com.google.common.util.concurrent.FutureCallback} for future object.
     * Will be executed when future result is availible or some exception thrown
     *
     * @param callback {@link com.google.common.util.concurrent.ListenableFuture} callback
     * @return This object
     */
    Will<T> callback(FutureCallback<T> callback);

    /**
     * Replaces future provided by fallback in case if current Will fails
     * <b>PAY ATTENTION - this method creates new Will instance</b>
     *
     * @param fallback New Future Factory
     * @return <b>NEW</b> Will
     */
    Will<T> replaceFailed(AsyncFunction<Throwable, ? extends T> fallback);

    /**
     * Replaces current Will with new one based on ListenableFuture in case of fail
     * <b>PAY ATTENTION - this method creates new Will instance</b>
     *
     * @param future New ListenableFuture
     * @return <b>NEW</b> Will
     */
    Will<T> replaceFailed(ListenableFuture<T> future);

    /**
     * Replaces current will with new one in case of fail
     * <b>PAY ATTENTION - this method creates new Will instance</b>
     *
     * @param future <b>NEW</b> Will
     * @return
     */
    Will<T> replaceFailed(Will<T> future);

    /**
     * Creates new {@link Will} containing transformed result of this {@link Will} result using provided function
     *
     * @param function Transformation Function
     * @param <R>      Type of new Will
     * @return New Will
     */
    <R> Will<R> map(Function<? super T, ? extends R> function);

    /**
     * Creates new {@link Will} containing transformed result of this {@link Will} result using provided function
     *
     * @param function Will of transformation function
     * @param <R>      Type of new Will
     * @return New Will
     */
    <R> Will<R> flatMap(Function<? super T, Will<R>> function);

}
