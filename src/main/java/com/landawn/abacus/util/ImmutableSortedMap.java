/*
 * Copyright (C) 2017 HaiYang Li
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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

// TODO: Auto-generated Javadoc
/**
 * The Class ImmutableSortedMap.
 *
 * @author Haiyang Li
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.1.4
 */
public class ImmutableSortedMap<K, V> extends ImmutableMap<K, V> implements SortedMap<K, V> {

    /** The Constant EMPTY. */
    @SuppressWarnings("rawtypes")
    private static final ImmutableSortedMap EMPTY = new ImmutableSortedMap(N.emptySortedMap());

    /** The sorted map. */
    private final SortedMap<K, V> sortedMap;

    /**
     * Instantiates a new immutable sorted map.
     *
     * @param sortedMap the sorted map
     */
    ImmutableSortedMap(SortedMap<? extends K, ? extends V> sortedMap) {
        super(sortedMap);
        this.sortedMap = (SortedMap<K, V>) sortedMap;
    }

    /**
     * Empty.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return the immutable sorted map
     */
    public static <K, V> ImmutableSortedMap<K, V> empty() {
        return EMPTY;
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2,
            final k k3, final v v3) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @param k4 the k 4
     * @param v4 the v 4
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2,
            final k k3, final v v3, final k k4, final v v4) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @param k4 the k 4
     * @param v4 the v 4
     * @param k5 the k 5
     * @param v5 the v 5
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2,
            final k k3, final v v3, final k k4, final v v4, final k k5, final v v5) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @param k4 the k 4
     * @param v4 the v 4
     * @param k5 the k 5
     * @param v5 the v 5
     * @param k6 the k 6
     * @param v6 the v 6
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2,
            final k k3, final v v3, final k k4, final v v4, final k k5, final v v5, final k k6, final v v6) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <k> the generic type
     * @param <v> the generic type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @param k4 the k 4
     * @param v4 the v 4
     * @param k5 the k 5
     * @param v5 the v 5
     * @param k6 the k 6
     * @param v6 the v 6
     * @param k7 the k 7
     * @param v7 the v 7
     * @return the immutable sorted map
     */
    public static <K extends Comparable<? super K>, V, k extends K, v extends V> ImmutableSortedMap<K, V> of(final k k1, final v v1, final k k2, final v v2,
            final k k3, final v v3, final k k4, final v v4, final k k5, final v v5, final k k6, final v v6, final k k7, final v v7) {
        final SortedMap<K, V> map = N.newTreeMap();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);

        return new ImmutableSortedMap<>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param sortedMap the elements in this <code>map</code> are shared by the returned ImmutableSortedMap.
     * @return the immutable sorted map
     */
    public static <K, V> ImmutableSortedMap<K, V> of(final SortedMap<? extends K, ? extends V> sortedMap) {
        if (sortedMap == null) {
            return empty();
        } else if (sortedMap instanceof ImmutableSortedMap) {
            return (ImmutableSortedMap<K, V>) sortedMap;
        }

        return new ImmutableSortedMap<>(sortedMap);
    }

    /**
     * Copy of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param sortedMap the sorted map
     * @return the immutable sorted map
     */
    public static <K, V> ImmutableSortedMap<K, V> copyOf(final SortedMap<? extends K, ? extends V> sortedMap) {
        if (N.isNullOrEmpty(sortedMap)) {
            return empty();
        }

        return new ImmutableSortedMap<>(new TreeMap<>(sortedMap));
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the immutable map
     */
    @Deprecated
    public static <K, V> ImmutableMap<K, V> of(final Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * Copy of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the immutable map
     */
    @Deprecated
    public static <K, V> ImmutableMap<K, V> copyOf(final Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * Comparator.
     *
     * @return the comparator<? super k>
     */
    @Override
    public Comparator<? super K> comparator() {
        return sortedMap.comparator();
    }

    /**
     * Sub map.
     *
     * @param fromKey the from key
     * @param toKey the to key
     * @return the sorted map
     */
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return of(sortedMap.subMap(fromKey, toKey));
    }

    /**
     * Head map.
     *
     * @param toKey the to key
     * @return the sorted map
     */
    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return of(sortedMap.headMap(toKey));
    }

    /**
     * Tail map.
     *
     * @param fromKey the from key
     * @return the sorted map
     */
    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return of(sortedMap.tailMap(fromKey));
    }

    /**
     * First key.
     *
     * @return the k
     */
    @Override
    public K firstKey() {
        return sortedMap.firstKey();
    }

    /**
     * Last key.
     *
     * @return the k
     */
    @Override
    public K lastKey() {
        return sortedMap.lastKey();
    }
}