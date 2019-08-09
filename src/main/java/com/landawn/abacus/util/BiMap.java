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

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.landawn.abacus.annotation.Internal;
import com.landawn.abacus.util.function.Supplier;

// TODO: Auto-generated Javadoc
/**
 * A BiMap (or "bidirectional map") is a map that preserves the uniqueness of its values as well as that of its keys. 
 * This constraint enables BiMaps to support an "inverse view", which is another BiMap containing the same entries as this BiMap but with reversed keys and values.
 *
 * @author Haiyang Li
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.8
 */
public final class BiMap<K, V> implements Map<K, V> {
    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with
     * arguments. MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** The key map supplier. */
    final Supplier<? extends Map<K, V>> keyMapSupplier;

    /** The value map supplier. */
    final Supplier<? extends Map<V, K>> valueMapSupplier;

    /** The key map. */
    final Map<K, V> keyMap;

    /** The value map. */
    final Map<V, K> valueMap;

    /** The inverse. */
    private transient BiMap<V, K> inverse;

    /**
     * Instantiates a new bi map.
     */
    public BiMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Instantiates a new bi map.
     *
     * @param initialCapacity the initial capacity
     */
    public BiMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Instantiates a new bi map.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor
     */
    public BiMap(int initialCapacity, float loadFactor) {
        this(new HashMap<K, V>(initialCapacity, loadFactor), new HashMap<V, K>(initialCapacity, loadFactor));
    }

    /**
     * Instantiates a new bi map.
     *
     * @param keyMapType the key map type
     * @param valueMapType the value map type
     */
    @SuppressWarnings("rawtypes")
    public BiMap(final Class<? extends Map> keyMapType, final Class<? extends Map> valueMapType) {
        this(Maps.mapType2Supplier(keyMapType), Maps.mapType2Supplier(valueMapType));
    }

    /**
     * Instantiates a new bi map.
     *
     * @param keyMapSupplier the key map supplier
     * @param valueMapSupplier the value map supplier
     */
    public BiMap(final Supplier<? extends Map<K, V>> keyMapSupplier, final Supplier<? extends Map<V, K>> valueMapSupplier) {
        this.keyMapSupplier = keyMapSupplier;
        this.valueMapSupplier = valueMapSupplier;
        this.keyMap = keyMapSupplier.get();
        this.valueMap = valueMapSupplier.get();
    }

