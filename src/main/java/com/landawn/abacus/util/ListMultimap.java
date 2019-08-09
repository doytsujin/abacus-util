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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.landawn.abacus.annotation.Internal;
import com.landawn.abacus.util.function.IntFunction;
import com.landawn.abacus.util.function.Supplier;

// TODO: Auto-generated Javadoc
/**
 * The Class ListMultimap.
 *
 * @author Haiyang Li
 * @param <K> the key type
 * @param <E> the element type
 * @see N#newListMultimap()
 * @see N#newListMultimap(Class, Class)
 * @see N#newListMultimap(Supplier, Supplier)
 * @since 0.9
 */
public final class ListMultimap<K, E> extends Multimap<K, E, List<E>> {

    /**
     * Instantiates a new list multimap.
     */
    ListMultimap() {
        this(HashMap.class, ArrayList.class);
    }

    /**
     * Instantiates a new list multimap.
     *
     * @param initialCapacity the initial capacity
     */
    ListMultimap(int initialCapacity) {
        this(new HashMap<K, List<E>>(initialCapacity), ArrayList.class);
    }

    /**
     * Instantiates a new list multimap.
     *
     * @param mapType the map type
     * @param valueType the value type
     */
    @SuppressWarnings("rawtypes")
    ListMultimap(final Class<? extends Map> mapType, final Class<? extends List> valueType) {
        super(mapType, valueType);
    }

    /**
     * Instantiates a new list multimap.
     *
     * @param mapSupplier the map supplier
     * @param valueSupplier the value supplier
     */
    ListMultimap(final Supplier<? extends Map<K, List<E>>> mapSupplier, final Supplier<? extends List<E>> valueSupplier) {
        super(mapSupplier, valueSupplier);
    }

    /**
     * Instantiates a new list multimap.
     *
     * @param valueMap the value map
     * @param valueType the value type
     */
    @Internal
    @SuppressWarnings("rawtypes")
    ListMultimap(final Map<K, List<E>> valueMap, final Class<? extends List> valueType) {
        super(valueMap, valueType2Supplier(valueType));
    }

    /**
     * Instantiates a new list multimap.
     *
     * @param valueMap the value map
     * @param valueSupplier the value supplier
     */
    @Internal
    ListMultimap(final Map<K, List<E>> valueMap, final Supplier<? extends List<E>> valueSupplier) {
        super(valueMap, valueSupplier);
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param k1 the k 1
     * @param v1 the v 1
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1) {
        final ListMultimap<K, E> map = new ListMultimap<>();

        map.put(k1, v1);

        return map;
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2) {
        final ListMultimap<K, E> map = new ListMultimap<>();

        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2, final K k3, final E v3) {
        final ListMultimap<K, E> map = new ListMultimap<>();

        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
    }

    /**
     * Of.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param k1 the k 1
     * @param v1 the v 1
     * @param k2 the k 2
     * @param v2 the v 2
     * @param k3 the k 3
     * @param v3 the v 3
     * @param k4 the k 4
     * @param v4 the v 4
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2, final K k3, final E v3, final K k4, final E v4) {
        final ListMultimap<K, E> map = new ListMultimap<>();

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
     * @param <E> the element type
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
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2, final K k3, final E v3, final K k4, final E v4, final K k5,
            final E v5) {
        final ListMultimap<K, E> map = new ListMultimap<>();

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
     * @param <E> the element type
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
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2, final K k3, final E v3, final K k4, final E v4, final K k5,
            final E v5, final K k6, final E v6) {
        final ListMultimap<K, E> map = new ListMultimap<>();

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
     * @param <E> the element type
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
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> of(final K k1, final E v1, final K k2, final E v2, final K k3, final E v3, final K k4, final E v4, final K k5,
            final E v5, final K k6, final E v6, final K k7, final E v7) {
        final ListMultimap<K, E> map = new ListMultimap<>();

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
     * From.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> from(final Map<? extends K, ? extends E> map) {
        final ListMultimap<K, E> multimap = new ListMultimap<>(Maps.newTargetMap(map), ArrayList.class);

        if (N.notNullOrEmpty(map)) {
            multimap.putAll(map);
        }

        return multimap;
    }

    /**
     * Fromm.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> fromm(final Map<? extends K, ? extends Collection<? extends E>> map) {
        final ListMultimap<K, E> multimap = new ListMultimap<>(Maps.newTargetMap(map), ArrayList.class);

        if (N.notNullOrEmpty(map)) {
            for (Map.Entry<? extends K, ? extends Collection<? extends E>> entry : map.entrySet()) {
                multimap.putAll(entry.getKey(), entry.getValue());
            }
        }

        return multimap;
    }

    /**
     * From.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <X> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @return the list multimap
     * @throws X the x
     */
    public static <T, K, X extends Exception> ListMultimap<K, T> from(final Collection<? extends T> c, final Try.Function<? super T, ? extends K, X> keyMapper)
            throws X {
        N.checkArgNotNull(keyMapper);

        final ListMultimap<K, T> multimap = N.newListMultimap(N.initHashCapacity(c == null ? 0 : c.size()));

        if (N.notNullOrEmpty(c)) {
            for (T e : c) {
                multimap.put(keyMapper.apply(e), e);
            }
        }

        return multimap;
    }

