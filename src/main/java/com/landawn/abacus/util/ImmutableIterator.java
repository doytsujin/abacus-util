/*
 * Copyright (c) 2017, Haiyang Li.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.landawn.abacus.util.function.Supplier;

// TODO: Auto-generated Javadoc
/**
 * The Class ImmutableIterator.
 *
 * @author Haiyang Li
 * @param <T>
 * @since 0.9
 */
abstract class ImmutableIterator<T> implements java.util.Iterator<T> {

    /**
     * Removes the.
     *
     * @deprecated - UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * To set.
     *
     * @return
     */
    public Set<T> toSet() {
        final Set<T> set = N.newHashSet();

        while (hasNext()) {
            set.add(next());
        }

        return set;
    }

    /**
     * To collection.
     *
     * @param <C>
     * @param supplier
     * @return
     */
    public <C extends Collection<T>> C toCollection(final Supplier<? extends C> supplier) {
        final C c = supplier.get();

        while (hasNext()) {
            c.add(next());
        }

        return c;
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <E>
     * @param keyMapper
     * @return
     * @throws E the e
     */
    public <K, E extends Exception> Map<K, T> toMap(final Try.Function<? super T, K, E> keyMapper) throws E {
        return Iterators.toMap(this, keyMapper);
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <E>
     * @param <E2>
     * @param keyMapper
     * @param valueExtractor
     * @return
     * @throws E the e
     * @throws E2 the e2
     */
    public <K, V, E extends Exception, E2 extends Exception> Map<K, V> toMap(final Try.Function<? super T, K, E> keyMapper,
            final Try.Function<? super T, ? extends V, E2> valueExtractor) throws E, E2 {
        return Iterators.toMap(this, keyMapper, valueExtractor);
    }

    /**
     * To map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param <M>
     * @param <E>
     * @param <E2>
     * @param keyMapper
     * @param valueExtractor
     * @param mapSupplier
     * @return
     * @throws E the e
     * @throws E2 the e2
     */
    public <K, V, M extends Map<K, V>, E extends Exception, E2 extends Exception> M toMap(final Try.Function<? super T, K, E> keyMapper,
            final Try.Function<? super T, ? extends V, E2> valueExtractor, final Supplier<? extends M> mapSupplier) throws E, E2 {
        return Iterators.toMap(this, keyMapper, valueExtractor, mapSupplier);
    }
}
