/*
 * Copyright (C) 2016 HaiYang Li
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

// TODO: Auto-generated Javadoc
/**
 * The Class ImmutableMap.
 *
 * @author Haiyang Li
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.8
 */
public class ImmutableMap<K, V> implements Map<K, V> {

    /** The Constant EMPTY. */
    @SuppressWarnings("rawtypes")
    private static final ImmutableMap EMPTY = new ImmutableMap(Collections.EMPTY_MAP);

    /** The map. */
    private final Map<K, V> map;

    /**
     * Instantiates a new immutable map.
     *
     * @param map the map
     */
    ImmutableMap(final Map<? extends K, ? extends V> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    /**
     * Empty.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return the immutable map
     */
    public static <K, V> ImmutableMap<K, V> empty() {
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1) {
        return new ImmutableMap<K, V>(Collections.singletonMap(k1, v1));
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2);
        return new ImmutableMap<K, V>(map);
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2, k3, v3);
        return new ImmutableMap<K, V>(map);
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4);
        return new ImmutableMap<K, V>(map);
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        return new ImmutableMap<K, V>(map);
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5, final k k6, final v v6) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        return new ImmutableMap<K, V>(map);
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
     * @return the immutable map
     */
    public static <K, V, k extends K, v extends V> ImmutableMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5, final k k6, final v v6, final k k7, final v v7) {
        final Map<k, v> map = N.asLinkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        return new ImmutableMap<K, V>(map);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the elements in this <code>map</code> are shared by the returned ImmutableMap.
     * @return the immutable map
     */
    public static <K, V> ImmutableMap<K, V> of(final Map<? extends K, ? extends V> map) {
        if (map == null) {
            return empty();
        } else if (map instanceof ImmutableMap) {
            return (ImmutableMap<K, V>) map;
        }

        return new ImmutableMap<>(map);
    }

    /**
     * Copy of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the immutable map
     */
    public static <K, V> ImmutableMap<K, V> copyOf(final Map<? extends K, ? extends V> map) {
        if (N.isNullOrEmpty(map)) {
            return empty();
        }

        final Map<K, V> tmp = map instanceof IdentityHashMap ? new IdentityHashMap<>(map)
                : ((map instanceof LinkedHashMap || map instanceof SortedMap) ? new LinkedHashMap<>(map) : new HashMap<>(map));

        return new ImmutableMap<>(tmp);
    }

    /**
     * Gets the or default.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the or default
     */
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        final V val = get(key);

        return val == null && containsKey(key) == false ? defaultValue : val;
    }

    /**
     * Put.
     *
     * @param k the k
     * @param v the v
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the.
     *
     * @param o the o
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Put all.
     *
     * @param map the map
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * Put if absent.
     *
     * @param key the key
     * @param value the value
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the.
     *
     * @param key the key
     * @param value the value
     * @return true, if successful
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Replace.
     *
     * @param key the key
     * @param oldValue the old value
     * @param newValue the new value
     * @return true, if successful
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Replace.
     *
     * @param key the key
     * @param value the value
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Compute if absent.
     *
     * @param key the key
     * @param mappingFunction the mapping function
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    /**
     * Compute if present.
     *
     * @param key the key
     * @param remappingFunction the remapping function
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /**
     * Compute.
     *
     * @param key the key
     * @param remappingFunction the remapping function
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /**
     * Merge.
     *
     * @param key the key
     * @param value the value
     * @param remappingFunction the remapping function
     * @return the v
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /**
     * Clear.
     *
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Contains key.
     *
     * @param key the key
     * @return true, if successful
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Contains value.
     *
     * @param value the value
     * @return true, if successful
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Gets the.
     *
     * @param key the key
     * @return the v
     */
    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * Key set.
     *
     * @return the sets the
     */
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Values.
     *
     * @return the collection
     */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /**
     * Entry set.
     *
     * @return the sets the
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Size.
     *
     * @return the int
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImmutableMap && ((ImmutableMap<K, V>) obj).map.equals(map);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return map.toString();
    }
}