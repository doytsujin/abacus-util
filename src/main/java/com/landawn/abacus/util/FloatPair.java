/*
 * Copyright (C) 2018 HaiYang Li
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

package com.landawn.abacus.util;

import com.landawn.abacus.util.u.Optional;
import com.landawn.abacus.util.stream.FloatStream;

/**
 * The Class FloatPair.
 *
 * @author Haiyang Li
 * @since 1.2
 */
public final class FloatPair extends PrimitivePair<FloatPair> {

    /** The  1. */
    public final float _1;

    /** The  2. */
    public final float _2;

    /**
     * Instantiates a new float pair.
     */
    FloatPair() {
        this(0, 0);
    }

    /**
     * Instantiates a new float pair.
     *
     * @param _1 the 1
     * @param _2 the 2
     */
    FloatPair(float _1, float _2) {
        this._1 = _1;
        this._2 = _2;
    }

    /**
     *
     * @param _1 the 1
     * @param _2 the 2
     * @return
     */
    public static FloatPair of(float _1, float _2) {
        return new FloatPair(_1, _2);
    }

    /**
     *
     * @return
     */
    public float min() {
        return N.min(_1, _2);
    }

    /**
     *
     * @return
     */
    public float max() {
        return N.max(_1, _2);
    }

    /**
     *
     * @return
     */
    public float sum() {
        return N.sum(_1, _2);
    }

    /**
     *
     * @return
     */
    public double average() {
        return N.average(_1, _2);
    }

    /**
     *
     * @return
     */
    public FloatPair reverse() {
        return new FloatPair(_2, _1);
    }

    /**
     *
     * @return
     */
    public float[] toArray() {
        return new float[] { _1, _2 };
    }

    /**
     *
     * @return
     */
    public FloatList toList() {
        return FloatList.of(_1, _2);
    }

    /**
     *
     * @param <E>
     * @param comsumer
     * @throws E the e
     */
    public <E extends Exception> void forEach(Throwables.FloatConsumer<E> comsumer) throws E {
        comsumer.accept(this._1);
        comsumer.accept(this._2);
    }

    /**
     *
     * @param <E>
     * @param action
     * @throws E the e
     */
    public <E extends Exception> void accept(Throwables.FloatBiConsumer<E> action) throws E {
        action.accept(_1, _2);
    }

    /**
     *
     * @param <U>
     * @param <E>
     * @param mapper
     * @return
     * @throws E the e
     */
    public <U, E extends Exception> U map(Throwables.FloatBiFunction<U, E> mapper) throws E {
        return mapper.apply(_1, _2);
    }

    /**
     *
     * @param <E>
     * @param predicate
     * @return
     * @throws E the e
     */
    public <E extends Exception> Optional<FloatPair> filter(final Throwables.FloatBiPredicate<E> predicate) throws E {
        return predicate.test(_1, _2) ? Optional.of(this) : Optional.<FloatPair> empty();
    }

    /**
     *
     * @return
     */
    public FloatStream stream() {
        return FloatStream.of(_1, _2);
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return (int) (31 * _1 + this._2);
    }

    /**
     *
     * @param obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof FloatPair)) {
            return false;
        } else {
            FloatPair other = (FloatPair) obj;
            return this._1 == other._1 && this._2 == other._2;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "[" + this._1 + ", " + this._2 + "]";
    }
}
