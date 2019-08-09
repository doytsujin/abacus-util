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

import java.util.NoSuchElementException;

import com.landawn.abacus.util.function.BooleanSupplier;
import com.landawn.abacus.util.function.FloatSupplier;
import com.landawn.abacus.util.function.Supplier;
import com.landawn.abacus.util.stream.FloatStream;

// TODO: Auto-generated Javadoc
/**
 * The Class FloatIterator.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public abstract class FloatIterator extends ImmutableIterator<Float> {

    /** The Constant EMPTY. */
    public static final FloatIterator EMPTY = new FloatIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public float nextFloat() {
            throw new NoSuchElementException();
        }
    };

    /**
     * Empty.
     *
     * @return the float iterator
     */
    public static FloatIterator empty() {
        return EMPTY;
    }

    /**
     * Of.
     *
     * @param a the a
     * @return the float iterator
     */
    @SafeVarargs
    public static FloatIterator of(final float... a) {
        return N.isNullOrEmpty(a) ? EMPTY : of(a, 0, a.length);
    }

    /**
     * Of.
     *
     * @param a the a
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the float iterator
     */
    public static FloatIterator of(final float[] a, final int fromIndex, final int toIndex) {
        N.checkFromToIndex(fromIndex, toIndex, a == null ? 0 : a.length);

        if (fromIndex == toIndex) {
            return EMPTY;
        }

        return new FloatIterator() {
            private int cursor = fromIndex;

            @Override
            public boolean hasNext() {
                return cursor < toIndex;
            }

            @Override
            public float nextFloat() {
                if (cursor >= toIndex) {
                    throw new NoSuchElementException();
                }

                return a[cursor++];
            }

            @Override
            public float[] toArray() {
                return N.copyOfRange(a, cursor, toIndex);
            }

            @Override
            public FloatList toList() {
                return FloatList.of(N.copyOfRange(a, cursor, toIndex));
            }
        };
    }

    /**
     * Lazy evaluation.
     *
     * @param iteratorSupplier the iterator supplier
     * @return the float iterator
     */
    public static FloatIterator of(final Supplier<? extends FloatIterator> iteratorSupplier) {
        N.checkArgNotNull(iteratorSupplier, "iteratorSupplier");

        return new FloatIterator() {
            private FloatIterator iter = null;
            private boolean isInitialized = false;

            @Override
            public boolean hasNext() {
                if (isInitialized == false) {
                    init();
                }

                return iter.hasNext();
            }

            @Override
            public float nextFloat() {
                if (isInitialized == false) {
                    init();
                }

                return iter.nextFloat();
            }

            private void init() {
                if (isInitialized == false) {
                    isInitialized = true;
                    iter = iteratorSupplier.get();
                }
            }
        };
    }

    /**
     * Lazy evaluation.
     *
     * @param arraySupplier the array supplier
     * @return the float iterator
     */
    public static FloatIterator oF(final Supplier<float[]> arraySupplier) {
        N.checkArgNotNull(arraySupplier, "arraySupplier");

        return new FloatIterator() {
            private float[] aar = null;
            private int len = 0;
            private int cur = 0;
            private boolean isInitialized = false;

            @Override
            public boolean hasNext() {
                if (isInitialized == false) {
                    init();
                }

                return cur < len;
            }

            @Override
            public float nextFloat() {
                if (isInitialized == false) {
                    init();
                }

                if (cur >= len) {
                    throw new NoSuchElementException();
                }

                return aar[cur++];
            }

            private void init() {
                if (isInitialized == false) {
                    isInitialized = true;
                    aar = arraySupplier.get();
                    len = N.len(aar);
                }
            }
        };
    }

    /**
     * Returns an infinite {@code FloatIterator}.
     *
     * @param supplier the supplier
     * @return the float iterator
     */
    public static FloatIterator generate(final FloatSupplier supplier) {
        N.checkArgNotNull(supplier);

        return new FloatIterator() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public float nextFloat() {
                return supplier.getAsFloat();
            }
        };
    }

    /**
     * Generate.
     *
     * @param hasNext the has next
     * @param supplier the supplier
     * @return the float iterator
     */
    public static FloatIterator generate(final BooleanSupplier hasNext, final FloatSupplier supplier) {
        N.checkArgNotNull(hasNext);
        N.checkArgNotNull(supplier);

        return new FloatIterator() {
            @Override
            public boolean hasNext() {
                return hasNext.getAsBoolean();
            }

            @Override
            public float nextFloat() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                return supplier.getAsFloat();
            }
        };
    }

    /**
     * Next.
     *
     * @return the float
     * @Deprecated use <code>nextFloat()</code> instead.
     */
    @Deprecated
    @Override
    public Float next() {
        return nextFloat();
    }

    /**
     * Next float.
     *
     * @return the float
     */
    public abstract float nextFloat();

    /**
     * To array.
     *
     * @return the float[]
     */
    public float[] toArray() {
        return toList().trimToSize().array();
    }

    /**
     * To list.
     *
     * @return the float list
     */
    public FloatList toList() {
        final FloatList list = new FloatList();

        while (hasNext()) {
            list.add(nextFloat());
        }

        return list;
    }

    /**
     * Stream.
     *
     * @return the float stream
     */
    public FloatStream stream() {
        return FloatStream.of(this);
    }

    /**
     * Foreach remaining.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void foreachRemaining(Try.FloatConsumer<E> action) throws E {
        N.checkArgNotNull(action);

        while (hasNext()) {
            action.accept(nextFloat());
        }
    }

    /**
     * For each remaining.
     *
     * @param action the action
     */
    @Override
    @Deprecated
    public void forEachRemaining(java.util.function.Consumer<? super Float> action) {
        super.forEachRemaining(action);
    }
}