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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.landawn.abacus.util.Fn.Factory;
import com.landawn.abacus.util.Try.Function;
import com.landawn.abacus.util.u.Optional;
import com.landawn.abacus.util.u.OptionalDouble;
import com.landawn.abacus.util.u.OptionalInt;
import com.landawn.abacus.util.u.OptionalShort;
import com.landawn.abacus.util.function.BiConsumer;
import com.landawn.abacus.util.function.BiFunction;
import com.landawn.abacus.util.function.IntFunction;
import com.landawn.abacus.util.function.Supplier;
import com.landawn.abacus.util.stream.Collector;
import com.landawn.abacus.util.stream.ShortStream;

// TODO: Auto-generated Javadoc
/**
 * The Class ShortList.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public final class ShortList extends PrimitiveList<Short, short[], ShortList> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 25682021483156507L;

    /** The Constant RAND. */
    static final Random RAND = new SecureRandom();

    /** The element data. */
    private short[] elementData = N.EMPTY_SHORT_ARRAY;

    /** The size. */
    private int size = 0;

    /**
     * Instantiates a new short list.
     */
    public ShortList() {
        super();
    }

    /**
     * Instantiates a new short list.
     *
     * @param initialCapacity the initial capacity
     */
    public ShortList(int initialCapacity) {
        elementData = initialCapacity == 0 ? N.EMPTY_SHORT_ARRAY : new short[initialCapacity];
    }

    /**
     * The specified array is used as the element array for this list without copying action.
     *
     * @param a the a
     */
    public ShortList(short[] a) {
        this(a, a.length);
    }

    /**
     * Instantiates a new short list.
     *
     * @param a the a
     * @param size the size
     */
    public ShortList(short[] a, int size) {
        N.checkFromIndexSize(0, size, a.length);

        this.elementData = a;
        this.size = size;
    }

    /**
     * Of.
     *
     * @param a the a
     * @return the short list
     */
    @SafeVarargs
    public static ShortList of(final short... a) {
        return new ShortList(N.nullToEmpty(a));
    }

    /**
     * Of.
     *
     * @param a the a
     * @param size the size
     * @return the short list
     */
    public static ShortList of(final short[] a, final int size) {
        N.checkFromIndexSize(0, size, N.len(a));

        return new ShortList(N.nullToEmpty(a), size);
    }

    /**
     * Copy of.
     *
     * @param a the a
     * @return the short list
     */
    public static ShortList copyOf(final short[] a) {
        return of(N.clone(a));
    }

    /**
     * Copy of.
     *
     * @param a the a
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the short list
     */
    public static ShortList copyOf(final short[] a, final int fromIndex, final int toIndex) {
        return of(N.copyOfRange(a, fromIndex, toIndex));
    }

    /**
     * From.
     *
     * @param c the c
     * @return the short list
     */
    public static ShortList from(Collection<Short> c) {
        if (N.isNullOrEmpty(c)) {
            return new ShortList();
        }

        return from(c, (short) 0);
    }

    /**
     * From.
     *
     * @param c the c
     * @param defaultForNull the default for null
     * @return the short list
     */
    public static ShortList from(Collection<Short> c, short defaultForNull) {
        if (N.isNullOrEmpty(c)) {
            return new ShortList();
        }

        final short[] a = new short[c.size()];
        int idx = 0;

        for (Short e : c) {
            a[idx++] = e == null ? defaultForNull : e;
        }

        return of(a);
    }

    /**
     * From.
     *
     * @param c the c
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the short list
     */
    public static ShortList from(final Collection<Short> c, final int fromIndex, final int toIndex) {
        N.checkFromToIndex(fromIndex, toIndex, N.size(c));

        if (N.isNullOrEmpty(c)) {
            return new ShortList();
        }

        return from(c, fromIndex, toIndex, (short) 0);
    }

    /**
     * From.
     *
     * @param c the c
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param defaultForNull the default for null
     * @return the short list
     */
    public static ShortList from(final Collection<Short> c, final int fromIndex, final int toIndex, short defaultForNull) {
        return of(N.toShortArray(c, fromIndex, toIndex, defaultForNull));
    }

    /**
     * Range.
     *
     * @param startInclusive the start inclusive
     * @param endExclusive the end exclusive
     * @return the short list
     */
    public static ShortList range(short startInclusive, final short endExclusive) {
        return of(Array.range(startInclusive, endExclusive));
    }

    /**
     * Range.
     *
     * @param startInclusive the start inclusive
     * @param endExclusive the end exclusive
     * @param by the by
     * @return the short list
     */
    public static ShortList range(short startInclusive, final short endExclusive, final short by) {
        return of(Array.range(startInclusive, endExclusive, by));
    }

    /**
     * Range closed.
     *
     * @param startInclusive the start inclusive
     * @param endInclusive the end inclusive
     * @return the short list
     */
    public static ShortList rangeClosed(short startInclusive, final short endInclusive) {
        return of(Array.rangeClosed(startInclusive, endInclusive));
    }

    /**
     * Range closed.
     *
     * @param startInclusive the start inclusive
     * @param endInclusive the end inclusive
     * @param by the by
     * @return the short list
     */
    public static ShortList rangeClosed(short startInclusive, final short endInclusive, final short by) {
        return of(Array.rangeClosed(startInclusive, endInclusive, by));
    }

    /**
     * Repeat.
     *
     * @param element the element
     * @param len the len
     * @return the short list
     */
    public static ShortList repeat(short element, final int len) {
        return of(Array.repeat(element, len));
    }

    /**
     * Random.
     *
     * @param len the len
     * @return the short list
     */
    public static ShortList random(final int len) {
        final int bound = Short.MAX_VALUE - Short.MIN_VALUE + 1;
        final short[] a = new short[len];

        for (int i = 0; i < len; i++) {
            a[i] = (short) (RAND.nextInt(bound) + Short.MIN_VALUE);
        }

        return of(a);
    }

    /**
     * Returns the original element array without copying.
     *
     * @return the short[]
     */
    @Override
    public short[] array() {
        return elementData;
    }

    /**
     * Gets the.
     *
     * @param index the index
     * @return the short
     */
    public short get(int index) {
        rangeCheck(index);

        return elementData[index];
    }

    /**
     * Range check.
     *
     * @param index the index
     */
    private void rangeCheck(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Sets the.
     *
     * @param index the index
     * @param e the e
     * @return the old value in the specified position.
     */
    public short set(int index, short e) {
        rangeCheck(index);

        short oldValue = elementData[index];

        elementData[index] = e;

        return oldValue;
    }

    /**
     * Adds the.
     *
     * @param e the e
     */
    public void add(short e) {
        ensureCapacityInternal(size + 1);

        elementData[size++] = e;
    }

    /**
     * Adds the.
     *
     * @param index the index
     * @param e the e
     */
    public void add(int index, short e) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);

        int numMoved = size - index;

        if (numMoved > 0) {
            N.copy(elementData, index, elementData, index + 1, numMoved);
        }

        elementData[index] = e;

        size++;
    }

    /**
     * Adds the all.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean addAll(ShortList c) {
        if (N.isNullOrEmpty(c)) {
            return false;
        }

        int numNew = c.size();

        ensureCapacityInternal(size + numNew);

        N.copy(c.array(), 0, elementData, size, numNew);

        size += numNew;

        return true;
    }

    /**
     * Adds the all.
     *
     * @param index the index
     * @param c the c
     * @return true, if successful
     */
    public boolean addAll(int index, ShortList c) {
        rangeCheckForAdd(index);

        if (N.isNullOrEmpty(c)) {
            return false;
        }

        int numNew = c.size();

        ensureCapacityInternal(size + numNew); // Increments modCount

        int numMoved = size - index;

        if (numMoved > 0) {
            N.copy(elementData, index, elementData, index + numNew, numMoved);
        }

        N.copy(c.array(), 0, elementData, index, numNew);

        size += numNew;

        return true;
    }

    /**
     * Adds the all.
     *
     * @param a the a
     * @return true, if successful
     */
    @Override
    public boolean addAll(short[] a) {
        return addAll(size(), a);
    }

    /**
     * Adds the all.
     *
     * @param index the index
     * @param a the a
     * @return true, if successful
     */
    @Override
    public boolean addAll(int index, short[] a) {
        rangeCheckForAdd(index);

        if (N.isNullOrEmpty(a)) {
            return false;
        }

        int numNew = a.length;

        ensureCapacityInternal(size + numNew); // Increments modCount

        int numMoved = size - index;

        if (numMoved > 0) {
            N.copy(elementData, index, elementData, index + numNew, numMoved);
        }

        N.copy(a, 0, elementData, index, numNew);

        size += numNew;

        return true;
    }

    /**
     * Range check for add.
     *
     * @param index the index
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Removes the.
     *
     * @param e the e
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(short e) {
        for (int i = 0; i < size; i++) {
            if (elementData[i] == e) {

                fastRemove(i);

                return true;
            }
        }

        return false;
    }

    /**
     * Removes the all occurrences.
     *
     * @param e the e
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean removeAllOccurrences(short e) {
        int w = 0;

        for (int i = 0; i < size; i++) {
            if (elementData[i] != e) {
                elementData[w++] = elementData[i];
            }
        }

        int numRemoved = size - w;

        if (numRemoved > 0) {
            N.fill(elementData, w, size, (short) 0);

            size = w;
        }

        return numRemoved > 0;
    }

    /**
     * Fast remove.
     *
     * @param index the index
     */
    private void fastRemove(int index) {
        int numMoved = size - index - 1;

        if (numMoved > 0) {
            N.copy(elementData, index + 1, elementData, index, numMoved);
        }

        elementData[--size] = 0; // clear to let GC do its work
    }

    /**
     * Removes the all.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean removeAll(ShortList c) {
        if (N.isNullOrEmpty(c)) {
            return false;
        }

        return batchRemove(c, false) > 0;
    }

    /**
     * Removes the all.
     *
     * @param a the a
     * @return true, if successful
     */
    @Override
    public boolean removeAll(short[] a) {
        if (N.isNullOrEmpty(a)) {
            return false;
        }

        return removeAll(of(a));
    }

    /**
     * Removes the if.
     *
     * @param <E> the element type
     * @param p the p
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean removeIf(Try.ShortPredicate<E> p) throws E {
        final ShortList tmp = new ShortList(size());

        for (int i = 0; i < size; i++) {
            if (p.test(elementData[i]) == false) {
                tmp.add(elementData[i]);
            }
        }

        if (tmp.size() == this.size()) {
            return false;
        }

        N.copy(tmp.elementData, 0, this.elementData, 0, tmp.size());
        N.fill(this.elementData, tmp.size(), size, (short) 0);
        size = tmp.size;

        return true;
    }

    /**
     * Retain all.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean retainAll(ShortList c) {
        if (N.isNullOrEmpty(c)) {
            boolean result = size() > 0;
            clear();
            return result;
        }

        return batchRemove(c, true) > 0;
    }

    /**
     * Retain all.
     *
     * @param a the a
     * @return true, if successful
     */
    public boolean retainAll(short[] a) {
        if (N.isNullOrEmpty(a)) {
            boolean result = size() > 0;
            clear();
            return result;
        }

        return retainAll(ShortList.of(a));
    }

    /**
     * Batch remove.
     *
     * @param c the c
     * @param complement the complement
     * @return the int
     */
    private int batchRemove(ShortList c, boolean complement) {
        final short[] elementData = this.elementData;

        int w = 0;

        if (c.size() > 3 && size() > 9) {
            final Set<Short> set = c.toSet();

            for (int i = 0; i < size; i++) {
                if (set.contains(elementData[i]) == complement) {
                    elementData[w++] = elementData[i];
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (c.contains(elementData[i]) == complement) {
                    elementData[w++] = elementData[i];
                }
            }
        }

        int numRemoved = size - w;

        if (numRemoved > 0) {
            N.fill(elementData, w, size, (short) 0);

            size = w;
        }

        return numRemoved;
    }

    /**
     * Delete.
     *
     * @param index the index
     * @return the deleted element
     */
    public short delete(int index) {
        rangeCheck(index);

        short oldValue = elementData[index];

        fastRemove(index);

        return oldValue;
    }

    /**
     * Delete all.
     *
     * @param indices the indices
     */
    @Override
    @SafeVarargs
    public final void deleteAll(int... indices) {
        final short[] tmp = N.deleteAll(elementData, indices);
        N.copy(tmp, 0, elementData, 0, tmp.length);
        N.fill(elementData, tmp.length, size, (short) 0);
        size = tmp.length;
    }

    /**
     * Delete range.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     */
    @Override
    public void deleteRange(final int fromIndex, final int toIndex) {
        N.checkFromToIndex(fromIndex, toIndex, size());

        if (fromIndex == toIndex) {
            return;
        }

        final int newSize = size() - (toIndex - fromIndex);

        if (toIndex < size()) {
            System.arraycopy(elementData, toIndex, elementData, fromIndex, size - toIndex);
        }

        N.fill(elementData, newSize, size(), (short) 0);

        size = newSize;
    }

    /**
     * Replace all.
     *
     * @param oldVal the old val
     * @param newVal the new val
     * @return the int
     */
    public int replaceAll(short oldVal, short newVal) {
        if (size() == 0) {
            return 0;
        }

        int result = 0;

        for (int i = 0, len = size(); i < len; i++) {
            if (elementData[i] == oldVal) {
                elementData[i] = newVal;

                result++;
            }
        }

        return result;
    }

    /**
     * Replace all.
     *
     * @param <E> the element type
     * @param operator the operator
     * @throws E the e
     */
    public <E extends Exception> void replaceAll(Try.ShortUnaryOperator<E> operator) throws E {
        for (int i = 0, len = size(); i < len; i++) {
            elementData[i] = operator.applyAsShort(elementData[i]);
        }
    }

    /**
     * Replace if.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @param newValue the new value
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean replaceIf(Try.ShortPredicate<E> predicate, short newValue) throws E {
        boolean result = false;

        for (int i = 0, len = size(); i < len; i++) {
            if (predicate.test(elementData[i])) {
                elementData[i] = newValue;

                result = true;
            }
        }

        return result;
    }

    /**
     * Fill.
     *
     * @param val the val
     */
    public void fill(final short val) {
        fill(0, size(), val);
    }

    /**
     * Fill.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param val the val
     */
    public void fill(final int fromIndex, final int toIndex, final short val) {
        checkFromToIndex(fromIndex, toIndex);

        N.fill(elementData, fromIndex, toIndex, val);
    }

    /**
     * Contains.
     *
     * @param e the e
     * @return true, if successful
     */
    public boolean contains(short e) {
        return indexOf(e) >= 0;
    }

    /**
     * Contains all.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean containsAll(ShortList c) {
        if (N.isNullOrEmpty(c)) {
            return true;
        } else if (isEmpty()) {
            return false;
        }

        final boolean isThisContainer = size() >= c.size();
        final ShortList container = isThisContainer ? this : c;
        final short[] iterElements = isThisContainer ? c.array() : this.array();

        if (needToSet(size(), c.size())) {
            final Set<Short> set = container.toSet();

            for (int i = 0, iterLen = isThisContainer ? c.size() : this.size(); i < iterLen; i++) {
                if (set.contains(iterElements[i]) == false) {
                    return false;
                }
            }
        } else {
            for (int i = 0, iterLen = isThisContainer ? c.size() : this.size(); i < iterLen; i++) {
                if (container.contains(iterElements[i]) == false) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Contains all.
     *
     * @param a the a
     * @return true, if successful
     */
    @Override
    public boolean containsAll(short[] a) {
        if (N.isNullOrEmpty(a)) {
            return true;
        } else if (isEmpty()) {
            return false;
        }

        return containsAll(of(a));
    }

    /**
     * Disjoint.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean disjoint(final ShortList c) {
        if (isEmpty() || N.isNullOrEmpty(c)) {
            return true;
        }

        final boolean isThisContainer = size() >= c.size();
        final ShortList container = isThisContainer ? this : c;
        final short[] iterElements = isThisContainer ? c.array() : this.array();

        if (needToSet(size(), c.size())) {
            final Set<Short> set = container.toSet();

            for (int i = 0, iterLen = isThisContainer ? c.size() : this.size(); i < iterLen; i++) {
                if (set.contains(iterElements[i])) {
                    return false;
                }
            }
        } else {
            for (int i = 0, iterLen = isThisContainer ? c.size() : this.size(); i < iterLen; i++) {
                if (container.contains(iterElements[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Contains any.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean containsAny(ShortList c) {
        if (this.isEmpty() || N.isNullOrEmpty(c)) {
            return false;
        }

        return !disjoint(c);
    }

    /**
     * Contains any.
     *
     * @param a the a
     * @return true, if successful
     */
    @Override
    public boolean containsAny(short[] a) {
        if (this.isEmpty() || N.isNullOrEmpty(a)) {
            return false;
        }

        return !disjoint(a);
    }

    /**
     * Disjoint.
     *
     * @param b the b
     * @return true, if successful
     */
    @Override
    public boolean disjoint(final short[] b) {
        if (isEmpty() || N.isNullOrEmpty(b)) {
            return true;
        }

        return disjoint(of(b));
    }

    /**
     * Intersection.
     *
     * @param b the b
     * @return the short list
     * @see IntList#intersection(IntList)
     */
    public ShortList intersection(final ShortList b) {
        if (N.isNullOrEmpty(b)) {
            return new ShortList();
        }

        final Multiset<Short> bOccurrences = b.toMultiset();

        final ShortList c = new ShortList(N.min(9, size(), b.size()));

        for (int i = 0, len = size(); i < len; i++) {
            if (bOccurrences.getAndRemove(elementData[i]) > 0) {
                c.add(elementData[i]);
            }
        }

        return c;
    }

    /**
     * Intersection.
     *
     * @param a the a
     * @return the short list
     */
    public ShortList intersection(final short[] a) {
        if (N.isNullOrEmpty(a)) {
            return new ShortList();
        }

        return intersection(of(a));
    }

    /**
     * Difference.
     *
     * @param b the b
     * @return the short list
     * @see IntList#difference(IntList)
     */
    public ShortList difference(final ShortList b) {
        if (N.isNullOrEmpty(b)) {
            return of(N.copyOfRange(elementData, 0, size()));
        }

        final Multiset<Short> bOccurrences = b.toMultiset();

        final ShortList c = new ShortList(N.min(size(), N.max(9, size() - b.size())));

        for (int i = 0, len = size(); i < len; i++) {
            if (bOccurrences.getAndRemove(elementData[i]) < 1) {
                c.add(elementData[i]);
            }
        }

        return c;
    }

    /**
     * Difference.
     *
     * @param a the a
     * @return the short list
     */
    public ShortList difference(final short[] a) {
        if (N.isNullOrEmpty(a)) {
            return of(N.copyOfRange(elementData, 0, size()));
        }

        return difference(of(a));
    }

    /**
     * Symmetric difference.
     *
     * @param b the b
     * @return this.difference(b).addAll(b.difference(this))
     * @see IntList#symmetricDifference(IntList)
     */
    public ShortList symmetricDifference(final ShortList b) {
        if (N.isNullOrEmpty(b)) {
            return this.copy();
        } else if (this.isEmpty()) {
            return b.copy();
        }

        final Multiset<Short> bOccurrences = b.toMultiset();
        final ShortList c = new ShortList(N.max(9, Math.abs(size() - b.size())));

        for (int i = 0, len = size(); i < len; i++) {
            if (bOccurrences.getAndRemove(elementData[i]) < 1) {
                c.add(elementData[i]);
            }
        }

        for (int i = 0, len = b.size(); i < len; i++) {
            if (bOccurrences.getAndRemove(b.elementData[i]) > 0) {
                c.add(b.elementData[i]);
            }

            if (bOccurrences.isEmpty()) {
                break;
            }
        }

        return c;
    }

    /**
     * Symmetric difference.
     *
     * @param a the a
     * @return the short list
     */
    public ShortList symmetricDifference(final short[] a) {
        if (N.isNullOrEmpty(a)) {
            return of(N.copyOfRange(elementData, 0, size()));
        } else if (this.isEmpty()) {
            return of(N.copyOfRange(a, 0, a.length));
        }

        return symmetricDifference(of(a));
    }

    /**
     * Occurrences of.
     *
     * @param objectToFind the object to find
     * @return the int
     */
    public int occurrencesOf(final short objectToFind) {
        return N.occurrencesOf(elementData, objectToFind);
    }

    /**
     * Index of.
     *
     * @param e the e
     * @return the int
     */
    public int indexOf(short e) {
        return indexOf(0, e);
    }

    /**
     * Index of.
     *
     * @param fromIndex the from index
     * @param e the e
     * @return the int
     */
    public int indexOf(final int fromIndex, short e) {
        checkFromToIndex(fromIndex, size);

        for (int i = fromIndex; i < size; i++) {
            if (elementData[i] == e) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Last index of.
     *
     * @param e the e
     * @return the int
     */
    public int lastIndexOf(short e) {
        return lastIndexOf(size, e);
    }

    /**
     * Last index of.
     *
     * @param fromIndex the start index to traverse backwards from. Inclusive.
     * @param e the e
     * @return the int
     */
    public int lastIndexOf(final int fromIndex, short e) {
        checkFromToIndex(0, fromIndex);

        for (int i = fromIndex == size ? size - 1 : fromIndex; i >= 0; i--) {
            if (elementData[i] == e) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Min.
     *
     * @return the optional short
     */
    public OptionalShort min() {
        return size() == 0 ? OptionalShort.empty() : OptionalShort.of(N.min(elementData, 0, size));
    }

    /**
     * Min.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the optional short
     */
    public OptionalShort min(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return fromIndex == toIndex ? OptionalShort.empty() : OptionalShort.of(N.min(elementData, fromIndex, toIndex));
    }

    /**
     * Median.
     *
     * @return the optional short
     */
    public OptionalShort median() {
        return size() == 0 ? OptionalShort.empty() : OptionalShort.of(N.median(elementData, 0, size));
    }

    /**
     * Median.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the optional short
     */
    public OptionalShort median(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return fromIndex == toIndex ? OptionalShort.empty() : OptionalShort.of(N.median(elementData, fromIndex, toIndex));
    }

    /**
     * Max.
     *
     * @return the optional short
     */
    public OptionalShort max() {
        return size() == 0 ? OptionalShort.empty() : OptionalShort.of(N.max(elementData, 0, size));
    }

    /**
     * Max.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the optional short
     */
    public OptionalShort max(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return fromIndex == toIndex ? OptionalShort.empty() : OptionalShort.of(N.max(elementData, fromIndex, toIndex));
    }

    /**
     * Kth largest.
     *
     * @param k the k
     * @return the optional short
     */
    public OptionalShort kthLargest(final int k) {
        return kthLargest(0, size(), k);
    }

    /**
     * Kth largest.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param k the k
     * @return the optional short
     */
    public OptionalShort kthLargest(final int fromIndex, final int toIndex, final int k) {
        checkFromToIndex(fromIndex, toIndex);
        N.checkArgPositive(k, "k");

        return toIndex - fromIndex < k ? OptionalShort.empty() : OptionalShort.of(N.kthLargest(elementData, fromIndex, toIndex, k));
    }

    /**
     * Sum.
     *
     * @return the int
     */
    public int sum() {
        return sum(0, size());
    }

    /**
     * Sum.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the int
     */
    public int sum(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return N.sum(elementData, fromIndex, toIndex);
    }

    /**
     * Average.
     *
     * @return the optional double
     */
    public OptionalDouble average() {
        return average(0, size());
    }

    /**
     * Average.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the optional double
     */
    public OptionalDouble average(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return fromIndex == toIndex ? OptionalDouble.empty() : OptionalDouble.of(N.average(elementData, fromIndex, toIndex));
    }

    /**
     * For each.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void forEach(Try.ShortConsumer<E> action) throws E {
        forEach(0, size, action);
    }

    /**
     * For each.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void forEach(final int fromIndex, final int toIndex, Try.ShortConsumer<E> action) throws E {
        N.checkFromToIndex(fromIndex < toIndex ? fromIndex : (toIndex == -1 ? 0 : toIndex), fromIndex < toIndex ? toIndex : fromIndex, size);

        if (size > 0) {
            if (fromIndex <= toIndex) {
                for (int i = fromIndex; i < toIndex; i++) {
                    action.accept(elementData[i]);
                }
            } else {
                for (int i = N.min(size - 1, fromIndex); i > toIndex; i--) {
                    action.accept(elementData[i]);
                }
            }
        }
    }

    /**
     * First.
     *
     * @return the optional short
     */
    public OptionalShort first() {
        return size() == 0 ? OptionalShort.empty() : OptionalShort.of(elementData[0]);
    }

    /**
     * Last.
     *
     * @return the optional short
     */
    public OptionalShort last() {
        return size() == 0 ? OptionalShort.empty() : OptionalShort.of(elementData[size() - 1]);
    }

    /**
     * Find first.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional short
     * @throws E the e
     */
    public <E extends Exception> OptionalShort findFirst(Try.ShortPredicate<E> predicate) throws E {
        for (int i = 0; i < size; i++) {
            if (predicate.test(elementData[i])) {
                return OptionalShort.of(elementData[i]);
            }
        }

        return OptionalShort.empty();
    }

    /**
     * Find last.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional short
     * @throws E the e
     */
    public <E extends Exception> OptionalShort findLast(Try.ShortPredicate<E> predicate) throws E {
        for (int i = size - 1; i >= 0; i--) {
            if (predicate.test(elementData[i])) {
                return OptionalShort.of(elementData[i]);
            }
        }

        return OptionalShort.empty();
    }

    /**
     * Find first index.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional int
     * @throws E the e
     */
    public <E extends Exception> OptionalInt findFirstIndex(Try.ShortPredicate<E> predicate) throws E {
        for (int i = 0; i < size; i++) {
            if (predicate.test(elementData[i])) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }

    /**
     * Find last index.
     *
     * @param <E> the element type
     * @param predicate the predicate
     * @return the optional int
     * @throws E the e
     */
    public <E extends Exception> OptionalInt findLastIndex(Try.ShortPredicate<E> predicate) throws E {
        for (int i = size - 1; i >= 0; i--) {
            if (predicate.test(elementData[i])) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }

    /**
     * Returns whether all elements of this List match the provided predicate.
     *
     * @param <E> the element type
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean allMatch(Try.ShortPredicate<E> filter) throws E {
        return allMatch(0, size(), filter);
    }

    /**
     * All match.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean allMatch(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter) throws E {
        checkFromToIndex(fromIndex, toIndex);

        if (size > 0) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (filter.test(elementData[i]) == false) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns whether any elements of this List match the provided predicate.
     *
     * @param <E> the element type
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean anyMatch(Try.ShortPredicate<E> filter) throws E {
        return anyMatch(0, size(), filter);
    }

    /**
     * Any match.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean anyMatch(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter) throws E {
        checkFromToIndex(fromIndex, toIndex);

        if (size > 0) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (filter.test(elementData[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns whether no elements of this List match the provided predicate.
     *
     * @param <E> the element type
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean noneMatch(Try.ShortPredicate<E> filter) throws E {
        return noneMatch(0, size(), filter);
    }

    /**
     * None match.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @return true, if successful
     * @throws E the e
     */
    public <E extends Exception> boolean noneMatch(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter) throws E {
        checkFromToIndex(fromIndex, toIndex);

        if (size > 0) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (filter.test(elementData[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Count.
     *
     * @param <E> the element type
     * @param filter the filter
     * @return the int
     * @throws E the e
     */
    public <E extends Exception> int count(Try.ShortPredicate<E> filter) throws E {
        return count(0, size(), filter);
    }

    /**
     * Count.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @return the int
     * @throws E the e
     */
    public <E extends Exception> int count(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter) throws E {
        checkFromToIndex(fromIndex, toIndex);

        return N.count(elementData, fromIndex, toIndex, filter);
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param filter the filter
     * @return a new List with the elements match the provided predicate.
     * @throws E the e
     */
    public <E extends Exception> ShortList filter(Try.ShortPredicate<E> filter) throws E {
        return filter(0, size(), filter);
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @return the short list
     * @throws E the e
     */
    public <E extends Exception> ShortList filter(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter) throws E {
        checkFromToIndex(fromIndex, toIndex);

        return N.filter(elementData, fromIndex, toIndex, filter);
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param filter the filter
     * @param max the max
     * @return a new List with the elements match the provided predicate.
     * @throws E the e
     */
    public <E extends Exception> ShortList filter(Try.ShortPredicate<E> filter, int max) throws E {
        return filter(0, size(), filter, max);
    }

    /**
     * Filter.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param filter the filter
     * @param max the max
     * @return the short list
     * @throws E the e
     */
    public <E extends Exception> ShortList filter(final int fromIndex, final int toIndex, Try.ShortPredicate<E> filter, final int max) throws E {
        checkFromToIndex(fromIndex, toIndex);

        return N.filter(elementData, fromIndex, toIndex, filter, max);
    }

    /**
     * Map.
     *
     * @param <E> the element type
     * @param mapper the mapper
     * @return the short list
     * @throws E the e
     */
    public <E extends Exception> ShortList map(final Try.ShortUnaryOperator<E> mapper) throws E {
        return map(0, size, mapper);
    }

    /**
     * Map.
     *
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param mapper the mapper
     * @return the short list
     * @throws E the e
     */
    public <E extends Exception> ShortList map(final int fromIndex, final int toIndex, final Try.ShortUnaryOperator<E> mapper) throws E {
        checkFromToIndex(fromIndex, toIndex);

        final ShortList result = new ShortList(toIndex - fromIndex);

        for (int i = fromIndex; i < toIndex; i++) {
            result.add(mapper.applyAsShort(elementData[i]));
        }

        return result;
    }

    /**
     * Map to obj.
     *
     * @param <T> the generic type
     * @param <E> the element type
     * @param mapper the mapper
     * @return the list
     * @throws E the e
     */
    public <T, E extends Exception> List<T> mapToObj(final Try.ShortFunction<? extends T, E> mapper) throws E {
        return mapToObj(0, size, mapper);
    }

    /**
     * Map to obj.
     *
     * @param <T> the generic type
     * @param <E> the element type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param mapper the mapper
     * @return the list
     * @throws E the e
     */
    public <T, E extends Exception> List<T> mapToObj(final int fromIndex, final int toIndex, final Try.ShortFunction<? extends T, E> mapper) throws E {
        checkFromToIndex(fromIndex, toIndex);

        final List<T> result = new ArrayList<>(toIndex - fromIndex);

        for (int i = fromIndex; i < toIndex; i++) {
            result.add(mapper.apply(elementData[i]));
        }

        return result;
    }

    /**
     * This is equivalent to:
     * <pre>
     * <code>
     *    if (isEmpty()) {
     *        return OptionalShort.empty();
     *    }
     * 
     *    short result = elementData[0];
     * 
     *    for (int i = 1; i < size; i++) {
     *        result = accumulator.applyAsShort(result, elementData[i]);
     *    }
     * 
     *    return OptionalShort.of(result);
     * </code>
     * </pre>
     *
     * @param <E> the element type
     * @param accumulator the accumulator
     * @return the optional short
     * @throws E the e
     */
    public <E extends Exception> OptionalShort reduce(final Try.ShortBinaryOperator<E> accumulator) throws E {
        if (isEmpty()) {
            return OptionalShort.empty();
        }

        short result = elementData[0];

        for (int i = 1; i < size; i++) {
            result = accumulator.applyAsShort(result, elementData[i]);
        }

        return OptionalShort.of(result);
    }

    /**
     * This is equivalent to:
     * <pre>
     * <code>
     *     if (isEmpty()) {
     *         return identity;
     *     }
     * 
     *     short result = identity;
     * 
     *     for (int i = 0; i < size; i++) {
     *         result = accumulator.applyAsShort(result, elementData[i]);
     *    }
     * 
     *     return result;
     * </code>
     * </pre>
     *
     * @param <E> the element type
     * @param identity the identity
     * @param accumulator the accumulator
     * @return the short
     * @throws E the e
     */
    public <E extends Exception> short reduce(final short identity, final Try.ShortBinaryOperator<E> accumulator) throws E {
        if (isEmpty()) {
            return identity;
        }

        short result = identity;

        for (int i = 0; i < size; i++) {
            result = accumulator.applyAsShort(result, elementData[i]);
        }

        return result;
    }

    /**
     * Checks for duplicates.
     *
     * @return true, if successful
     */
    @Override
    public boolean hasDuplicates() {
        return N.hasDuplicates(elementData, 0, size, false);
    }

    /**
     * Distinct.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the short list
     */
    @Override
    public ShortList distinct(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        if (toIndex - fromIndex > 1) {
            return of(N.distinct(elementData, fromIndex, toIndex));
        } else {
            return of(N.copyOfRange(elementData, fromIndex, toIndex));
        }
    }

    /**
     * Top.
     *
     * @param n the n
     * @return the short list
     */
    public ShortList top(final int n) {
        return top(0, size(), n);
    }

    /**
     * Top.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param n the n
     * @return the short list
     */
    public ShortList top(final int fromIndex, final int toIndex, final int n) {
        checkFromToIndex(fromIndex, toIndex);

        return of(N.top(elementData, fromIndex, toIndex, n));
    }

    /**
     * Top.
     *
     * @param n the n
     * @param cmp the cmp
     * @return the short list
     */
    public ShortList top(final int n, Comparator<? super Short> cmp) {
        return top(0, size(), n, cmp);
    }

    /**
     * Top.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param n the n
     * @param cmp the cmp
     * @return the short list
     */
    public ShortList top(final int fromIndex, final int toIndex, final int n, Comparator<? super Short> cmp) {
        checkFromToIndex(fromIndex, toIndex);

        return of(N.top(elementData, fromIndex, toIndex, n, cmp));
    }

    /**
     * Sort.
     */
    @Override
    public void sort() {
        if (size > 1) {
            N.sort(elementData, 0, size);
        }
    }

    /**
     * Parallel sort.
     */
    public void parallelSort() {
        if (size > 1) {
            N.parallelSort(elementData, 0, size);
        }
    }

    /**
     * Reverse sort.
     */
    public void reverseSort() {
        if (size > 1) {
            sort();
            reverse();
        }
    }

    /**
     * This List should be sorted first.
     *
     * @param key the key
     * @return the int
     */
    public int binarySearch(final short key) {
        return N.binarySearch(elementData, key);
    }

    /**
     * This List should be sorted first.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param key the key
     * @return the int
     */
    public int binarySearch(final int fromIndex, final int toIndex, final short key) {
        checkFromToIndex(fromIndex, toIndex);

        return N.binarySearch(elementData, fromIndex, toIndex, key);
    }

    /**
     * Reverse.
     */
    @Override
    public void reverse() {
        if (size > 1) {
            N.reverse(elementData, 0, size);
        }
    }

    /**
     * Reverse.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     */
    @Override
    public void reverse(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        if (toIndex - fromIndex > 1) {
            N.reverse(elementData, fromIndex, toIndex);
        }
    }

    /**
     * Rotate.
     *
     * @param distance the distance
     */
    @Override
    public void rotate(int distance) {
        if (size > 1) {
            N.rotate(elementData, distance);
        }
    }

    /**
     * Shuffle.
     */
    @Override
    public void shuffle() {
        if (size() > 1) {
            N.shuffle(elementData);
        }
    }

    /**
     * Shuffle.
     *
     * @param rnd the rnd
     */
    @Override
    public void shuffle(final Random rnd) {
        if (size() > 1) {
            N.shuffle(elementData, rnd);
        }
    }

    /**
     * Swap.
     *
     * @param i the i
     * @param j the j
     */
    @Override
    public void swap(int i, int j) {
        rangeCheck(i);
        rangeCheck(j);

        set(i, set(j, elementData[i]));
    }

    /**
     * Copy.
     *
     * @return the short list
     */
    @Override
    public ShortList copy() {
        return new ShortList(N.copyOfRange(elementData, 0, size));
    }

    /**
     * Copy.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the short list
     */
    @Override
    public ShortList copy(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return new ShortList(N.copyOfRange(elementData, fromIndex, toIndex));
    }

    /**
     * Copy.
     *
     * @param from the from
     * @param to the to
     * @param step the step
     * @return the short list
     * @see N#copyOfRange(int[], int, int, int)
     */
    @Override
    public ShortList copy(final int from, final int to, final int step) {
        checkFromToIndex(from < to ? from : (to == -1 ? 0 : to), from < to ? to : from);

        return new ShortList(N.copyOfRange(elementData, from, to, step));
    }

    /**
     * Returns List of {@code ShortList} with consecutive sub sequences of the elements, each of the same size (the final sequence may be smaller).
     *  
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param size the size
     * @return the list
     */
    @Override
    public List<ShortList> split(final int fromIndex, final int toIndex, final int size) {
        checkFromToIndex(fromIndex, toIndex);

        final List<short[]> list = N.split(elementData, fromIndex, toIndex, size);
        @SuppressWarnings("rawtypes")
        final List<ShortList> result = (List) list;

        for (int i = 0, len = list.size(); i < len; i++) {
            result.set(i, of(list.get(i)));
        }

        return result;
    }

    //    @Override
    //    public List<ShortList> split(int fromIndex, int toIndex, ShortPredicate predicate) {
    //        checkIndex(fromIndex, toIndex);
    //
    //        final List<ShortList> result = new ArrayList<>();
    //        ShortList piece = null;
    //
    //        for (int i = fromIndex; i < toIndex;) {
    //            if (piece == null) {
    //                piece = ShortList.of(N.EMPTY_SHORT_ARRAY);
    //            }
    //
    //            if (predicate.test(elementData[i])) {
    //                piece.add(elementData[i]);
    //                i++;
    //            } else {
    //                result.add(piece);
    //                piece = null;
    //            }
    //        }
    //
    //        if (piece != null) {
    //            result.add(piece);
    //        }
    //
    //        return result;
    //    }

    /**
     * Join.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param delimiter the delimiter
     * @return the string
     */
    @Override
    public String join(int fromIndex, int toIndex, char delimiter) {
        checkFromToIndex(fromIndex, toIndex);

        return StringUtil.join(elementData, fromIndex, toIndex, delimiter);
    }

    /**
     * Join.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param delimiter the delimiter
     * @return the string
     */
    @Override
    public String join(int fromIndex, int toIndex, String delimiter) {
        checkFromToIndex(fromIndex, toIndex);

        return StringUtil.join(elementData, fromIndex, toIndex, delimiter);
    }

    /**
     * Trim to size.
     *
     * @return the short list
     */
    @Override
    public ShortList trimToSize() {
        if (elementData.length > size) {
            elementData = N.copyOfRange(elementData, 0, size);
        }

        return this;
    }

    /**
     * Clear.
     */
    @Override
    public void clear() {
        if (size > 0) {
            N.fill(elementData, 0, size, (short) 0);
        }

        size = 0;
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Size.
     *
     * @return the int
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Boxed.
     *
     * @return the list
     */
    public List<Short> boxed() {
        return boxed(0, size);
    }

    /**
     * Boxed.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the list
     */
    public List<Short> boxed(int fromIndex, int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        final List<Short> res = new ArrayList<>(toIndex - fromIndex);

        for (int i = fromIndex; i < toIndex; i++) {
            res.add(elementData[i]);
        }

        return res;
    }

    /**
     * To array.
     *
     * @return the short[]
     */
    @Override
    public short[] toArray() {
        return N.copyOfRange(elementData, 0, size);
    }

    /**
     * To int list.
     *
     * @return the int list
     */
    public IntList toIntList() {
        final int[] a = new int[size];

        for (int i = 0; i < size; i++) {
            a[i] = elementData[i];
        }

        return IntList.of(a);
    }

    /**
     * To collection.
     *
     * @param <C> the generic type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param supplier the supplier
     * @return the c
     */
    @Override
    public <C extends Collection<Short>> C toCollection(final int fromIndex, final int toIndex, final IntFunction<? extends C> supplier) {
        checkFromToIndex(fromIndex, toIndex);

        final C c = supplier.apply(toIndex - fromIndex);

        for (int i = fromIndex; i < toIndex; i++) {
            c.add(elementData[i]);
        }

        return c;
    }

    /**
     * To multiset.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param supplier the supplier
     * @return the multiset
     */
    @Override
    public Multiset<Short> toMultiset(final int fromIndex, final int toIndex, final IntFunction<Multiset<Short>> supplier) {
        checkFromToIndex(fromIndex, toIndex);

        final Multiset<Short> multiset = supplier.apply(toIndex - fromIndex);

        for (int i = fromIndex; i < toIndex; i++) {
            multiset.add(elementData[i]);
        }

        return multiset;
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param keyMapper the key mapper
     * @param valueMapper the value mapper
     * @return the map
     * @throws E the e
     * @throws E2 the e2
     */
    public <K, V, E extends Exception, E2 extends Exception> Map<K, V> toMap(Try.ShortFunction<? extends K, E> keyMapper,
            Try.ShortFunction<? extends V, E2> valueMapper) throws E, E2 {
        return toMap(keyMapper, valueMapper, Factory.<K, V> ofMap());
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the generic type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param keyMapper the key mapper
     * @param valueMapper the value mapper
     * @param mapFactory the map factory
     * @return the m
     * @throws E the e
     * @throws E2 the e2
     */
    public <K, V, M extends Map<K, V>, E extends Exception, E2 extends Exception> M toMap(Try.ShortFunction<? extends K, E> keyMapper,
            Try.ShortFunction<? extends V, E2> valueMapper, IntFunction<? extends M> mapFactory) throws E, E2 {
        final Try.BinaryOperator<V, RuntimeException> mergeFunction = Fn.throwingMerger();

        return toMap(keyMapper, valueMapper, mergeFunction, mapFactory);
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param <E3> the generic type
     * @param keyMapper the key mapper
     * @param valueMapper the value mapper
     * @param mergeFunction the merge function
     * @return the map
     * @throws E the e
     * @throws E2 the e2
     * @throws E3 the e3
     */
    public <K, V, E extends Exception, E2 extends Exception, E3 extends Exception> Map<K, V> toMap(Try.ShortFunction<? extends K, E> keyMapper,
            Try.ShortFunction<? extends V, E2> valueMapper, Try.BinaryOperator<V, E3> mergeFunction) throws E, E2, E3 {
        return toMap(keyMapper, valueMapper, mergeFunction, Factory.<K, V> ofMap());
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the generic type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param <E3> the generic type
     * @param keyMapper the key mapper
     * @param valueMapper the value mapper
     * @param mergeFunction the merge function
     * @param mapFactory the map factory
     * @return the m
     * @throws E the e
     * @throws E2 the e2
     * @throws E3 the e3
     */
    public <K, V, M extends Map<K, V>, E extends Exception, E2 extends Exception, E3 extends Exception> M toMap(Try.ShortFunction<? extends K, E> keyMapper,
            Try.ShortFunction<? extends V, E2> valueMapper, Try.BinaryOperator<V, E3> mergeFunction, IntFunction<? extends M> mapFactory) throws E, E2, E3 {
        final M result = mapFactory.apply(size);

        for (int i = 0; i < size; i++) {
            Maps.merge(result, keyMapper.apply(elementData[i]), valueMapper.apply(elementData[i]), mergeFunction);
        }

        return result;
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <A> the generic type
     * @param <D> the generic type
     * @param <E> the element type
     * @param keyMapper the key mapper
     * @param downstream the downstream
     * @return the map
     * @throws E the e
     */
    public <K, A, D, E extends Exception> Map<K, D> toMap(Try.ShortFunction<? extends K, E> keyMapper, Collector<Short, A, D> downstream) throws E {
        return toMap(keyMapper, downstream, Factory.<K, D> ofMap());
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <A> the generic type
     * @param <D> the generic type
     * @param <M> the generic type
     * @param <E> the element type
     * @param keyMapper the key mapper
     * @param downstream the downstream
     * @param mapFactory the map factory
     * @return the m
     * @throws E the e
     */
    public <K, A, D, M extends Map<K, D>, E extends Exception> M toMap(final Try.ShortFunction<? extends K, E> keyMapper,
            final Collector<Short, A, D> downstream, final IntFunction<? extends M> mapFactory) throws E {
        final M result = mapFactory.apply(size);
        final Supplier<A> downstreamSupplier = downstream.supplier();
        final BiConsumer<A, Short> downstreamAccumulator = downstream.accumulator();
        final Map<K, A> intermediate = (Map<K, A>) result;
        K key = null;
        A v = null;

        for (int i = 0; i < size; i++) {
            key = N.checkArgNotNull(keyMapper.apply(elementData[i]), "element cannot be mapped to a null key");

            if ((v = intermediate.get(key)) == null) {
                if ((v = downstreamSupplier.get()) != null) {
                    intermediate.put(key, v);
                }
            }

            downstreamAccumulator.accept(v, elementData[i]);
        }

        final BiFunction<? super K, ? super A, ? extends A> function = new BiFunction<K, A, A>() {
            @Override
            public A apply(K k, A v) {
                return (A) downstream.finisher().apply(v);
            }
        };

        Maps.replaceAll(intermediate, function);

        return result;
    }

    /**
     * Iterator.
     *
     * @return the short iterator
     */
    public ShortIterator iterator() {
        if (isEmpty()) {
            return ShortIterator.EMPTY;
        }

        return ShortIterator.of(elementData, 0, size);
    }

    /**
     * Stream.
     *
     * @return the short stream
     */
    public ShortStream stream() {
        return ShortStream.of(elementData, 0, size());
    }

    /**
     * Stream.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the short stream
     */
    public ShortStream stream(final int fromIndex, final int toIndex) {
        checkFromToIndex(fromIndex, toIndex);

        return ShortStream.of(elementData, fromIndex, toIndex);
    }

    /**
     * Apply.
     *
     * @param <R> the generic type
     * @param <E> the element type
     * @param func the func
     * @return the r
     * @throws E the e
     */
    @Override
    public <R, E extends Exception> R apply(Try.Function<? super ShortList, R, E> func) throws E {
        return func.apply(this);
    }

    /**
     * Apply if not empty.
     *
     * @param <R> the generic type
     * @param <E> the element type
     * @param func the func
     * @return the optional
     * @throws E the e
     */
    @Override
    public <R, E extends Exception> Optional<R> applyIfNotEmpty(Function<? super ShortList, R, E> func) throws E {
        return isEmpty() ? Optional.<R> empty() : Optional.ofNullable(func.apply(this));
    }

    /**
     * Accept.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    @Override
    public <E extends Exception> void accept(Try.Consumer<? super ShortList, E> action) throws E {
        action.accept(this);
    }

    /**
     * Accept if not empty.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    @Override
    public <E extends Exception> void acceptIfNotEmpty(Try.Consumer<? super ShortList, E> action) throws E {
        if (size > 0) {
            action.accept(this);
        }
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return N.hashCode(elementData, 0, size);
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ShortList) {
            final ShortList other = (ShortList) obj;

            return this.size == other.size && N.equals(this.elementData, 0, other.elementData, 0, this.size);
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
        return size == 0 ? "[]" : N.toString(elementData, 0, size);
    }

    /**
     * Ensure capacity internal.
     *
     * @param minCapacity the min capacity
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == N.EMPTY_SHORT_ARRAY) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    /**
     * Ensure explicit capacity.
     *
     * @param minCapacity the min capacity
     */
    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - elementData.length > 0) {
            grow(minCapacity);
        }
    }

    /**
     * Grow.
     *
     * @param minCapacity the min capacity
     */
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);

        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }

        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }

        elementData = Arrays.copyOf(elementData, newCapacity);
    }
}