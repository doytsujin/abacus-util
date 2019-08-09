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

import com.landawn.abacus.annotation.Beta;
import com.landawn.abacus.util.Tuple.Tuple3;
import com.landawn.abacus.util.u.Optional;
import com.landawn.abacus.util.stream.Stream;

// TODO: Auto-generated Javadoc
/**
 * The Class Triple.
 *
 * @author Haiyang Li
 * @param <L> the generic type
 * @param <M> the generic type
 * @param <R> the generic type
 * @since 0.8
 */
public final class Triple<L, M, R> {

    /** The left. */
    public L left;

    /** The middle. */
    public M middle;

    /** The right. */
    public R right;

    /**
     * Instantiates a new triple.
     */
    public Triple() {
    }

    /**
     * Instantiates a new triple.
     *
     * @param l the l
     * @param m the m
     * @param r the r
     */
    Triple(final L l, final M m, final R r) {
        this.left = l;
        this.middle = m;
        this.right = r;
    }

    /**
     * Of.
     *
     * @param <L> the generic type
     * @param <M> the generic type
     * @param <R> the generic type
     * @param l the l
     * @param m the m
     * @param r the r
     * @return the triple
     */
    public static <L, M, R> Triple<L, M, R> of(final L l, final M m, final R r) {
        return new Triple<>(l, m, r);
    }

    /**
     * Gets the left.
     *
     * @return the left
     */
    public L getLeft() {
        return left;
    }

    /**
     * Sets the left.
     *
     * @param left the new left
     */
    public void setLeft(final L left) {
        this.left = left;
    }

    /**
     * Gets the middle.
     *
     * @return the middle
     */
    public M getMiddle() {
        return middle;
    }

    /**
     * Sets the middle.
     *
     * @param middle the new middle
     */
    public void setMiddle(final M middle) {
        this.middle = middle;
    }

    /**
     * Gets the right.
     *
     * @return the right
     */
    public R getRight() {
        return right;
    }

    /**
     * Sets the right.
     *
     * @param right the new right
     */
    public void setRight(final R right) {
        this.right = right;
    }

