/*
 * Copyright (C) 2015 HaiYang Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.landawn.abacus.pool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.landawn.abacus.exception.AbacusException;
import com.landawn.abacus.logging.Logger;
import com.landawn.abacus.logging.LoggerFactory;
import com.landawn.abacus.util.ClassUtil;
import com.landawn.abacus.util.MoreExecutors;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractPool.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public abstract class AbstractPool implements Pool {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7780250223658416202L;

    /** The Constant logger. */
    static final Logger logger = LoggerFactory.getLogger(AbstractPool.class);

    /** The Constant DEFAULT_EVICT_DELAY. */
    static final long DEFAULT_EVICT_DELAY = 3000;

    /** The Constant DEFAULT_BALANCE_FACTOR. */
    static final float DEFAULT_BALANCE_FACTOR = 0.2f;

    /** The Constant scheduledExecutor. */
    static final ScheduledExecutorService scheduledExecutor;
    static {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(64);
        executor.setKeepAliveTime(180, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
        executor.setRemoveOnCancelPolicy(true);
        scheduledExecutor = MoreExecutors.getExitingScheduledExecutorService(executor);
    }

    /** The put count. */
    final AtomicLong putCount = new AtomicLong();

    /** The hit count. */
    final AtomicLong hitCount = new AtomicLong();

    /** The miss count. */
    final AtomicLong missCount = new AtomicLong();

    /** The eviction count. */
    final AtomicLong evictionCount = new AtomicLong();

    /** The lock. */
    final ReentrantLock lock = new ReentrantLock();

    /** The not empty. */
    final Condition notEmpty = lock.newCondition();

    /** The not full. */
    final Condition notFull = lock.newCondition();

    /** The capacity. */
    final int capacity;

    /** The eviction policy. */
    final EvictionPolicy evictionPolicy;

    /** The auto balance. */
    final boolean autoBalance;

    /** The balance factor. */
    final float balanceFactor;

    /** The is closed. */
    boolean isClosed = false;

    /**
     * Instantiates a new abstract pool.
     *
     * @param capacity the capacity
     * @param evictDelay the evict delay
     * @param evictionPolicy the eviction policy
     * @param autoBalance the auto balance
     * @param balanceFactor the balance factor
     */
    protected AbstractPool(int capacity, long evictDelay, EvictionPolicy evictionPolicy, boolean autoBalance, float balanceFactor) {
        if (capacity < 0 || evictDelay < 0 || balanceFactor < 0) {
            throw new IllegalArgumentException(
                    "Capacity(" + capacity + "), evict delay(" + evictDelay + "), balanc factor(" + balanceFactor + ") can not be negative");
        }

        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy == null ? EvictionPolicy.LAST_ACCESS_TIME : evictionPolicy;
        this.autoBalance = autoBalance;
        this.balanceFactor = balanceFactor == 0f ? DEFAULT_BALANCE_FACTOR : balanceFactor;

        final Class<?> cls = this.getClass();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.warn("Starting to shutdown pool: " + ClassUtil.getCanonicalClassName(cls));

                try {
                    close();
                } finally {
                    logger.warn("Completed to shutdown pool: " + ClassUtil.getCanonicalClassName(cls));
                }
            }
        });
    }

    /**
     * Lock.
     */
    @Override
    public void lock() {
        lock.lock();
    }

    /**
     * Unlock.
     */
    @Override
    public void unlock() {
        lock.unlock();
    }

    /**
     * Gets the capacity.
     *
     * @return the capacity
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * Put count.
     *
     * @return the long
     */
    @Override
    public long putCount() {
        return putCount.get();
    }

    /**
     * Hit count.
     *
     * @return the long
     */
    @Override
    public long hitCount() {
        return hitCount.get();
    }

    /**
     * Miss count.
     *
     * @return the long
     */
    @Override
    public long missCount() {
        return missCount.get();
    }

    /**
     * Eviction count.
     *
     * @return the long
     */
    @Override
    public long evictionCount() {
        return evictionCount.get();
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Assert not closed.
     */
    protected void assertNotClosed() {
        if (isClosed) {
            throw new AbacusException(ClassUtil.getCanonicalClassName(getClass()) + " has been closed");
        }
    }

    /**
     * Finalize.
     *
     * @throws Throwable the throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (!isClosed) {
            close();
        }
    }
}