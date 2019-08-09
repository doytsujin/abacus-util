/*
 * Copyright (C) 2019 HaiYang Li
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.landawn.abacus.DirtyMarker;
import com.landawn.abacus.core.DirtyMarkerUtil;
import com.landawn.abacus.util.Fn.Suppliers;
import com.landawn.abacus.util.u.Nullable;
import com.landawn.abacus.util.function.BiFunction;
import com.landawn.abacus.util.function.IntFunction;
import com.landawn.abacus.util.function.Supplier;

// TODO: Auto-generated Javadoc
/**
 * The Class Maps.
 *
 * @author Haiyang Li
 * @since 1.3
 */
public final class Maps {

    /**
     * Instantiates a new maps.
     */
    private Maps() {
        // Utility class.
    }

    /**
     * New map.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <E> the element type
     * @param c the c
     * @param keyMapper the key mapper
     * @return the map
     * @throws E the e
     */
    public static <T, K, E extends Exception> Map<K, T> newMap(Collection<? extends T> c, final Try.Function<? super T, ? extends K, E> keyMapper) throws E {
        N.checkArgNotNull(keyMapper);

        if (N.isNullOrEmpty(c)) {
            return new HashMap<K, T>();
        }

        final Map<K, T> result = new HashMap<>(N.initHashCapacity(c.size()));

        for (T e : c) {
            result.put(keyMapper.apply(e), e);
        }

        return result;
    }

    /**
     * New linked hash map.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <E> the element type
     * @param c the c
     * @param keyMapper the key mapper
     * @return the map
     * @throws E the e
     */
    public static <T, K, E extends Exception> Map<K, T> newLinkedHashMap(Collection<? extends T> c, final Try.Function<? super T, ? extends K, E> keyMapper)
            throws E {
        N.checkArgNotNull(keyMapper);

        if (N.isNullOrEmpty(c)) {
            return new LinkedHashMap<K, T>();
        }

        final Map<K, T> result = new LinkedHashMap<>(N.initHashCapacity(c.size()));

        for (T e : c) {
            result.put(keyMapper.apply(e), e);
        }

        return result;
    }

    /**
     * New map.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @param valueExtractor the value extractor
     * @return the map
     * @throws E the e
     * @throws E2 the e2
     */
    public static <T, K, V, E extends Exception, E2 extends Exception> Map<K, V> newMap(Collection<? extends T> c,
            final Try.Function<? super T, ? extends K, E> keyMapper, final Try.Function<? super T, ? extends V, E2> valueExtractor) throws E, E2 {
        N.checkArgNotNull(keyMapper);
        N.checkArgNotNull(valueExtractor);

        if (N.isNullOrEmpty(c)) {
            return new HashMap<K, V>();
        }

        final Map<K, V> result = new HashMap<>(N.initHashCapacity(c.size()));

        for (T e : c) {
            result.put(keyMapper.apply(e), valueExtractor.apply(e));
        }

        return result;
    }

    /**
     * New map.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the generic type
     * @param <E> the element type
     * @param <E2> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @param valueExtractor the value extractor
     * @param mapSupplier the map supplier
     * @return the m
     * @throws E the e
     * @throws E2 the e2
     */
    public static <T, K, V, M extends Map<K, V>, E extends Exception, E2 extends Exception> M newMap(Collection<? extends T> c,
            final Try.Function<? super T, ? extends K, E> keyMapper, final Try.Function<? super T, ? extends V, E2> valueExtractor,
            final IntFunction<? extends M> mapSupplier) throws E, E2 {
        N.checkArgNotNull(keyMapper);
        N.checkArgNotNull(valueExtractor);

        if (N.isNullOrEmpty(c)) {
            return mapSupplier.apply(0);
        }

        final M result = mapSupplier.apply(c.size());

        for (T e : c) {
            result.put(keyMapper.apply(e), valueExtractor.apply(e));
        }

        return result;
    }

    /**
     * New target map.
     *
     * @param m the m
     * @return the map
     */
    @SuppressWarnings("rawtypes")
    static Map newTargetMap(Map<?, ?> m) {
        return newTargetMap(m, m == null ? 0 : m.size());
    }

    /**
     * New target map.
     *
     * @param m the m
     * @param size the size
     * @return the map
     */
    @SuppressWarnings("rawtypes")
    static Map newTargetMap(Map<?, ?> m, int size) {
        if (m == null) {
            return new HashMap<>();
        }

        Map res = null;

        if (HashMap.class.equals(m.getClass())) {
            res = new HashMap<>(N.initHashCapacity(size));
        } else if (m instanceof SortedMap) {
            res = new TreeMap<>(((SortedMap) m).comparator());
        } else if (m instanceof IdentityHashMap) {
            res = new IdentityHashMap<>(N.initHashCapacity(size));
        } else if (m instanceof LinkedHashMap) {
            res = new LinkedHashMap<>(N.initHashCapacity(size));
        } else if (m instanceof ImmutableMap) {
            res = new LinkedHashMap<>(N.initHashCapacity(size));
        } else {
            try {
                res = N.newInstance(m.getClass());
            } catch (Exception e) {
                res = new LinkedHashMap<>(N.initHashCapacity(size));
            }
        }

        return res;
    }

    /**
     * New ordering map.
     *
     * @param m the m
     * @return the map
     */
    @SuppressWarnings("rawtypes")
    static Map newOrderingMap(Map<?, ?> m) {
        if (m == null) {
            return new HashMap<>();
        }

        Map res = null;

        if (HashMap.class.equals(m.getClass())) {
            res = new HashMap<>(N.initHashCapacity(m.size()));
        } else if (m instanceof SortedMap) {
            res = new LinkedHashMap<>(N.initHashCapacity(m.size()));
        } else if (m instanceof IdentityHashMap) {
            res = new IdentityHashMap<>(N.initHashCapacity(m.size()));
        } else if (m instanceof LinkedHashMap) {
            res = new LinkedHashMap<>(N.initHashCapacity(m.size()));
        } else if (m instanceof ImmutableMap) {
            res = new LinkedHashMap<>(N.initHashCapacity(m.size()));
        } else {
            try {
                res = N.newInstance(m.getClass());
            } catch (Exception e) {
                res = new LinkedHashMap<>(N.initHashCapacity(m.size()));
            }
        }

        return res;
    }

    /**
     * Gets the.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @return the nullable
     */
    public static <K, V> Nullable<V> get(final Map<K, V> map, final Object key) {
        if (N.isNullOrEmpty(map)) {
            return Nullable.empty();
        }

        final V val = map.get(key);

        if (val != null || map.containsKey(key)) {
            return Nullable.of(val);
        } else {
            return Nullable.empty();
        }
    }

