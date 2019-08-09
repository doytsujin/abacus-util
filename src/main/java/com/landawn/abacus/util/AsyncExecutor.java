/*
 * Copyright (c) 2015, Haiyang Li.
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

package com.landawn.abacus.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.landawn.abacus.logging.Logger;
import com.landawn.abacus.logging.LoggerFactory;
import com.landawn.abacus.util.Fn.FN;
import com.landawn.abacus.util.function.BiPredicate;
import com.landawn.abacus.util.function.Predicate;

// TODO: Auto-generated Javadoc
/**
 * The Class AsyncExecutor.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public class AsyncExecutor {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);

    /** The Constant DEFAULT_CORE_POOL_SIZE. */
    private static final int DEFAULT_CORE_POOL_SIZE = Math.max(8, IOUtil.CPU_CORES);

    /** The Constant DEFAULT_MAX_THREAD_POOL_SIZE. */
    private static final int DEFAULT_MAX_THREAD_POOL_SIZE = Math.max(16, IOUtil.CPU_CORES);

    /** The core thread pool size. */
    private final int coreThreadPoolSize;

    /** The max thread pool size. */
    private final int maxThreadPoolSize;

    /** The keep alive time. */
    private final long keepAliveTime;

    /** The unit. */
    private final TimeUnit unit;

    /** The executor. */
    private volatile Executor executor;

    /**
     * Instantiates a new async executor.
     */
    public AsyncExecutor() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_THREAD_POOL_SIZE, 180L, TimeUnit.SECONDS);
    }

    /**
     * Instantiates a new async executor.
     *
     * @param coreThreadPoolSize the core thread pool size
     * @param maxThreadPoolSize the max thread pool size
     * @param keepAliveTime the keep alive time
     * @param unit the unit
     */
    public AsyncExecutor(int coreThreadPoolSize, int maxThreadPoolSize, long keepAliveTime, TimeUnit unit) {
        N.checkArgNotNegative(coreThreadPoolSize, "coreThreadPoolSize");
        N.checkArgNotNegative(maxThreadPoolSize, "maxThreadPoolSize");
        N.checkArgNotNegative(keepAliveTime, "keepAliveTime");
        N.checkArgNotNull(unit, "unit");

        this.coreThreadPoolSize = coreThreadPoolSize;
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (executor == null || !(executor instanceof ExecutorService)) {
                    return;
                }

                final ExecutorService executorService = (ExecutorService) executor;

                logger.warn("Starting to shutdown task in AsyncExecutor");

                try {
                    executorService.shutdown();

                    executorService.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.warn("Not all the requests/tasks executed in AsyncExecutor are completed successfully before shutdown.");
                } finally {
                    logger.warn("Completed to shutdown task in AsyncExecutor");
                }
            }
        });
    }

    /**
     * Instantiates a new async executor.
     *
     * @param executor the executor
     */
    public AsyncExecutor(final Executor executor) {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_THREAD_POOL_SIZE, 180L, TimeUnit.SECONDS);

        this.executor = executor;
    }

    /**
     * Execute.
     *
     * @param command the command
     * @return the continuable future
     */
    public ContinuableFuture<Void> execute(final Try.Runnable<? extends Exception> command) {
        return execute(new FutureTask<Void>(FN.toCallable(command)));
    }

    /**
     * Execute.
     *
     * @param commands the commands
     * @return the list
     */
    @SafeVarargs
    public final List<ContinuableFuture<Void>> execute(final Try.Runnable<? extends Exception>... commands) {
        if (N.isNullOrEmpty(commands)) {
            return new ArrayList<>();
        }

        final List<ContinuableFuture<Void>> results = new ArrayList<>(commands.length);

        for (int i = 0, len = commands.length; i < len; i++) {
            results.add(execute(commands[i]));
        }

        return results;
    }

    /**
     * Execute.
     *
     * @param commands the commands
     * @return the list
     */
    public List<ContinuableFuture<Void>> execute(final List<? extends Try.Runnable<? extends Exception>> commands) {
        if (N.isNullOrEmpty(commands)) {
            return new ArrayList<>();
        }

        final List<ContinuableFuture<Void>> results = new ArrayList<>(commands.size());

        for (Try.Runnable<? extends Exception> cmd : commands) {
            results.add(execute(cmd));
        }

        return results;
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param command the command
     * @return the continuable future
     */
    public <T> ContinuableFuture<T> execute(final Callable<T> command) {
        return execute(new FutureTask<>(command));
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param commands the commands
     * @return the list
     */
    @SafeVarargs
    public final <T> List<ContinuableFuture<T>> execute(final Callable<T>... commands) {
        if (N.isNullOrEmpty(commands)) {
            return new ArrayList<>();
        }

        final List<ContinuableFuture<T>> results = new ArrayList<>(commands.length);

        for (int i = 0, len = commands.length; i < len; i++) {
            results.add(execute(commands[i]));
        }

        return results;
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param commands the commands
     * @return the list
     */
    public <T> List<ContinuableFuture<T>> execute(final Collection<? extends Callable<T>> commands) {
        if (N.isNullOrEmpty(commands)) {
            return new ArrayList<>();
        }

        final List<ContinuableFuture<T>> results = new ArrayList<>(commands.size());

        for (Callable<T> cmd : commands) {
            results.add(execute(cmd));
        }

        return results;
    }

    /**
     * Execute.
     *
     * @param action the action
     * @param retryTimes the retry times
     * @param retryInterval the retry interval
     * @param retryCondition the retry condition
     * @return the continuable future
     */
    public ContinuableFuture<Void> execute(final Try.Runnable<? extends Exception> action, final int retryTimes, final long retryInterval,
            final Predicate<? super Exception> retryCondition) {
        return execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Retry.of(retryTimes, retryInterval, retryCondition).run(action);
                return null;
            }
        });
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param action the action
     * @param retryTimes the retry times
     * @param retryInterval the retry interval
     * @param retryCondition the retry condition
     * @return the continuable future
     */
    public <T> ContinuableFuture<T> execute(final Callable<T> action, final int retryTimes, final long retryInterval,
            final BiPredicate<? super T, ? super Exception> retryCondition) {
        return execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                final Retry<T> retry = Retry.of(retryTimes, retryInterval, retryCondition);
                return retry.call(action);
            }
        });
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param futureTask the future task
     * @return the continuable future
     */
    private <T> ContinuableFuture<T> execute(final FutureTask<T> futureTask) {
        final Executor executor = getExecutor();

        executor.execute(futureTask);

        return new ContinuableFuture<>(futureTask, null, executor);
    }

    /**
     * Gets the executor.
     *
     * @return the executor
     */
    private Executor getExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreThreadPoolSize, maxThreadPoolSize, keepAliveTime, unit,
                            new LinkedBlockingQueue<Runnable>());
                    threadPoolExecutor.allowCoreThreadTimeOut(true);
                    executor = threadPoolExecutor;
                }
            }
        }

        return executor;
    }
}