    /**
     * Sets the.
     *
     * @param left the left
     * @param middle the middle
     * @param right the right
     */
    public void set(final L left, final M middle, final R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    /**
     * Gets the and set left.
     *
     * @param newLeft the new left
     * @return the and set left
     */
    public L getAndSetLeft(L newLeft) {
        final L res = left;
        left = newLeft;
        return res;
    }

    /**
     * Sets the and get left.
     *
     * @param newLeft the new left
     * @return the l
     */
    public L setAndGetLeft(L newLeft) {
        left = newLeft;
        return left;
    }

    /**
     * Gets the and set middle.
     *
     * @param newMiddle the new middle
     * @return the and set middle
     */
    public M getAndSetMiddle(M newMiddle) {
        final M res = middle;
        middle = newMiddle;
        return res;
    }

    /**
     * Sets the and get middle.
     *
     * @param newMiddle the new middle
     * @return the m
     */
    public M setAndGetMiddle(M newMiddle) {
        middle = newMiddle;
        return middle;
    }

    /**
     * Gets the and set right.
     *
     * @param newRight the new right
     * @return the and set right
     */
    public R getAndSetRight(R newRight) {
        final R res = newRight;
        right = newRight;
        return res;
    }

    /**
     * Sets the and get right.
     *
     * @param newRight the new right
     * @return the r
     */
    public R setAndGetRight(R newRight) {
        right = newRight;
        return right;
    }

    /**
     * Set to the specified <code>newLeft</code> and returns <code>true</code>
     * if <code>predicate</code> returns true. Otherwise returns
     * <code>false</code> without setting the value to new value.
     *
     * @param <E> the element type
     * @param newLeft the new left
     * @param predicate - the first parameter is current pair, the second
     *        parameter is the <code>newLeft</code>
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean setLeftIf(final L newLeft, Try.BiPredicate<? super Triple<L, M, R>, ? super L, E> predicate) throws E {
        if (predicate.test(this, newLeft)) {
            this.left = newLeft;
            return true;
        }

        return false;
    }

    /**
     * Set to the specified <code>newMiddle</code> and returns <code>true</code>
     * if <code>predicate</code> returns true. Otherwise returns
     * <code>false</code> without setting the value to new value.
     *
     * @param <E> the element type
     * @param newMiddle the new middle
     * @param predicate - the first parameter is current pair, the second
     *        parameter is the <code>newMiddle</code>
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean setMiddleIf(final M newMiddle, Try.BiPredicate<? super Triple<L, M, R>, ? super M, E> predicate) throws E {
        if (predicate.test(this, newMiddle)) {
            this.middle = newMiddle;
            return true;
        }

        return false;
    }

    /**
     * Set to the specified <code>newRight</code> and returns <code>true</code>
     * if <code>predicate</code> returns true. Otherwise returns
     * <code>false</code> without setting the value to new value.
     *
     * @param <E> the element type
     * @param newRight the new right
     * @param predicate - the first parameter is current pair, the second
     *        parameter is the <code>newRight</code>
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean setRightIf(final R newRight, Try.BiPredicate<? super Triple<L, M, R>, ? super R, E> predicate) throws E {
        if (predicate.test(this, newRight)) {
            this.right = newRight;
            return true;
        }

        return false;
    }

    /**
     * Set to the specified <code>newLeft</code> and <code>newRight</code> and
     * returns <code>true</code> if <code>predicate</code> returns true.
     * Otherwise returns <code>false</code> without setting the left/right to
     * new values.
     *
     * @param <E> the element type
     * @param newLeft the new left
     * @param newMiddle the new middle
     * @param newRight the new right
     * @param predicate - the first parameter is current pair, the second
     *        parameter is the <code>newLeft</code>, the third parameter is the
     *        <code>newMiddle</code>, the fourth parameter is the
     *        <code>newRight</code>
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean setIf(final L newLeft, final M newMiddle, final R newRight,
            Try.QuadPredicate<? super Triple<L, M, R>, ? super L, ? super M, ? super R, E> predicate) throws E {
        if (predicate.test(this, newLeft, newMiddle, newRight)) {
            this.left = newLeft;
            this.middle = newMiddle;
            this.right = newRight;
            return true;
        }

        return false;
    }

    //    /**
    //     * Swap the left and right value. they must be same type.
    //     */
    //    public void reverse() {
    //        Object tmp = left;
    //        this.left = (L) right;
    //        this.right = (R) tmp;
    //    }

    /**
     * Returns a new instance of Triple&lt;R, M, L&gt;.
     * 
     * @return a new instance of Triple&lt;R, M, L&gt;.
     */
    @Beta
    public Triple<R, M, L> reversed() {
        return new Triple<>(this.right, this.middle, this.left);
    }

    /**
     * Copy.
     *
     * @return the triple
     */
    public Triple<L, M, R> copy() {
        return new Triple<>(this.left, this.middle, this.right);
    }

    /**
     * To array.
     *
     * @return the object[]
     */
    public Object[] toArray() {
        return new Object[] { left, middle, right };
    }

    /**
     * To array.
     *
     * @param <A> the generic type
     * @param a the a
     * @return the a[]
     */
    public <A> A[] toArray(A[] a) {
        if (a.length < 3) {
            a = N.copyOf(a, 3);
        }

        a[0] = (A) left;
        a[1] = (A) middle;
        a[2] = (A) right;

        return a;
    }

    /**
     * For each.
     *
     * @param <E> the element type
     * @param comsumer the comsumer
     * @throws E the e
     */
    public <E extends Exception> void forEach(Try.Consumer<?, E> comsumer) throws E {
        final Try.Consumer<Object, E> objComsumer = (Try.Consumer<Object, E>) comsumer;

        objComsumer.accept(left);
        objComsumer.accept(middle);
        objComsumer.accept(right);
    }

    /**
     * Accept.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void accept(final Try.TriConsumer<? super L, ? super M, ? super R, E> action) throws E {
        action.accept(left, middle, right);
    }

    /**
     * Accept.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void accept(final Try.Consumer<? super Triple<L, M, R>, E> action) throws E {
        action.accept(this);
    }

    /**
     * Map.
     *
     * @param <U> the generic type
     * @param <E> the element type
     * @param mapper the mapper
     * @return the u
     * @throws E the e
     */
    public <U, E extends Exception> U map(final Try.TriFunction<? super L, ? super M, ? super R, U, E> mapper) throws E {
        return mapper.apply(left, middle, right);
    }

    /**
     * Map.
     *
     * @param <U> the generic type
     * @param <E> the element type
     * @param mapper the mapper
     * @return the u
     * @throws E the e
     */
    public <U, E extends Exception> U map(final Try.Function<? super Triple<L, M, R>, U, E> mapper) throws E {
        return mapper.apply(this);
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional
     * @throws E the e
     */
    public <E extends Exception> Optional<Triple<L, M, R>> filter(final Try.TriPredicate<? super L, ? super M, ? super R, E> predicate) throws E {
        return predicate.test(left, middle, right) ? Optional.of(this) : Optional.<Triple<L, M, R>> empty();
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional
     * @throws E the e
     */
    public <E extends Exception> Optional<Triple<L, M, R>> filter(final Try.Predicate<? super Triple<L, M, R>, E> predicate) throws E {
        return predicate.test(this) ? Optional.of(this) : Optional.<Triple<L, M, R>> empty();
    }

    /**
     * Stream.
     *
     * @return the stream
     */
    public Stream<Triple<L, M, R>> stream() {
        return Stream.of(this);
    }

    /**
     * To tuple.
     *
     * @return the tuple 3
     */
    public Tuple3<L, M, R> toTuple() {
        return Tuple.of(left, middle, right);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + N.hashCode(left);
        result = prime * result + N.hashCode(middle);
        result = prime * result + N.hashCode(right);
        return result;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Triple) {
            final Triple<L, M, R> other = (Triple<L, M, R>) obj;

            return N.equals(left, other.left) && N.equals(middle, other.middle) && N.equals(right, other.right);
        }

        return false;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "[" + N.toString(left) + ", " + N.toString(middle) + ", " + N.toString(right) + "]";
    }
}