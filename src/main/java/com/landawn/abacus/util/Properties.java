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

package com.landawn.abacus.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class Properties.
 *
 * @author Haiyang Li
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.8
 */
public class Properties<K, V> implements Map<K, V> {

    /** The values. */
    protected final Map<K, V> values;

    /**
     * Instantiates a new properties.
     */
    public Properties() {
        this(new ConcurrentHashMap<K, V>());
    }

    /**
     * Instantiates a new properties.
     *
     * @param valueMap The valueMap and this Properties share the same data; any changes to one will appear in the other.
     */
    Properties(final ConcurrentHashMap<? extends K, ? extends V> valueMap) {
        this.values = (Map<K, V>) valueMap;
    }

    /**
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map
     * @return
     */
    public static <K, V> Properties<K, V> from(final Map<? extends K, ? extends V> map) {
        return new Properties<K, V>(new ConcurrentHashMap<K, V>(map));
    }

    /**
     *
     * @param propName
     * @return
     */
    @Override
    public V get(Object propName) {
        return values.get(propName);
    }

    /**
     * To avoid <code>NullPointerException</code> for primitive type if the target property is null or not set.
     *
     * @param <T>
     * @param targetClass
     * @param propName
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> targetClass, Object propName) {
        return N.convert(values.get(propName), targetClass);
    }

    /**
     * Gets the or default.
     *
     * @param propName
     * @param defaultValue is returned if the specified {@code propName} is not contained in this Properties instance or it's
     *            null.
     * @return
     */
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object propName, V defaultValue) {
        V result = values.get(propName);

        if (result == null) {
            return defaultValue;
        }

        return result;
    }

    /**
     * Gets the or default.
     *
     * @param <T>
     * @param targetClass
     * @param propName
     * @param defaultValue is returned if the specified {@code propName} is not contained in this Properties instance or it's null.
     * @return
     */
    public <T> T getOrDefault(Class<T> targetClass, Object propName, T defaultValue) {
        Object result = values.get(propName);

        if (result == null) {
            return defaultValue;
        }

        return N.convert(result, targetClass);
    }

    /**
     *
     * @param propName
     * @param propValue
     * @return
     */
    public Properties<K, V> set(K propName, V propValue) {
        put(propName, propValue);

        return this;
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public V put(K key, V value) {
        return values.put(key, value);
    }

    /**
     *
     * @param m
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        values.putAll(m);
    }

    /**
     * Put if absent.
     *
     * @param key
     * @param value
     * @return
     */
    public V putIfAbsent(K key, V value) {
        V v = get(key);

        if (v == null) {
            v = put(key, value);
        }

        return v;
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public V remove(Object key) {
        return values.remove(key);
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key
     * @param value
     * @return true, if successful
     */
    public boolean remove(Object key, Object value) {
        final Object curValue = get(key);

        if (!Objects.equals(curValue, value) || (curValue == null && !containsKey(key))) {
            return false;
        }

        remove(key);

        return true;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * @param key
     * @param value
     * @return
     */
    public V replace(K key, V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * @param key
     * @param oldValue
     * @param newValue
     * @return true, if successful
     */
    public boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    /**
     *
     * @param key
     * @return true, if successful
     */
    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    /**
     *
     * @param value
     * @return true, if successful
     */
    @Override
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    /**
     *
     * @return
     */
    @Override
    public Set<K> keySet() {
        return values.keySet();
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<V> values() {
        return values.values();
    }

    /**
     *
     * @return
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return values.entrySet();
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return values.size();
    }

    /**
     * Clear.
     */
    @Override
    public void clear() {
        values.clear();
    }

    /**
     *
     * @return
     */
    public Properties<K, V> copy() {
        final Properties<K, V> copy = new Properties<K, V>();

        copy.values.putAll(this.values);

        return copy;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return 31 + ((values == null) ? 0 : values.hashCode());
    }

    /**
     *
     * @param obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Properties && N.equals(((Properties<K, V>) obj).values, values));
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return values.toString();
    }
}
