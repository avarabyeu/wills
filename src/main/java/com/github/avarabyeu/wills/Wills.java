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
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

/**
 * Set of useful utility methods to simplify working with {@link Will}
 *
 * @author Andrei Varabyeu
 */
public final class Wills {

    private Wills() {
        //statics only
    }

    /**
     * Creates chain from provided Wills
     *
     * @param wills Wills to be chained
     * @param <A>   Type of Wills
     * @return Chained Will
     */
    public static <A> Will<List<A>> when(Will<? extends A>... wills) {
        return when(asList(wills));
    }

    /**
     * Creates chain from provided Wills
     *
     * @param wills Wills to be chained
     * @param <A>   Type of Wills
     * @return Chained Will
     */
    public static <A> Will<List<A>> when(Iterable<? extends Will<? extends A>> wills) {
        return forListenableFuture(Futures.<A>allAsList(wills));
    }

    /**
     * Creates successful {@link Will} from provided object
     *
     * @param value Object {@link Will} be created from
     * @param <A>   Type of Will
     * @return Will
     */
    public static <A> Will<A> of(A value) {
        return new Of<A>(Futures.immediateFuture(value));
    }

    /**
     * Creates failed {@link Will} using provided {@link java.lang.Throwable}
     *
     * @param throwable Will exception
     * @param <A>       Type of Will
     * @return Created Will
     */
    public static <A> Will<A> failedWill(@Nonnull Throwable throwable) {
        return new Of<A>(Futures.<A>immediateFailedFuture(throwable));
    }

    /**
     * Creates Will object from Guava's {@link com.google.common.util.concurrent.ListenableFuture}
     *
     * @param future Guava's ListenableFuture
     * @param <A>    Type of ListenableFuture and will be created
     * @return Created Will
     */
    public static <A> Will<A> forListenableFuture(ListenableFuture<A> future) {
        return new Of<A>(future);
    }


    /**
     * Creates Will object from JKS's {@link java.util.concurrent.Future}
     *
     * @param future JKS's Future
     * @param <A>    Type of Future and Will to be created
     * @return Created Will
     */
    public static <A> Will<A> forFuture(Future<A> future) {
        return new Of<A>(JdkFutureAdapters.listenInPoolThread(future));
    }


    /**
     * Creates Guava's {@link com.google.common.util.concurrent.FutureCallback} from provided Actions
     *
     * @param success Success callback
     * @param failure Failure callback
     * @param <A>     Type of Future result
     * @return Created FutureCallback
     */
    public static <A> FutureCallback<A> futureCallback(final Action<A> success, final Action<Throwable> failure) {
        return new FutureCallback<A>() {
            @Override
            public void onSuccess(A result) {
                success.apply(result);
            }

            @Override
            public void onFailure(Throwable t) {
                failure.apply(t);
            }
        };
    }


    /**
     * Creates Guava's {@link com.google.common.util.concurrent.FutureCallback} with success callback defined
     *
     * @param action Success callback
     * @param <A>    Type of Future result
     * @return Created FutureCallback
     */
    public static <A> FutureCallback<A> onSuccessDo(final Action<A> action) {
        return futureCallback(action, Actions.<Throwable>nothing());
    }


    /**
     * Creates Guava's {@link com.google.common.util.concurrent.FutureCallback} with failure callback defined
     *
     * @param action Failure callback
     * @param <A>    Type of Future result
     * @return Created FutureCallback
     */
    public static <A> FutureCallback<A> onFailureDo(final Action<Throwable> action) {
        return futureCallback(Actions.<A>nothing(), action);
    }

    /**
     * Default {@link Will} implementation
     * Based on Guava's {@link com.google.common.util.concurrent.ForwardingListenableFuture.SimpleForwardingListenableFuture}
     *
     * @param <A>
     */
    private static final class Of<A> extends ForwardingListenableFuture.SimpleForwardingListenableFuture<A> implements Will<A> {
        public Of(ListenableFuture<A> delegate) {
            super(delegate);
        }


        @Override
        public A obtain() {
            try {
                return delegate().get();
            } catch (InterruptedException e) {
                /* throw RuntimeException here? */
                throw Throwables.propagate(e);
            } catch (ExecutionException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public Will<A> whenSuccessful(@Nonnull Action<A> e) {
            callback(onSuccessDo(e));
            return this;
        }

        @Override
        public Will<A> whenFailed(@Nonnull Action<Throwable> e) {
            callback(Wills.<A>onFailureDo(e));
            return this;
        }

        @Override
        public Will<A> whenCompleted(final Action<Boolean> action) {
            callback(new FutureCallback<A>() {
                @Override
                public void onSuccess(@Nullable A result) {
                    action.apply(true);
                }

                @Override
                public void onFailure(Throwable t) {
                    action.apply(false);
                }
            });
            return this;
        }

        @Override
        public Will<A> callback(@Nonnull FutureCallback<A> callback) {
            Futures.addCallback(delegate(), callback);
            return this;
        }

        @Override
        public <R> Will<R> map(@Nonnull Function<? super A, ? extends R> function) {
            return forListenableFuture(Futures.transform(this, function));
        }


        @Override
        public <B> Will<B> flatMap(final Function<? super A, Will<B>> f) {
            final SettableFuture<B> result = SettableFuture.create();
            final Action<Throwable> failResult = new Action<Throwable>() {
                @Override
                public void apply(Throwable t) {
                    result.setException(t);
                }
            };

            whenSuccessful(new Action<A>() {
                @Override
                public void apply(A v) {
                    try {
                        Will<B> next = f.apply(v);
                        Preconditions.checkNotNull(next, "Created Will shouldn't be null");
                        next.whenSuccessful(new Action<B>() {
                            @Override
                            public void apply(B t) {
                                result.set(t);
                            }
                        }).whenFailed(failResult);
                    } catch (Throwable t) {
                        result.setException(t);
                    }
                }
            }).whenFailed(failResult);
            return new Of<B>(result);
        }


    }
}