    /**
     * Instantiates a new bi map.
     *
     * @param keyMap The keyMap and this BiMap share the same data; any changes to one will appear in the other.
     * @param valueMap The valueMap and this BiMap share the same data; any changes to one will appear in the other.
     */
    @Internal
    BiMap(final Map<K, V> keyMap, final Map<V, K> valueMap) {
        this.keyMapSupplier = Maps.mapType2Supplier(keyMap.getClass());
        this.valueMapSupplier = Maps.mapType2Supplier(valueMap.getClass());
        this.keyMap = keyMap;
        this.valueMap = valueMap;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5, final k k6, final v v6) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);

        return map;
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
     * @return the bi map
     */
    public static <K, V, k extends K, v extends V> BiMap<K, V> of(final k k1, final v v1, final k k2, final v v2, final k k3, final v v3, final k k4,
            final v v4, final k k5, final v v5, final k k6, final v v6, final k k7, final v v7) {
        final BiMap<K, V> map = new BiMap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);

        return map;
    }

    /**
     * Copy of.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the bi map
     */
    public static <K, V> BiMap<K, V> copyOf(final Map<? extends K, ? extends V> map) {
        final BiMap<K, V> biMap = new BiMap<>(Maps.newTargetMap(map), Maps.newOrderingMap(map));

        biMap.putAll(map);

        return biMap;
    }

    /**
     * From.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the bi map
     * @deprecated replaced by {@code copyOf}
     */
    @Deprecated
    public static <K, V> BiMap<K, V> from(final Map<? extends K, ? extends V> map) {
        return copyOf(map);
    }

    /**
     * Gets the.
     *
     * @param key the key
     * @return the v
     */
    @Override
    public V get(Object key) {
        return keyMap.get(key);
    }

    /**
     * Gets the by value.
     *
     * @param value the value
     * @return the by value
     */
    public K getByValue(Object value) {
        return valueMap.get(value);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the v
     * @throws IllegalArgumentException if the given value is already bound to a
     *     different key in this bimap. The bimap will remain unmodified in this
     *     event. To avoid this exception, call {@link #forcePut} instead.
     */
    @Override
    public V put(final K key, final V value) {
        return put(key, value, false);
    }

    /**
     * An alternate form of {@code put} that silently removes any existing entry
     * with the value {@code value} before proceeding with the {@link #put}
     * operation. If the bimap previously contained the provided key-value
     * mapping, this method has no effect.
     *
     * <p>Note that a successful call to this method could cause the size of the
     * bimap to increase by one, stay the same, or even decrease by one.
     *
     * <p><b>Warning:</b> If an existing entry with this value is removed, the key
     * for that entry is discarded and not returned.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the value which was previously associated with the key, which may
     *     be {@code null}, or {@code null} if there was no previous entry
     */
    public V forcePut(final K key, final V value) {
        return put(key, value, true);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @param isForce the is force
     * @return the v
     */
    private V put(final K key, final V value, final boolean isForce) {
        if ((key == null) || (value == null)) {
            throw new NullPointerException("key or value can't be null");
        } else if (isForce == false && valueMap.containsKey(value)) {
            throw new IllegalArgumentException("Value already exists: " + value);
        }

        V v = keyMap.remove(key);

        if (v != null) {
            valueMap.remove(v);
        }

        K k = valueMap.remove(value);

        if (k != null) {
            keyMap.remove(k);
        }

        keyMap.put(key, value);
        valueMap.put(value, key);

        return v;
    }

    /**
     * <p><b>Warning:</b> the results of calling this method may vary depending on
     * the iteration order of {@code map}.
     *
     * @param m the m
     * @throws IllegalArgumentException if an attempt to {@code put} any
     *     entry fails. Note that some map entries may have been added to the
     *     bimap before the exception was thrown.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the.
     *
     * @param key the key
     * @return the v
     */
    @Override
    public V remove(Object key) {
        V value = keyMap.remove(key);

        if (value != null) {
            valueMap.remove(value);
        }

        return value;
    }

    /**
     * Removes the by value.
     *
     * @param value the value
     * @return the k
     */
    public K removeByValue(Object value) {
        K key = valueMap.remove(value);

        if (key != null) {
            keyMap.remove(key);
        }

        return key;
    }

    /**
     * Contains key.
     *
     * @param key the key
     * @return true, if successful
     */
    @Override
    public boolean containsKey(Object key) {
        return keyMap.containsKey(key);
    }

    /**
     * Contains value.
     *
     * @param value the value
     * @return true, if successful
     */
    @Override
    public boolean containsValue(Object value) {
        return valueMap.containsKey(value);
    }

    /**
     * Returns an immutable key set.
     *
     * @return the sets the
     */
    @Override
    public Set<K> keySet() {
        return ImmutableSet.of(keyMap.keySet());
    }

    /**
     * Returns an immutable value set.
     *
     * @return the sets the
     */
    @Override
    public Set<V> values() {
        return ImmutableSet.of(valueMap.keySet());
    }

    /**
     * Returns an immutable Set of Immutable entry.
     *
     * @return the sets the
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new ObjIterator<Map.Entry<K, V>>() {
                    private final Iterator<Map.Entry<K, V>> keyValueEntryIter = keyMap.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return keyValueEntryIter.hasNext();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        final Map.Entry<K, V> entry = keyValueEntryIter.next();

                        return new Map.Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public V getValue() {
                                return entry.getValue();
                            }

                            @Override
                            public V setValue(V value) {
                                //    if (N.equals(entry.getValue(), value)) {
                                //        return entry.getValue();
                                //    }
                                //
                                //    //    if (valueMap.containsKey(value)) {
                                //    //        throw new IllegalStateException("Value: " + N.toString(value) + " already existed.");
                                //    //    }
                                //
                                //    valueMap.remove(entry.getValue());
                                //    valueMap.put(value, entry.getKey());
                                //    return entry.setValue(value);

                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return keyMap.size();
            }
        };
    }

    /**
     * Returns the inverse view of this BiMap, which maps each of this bimap's values to its associated key. 
     * The two BiMaps are backed by the same data; any changes to one will appear in the other.
     *
     * @return the bi map
     */
    public BiMap<V, K> inversed() {
        return (inverse == null) ? inverse = new BiMap<>(valueMap, keyMap) : inverse;
    }

    /**
     * Copy.
     *
     * @return the bi map
     */
    public BiMap<K, V> copy() {
        final BiMap<K, V> copy = new BiMap<>(keyMapSupplier, valueMapSupplier);

        copy.putAll(keyMap);

        return copy;
    }

    /**
     * Clear.
     */
    @Override
    public void clear() {
        keyMap.clear();
        valueMap.clear();
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    @Override
    public boolean isEmpty() {
        return keyMap.isEmpty();
    }

    /**
     * Size.
     *
     * @return the int
     */
    @Override
    public int size() {
        return keyMap.size();
    }

    //    public Stream<Map.Entry<K, V>> stream() {
    //        return Stream.of(keyMap.entrySet());
    //    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return keyMap.hashCode();
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BiMap && keyMap.equals(((BiMap<K, V>) obj).keyMap));
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return keyMap.toString();
    }
}