    /**
     * Returns a list of values of the keys which exist in the specified <code>Map</code>.
     * If the key dosn't exist in the <code>Map</code>, No value will be added into the returned list. 
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param keys the keys
     * @return the if present for each
     */
    public static <K, V> List<V> getIfPresentForEach(final Map<K, V> map, final Collection<?> keys) {
        if (N.isNullOrEmpty(map) || N.isNullOrEmpty(keys)) {
            return new ArrayList<>(0);
        }

        final List<V> result = new ArrayList<>(keys.size());
        V val = null;

        for (Object key : keys) {
            val = map.get(key);

            if (val != null || map.containsKey(key)) {
                result.add(val);
            }
        }

        return result;
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param defaultValue the default value
     * @return the or default
     */
    public static <K, V> V getOrDefault(final Map<K, V> map, final Object key, final V defaultValue) {
        if (N.isNullOrEmpty(map)) {
            return defaultValue;
        }

        final V val = map.get(key);

        if (val != null || map.containsKey(key)) {
            return val;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * an empty immutable {@code List} if this map contains no mapping for the key.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @return the or empty list
     */
    public static <K, E, V extends List<E>> List<E> getOrEmptyList(final Map<K, V> map, final Object key) {
        if (N.isNullOrEmpty(map)) {
            return N.<E> emptyList();
        }

        final V val = map.get(key);

        if (val != null || map.containsKey(key)) {
            return val;
        } else {
            return N.emptyList();
        }
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * an empty immutable {@code Set} if this map contains no mapping for the key.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @return the or empty set
     */
    public static <K, E, V extends Set<E>> Set<E> getOrEmptySet(final Map<K, V> map, final Object key) {
        if (N.isNullOrEmpty(map)) {
            return N.<E> emptySet();
        }

        final V val = map.get(key);

        if (val != null || map.containsKey(key)) {
            return val;
        } else {
            return N.emptySet();
        }
    }

    /**
     * Gets the or default for each.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param keys the keys
     * @param defaultValue the default value
     * @return the or default for each
     */
    public static <K, V> List<V> getOrDefaultForEach(final Map<K, V> map, final Collection<?> keys, final V defaultValue) {
        if (N.isNullOrEmpty(keys)) {
            return new ArrayList<>(0);
        } else if (N.isNullOrEmpty(map)) {
            return new ArrayList<>(Arrays.asList(Array.repeat(defaultValue, keys.size())));
        }

        final List<V> result = new ArrayList<>(keys.size());
        V val = null;

        for (Object key : keys) {
            val = map.get(key);

            if (val != null || map.containsKey(key)) {
                result.add(val);
            } else {
                result.add(defaultValue);
            }
        }

        return result;
    }

    /**
     * Returns the value associated with the specified {@code key} if it exists in the specified {@code map} contains, or the new put {@code List} if it's absent.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @param key the key
     * @return the and put list if absent
     */
    public static <K, E> List<E> getAndPutListIfAbsent(final Map<K, List<E>> map, final K key) {
        List<E> v = map.get(key);

        if (v == null) {
            v = new ArrayList<>();
            v = map.put(key, v);
        }

        return v;
    }

    /**
     * Returns the value associated with the specified {@code key} if it exists in the specified {@code map} contains, or the new put {@code Set} if it's absent.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @param key the key
     * @return the and put set if absent
     */
    public static <K, E> Set<E> getAndPutSetIfAbsent(final Map<K, Set<E>> map, final K key) {
        Set<E> v = map.get(key);

        if (v == null) {
            v = new HashSet<>();
            v = map.put(key, v);
        }

        return v;
    }

    /**
     * Returns the value associated with the specified {@code key} if it exists in the specified {@code map} contains, or the new put {@code Set} if it's absent.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @param key the key
     * @return the and put linked hash set if absent
     */
    public static <K, E> Set<E> getAndPutLinkedHashSetIfAbsent(final Map<K, Set<E>> map, final K key) {
        Set<E> v = map.get(key);

        if (v == null) {
            v = new LinkedHashSet<>();
            v = map.put(key, v);
        }

        return v;
    }

    /**
     * Returns the value associated with the specified {@code key} if it exists in the specified {@code map} contains, or the new put {@code Map} if it's absent.
     *
     * @param <K> the key type
     * @param <KK> the generic type
     * @param <VV> the generic type
     * @param map the map
     * @param key the key
     * @return the and put map if absent
     */
    public static <K, KK, VV> Map<KK, VV> getAndPutMapIfAbsent(final Map<K, Map<KK, VV>> map, final K key) {
        Map<KK, VV> v = map.get(key);

        if (v == null) {
            v = new HashMap<>();
            v = map.put(key, v);
        }

        return v;
    }

    /**
     * Check if the specified <code>Map</code> contains the specified <code>Entry</code>.
     *
     * @param map the map
     * @param entry the entry
     * @return true, if successful
     */
    public static boolean contains(final Map<?, ?> map, final Map.Entry<?, ?> entry) {
        return contains(map, entry.getKey(), entry.getValue());
    }

    /**
     * Contains.
     *
     * @param map the map
     * @param key the key
     * @param value the value
     * @return true, if successful
     */
    public static boolean contains(final Map<?, ?> map, final Object key, final Object value) {
        if (N.isNullOrEmpty(map)) {
            return false;
        }

        final Object val = map.get(key);

        return val == null ? value == null && map.containsKey(key) : N.equals(val, value);
    }

    /**
     * Intersection.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param map2 the map 2
     * @return the map
     */
    public static <K, V> Map<K, V> intersection(final Map<K, V> map, final Map<? extends K, ? extends V> map2) {
        if (N.isNullOrEmpty(map) || N.isNullOrEmpty(map2)) {
            return new LinkedHashMap<>();
        }

        final Map<K, V> result = map instanceof IdentityHashMap ? new IdentityHashMap<K, V>() : new LinkedHashMap<K, V>();
        Object val = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            val = map2.get(entry.getKey());

            if ((val != null && N.equals(val, entry.getValue())) || (entry.getValue() == null && map.containsKey(entry.getKey()))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Difference.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param map2 the map 2
     * @return the map
     */
    public static <K, V> Map<K, Pair<V, Nullable<V>>> difference(final Map<K, V> map, final Map<K, V> map2) {
        if (N.isNullOrEmpty(map)) {
            return new LinkedHashMap<>();
        }

        final Map<K, Pair<V, Nullable<V>>> result = map instanceof IdentityHashMap ? new IdentityHashMap<K, Pair<V, Nullable<V>>>()
                : new LinkedHashMap<K, Pair<V, Nullable<V>>>();

        if (N.isNullOrEmpty(map2)) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                result.put(entry.getKey(), Pair.of(entry.getValue(), Nullable.<V> empty()));
            }
        } else {
            V val = null;

            for (Map.Entry<K, V> entry : map.entrySet()) {
                val = map2.get(entry.getKey());

                if (val == null && map2.containsKey(entry.getKey()) == false) {
                    result.put(entry.getKey(), Pair.of(entry.getValue(), Nullable.<V> empty()));
                } else if (N.equals(val, entry.getValue()) == false) {
                    result.put(entry.getKey(), Pair.of(entry.getValue(), Nullable.of(val)));
                }
            }
        }

        return result;
    }

    /**
     * Symmetric difference.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param map2 the map 2
     * @return the map
     */
    public static <K, V> Map<K, Pair<Nullable<V>, Nullable<V>>> symmetricDifference(final Map<K, V> map, final Map<K, V> map2) {
        final boolean isIdentityHashMap = (N.notNullOrEmpty(map) && map instanceof IdentityHashMap)
                || (N.notNullOrEmpty(map2) && map2 instanceof IdentityHashMap);

        final Map<K, Pair<Nullable<V>, Nullable<V>>> result = isIdentityHashMap ? new IdentityHashMap<K, Pair<Nullable<V>, Nullable<V>>>()
                : new LinkedHashMap<K, Pair<Nullable<V>, Nullable<V>>>();

        if (N.notNullOrEmpty(map)) {
            if (N.isNullOrEmpty(map2)) {
                for (Map.Entry<K, V> entry : map.entrySet()) {
                    result.put(entry.getKey(), Pair.of(Nullable.of(entry.getValue()), Nullable.<V> empty()));
                }
            } else {
                K key = null;
                V val2 = null;

                for (Map.Entry<K, V> entry : map.entrySet()) {
                    key = entry.getKey();
                    val2 = map2.get(key);

                    if (val2 == null && map2.containsKey(key) == false) {
                        result.put(key, Pair.of(Nullable.of(entry.getValue()), Nullable.<V> empty()));
                    } else if (N.equals(val2, entry.getValue()) == false) {
                        result.put(key, Pair.of(Nullable.of(entry.getValue()), Nullable.of(val2)));
                    }
                }
            }
        }

        if (N.notNullOrEmpty(map2)) {
            if (N.isNullOrEmpty(map)) {
                for (Map.Entry<K, V> entry : map2.entrySet()) {
                    result.put(entry.getKey(), Pair.of(Nullable.<V> empty(), Nullable.of(entry.getValue())));
                }
            } else {
                for (Map.Entry<K, V> entry : map2.entrySet()) {
                    if (map.containsKey(entry.getKey()) == false) {
                        result.put(entry.getKey(), Pair.of(Nullable.<V> empty(), Nullable.of(entry.getValue())));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Put if absent.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param value the value
     * @return the v
     */
    public static <K, V> V putIfAbsent(final Map<K, V> map, K key, final V value) {
        V v = map.get(key);

        if (v == null) {
            v = map.put(key, value);
        }

        return v;
    }

    /**
     * Put if absent.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param supplier the supplier
     * @return the v
     */
    public static <K, V> V putIfAbsent(final Map<K, V> map, K key, final Supplier<V> supplier) {
        V v = map.get(key);

        if (v == null) {
            v = map.put(key, supplier.get());
        }

        return v;
    }

    /**
     * Removes the specified entry.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param entry the entry
     * @return true, if successful
     */
    public static <K, V> boolean remove(final Map<K, V> map, Map.Entry<?, ?> entry) {
        return remove(map, entry.getKey(), entry.getValue());
    }

    /**
     * Removes the.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param value the value
     * @return true, if successful
     */
    public static <K, V> boolean remove(final Map<K, V> map, final Object key, final Object value) {
        if (N.isNullOrEmpty(map)) {
            return false;
        }

        final Object curValue = map.get(key);

        if (!N.equals(curValue, value) || (curValue == null && !map.containsKey(key))) {
            return false;
        }

        map.remove(key);
        return true;
    }

    /**
     * Removes the keys.
     *
     * @param map the map
     * @param keysToRemove the keys to remove
     * @return <code>true</code> if any key/value was removed, otherwise <code>false</code>.
     */
    public static boolean removeKeys(final Map<?, ?> map, final Collection<?> keysToRemove) {
        if (N.isNullOrEmpty(map) || N.isNullOrEmpty(keysToRemove)) {
            return false;
        }

        final int originalSize = map.size();

        for (Object key : keysToRemove) {
            map.remove(key);
        }

        return map.size() < originalSize;
    }

    /**
     * The the entries from the specified <code>Map</code>.
     *
     * @param map the map
     * @param entriesToRemove the entries to remove
     * @return <code>true</code> if any key/value was removed, otherwise <code>false</code>.
     */
    public static boolean removeEntries(final Map<?, ?> map, final Map<?, ?> entriesToRemove) {
        if (N.isNullOrEmpty(map) || N.isNullOrEmpty(entriesToRemove)) {
            return false;
        }

        final int originalSize = map.size();

        for (Map.Entry<?, ?> entry : entriesToRemove.entrySet()) {
            if (N.equals(map.get(entry.getKey()), entry.getValue())) {
                map.remove(entry.getKey());
            }
        }

        return map.size() < originalSize;
    }

    /**
     * Removes entries from the specified {@code map} by the the specified {@code filter}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param filter the filter
     * @return {@code true} if there are one or more than one entries removed from the specified map.
     * @throws E the e
     */
    public static <K, V, E extends Exception> boolean removeIf(final Map<K, V> map, final Try.Predicate<? super Map.Entry<K, V>, E> filter) throws E {
        List<K> keysToRemove = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (filter.test(entry)) {
                if (keysToRemove == null) {
                    keysToRemove = new ArrayList<>(7);
                }

                keysToRemove.add(entry.getKey());
            }
        }

        if (N.notNullOrEmpty(keysToRemove)) {
            for (K key : keysToRemove) {
                map.remove(key);
            }

            return true;
        }

        return false;
    }

    /**
     * Removes entries from the specified {@code map} by the the specified {@code filter}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param filter the filter
     * @return {@code true} if there are one or more than one entries removed from the specified map.
     * @throws E the e
     */
    public static <K, V, E extends Exception> boolean removeIfKey(final Map<K, V> map, final Try.Predicate<? super K, E> filter) throws E {
        List<K> keysToRemove = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (filter.test(entry.getKey())) {
                if (keysToRemove == null) {
                    keysToRemove = new ArrayList<>(7);
                }

                keysToRemove.add(entry.getKey());
            }
        }

        if (N.notNullOrEmpty(keysToRemove)) {
            for (K key : keysToRemove) {
                map.remove(key);
            }

            return true;
        }

        return false;
    }

    /**
     * Removes entries from the specified {@code map} by the the specified {@code filter}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param filter the filter
     * @return {@code true} if there are one or more than one entries removed from the specified map.
     * @throws E the e
     */
    public static <K, V, E extends Exception> boolean removeIfValue(final Map<K, V> map, final Try.Predicate<? super V, E> filter) throws E {
        List<K> keysToRemove = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (filter.test(entry.getValue())) {
                if (keysToRemove == null) {
                    keysToRemove = new ArrayList<>(7);
                }

                keysToRemove.add(entry.getKey());
            }
        }

        if (N.notNullOrEmpty(keysToRemove)) {
            for (K key : keysToRemove) {
                map.remove(key);
            }

            return true;
        }

        return false;
    }

    /**
     * Replace.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param oldValue the old value
     * @param newValue the new value
     * @return true, if successful
     */
    public static <K, V> boolean replace(final Map<K, V> map, final K key, final V oldValue, final V newValue) {
        if (N.isNullOrEmpty(map)) {
            return false;
        }

        final Object curValue = map.get(key);

        if (!N.equals(curValue, oldValue) || (curValue == null && !map.containsKey(key))) {
            return false;
        }

        map.put(key, newValue);
        return true;
    }

    /**
     * Replace.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param key the key
     * @param newValue the new value
     * @return the v
     */
    public static <K, V> V replace(final Map<K, V> map, final K key, final V newValue) {
        if (N.isNullOrEmpty(map)) {
            return null;
        }

        V curValue = null;

        if (((curValue = map.get(key)) != null) || map.containsKey(key)) {
            curValue = map.put(key, newValue);
        }

        return curValue;
    }

    /**
     * Replace all.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param function the function
     * @throws E the e
     */
    public static <K, V, E extends Exception> void replaceAll(final Map<K, V> map, final Try.BiFunction<? super K, ? super V, ? extends V, E> function)
            throws E {
        N.checkArgNotNull(function);

        if (N.isNullOrEmpty(map)) {
            return;
        }

        K k = null;
        V v = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                entry.setValue(v);
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    /**
     * For each.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param action the action
     * @throws E the e
     */
    public static <K, V, E extends Exception> void forEach(final Map<K, V> map, final Try.BiConsumer<? super K, ? super V, E> action) throws E {
        N.checkArgNotNull(action);

        if (N.isNullOrEmpty(map)) {
            return;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Filter.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param predicate the predicate
     * @return the map
     * @throws E the e
     */
    public static <K, V, E extends Exception> Map<K, V> filter(final Map<K, V> map, final Try.BiPredicate<? super K, ? super V, E> predicate) throws E {
        if (map == null) {
            return new HashMap<K, V>();
        }

        final Map<K, V> result = newTargetMap(map, 0);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Filter by key.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param predicate the predicate
     * @return the map
     * @throws E the e
     */
    public static <K, V, E extends Exception> Map<K, V> filterByKey(final Map<K, V> map, final Try.Predicate<? super K, E> predicate) throws E {
        if (map == null) {
            return new HashMap<K, V>();
        }

        final Map<K, V> result = newTargetMap(map, 0);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Filter by value.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param predicate the predicate
     * @return the map
     * @throws E the e
     */
    public static <K, V, E extends Exception> Map<K, V> filterByValue(final Map<K, V> map, final Try.Predicate<? super V, E> predicate) throws E {
        if (map == null) {
            return new HashMap<K, V>();
        }

        final Map<K, V> result = newTargetMap(map, 0);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Invert.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the map
     * @see Multimap#invertFrom(Map, com.landawn.abacus.util.function.Supplier)
     * @see ListMultimap#invertFrom(Map)
     * @see ListMultimap#invertFrom(Map)
     */
    public static <K, V> Map<V, K> invert(final Map<K, V> map) {
        if (map == null) {
            return new HashMap<V, K>();
        }

        final Map<V, K> result = newOrderingMap(map);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }

        return result;
    }

    /**
     * Invert.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param mergeOp the merge op
     * @return the map
     * @throws E the e
     */
    public static <K, V, E extends Exception> Map<V, K> invert(final Map<K, V> map, Try.BinaryOperator<K, E> mergeOp) throws E {
        N.checkArgNotNull(mergeOp, "mergeOp");

        if (map == null) {
            return new HashMap<V, K>();
        }

        final Map<V, K> result = newOrderingMap(map);
        K oldVal = null;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            oldVal = result.get(entry.getValue());

            if (oldVal != null || result.containsKey(entry.getValue())) {
                result.put(entry.getValue(), mergeOp.apply(oldVal, entry.getKey()));
            } else {
                result.put(entry.getValue(), entry.getKey());
            }
        }

        return result;
    }

    /**
     * Flat invert.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the map
     * @see Multimap#flatInvertFrom(Map, com.landawn.abacus.util.function.Supplier)
     * @see ListMultimap#flatInvertFrom(Map)
     * @see SetMultimap#flatInvertFrom(Map)
     */
    public static <K, V> Map<V, List<K>> flatInvert(final Map<K, ? extends Collection<? extends V>> map) {
        if (map == null) {
            return new HashMap<V, List<K>>();
        }

        final Map<V, List<K>> result = newOrderingMap(map);

        for (Map.Entry<K, ? extends Collection<? extends V>> entry : map.entrySet()) {
            final Collection<? extends V> c = entry.getValue();

            if (N.notNullOrEmpty(c)) {
                for (V v : c) {
                    List<K> list = result.get(v);

                    if (list == null) {
                        list = new ArrayList<>();
                        result.put(v, list);
                    }

                    list.add(entry.getKey());
                }
            }
        }

        return result;
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param m the m
     * @return the t
     */
    public static <T> T map2Entity(final Class<T> targetClass, final Map<String, Object> m) {
        return map2Entity(targetClass, m, false, true);
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param m the m
     * @param ignoreNullProperty the ignore null property
     * @param ignoreUnknownProperty the ignore unknown property
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public static <T> T map2Entity(final Class<T> targetClass, final Map<String, Object> m, final boolean ignoreNullProperty,
            final boolean ignoreUnknownProperty) {
        checkEntityClass(targetClass);

        final T entity = N.newInstance(targetClass);

        String propName = null;
        Object propValue = null;
        Method propSetMethod = null;
        Class<?> paramClass = null;

        for (Map.Entry<String, Object> entry : m.entrySet()) {
            propName = entry.getKey();
            propValue = entry.getValue();

            if (ignoreNullProperty && (propValue == null)) {
                continue;
            }

            propSetMethod = ClassUtil.getPropSetMethod(targetClass, propName);

            if (propSetMethod == null) {
                ClassUtil.setPropValue(entity, propName, propValue, ignoreUnknownProperty);
            } else {
                paramClass = propSetMethod.getParameterTypes()[0];

                if (propValue != null && N.typeOf(propValue.getClass()).isMap() && ClassUtil.isEntity(paramClass)) {
                    ClassUtil.setPropValue(entity, propSetMethod,
                            map2Entity(paramClass, (Map<String, Object>) propValue, ignoreNullProperty, ignoreUnknownProperty));
                } else {
                    ClassUtil.setPropValue(entity, propSetMethod, propValue);
                }
            }
        }

        return entity;
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param m the m
     * @param selectPropNames the select prop names
     * @return the t
     */
    public static <T> T map2Entity(final Class<T> targetClass, final Map<String, Object> m, final Collection<String> selectPropNames) {
        checkEntityClass(targetClass);

        final T entity = N.newInstance(targetClass);

        Object propValue = null;
        Method propSetMethod = null;
        Class<?> paramClass = null;

        for (String propName : selectPropNames) {
            propValue = m.get(propName);

            if (propValue == null && m.containsKey(propName) == false) {
                throw new IllegalArgumentException("Property name: " + propName + " is not found in map with key set: " + m.keySet());
            }

            propSetMethod = ClassUtil.getPropSetMethod(targetClass, propName);

            if (propSetMethod == null) {
                ClassUtil.setPropValue(entity, propName, propValue, false);
            } else {
                paramClass = propSetMethod.getParameterTypes()[0];

                if (propValue != null && N.typeOf(propValue.getClass()).isMap() && ClassUtil.isEntity(paramClass)) {
                    ClassUtil.setPropValue(entity, propSetMethod, map2Entity(paramClass, (Map<String, Object>) propValue));
                } else {
                    ClassUtil.setPropValue(entity, propSetMethod, propValue);
                }
            }
        }

        return entity;
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param mList the m list
     * @return the list
     */
    public static <T> List<T> map2Entity(final Class<T> targetClass, final Collection<Map<String, Object>> mList) {
        return map2Entity(targetClass, mList, false, true);
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param mList the m list
     * @param igoreNullProperty the igore null property
     * @param ignoreUnknownProperty the ignore unknown property
     * @return the list
     */
    public static <T> List<T> map2Entity(final Class<T> targetClass, final Collection<Map<String, Object>> mList, final boolean igoreNullProperty,
            final boolean ignoreUnknownProperty) {
        checkEntityClass(targetClass);

        final List<T> entityList = new ArrayList<>(mList.size());

        for (Map<String, Object> m : mList) {
            entityList.add(map2Entity(targetClass, m, igoreNullProperty, ignoreUnknownProperty));
        }

        return entityList;
    }

    /**
     * Map 2 entity.
     *
     * @param <T> the generic type
     * @param targetClass the target class
     * @param mList the m list
     * @param selectPropNames the select prop names
     * @return the list
     */
    public static <T> List<T> map2Entity(final Class<T> targetClass, final Collection<Map<String, Object>> mList, final Collection<String> selectPropNames) {
        checkEntityClass(targetClass);

        final List<T> entityList = new ArrayList<>(mList.size());

        for (Map<String, Object> m : mList) {
            entityList.add(map2Entity(targetClass, m, selectPropNames));
        }

        return entityList;
    }

    /**
     * Check entity class.
     *
     * @param <T> the generic type
     * @param cls the cls
     */
    private static <T> void checkEntityClass(final Class<T> cls) {
        if (!ClassUtil.isEntity(cls)) {
            throw new IllegalArgumentException("No property getter/setter method is found in the specified class: " + ClassUtil.getCanonicalClassName(cls));
        }
    }

    /**
     * Entity 2 map.
     *
     * @param entity the entity
     * @return the map
     */
    public static Map<String, Object> entity2Map(final Object entity) {
        return entity2Map(entity, false);
    }

    /**
     * Entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the map
     */
    public static Map<String, Object> entity2Map(final Object entity, final boolean ignoreNullProperty) {
        return entity2Map(entity, ignoreNullProperty, null);
    }

    /**
     * Entity 2 map.
     *
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> entity2Map(final Object entity, final Collection<String> ignoredPropNames) {
        return entity2Map(entity, false, ignoredPropNames);
    }

    /**
     * Entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> entity2Map(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames) {
        return entity2Map(entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the map
     */
    public static Map<String, Object> entity2Map(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames,
            final NamingPolicy keyNamingPolicy) {
        final int initCapacity = (entity instanceof DirtyMarker ? DirtyMarkerUtil.signedPropNames((DirtyMarker) entity).size()
                : N.initHashCapacity(ClassUtil.getPropGetMethodList(entity.getClass()).size()));
        final Map<String, Object> resultMap = new LinkedHashMap<>(initCapacity);

        entity2Map(resultMap, entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy);

        return resultMap;
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param entity the entity
     * @param mapSupplier the map supplier
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final Object entity, final Supplier<? extends M> mapSupplier) {
        return entity2Map(mapSupplier.get(), entity);
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final M resultMap, final Object entity) {
        return entity2Map(resultMap, entity, false);
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty) {
        return entity2Map(resultMap, entity, ignoreNullProperty, null);
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final M resultMap, final Object entity, final Collection<String> ignoredPropNames) {
        return entity2Map(resultMap, entity, false, ignoredPropNames);
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return entity2Map(resultMap, entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, NamingPolicy keyNamingPolicy) {
        keyNamingPolicy = keyNamingPolicy == null ? NamingPolicy.LOWER_CAMEL_CASE : keyNamingPolicy;

        final boolean hasIgnoredPropNames = N.notNullOrEmpty(ignoredPropNames);
        Set<String> signedPropNames = null;

        if (entity instanceof DirtyMarker) {
            final Class<?> entityClass = entity.getClass();
            signedPropNames = DirtyMarkerUtil.signedPropNames((DirtyMarker) entity);

            if (signedPropNames.size() == 0) {
                // logger.warn("no property is signed in the specified source entity: "
                // + toString(entity));

                return resultMap;
            } else {
                final Set<String> tmp = new HashSet<>(N.initHashCapacity(signedPropNames.size()));

                for (String propName : signedPropNames) {
                    tmp.add(ClassUtil.getPropNameByMethod(ClassUtil.getPropGetMethod(entityClass, propName)));
                }

                signedPropNames = tmp;
            }
        }

        final Map<String, Method> getterMethodList = ClassUtil.checkPropGetMethodList(entity.getClass());
        String propName = null;
        Object propValue = null;

        try {
            switch (keyNamingPolicy) {
                case LOWER_CAMEL_CASE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        resultMap.put(propName, propValue);
                    }

                    break;
                }

                case LOWER_CASE_WITH_UNDERSCORE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        resultMap.put(ClassUtil.toLowerCaseWithUnderscore(propName), propValue);
                    }

                    break;
                }

                case UPPER_CASE_WITH_UNDERSCORE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        resultMap.put(ClassUtil.toUpperCaseWithUnderscore(propName), propValue);
                    }

                    break;
                }

                default:
                    throw new IllegalArgumentException("Unsupported NamingPolicy: " + keyNamingPolicy);
            }
        } catch (Exception e) {
            throw N.toRuntimeException(e);
        }

        return resultMap;
    }

    /**
     * Entity 2 map.
     *
     * @param entityList the entity list
     * @return the list
     */
    public static List<Map<String, Object>> entity2Map(final Collection<?> entityList) {
        return entity2Map(entityList, false);
    }

    /**
     * Entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @return the list
     */
    public static List<Map<String, Object>> entity2Map(final Collection<?> entityList, final boolean ignoreNullProperty) {
        return entity2Map(entityList, ignoreNullProperty, null);
    }

    /**
     * Entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> entity2Map(final Collection<?> entityList, final Collection<String> ignoredPropNames) {
        return entity2Map(entityList, false, ignoredPropNames);
    }

    /**
     * Entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> entity2Map(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return entity2Map(entityList, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the list
     */
    public static List<Map<String, Object>> entity2Map(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, final NamingPolicy keyNamingPolicy) {
        final List<Map<String, Object>> resultList = new ArrayList<>(entityList.size());

        for (Object entity : entityList) {
            resultList.add(entity2Map(entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy));
        }

        return resultList;
    }

    /**
     * Deep entity 2 map.
     *
     * @param entity the entity
     * @return the map
     */
    public static Map<String, Object> deepEntity2Map(final Object entity) {
        return deepEntity2Map(entity, false);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the map
     */
    public static Map<String, Object> deepEntity2Map(final Object entity, final boolean ignoreNullProperty) {
        return deepEntity2Map(entity, ignoreNullProperty, null);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> deepEntity2Map(final Object entity, final Collection<String> ignoredPropNames) {
        return deepEntity2Map(entity, false, ignoredPropNames);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> deepEntity2Map(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames) {
        return deepEntity2Map(entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the map
     */
    public static Map<String, Object> deepEntity2Map(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames,
            final NamingPolicy keyNamingPolicy) {
        final int initCapacity = entity instanceof DirtyMarker ? DirtyMarkerUtil.signedPropNames((DirtyMarker) entity).size()
                : N.initHashCapacity(ClassUtil.getPropGetMethodList(entity.getClass()).size());
        final Map<String, Object> resultMap = new LinkedHashMap<>(initCapacity);

        deepEntity2Map(resultMap, entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy);

        return resultMap;
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param entity the entity
     * @param mapSupplier the map supplier
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final Object entity, final Supplier<? extends M> mapSupplier) {
        return deepEntity2Map(mapSupplier.get(), entity);
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final M resultMap, final Object entity) {
        return deepEntity2Map(resultMap, entity, false);
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty) {
        return deepEntity2Map(resultMap, entity, ignoreNullProperty, null);
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final M resultMap, final Object entity, final Collection<String> ignoredPropNames) {
        return deepEntity2Map(resultMap, entity, false, ignoredPropNames);
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return deepEntity2Map(resultMap, entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Deep entity 2 map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M deepEntity2Map(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, NamingPolicy keyNamingPolicy) {
        keyNamingPolicy = keyNamingPolicy == null ? NamingPolicy.LOWER_CAMEL_CASE : keyNamingPolicy;
        final boolean hasIgnoredPropNames = N.notNullOrEmpty(ignoredPropNames);

        Set<String> signedPropNames = null;

        if (entity instanceof DirtyMarker) {
            final Class<?> entityClass = entity.getClass();
            signedPropNames = DirtyMarkerUtil.signedPropNames((DirtyMarker) entity);

            if (signedPropNames.size() == 0) {
                // logger.warn("no property is signed in the specified source entity: "
                // + toString(entity));

                return resultMap;
            } else {
                final Set<String> tmp = new HashSet<>(N.initHashCapacity(signedPropNames.size()));

                for (String propName : signedPropNames) {
                    tmp.add(ClassUtil.getPropNameByMethod(ClassUtil.getPropGetMethod(entityClass, propName)));
                }

                signedPropNames = tmp;
            }
        }

        final Map<String, Method> getterMethodList = ClassUtil.checkPropGetMethodList(entity.getClass());
        String propName = null;
        Object propValue = null;

        try {
            switch (keyNamingPolicy) {
                case LOWER_CAMEL_CASE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                            resultMap.put(propName, propValue);
                        } else {
                            resultMap.put(propName, deepEntity2Map(propValue, ignoreNullProperty, null, keyNamingPolicy));
                        }
                    }

                    break;
                }

                case LOWER_CASE_WITH_UNDERSCORE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                            resultMap.put(ClassUtil.toLowerCaseWithUnderscore(propName), propValue);
                        } else {
                            resultMap.put(ClassUtil.toLowerCaseWithUnderscore(propName), deepEntity2Map(propValue, ignoreNullProperty, null, keyNamingPolicy));
                        }
                    }

                    break;
                }

                case UPPER_CASE_WITH_UNDERSCORE: {
                    for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                        propName = entry.getKey();

                        if (signedPropNames != null && signedPropNames.contains(propName) == false) {
                            continue;
                        }

                        if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                            continue;
                        }

                        propValue = entry.getValue().invoke(entity);

                        if (ignoreNullProperty && (propValue == null)) {
                            continue;
                        }

                        if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                            resultMap.put(ClassUtil.toUpperCaseWithUnderscore(propName), propValue);
                        } else {
                            resultMap.put(ClassUtil.toUpperCaseWithUnderscore(propName), deepEntity2Map(propValue, ignoreNullProperty, null, keyNamingPolicy));
                        }
                    }

                    break;
                }

                default:
                    throw new IllegalArgumentException("Unsupported NamingPolicy: " + keyNamingPolicy);
            }
        } catch (Exception e) {
            throw N.toRuntimeException(e);
        }

        return resultMap;
    }

    /**
     * Deep entity 2 map.
     *
     * @param entityList the entity list
     * @return the list
     */
    public static List<Map<String, Object>> deepEntity2Map(final Collection<?> entityList) {
        return deepEntity2Map(entityList, false);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @return the list
     */
    public static List<Map<String, Object>> deepEntity2Map(final Collection<?> entityList, final boolean ignoreNullProperty) {
        return deepEntity2Map(entityList, ignoreNullProperty, null);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> deepEntity2Map(final Collection<?> entityList, final Collection<String> ignoredPropNames) {
        final boolean ignoreNullProperty = N.isNullOrEmpty(entityList) ? true
                : (entityList instanceof ArrayList ? ((ArrayList<?>) entityList).get(0) : entityList.iterator().next()) instanceof DirtyMarker == false;

        return deepEntity2Map(entityList, ignoreNullProperty, ignoredPropNames);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> deepEntity2Map(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return deepEntity2Map(entityList, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Deep entity 2 map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the list
     */
    public static List<Map<String, Object>> deepEntity2Map(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, final NamingPolicy keyNamingPolicy) {
        final List<Map<String, Object>> resultList = new ArrayList<>(entityList.size());

        for (Object entity : entityList) {
            resultList.add(deepEntity2Map(entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy));
        }

        return resultList;
    }

    /**
     * Entity 2 flat map.
     *
     * @param entity the entity
     * @return the map
     */
    public static Map<String, Object> entity2FlatMap(final Object entity) {
        return entity2FlatMap(entity, false);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the map
     */
    public static Map<String, Object> entity2FlatMap(final Object entity, final boolean ignoreNullProperty) {
        return entity2FlatMap(entity, ignoreNullProperty, null);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> entity2FlatMap(final Object entity, final Collection<String> ignoredPropNames) {
        return entity2FlatMap(entity, false, ignoredPropNames);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the map
     */
    public static Map<String, Object> entity2FlatMap(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames) {
        return entity2FlatMap(entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the map
     */
    public static Map<String, Object> entity2FlatMap(final Object entity, final boolean ignoreNullProperty, final Collection<String> ignoredPropNames,
            final NamingPolicy keyNamingPolicy) {
        final int initCapacity = entity instanceof DirtyMarker ? DirtyMarkerUtil.signedPropNames((DirtyMarker) entity).size()
                : N.initHashCapacity(ClassUtil.getPropGetMethodList(entity.getClass()).size());
        final Map<String, Object> resultMap = new LinkedHashMap<>(initCapacity);

        entity2FlatMap(resultMap, entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy);

        return resultMap;
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param entity the entity
     * @param mapSupplier the map supplier
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final Object entity, final Supplier<? extends M> mapSupplier) {
        return entity2FlatMap(mapSupplier.get(), entity);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final M resultMap, final Object entity) {
        return entity2FlatMap(resultMap, entity, false);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final M resultMap, final Object entity, final boolean ignoreNullProperty) {
        return entity2FlatMap(resultMap, entity, ignoreNullProperty, null);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final M resultMap, final Object entity, final Collection<String> ignoredPropNames) {
        return entity2FlatMap(resultMap, entity, false, ignoredPropNames);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return entity2FlatMap(resultMap, entity, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <M> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the input <code>resultMap</code>
     */
    public static <M extends Map<String, Object>> M entity2FlatMap(final M resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, final NamingPolicy keyNamingPolicy) {
        return entity2FlatMap(resultMap, entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy, null);
    }

    /**
     * Entity 2 flat map.
     *
     * @param <T> the generic type
     * @param resultMap the result map
     * @param entity the entity
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @param parentPropName the parent prop name
     * @return the input <code>resultMap</code>
     */
    static <T extends Map<String, Object>> T entity2FlatMap(final T resultMap, final Object entity, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, final NamingPolicy keyNamingPolicy, final String parentPropName) {
        final boolean hasIgnoredPropNames = N.notNullOrEmpty(ignoredPropNames);
        final boolean isNullParentPropName = (parentPropName == null);

        if (entity instanceof DirtyMarker) {
            final Class<?> entityClass = entity.getClass();
            final Set<String> signedPropNames = DirtyMarkerUtil.signedPropNames((DirtyMarker) entity);

            if (signedPropNames.size() == 0) {
                // logger.warn("no property is signed in the specified source entity: "
                // + toString(entity));
            } else {
                Method propGetMethod = null;
                Object propValue = null;

                try {
                    switch (keyNamingPolicy) {
                        case LOWER_CAMEL_CASE: {
                            for (String propName : signedPropNames) {
                                propGetMethod = ClassUtil.getPropGetMethod(entityClass, propName);
                                propName = ClassUtil.getPropNameByMethod(propGetMethod);

                                if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                    continue;
                                }

                                propValue = propGetMethod.invoke(entity);

                                if (ignoreNullProperty && (propValue == null)) {
                                    continue;
                                }

                                if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                    if (isNullParentPropName) {
                                        resultMap.put(propName, propValue);
                                    } else {
                                        resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                    }
                                } else {
                                    if (isNullParentPropName) {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                    } else {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                    }
                                }
                            }

                            break;
                        }

                        case LOWER_CASE_WITH_UNDERSCORE: {
                            for (String propName : signedPropNames) {
                                propGetMethod = ClassUtil.getPropGetMethod(entityClass, propName);
                                propName = ClassUtil.getPropNameByMethod(propGetMethod);

                                if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                    continue;
                                }

                                propName = ClassUtil.toLowerCaseWithUnderscore(propName);
                                propValue = propGetMethod.invoke(entity);

                                if (ignoreNullProperty && (propValue == null)) {
                                    continue;
                                }

                                if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                    if (isNullParentPropName) {
                                        resultMap.put(propName, propValue);
                                    } else {
                                        resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                    }
                                } else {
                                    if (isNullParentPropName) {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                    } else {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                    }
                                }
                            }

                            break;
                        }

                        case UPPER_CASE_WITH_UNDERSCORE: {
                            for (String propName : signedPropNames) {
                                propGetMethod = ClassUtil.getPropGetMethod(entityClass, propName);
                                propName = ClassUtil.getPropNameByMethod(propGetMethod);

                                if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                    continue;
                                }

                                propName = ClassUtil.toUpperCaseWithUnderscore(propName);
                                propValue = propGetMethod.invoke(entity);

                                if (ignoreNullProperty && (propValue == null)) {
                                    continue;
                                }

                                if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                    if (isNullParentPropName) {
                                        resultMap.put(propName, propValue);
                                    } else {
                                        resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                    }
                                } else {
                                    if (isNullParentPropName) {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                    } else {
                                        entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                    }
                                }
                            }

                            break;
                        }

                        default:
                            throw new IllegalArgumentException("Unsupported NamingPolicy: " + keyNamingPolicy);
                    }

                } catch (Exception e) {
                    throw N.toRuntimeException(e);
                }
            }
        } else {
            final Map<String, Method> getterMethodList = ClassUtil.checkPropGetMethodList(entity.getClass());
            String propName = null;
            Object propValue = null;

            try {
                switch (keyNamingPolicy) {
                    case LOWER_CAMEL_CASE: {
                        for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                            propName = entry.getKey();

                            if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                continue;
                            }

                            propValue = entry.getValue().invoke(entity);

                            if (ignoreNullProperty && (propValue == null)) {
                                continue;
                            }

                            if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                if (isNullParentPropName) {
                                    resultMap.put(propName, propValue);
                                } else {
                                    resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                }
                            } else {
                                if (isNullParentPropName) {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                } else {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                }
                            }
                        }

                        break;
                    }

                    case LOWER_CASE_WITH_UNDERSCORE: {
                        for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                            propName = entry.getKey();

                            if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                continue;
                            }

                            propName = ClassUtil.toLowerCaseWithUnderscore(propName);
                            propValue = entry.getValue().invoke(entity);

                            if (ignoreNullProperty && (propValue == null)) {
                                continue;
                            }

                            if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                if (isNullParentPropName) {
                                    resultMap.put(propName, propValue);
                                } else {
                                    resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                }
                            } else {
                                if (isNullParentPropName) {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                } else {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                }
                            }
                        }

                        break;
                    }

                    case UPPER_CASE_WITH_UNDERSCORE: {
                        for (Map.Entry<String, Method> entry : getterMethodList.entrySet()) {
                            propName = entry.getKey();

                            if (hasIgnoredPropNames && ignoredPropNames.contains(propName)) {
                                continue;
                            }

                            propName = ClassUtil.toUpperCaseWithUnderscore(propName);
                            propValue = entry.getValue().invoke(entity);

                            if (ignoreNullProperty && (propValue == null)) {
                                continue;
                            }

                            if ((propValue == null) || !ClassUtil.isEntity(propValue.getClass())) {
                                if (isNullParentPropName) {
                                    resultMap.put(propName, propValue);
                                } else {
                                    resultMap.put(parentPropName + WD.PERIOD + propName, propValue);
                                }
                            } else {
                                if (isNullParentPropName) {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, propName);
                                } else {
                                    entity2FlatMap(resultMap, propValue, ignoreNullProperty, null, keyNamingPolicy, parentPropName + WD.PERIOD + propName);
                                }
                            }
                        }

                        break;
                    }

                    default:
                        throw new IllegalArgumentException("Unsupported NamingPolicy: " + keyNamingPolicy);
                }
            } catch (Exception e) {
                throw N.toRuntimeException(e);
            }
        }

        return resultMap;
    }

    /**
     * Entity 2 flat map.
     *
     * @param entityList the entity list
     * @return the list
     */
    public static List<Map<String, Object>> entity2FlatMap(final Collection<?> entityList) {
        return entity2FlatMap(entityList, false);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @return the list
     */
    public static List<Map<String, Object>> entity2FlatMap(final Collection<?> entityList, final boolean ignoreNullProperty) {
        return entity2FlatMap(entityList, ignoreNullProperty, null);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entityList the entity list
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> entity2FlatMap(final Collection<?> entityList, final Collection<String> ignoredPropNames) {
        return entity2FlatMap(entityList, false, ignoredPropNames);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @return the list
     */
    public static List<Map<String, Object>> entity2FlatMap(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames) {
        return entity2FlatMap(entityList, ignoreNullProperty, ignoredPropNames, NamingPolicy.LOWER_CAMEL_CASE);
    }

    /**
     * Entity 2 flat map.
     *
     * @param entityList the entity list
     * @param ignoreNullProperty the ignore null property
     * @param ignoredPropNames the ignored prop names
     * @param keyNamingPolicy the key naming policy
     * @return the list
     */
    public static List<Map<String, Object>> entity2FlatMap(final Collection<?> entityList, final boolean ignoreNullProperty,
            final Collection<String> ignoredPropNames, final NamingPolicy keyNamingPolicy) {
        final List<Map<String, Object>> resultList = new ArrayList<>(entityList.size());

        for (Object entity : entityList) {
            resultList.add(entity2FlatMap(entity, ignoreNullProperty, ignoredPropNames, keyNamingPolicy));
        }

        return resultList;
    }

    /**
     * Flatten.
     *
     * @param map the map
     * @return the map
     */
    public static Map<String, Object> flatten(Map<String, Object> map) {
        return flatten(map, Suppliers.<String, Object> ofMap());
    }

    /**
     * Flatten.
     *
     * @param <M> the generic type
     * @param map the map
     * @param mapSupplier the map supplier
     * @return the m
     */
    public static <M extends Map<String, Object>> M flatten(Map<String, Object> map, Supplier<? extends M> mapSupplier) {
        return flatten(map, ".", mapSupplier);
    }

    /**
     * Flatten.
     *
     * @param <M> the generic type
     * @param map the map
     * @param delimiter the delimiter
     * @param mapSupplier the map supplier
     * @return the m
     */
    public static <M extends Map<String, Object>> M flatten(Map<String, Object> map, String delimiter, Supplier<? extends M> mapSupplier) {
        final M result = mapSupplier.get();

        flatten(map, null, delimiter, result);

        return result;
    }

    /**
     * Flatten.
     *
     * @param map the map
     * @param prefix the prefix
     * @param delimiter the delimiter
     * @param output the output
     */
    private static void flatten(Map<String, Object> map, String prefix, String delimiter, Map<String, Object> output) {
        if (N.isNullOrEmpty(map)) {
            return;
        }

        if (N.isNullOrEmpty(prefix)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    flatten((Map<String, Object>) entry.getValue(), entry.getKey(), delimiter, output);
                } else {
                    output.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    flatten((Map<String, Object>) entry.getValue(), prefix + delimiter + entry.getKey(), delimiter, output);
                } else {
                    output.put(prefix + delimiter + entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Unflatten.
     *
     * @param map the map
     * @return the map
     */
    public static Map<String, Object> unflatten(Map<String, Object> map) {
        return unflatten(map, Suppliers.<String, Object> ofMap());
    }

    /**
     * Unflatten.
     *
     * @param <M> the generic type
     * @param map the map
     * @param mapSupplier the map supplier
     * @return the m
     */
    public static <M extends Map<String, Object>> M unflatten(Map<String, Object> map, Supplier<? extends M> mapSupplier) {
        return unflatten(map, ".", mapSupplier);
    }

    /**
     * Unflatten.
     *
     * @param <M> the generic type
     * @param map the map
     * @param delimiter the delimiter
     * @param mapSupplier the map supplier
     * @return the m
     */
    public static <M extends Map<String, Object>> M unflatten(Map<String, Object> map, String delimiter, Supplier<? extends M> mapSupplier) {
        final M result = mapSupplier.get();
        final Splitter keySplitter = Splitter.with(delimiter);

        if (N.notNullOrEmpty(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().indexOf(delimiter) >= 0) {
                    final String[] keys = keySplitter.splitToArray(entry.getKey());
                    Map<String, Object> lastMap = result;

                    for (int i = 0, to = keys.length - 1; i < to; i++) {
                        Map<String, Object> tmp = (Map<String, Object>) lastMap.get(keys[i]);

                        if (tmp == null) {
                            tmp = mapSupplier.get();
                            lastMap.put(keys[i], tmp);
                        }

                        lastMap = tmp;
                    }

                    lastMap.put(keys[keys.length - 1], entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return result;
    }

    /**
     * Map type 2 supplier.
     *
     * @param mapType the map type
     * @return the supplier
     */
    @SuppressWarnings("rawtypes")
    static Supplier mapType2Supplier(final Class<? extends Map> mapType) {
        if (HashMap.class.equals(mapType)) {
            return Suppliers.ofMap();
        } else if (SortedMap.class.isAssignableFrom(mapType)) {
            return Suppliers.ofTreeMap();
        } else if (IdentityHashMap.class.isAssignableFrom(mapType)) {
            return Suppliers.ofIdentityHashMap();
        } else if (LinkedHashMap.class.isAssignableFrom(mapType)) {
            return Suppliers.ofLinkedHashMap();
        } else if (ImmutableMap.class.isAssignableFrom(mapType)) {
            return Suppliers.ofLinkedHashMap();
        } else {
            return new Supplier<Map>() {
                @Override
                public Map get() {
                    try {
                        return N.newInstance(mapType);
                    } catch (Exception e) {
                        return new LinkedHashMap<>();
                    }
                }
            };
        }
    }

    /**
     * Replace all.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @param function the function
     */
    static <K, V> void replaceAll(Map<K, V> map, BiFunction<? super K, ? super V, ? extends V> function) {
        N.checkArgNotNull(function);

        try {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                entry.setValue(function.apply(entry.getKey(), entry.getValue()));
            }
        } catch (IllegalStateException ise) {
            throw new ConcurrentModificationException(ise);
        }
    }

    /**
     * Merge.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E> the element type
     * @param map the map
     * @param key the key
     * @param value the value
     * @param remappingFunction the remapping function
     * @throws E the e
     */
    static <K, V, E extends Exception> void merge(Map<K, V> map, K key, V value, Try.BiFunction<? super V, ? super V, ? extends V, E> remappingFunction)
            throws E {
        final V oldValue = map.get(key);

        if (oldValue == null && map.containsKey(key) == false) {
            map.put(key, value);
        } else {
            map.put(key, remappingFunction.apply(oldValue, value));
        }
    }
}