    /**
     * From.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <E> the element type
     * @param <X> the generic type
     * @param <X2> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @param valueExtractor the value extractor
     * @return the list multimap
     * @throws X the x
     * @throws X2 the x2
     */
    public static <T, K, E, X extends Exception, X2 extends Exception> ListMultimap<K, E> from(final Collection<? extends T> c,
            final Try.Function<? super T, ? extends K, X> keyMapper, final Try.Function<? super T, ? extends E, X2> valueExtractor) throws X, X2 {
        N.checkArgNotNull(keyMapper);
        N.checkArgNotNull(valueExtractor);

        final ListMultimap<K, E> multimap = N.newListMultimap(N.initHashCapacity(c == null ? 0 : c.size()));

        if (N.notNullOrEmpty(c)) {
            for (T e : c) {
                multimap.put(keyMapper.apply(e), valueExtractor.apply(e));
            }
        }

        return multimap;
    }

    /**
     * Invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @return the list multimap
     * @see Multimap#invertFrom(Map, com.landawn.abacus.util.function.Supplier)
     */
    public static <K, E> ListMultimap<E, K> invertFrom(final Map<K, E> map) {
        final ListMultimap<E, K> multimap = new ListMultimap<>(Maps.newOrderingMap(map), ArrayList.class);

        if (N.notNullOrEmpty(map)) {
            for (Map.Entry<K, E> entry : map.entrySet()) {
                multimap.put(entry.getValue(), entry.getKey());
            }
        }

        return multimap;
    }

    /**
     * Flat invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param map the map
     * @return the list multimap
     * @see Multimap#flatInvertFrom(Map, com.landawn.abacus.util.function.Supplier)
     */
    public static <K, E> ListMultimap<E, K> flatInvertFrom(final Map<K, ? extends Collection<? extends E>> map) {
        final ListMultimap<E, K> multimap = new ListMultimap<>(Maps.newOrderingMap(map), ArrayList.class);

        if (N.notNullOrEmpty(map)) {
            for (Map.Entry<K, ? extends Collection<? extends E>> entry : map.entrySet()) {
                final Collection<? extends E> c = entry.getValue();

                if (N.notNullOrEmpty(c)) {
                    for (E e : c) {
                        multimap.put(e, entry.getKey());
                    }
                }
            }
        }

        return multimap;
    }

