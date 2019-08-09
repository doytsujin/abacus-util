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
import com.landawn.abacus.util.function.ByteSupplier;
import com.landawn.abacus.util.function.Supplier;
import com.landawn.abacus.util.stream.ByteStream;

// TODO: Auto-generated Javadoc
/**
 * The Class ByteIterator.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public abstract class ByteIterator extends ImmutableIterator<Byte> {

    /** The Constant EMPTY. */
    public static final ByteIterator EMPTY = new ByteIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public byte nextByte() {
            throw new NoSuchElementException();
        }
    };

    /**
     * Empty.
     *
     * @return the byte iterator
     */
    public static ByteIterator empty() {
        return EMPTY;
    }

    /**
     * Of.
     *
     * @param a the a
     * @return the byte iterator
     */
    @SafeVarargs
    public static ByteIterator of(final byte... a) {
        return N.isNullOrEmpty(a) ? EMPTY : of(a, 0, a.length);
    }

    /**
     * Of.
     *
     * @param a the a
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the byte iterator
     */
    public static ByteIterator of(final byte[] a, final int fromIndex, final int toIndex) {
        N.checkFromToIndex(fromIndex, toIndex, a == null ? 0 : a.length);

        if (fromIndex == toIndex) {
            return EMPTY;
        }

        return new ByteIterator() {
            private int cursor = fromIndex;

            @Override
            public boolean hasNext() {
                return cursor < toIndex;
            }

            @Override
            public byte nextByte() {
                if (cursor >= toIndex) {
                    throw new NoSuchElementException();
                }

                return a[cursor++];
            }

            @Override
            public byte[] toArray() {
                return N.copyOfRange(a, cursor, toIndex);
            }

            @Override
            public ByteList toList() {
                return ByteList.of(N.copyOfRange(a, cursor, toIndex));
            }
        };
    }

    /**
     * Lazy evaluation.
     *
     * @param iteratorSupplier the iterator supplier
     * @return the byte iterator
     */
    public static ByteIterator of(final Supplier<? extends ByteIterator> iteratorSupplier) {
        N.checkArgNotNull(iteratorSupplier, "iteratorSupplier");

        return new ByteIterator() {
            private ByteIterator iter = null;
            private boolean isInitialized = false;

            @Override
            public boolean hasNext() {
                if (isInitialized == false) {
                    init();
                }

                return iter.hasNext();
            }

            @Override
            public byte nextByte() {
                if (isInitialized == false) {
                    init();
                }

                return iter.nextByte();
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
     * @return the byte iterator
     */
    public static ByteIterator oF(final Supplier<byte[]> arraySupplier) {
        N.checkArgNotNull(arraySupplier, "arraySupplier");

        return new ByteIterator() {
            private byte[] aar = null;
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
            public byte nextByte() {
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
     * Returns an infinite {@code ByteIterator}.
     *
     * @param supplier the supplier
     * @return the byte iterator
     */
    public static ByteIterator generate(final ByteSupplier supplier) {
        N.checkArgNotNull(supplier);

        return new ByteIterator() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public byte nextByte() {
                return supplier.getAsByte();
            }
        };
    }

    /**
     * Generate.
     *
     * @param hasNext the has next
     * @param supplier the supplier
     * @return the byte iterator
     */
    public static ByteIterator generate(final BooleanSupplier hasNext, final ByteSupplier supplier) {
        N.checkArgNotNull(hasNext);
        N.checkArgNotNull(supplier);

        return new ByteIterator() {
            @Override
            public boolean hasNext() {
                return hasNext.getAsBoolean();
            }

            @Override
            public byte nextByte() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                return supplier.getAsByte();
            }
        };
    }

    /**
     * Next.
     *
     * @return the byte
     * @Deprecated use <code>nextByte()</code> instead.
     */
    @Deprecated
    @Override
    public Byte next() {
        return nextByte();
    }

    /**
     * Next byte.
     *
     * @return the byte
     */
    public abstract byte nextByte();

    /**
     * To array.
     *
     * @return the byte[]
     */
    public byte[] toArray() {
        return toList().trimToSize().array();
    }

    /**
     * To list.
     *
     * @return the byte list
     */
    public ByteList toList() {
        final ByteList list = new ByteList();

        while (hasNext()) {
            list.add(nextByte());
        }

        return list;
    }

    /**
     * Stream.
     *
     * @return the byte stream
     */
    public ByteStream stream() {
        return ByteStream.of(this);
    }

    /**
     * Foreach remaining.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public <E extends Exception> void foreachRemaining(Try.ByteConsumer<E> action) throws E {
        N.checkArgNotNull(action);

        while (hasNext()) {
            action.accept(nextByte());
        }
    }

    /**
     * For each remaining.
     *
     * @param action the action
     */
    @Override
    @Deprecated
    public void forEachRemaining(java.util.function.Consumer<? super Byte> action) {
        super.forEachRemaining(action);
    }
}