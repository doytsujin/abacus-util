/*
 * Copyright (c) 2018, Haiyang Li.
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
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.landawn.abacus.util.function.BiConsumer;
import com.landawn.abacus.util.function.BooleanSupplier;
import com.landawn.abacus.util.function.Consumer;
import com.landawn.abacus.util.function.IndexedConsumer;
import com.landawn.abacus.util.function.TriFunction;
import com.landawn.abacus.util.stream.Stream;

// TODO: Auto-generated Javadoc
/**
 * The Class TriIterator.
 *
 * @author Haiyang Li
 * @param <A> the generic type
 * @param <B> the generic type
 * @param <C> the generic type
 * @since 1.2.10
 */
public abstract class TriIterator<A, B, C> extends ImmutableIterator<Triple<A, B, C>> {

    /** The Constant EMPTY. */
    @SuppressWarnings("rawtypes")
    private static final TriIterator EMPTY = new TriIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void forEachRemaining(Try.TriConsumer action) throws Exception {
            N.checkArgNotNull(action);
        }

        @Override
        public ObjIterator map(TriFunction mapper) {
            N.checkArgNotNull(mapper);

            return ObjIterator.empty();
        }
    };

    /**
     * Empty.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> empty() {
        return EMPTY;
    }

    /**
     * Returns an infinite {@code BiIterator}.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param output transfer the next values.
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> generate(final Consumer<Triple<A, B, C>> output) {
        return generate(BooleanSupplier.TRUE, output);
    }

    /**
     * Generate.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param hasNext the has next
     * @param output the output
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> generate(final BooleanSupplier hasNext, final Consumer<Triple<A, B, C>> output) {
        N.checkArgNotNull(hasNext);
        N.checkArgNotNull(output);

        return new TriIterator<A, B, C>() {
            private final Triple<A, B, C> tmp = new Triple<A, B, C>();

            @Override
            public boolean hasNext() {
                return hasNext.getAsBoolean();
            }

            @Override
            public Triple<A, B, C> next() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                output.accept(tmp);

                return Triple.of(tmp.left, tmp.middle, tmp.right);
            }

            @Override
            public <E extends Exception> void forEachRemaining(final Try.TriConsumer<? super A, ? super B, ? super C, E> action) throws E {
                N.checkArgNotNull(action);

                while (hasNext.getAsBoolean()) {
                    output.accept(tmp);

                    action.accept(tmp.left, tmp.middle, tmp.right);
                }
            }

            @Override
            public <R> ObjIterator<R> map(final TriFunction<? super A, ? super B, ? super C, R> mapper) {
                N.checkArgNotNull(mapper);

                return new ObjIterator<R>() {
                    @Override
                    public boolean hasNext() {
                        return hasNext.getAsBoolean();
                    }

                    @Override
                    public R next() {
                        if (hasNext() == false) {
                            throw new NoSuchElementException();
                        }

                        output.accept(tmp);

                        return mapper.apply(tmp.left, tmp.middle, tmp.right);
                    }
                };
            }
        };
    }

    /**
     * Generate.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param fromIndex the from index
     * @param toIndex the to index
     * @param output the output
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> generate(final int fromIndex, final int toIndex, final IndexedConsumer<Triple<A, B, C>> output) {
        N.checkFromToIndex(fromIndex, toIndex, Integer.MAX_VALUE);
        N.checkArgNotNull(output);

        return new TriIterator<A, B, C>() {
            private final MutableInt cursor = MutableInt.of(fromIndex);
            private final Triple<A, B, C> tmp = new Triple<A, B, C>();

            @Override
            public boolean hasNext() {
                return cursor.value() < toIndex;
            }

            @Override
            public Triple<A, B, C> next() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                output.accept(cursor.getAndIncrement(), tmp);

                return Triple.of(tmp.left, tmp.middle, tmp.right);
            }

            @Override
            public <E extends Exception> void forEachRemaining(final Try.TriConsumer<? super A, ? super B, ? super C, E> action) throws E {
                N.checkArgNotNull(action);

                while (cursor.value() < toIndex) {
                    output.accept(cursor.getAndIncrement(), tmp);

                    action.accept(tmp.left, tmp.middle, tmp.right);
                }
            }

            @Override
            public <R> ObjIterator<R> map(final TriFunction<? super A, ? super B, ? super C, R> mapper) {
                N.checkArgNotNull(mapper);

                return new ObjIterator<R>() {
                    @Override
                    public boolean hasNext() {
                        return cursor.value() < toIndex;
                    }

                    @Override
                    public R next() {
                        if (hasNext() == false) {
                            throw new NoSuchElementException();
                        }

                        output.accept(cursor.getAndIncrement(), tmp);

                        return mapper.apply(tmp.left, tmp.middle, tmp.right);
                    }
                };
            }
        };
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final A[] a, final B[] b, final C[] c) {
        return zip(Array.asList(a), Array.asList(b), Array.asList(c));
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param a the a
     * @param b the b
     * @param c the c
     * @param valueForNoneA the value for none A
     * @param valueForNoneB the value for none B
     * @param valueForNoneC the value for none C
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final A[] a, final B[] b, final C[] c, final A valueForNoneA, final B valueForNoneB,
            final C valueForNoneC) {
        return zip(Array.asList(a), Array.asList(b), Array.asList(c), valueForNoneA, valueForNoneB, valueForNoneC);
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final Collection<A> a, final Collection<B> b, final Collection<C> c) {
        return zip(a == null ? null : a.iterator(), b == null ? null : b.iterator(), c == null ? null : c.iterator());
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param a the a
     * @param b the b
     * @param c the c
     * @param valueForNoneA the value for none A
     * @param valueForNoneB the value for none B
     * @param valueForNoneC the value for none C
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final Collection<A> a, final Collection<B> b, final Collection<C> c, final A valueForNoneA,
            final B valueForNoneB, final C valueForNoneC) {
        return zip(a == null ? null : a.iterator(), b == null ? null : b.iterator(), c == null ? null : c.iterator(), valueForNoneA, valueForNoneB,
                valueForNoneC);
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param iterA the iter A
     * @param iterB the iter B
     * @param iterC the iter C
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final Iterator<A> iterA, final Iterator<B> iterB, final Iterator<C> iterC) {
        if (iterA == null || iterB == null || iterC == null) {
            return empty();
        }

        return new TriIterator<A, B, C>() {
            @Override
            public boolean hasNext() {
                return iterA.hasNext() && iterB.hasNext() && iterC.hasNext();
            }

            @Override
            public Triple<A, B, C> next() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                return Triple.of(iterA.next(), iterB.next(), iterC.next());
            }

            @Override
            public <E extends Exception> void forEachRemaining(final Try.TriConsumer<? super A, ? super B, ? super C, E> action) throws E {
                N.checkArgNotNull(action);

                while (iterA.hasNext() && iterB.hasNext() && iterC.hasNext()) {
                    action.accept(iterA.next(), iterB.next(), iterC.next());
                }
            }

            @Override
            public <R> ObjIterator<R> map(final TriFunction<? super A, ? super B, ? super C, R> mapper) {
                N.checkArgNotNull(mapper);

                return new ObjIterator<R>() {
                    @Override
                    public boolean hasNext() {
                        return iterA.hasNext() && iterB.hasNext() && iterC.hasNext();
                    }

                    @Override
                    public R next() {
                        if (hasNext() == false) {
                            throw new NoSuchElementException();
                        }

                        return mapper.apply(iterA.next(), iterB.next(), iterC.next());
                    }
                };
            }
        };
    }

    /**
     * Zip.
     *
     * @param <A> the generic type
     * @param <B> the generic type
     * @param <C> the generic type
     * @param iterA the iter A
     * @param iterB the iter B
     * @param iterC the iter C
     * @param valueForNoneA the value for none A
     * @param valueForNoneB the value for none B
     * @param valueForNoneC the value for none C
     * @return the tri iterator
     */
    public static <A, B, C> TriIterator<A, B, C> zip(final Iterator<A> iterA, final Iterator<B> iterB, final Iterator<C> iterC, final A valueForNoneA,
            final B valueForNoneB, final C valueForNoneC) {
        final Iterator<A> iter1 = iterA == null ? ObjIterator.<A> empty() : iterA;
        final Iterator<B> iter2 = iterB == null ? ObjIterator.<B> empty() : iterB;
        final Iterator<C> iter3 = iterC == null ? ObjIterator.<C> empty() : iterC;

        return new TriIterator<A, B, C>() {
            @Override
            public boolean hasNext() {
                return iter1.hasNext() || iter2.hasNext() || iter3.hasNext();
            }

            @Override
            public Triple<A, B, C> next() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }

                return Triple.of(iter1.hasNext() ? iter1.next() : valueForNoneA, iter2.hasNext() ? iter2.next() : valueForNoneB,
                        iter3.hasNext() ? iter3.next() : valueForNoneC);
            }

            @Override
            public <E extends Exception> void forEachRemaining(final Try.TriConsumer<? super A, ? super B, ? super C, E> action) throws E {
                N.checkArgNotNull(action);

                while (iter1.hasNext() || iter2.hasNext() || iter3.hasNext()) {
                    action.accept(iter1.hasNext() ? iter1.next() : valueForNoneA, iter2.hasNext() ? iter2.next() : valueForNoneB,
                            iter3.hasNext() ? iter3.next() : valueForNoneC);
                }
            }

            @Override
            public <R> ObjIterator<R> map(final TriFunction<? super A, ? super B, ? super C, R> mapper) {
                N.checkArgNotNull(mapper);

                return new ObjIterator<R>() {
                    @Override
                    public boolean hasNext() {
                        return iter1.hasNext() || iter2.hasNext() || iter3.hasNext();
                    }

                    @Override
                    public R next() {
                        if (hasNext() == false) {
                            throw new NoSuchElementException();
                        }

                        return mapper.apply(iter1.hasNext() ? iter1.next() : valueForNoneA, iter2.hasNext() ? iter2.next() : valueForNoneB,
                                iter3.hasNext() ? iter3.next() : valueForNoneC);
                    }
                };
            }
        };
    }

    /**
     * Unzip.
     *
     * @param <T> the generic type
     * @param <L> the generic type
     * @param <M> the generic type
     * @param <R> the generic type
     * @param iter the iter
     * @param unzip output parameter.
     * @return the tri iterator
     */
    public static <T, L, M, R> TriIterator<L, M, R> unzip(final Iterator<? extends T> iter, final BiConsumer<? super T, Triple<L, M, R>> unzip) {
        if (iter == null) {
            return TriIterator.empty();
        }

        final BooleanSupplier hasNext = new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return iter.hasNext();
            }
        };

        final Consumer<Triple<L, M, R>> output = new Consumer<Triple<L, M, R>>() {
            @Override
            public void accept(Triple<L, M, R> out) {
                unzip.accept(iter.next(), out);
            }
        };

        return TriIterator.generate(hasNext, output);
    }

    /**
     * For each remaining.
     *
     * @param <E> the element type
     * @param action the action
     * @throws E the e
     */
    public abstract <E extends Exception> void forEachRemaining(final Try.TriConsumer<? super A, ? super B, ? super C, E> action) throws E;

    /**
     * It's preferred to call <code>forEachRemaining(Try.TriConsumer)</code> to avoid the create the unnecessary <code>Triple</code> Objects.
     *
     * @param action the action
     * @deprecated 
     */
    @Override
    @Deprecated
    public void forEachRemaining(java.util.function.Consumer<? super Triple<A, B, C>> action) {
        super.forEachRemaining(action);
    }

    /**
     * Map.
     *
     * @param <R> the generic type
     * @param mapper the mapper
     * @return the obj iterator
     */
    public abstract <R> ObjIterator<R> map(final TriFunction<? super A, ? super B, ? super C, R> mapper);

    /**
     * Stream.
     *
     * @param <R> the generic type
     * @param mapper the mapper
     * @return the stream
     */
    public <R> Stream<R> stream(final TriFunction<? super A, ? super B, ? super C, R> mapper) {
        N.checkArgNotNull(mapper);

        return Stream.of(map(mapper));
    }
}