    /**
     * Invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @return the list multimap
     */
    public static <K, E, V extends Collection<E>> ListMultimap<E, K> invertFrom(final Multimap<K, E, V> map) {
        final ListMultimap<E, K> multimap = new ListMultimap<>(Maps.newOrderingMap(map.valueMap), ArrayList.class);

        if (N.notNullOrEmpty(map)) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                final V c = entry.getValue();

                if (N.notNullOrEmpty(c)) {
                    for (E e : c) {
                        multimap.put(e, entry.getKey());
                    }
                }
            }
        }

        return multimap;
    }

    /**
     * Concat.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param a the a
     * @param b the b
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> concat(final Map<? extends K, ? extends E> a, final Map<? extends K, ? extends E> b) {
        if (a == null) {
            return b == null ? N.<K, E> newListMultimap() : from(b);
        } else {
            final ListMultimap<K, E> res = from(a);
            res.putAll(b);
            return res;
        }
    }

    /**
     * Concat.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the list multimap
     */
    public static <K, E> ListMultimap<K, E> concat(final Map<? extends K, ? extends E> a, final Map<? extends K, ? extends E> b,
            final Map<? extends K, ? extends E> c) {
        if (a == null) {
            if (b == null) {
                return c == null ? N.<K, E> newListMultimap() : from(c);
            } else {
                final ListMultimap<K, E> res = from(b);
                res.putAll(c);
                return res;
            }
        } else {
            final ListMultimap<K, E> res = from(a);
            res.putAll(b);
            res.putAll(c);
            return res;
        }
    }

    /**
     * Wrap.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @return the list multimap
     */
    @SuppressWarnings("rawtypes")
    public static <K, E, V extends List<E>> ListMultimap<K, E> wrap(final Map<K, V> map) {
        N.checkArgNotNull(map);
        N.checkArgument(N.anyNull(map.values()), "The specified map contains null value: %s", map);

        Class<? extends List> valueType = ArrayList.class;

        for (V v : map.values()) {
            if (v != null) {
                valueType = v.getClass();
                break;
            }
        }

        return new ListMultimap<K, E>((Map<K, List<E>>) map, valueType);
    }

    /**
     * Wrapp.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @param valueSupplier the value supplier
     * @return the list multimap
     */
    @SuppressWarnings("rawtypes")
    public static <K, E, V extends List<E>> ListMultimap<K, E> wrapp(final Map<K, V> map, final Supplier<? extends V> valueSupplier) {
        N.checkArgNotNull(map, "map");
        N.checkArgNotNull(valueSupplier, "valueSupplier");

        return new ListMultimap<K, E>((Map) map, valueSupplier);
    }

    /**
     * From.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param map the map
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<E>, M extends Multimap<K, E, V>> M from(final Map<? extends K, ? extends E> map,
            final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Fromm.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param map the map
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<E>, M extends Multimap<K, E, V>> M fromm(final Map<? extends K, ? extends Collection<? extends E>> map,
            final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * From.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <V> the value type
     * @param <M> the generic type
     * @param <X> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @param multimapSupplier the multimap supplier
     * @return the m
     * @throws X the x
     */
    @Deprecated
    public static <T, K, V extends Collection<T>, M extends Multimap<K, T, V>, X extends Exception> M from(final Collection<? extends T> c,
            final Try.Function<? super T, ? extends K, X> keyMapper, final IntFunction<? extends M> multimapSupplier) throws X {
        throw new UnsupportedOperationException();
    }

    /**
     * From.
     *
     * @param <T> the generic type
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param <X> the generic type
     * @param <X2> the generic type
     * @param c the c
     * @param keyMapper the key mapper
     * @param valueExtractor the value extractor
     * @param multimapSupplier the multimap supplier
     * @return the m
     * @throws X the x
     * @throws X2 the x2
     */
    @Deprecated
    public static <T, K, E, V extends Collection<E>, M extends Multimap<K, E, V>, X extends Exception, X2 extends Exception> M from(
            final Collection<? extends T> c, final Try.Function<? super T, ? extends K, X> keyMapper,
            final Try.Function<? super T, ? extends E, X2> valueExtractor, final IntFunction<? extends M> multimapSupplier) throws X, X2 {
        throw new UnsupportedOperationException();
    }

    /**
     * Invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param map the map
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<K>, M extends Multimap<E, K, V>> M invertFrom(final Map<K, E> map,
            final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Flat invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param map the map
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<K>, M extends Multimap<E, K, V>> M flatInvertFrom(final Map<K, ? extends Collection<? extends E>> map,
            final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Invert from.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <VV> the generic type
     * @param <M> the generic type
     * @param multimap the multimap
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<E>, VV extends Collection<K>, M extends Multimap<E, K, VV>> M invertFrom(final Multimap<K, E, V> multimap,
            final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Concat.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param a the a
     * @param b the b
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<E>, M extends Multimap<K, E, V>> M concat(final Map<? extends K, ? extends E> a,
            final Map<? extends K, ? extends E> b, final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Concat.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param <M> the generic type
     * @param a the a
     * @param b the b
     * @param c the c
     * @param multimapSupplier the multimap supplier
     * @return the m
     */
    @Deprecated
    public static <K, E, V extends Collection<E>, M extends Multimap<K, E, V>> M concat(final Map<? extends K, ? extends E> a,
            final Map<? extends K, ? extends E> b, final Map<? extends K, ? extends E> c, final IntFunction<? extends M> multimapSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Wrap.
     *
     * @param <K> the key type
     * @param <E> the element type
     * @param <V> the value type
     * @param map the map
     * @param valueSupplier the value supplier
     * @return the multimap
     */
    @Deprecated
    public static <K, E, V extends Collection<E>> Multimap<K, E, V> wrap(final Map<K, V> map, final Supplier<? extends V> valueSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Filter by key.
     *
     * @param <X> the generic type
     * @param filter the filter
     * @return the list multimap
     * @throws X the x
     */
    @Override
    public <X extends Exception> ListMultimap<K, E> filterByKey(Try.Predicate<? super K, X> filter) throws X {
        final ListMultimap<K, E> result = new ListMultimap<>(mapSupplier, valueSupplier);

        for (Map.Entry<K, List<E>> entry : valueMap.entrySet()) {
            if (filter.test(entry.getKey())) {
                result.valueMap.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Filter by value.
     *
     * @param <X> the generic type
     * @param filter the filter
     * @return the list multimap
     * @throws X the x
     */
    @Override
    public <X extends Exception> ListMultimap<K, E> filterByValue(Try.Predicate<? super List<E>, X> filter) throws X {
        final ListMultimap<K, E> result = new ListMultimap<>(mapSupplier, valueSupplier);

        for (Map.Entry<K, List<E>> entry : valueMap.entrySet()) {
            if (filter.test(entry.getValue())) {
                result.valueMap.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Filter.
     *
     * @param <X> the generic type
     * @param filter the filter
     * @return the list multimap
     * @throws X the x
     */
    @Override
    public <X extends Exception> ListMultimap<K, E> filter(Try.BiPredicate<? super K, ? super List<E>, X> filter) throws X {
        final ListMultimap<K, E> result = new ListMultimap<>(mapSupplier, valueSupplier);

        for (Map.Entry<K, List<E>> entry : valueMap.entrySet()) {
            if (filter.test(entry.getKey(), entry.getValue())) {
                result.valueMap.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Copy.
     *
     * @return the list multimap
     */
    @Override
    public ListMultimap<K, E> copy() {
        final ListMultimap<K, E> copy = new ListMultimap<>(mapSupplier, valueSupplier);

        copy.putAll(this);

        return copy;
    }

    /**
     * To immutable map.
     *
     * @return the immutable map
     */
    public ImmutableMap<K, ImmutableList<E>> toImmutableMap() {
        final Map<K, ImmutableList<E>> map = Maps.newOrderingMap(valueMap);

        for (Map.Entry<K, List<E>> entry : valueMap.entrySet()) {
            map.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
        }

        return ImmutableMap.of(map);
    }

    /**
     * To immutable map.
     *
     * @param mapSupplier the map supplier
     * @return the immutable map
     */
    public ImmutableMap<K, ImmutableList<E>> toImmutableMap(final IntFunction<? extends Map<K, ImmutableList<E>>> mapSupplier) {
        final Map<K, ImmutableList<E>> map = mapSupplier.apply(valueMap.size());

        for (Map.Entry<K, List<E>> entry : valueMap.entrySet()) {
            map.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
        }

        return ImmutableMap.of(map);
    }

    // It won't work.
    //    /**
    //     * Returns a synchronized {@code ListMultimap} which shares the same internal {@code Map} with this {@code ListMultimap}.
    //     * That's to say the changes in one of the returned {@code ListMultimap} and this {@code ListMultimap} will impact another one.
    //     * 
    //     * @see Collections#synchronizedMap(Map)
    //     */
    //    @Override
    //    public ListMultimap<K, E> synchronizedd() {
    //        return new ListMultimap<>(Collections.synchronizedMap(valueMap), concreteValueType);
    //    }

    //    public ListMultimap<E, K> inversed() {
    //        final ListMultimap<E, K> multimap = new ListMultimap<E, K>(valueMap.getClass(), concreteValueType);
    //
    //        if (N.notNullOrEmpty(valueMap)) {
    //            for (Map.Entry<K, ? extends List<? extends E>> entry : valueMap.entrySet()) {
    //                final List<? extends E> c = entry.getValue();
    //
    //                if (N.notNullOrEmpty(c)) {
    //                    for (E e : c) {
    //                        multimap.put(e, entry.getKey());
    //                    }
    //                }
    //            }
    //        }
    //
    //        return multimap;
    //    }
}