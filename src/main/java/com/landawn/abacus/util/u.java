/*
 * Copyright (c) 2019, Haiyang Li.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import com.landawn.abacus.util.function.Supplier;
import com.landawn.abacus.util.stream.DoubleStream;
import com.landawn.abacus.util.stream.IntStream;
import com.landawn.abacus.util.stream.LongStream;
import com.landawn.abacus.util.stream.Stream;

// TODO: Auto-generated Javadoc
/**
 * The Class u.
 */
public class u {

    /**
     * Instantiates a new u.
     */
    private u() {
        // utility class
    }

    /**
     * The Class Optional.
     *
     * @param <T> the generic type
     */
    public static final class Optional<T> {

        /** The Constant EMPTY. */
        private static final Optional<?> EMPTY = new Optional<>();

        /** The value. */
        private final T value;

        /**
         * Instantiates a new optional.
         */
        private Optional() {
            this.value = null;
        }

        /**
         * Instantiates a new optional.
         *
         * @param value the value
         */
        private Optional(T value) {
            this.value = N.checkArgNotNull(value);
        }

        /**
         * Empty.
         *
         * @param <T> the generic type
         * @return the optional
         */
        public static <T> Optional<T> empty() {
            return (Optional<T>) EMPTY;
        }

        /**
         * Of.
         *
         * @param <T> the generic type
         * @param value the value
         * @return the optional
         */
        public static <T> Optional<T> of(T value) {
            return new Optional<>(value);
        }

        /**
         * Of nullable.
         *
         * @param <T> the generic type
         * @param value the value
         * @return the optional
         */
        public static <T> Optional<T> ofNullable(T value) {
            if (value == null) {
                return empty();
            }

            return new Optional<>(value);
        }

        /**
         * From.
         *
         * @param <T> the generic type
         * @param op the op
         * @return the optional
         */
        public static <T> Optional<T> from(java.util.Optional<T> op) {
            if (op.isPresent()) {
                return of(op.get());
            } else {
                return empty();
            }
        }

        /**
         * Gets the.
         *
         * @return the t
         * @throws NoSuchElementException the no such element exception
         */
        public T get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return value != null;
        }

        /**
         * Checks if is empty.
         *
         * @return true, if is empty
         */
        public boolean isEmpty() {
            return value == null;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return itself
         * @throws E the e
         */
        public <E extends Exception> Optional<T> ifPresent(Try.Consumer<? super T, E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent()) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return itself
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> Optional<T> ifPresentOrElse(Try.Consumer<? super T, E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional
         * @throws E the e
         */
        public <E extends Exception> Optional<T> filter(Try.Predicate<? super T, E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent() && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> map(final Try.Function<? super T, ? extends U, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Nullable.<U> of(mapper.apply(value));
            } else {
                return Nullable.<U> empty();
            }
        }

        /**
         * Map to boolean.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean mapToBoolean(final Try.ToBooleanFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalBoolean.of(mapper.applyAsBoolean(value));
            } else {
                return OptionalBoolean.empty();
            }
        }

        /**
         * Map to char.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar mapToChar(final Try.ToCharFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalChar.of(mapper.applyAsChar(value));
            } else {
                return OptionalChar.empty();
            }
        }

        /**
         * Map to byte.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte mapToByte(final Try.ToByteFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalByte.of(mapper.applyAsByte(value));
            } else {
                return OptionalByte.empty();
            }
        }

        /**
         * Map to short.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort mapToShort(final Try.ToShortFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalShort.of(mapper.applyAsShort(value));
            } else {
                return OptionalShort.empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to long.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong mapToLong(final Try.ToLongFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return OptionalLong.empty();
            }
        }

        /**
         * Map to float.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat mapToFloat(final Try.ToFloatFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalFloat.of(mapper.applyAsFloat(value));
            } else {
                return OptionalFloat.empty();
            }
        }

        /**
         * Map to double.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDouble(final Try.ToDoubleFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional
         * @throws E the e
         */
        public <U, E extends Exception> Optional<U> flatMap(Try.Function<? super T, Optional<U>, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional
         * @throws E the e
         */
        public <E extends Exception> Optional<T> or(Try.Supplier<Optional<? extends T>, E> supplier) throws E {
            N.checkArgNotNull(supplier, "supplier");

            if (isPresent()) {
                return this;
            } else {
                return Objects.requireNonNull((Optional<T>) supplier.get());
            }
        }

        /**
         * Or null.
         *
         * @return the t
         */
        public T orNull() {
            return isPresent() ? value : null;
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the t
         */
        public T orElse(T other) {
            return isPresent() ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the t
         * @throws E the e
         */
        public <E extends Exception> T orElseGet(Try.Supplier<? extends T, E> other) throws E {
            if (isPresent()) {
                return value;
            } else {
                return other.get();
            }
        }

        //    public T orElseNull() {
        //        return isPresent() ? value : null;
        //    }

        /**
         * Or else throw.
         *
         * @return the t
         * @throws NoSuchElementException the no such element exception
         */
        public T orElseThrow() throws NoSuchElementException {
            if (isPresent()) {
                return value;
            } else {
                throw new NoSuchElementException("No value is present");
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the t
         * @throws X the x
         */
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            if (isPresent()) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Stream.
         *
         * @return the stream
         */
        public Stream<T> stream() {
            if (isPresent()) {
                return Stream.of(value);
            } else {
                return Stream.<T> empty();
            }
        }

        /**
         * To list.
         *
         * @return the list
         */
        public List<T> toList() {
            if (isPresent()) {
                return N.asList(value);
            } else {
                return new ArrayList<>();
            }
        }

        /**
         * To set.
         *
         * @return the sets the
         */
        public Set<T> toSet() {
            if (isPresent()) {
                return N.asSet(value);
            } else {
                return new HashSet<>();
            }
        }

        /**
         * To immutable list.
         *
         * @return the immutable list
         */
        public ImmutableList<T> toImmutableList() {
            if (isPresent()) {
                return ImmutableList.of(value);
            } else {
                return ImmutableList.<T> empty();
            }
        }

        /**
         * To immutable set.
         *
         * @return the immutable set
         */
        public ImmutableSet<T> toImmutableSet() {
            if (isPresent()) {
                return ImmutableSet.of(value);
            } else {
                return ImmutableSet.<T> empty();
            }
        }

        /**
         * .
         *
         * @return the java.util. optional
         */
        public java.util.Optional<T> __() {
            if (isPresent()) {
                return java.util.Optional.of(value);
            } else {
                return java.util.Optional.empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof Optional) {
                final Optional<?> other = (Optional<?>) obj;

                return N.equals(value, other.value);
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent()) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent()) {
                return String.format("Optional[%s]", N.toString(value));
            }

            return "Optional.empty";
        }
    }

    /**
     * The Class OptionalBoolean.
     */
    public static final class OptionalBoolean implements Comparable<OptionalBoolean> {

        /** The Constant EMPTY. */
        private static final OptionalBoolean EMPTY = new OptionalBoolean();

        /** The Constant TRUE. */
        private static final OptionalBoolean TRUE = new OptionalBoolean(true);

        /** The Constant FALSE. */
        private static final OptionalBoolean FALSE = new OptionalBoolean(false);

        /** The value. */
        private final boolean value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional boolean.
         */
        private OptionalBoolean() {
            this.value = false;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional boolean.
         *
         * @param value the value
         */
        private OptionalBoolean(boolean value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional boolean
         */
        public static OptionalBoolean empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional boolean
         */
        public static OptionalBoolean of(boolean value) {
            return value ? TRUE : FALSE;
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional boolean
         */
        public static OptionalBoolean ofNullable(Boolean val) {
            if (val == null) {
                return empty();
            } else {
                return of(val);
            }
        }

        /**
         * Gets the.
         *
         * @return true, if successful
         * @throws NoSuchElementException the no such element exception
         */
        public boolean get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean ifPresent(Try.BooleanConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional boolean
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalBoolean ifPresentOrElse(Try.BooleanConsumer<E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean filter(Try.BooleanPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean map(final Try.BooleanUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalBoolean.of(mapper.applyAsBoolean(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.BooleanFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean flatMap(Try.BooleanFunction<OptionalBoolean, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean or(Try.Supplier<OptionalBoolean, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or false.
         *
         * @return true, if successful
         */
        public boolean orFalse() {
            return isPresent ? value : false;
        }

        //    public boolean orElseFalse() {
        //        return isPresent ? value : false;
        //    }

        /**
         * Or true.
         *
         * @return true, if successful
         */
        public boolean orTrue() {
            return isPresent ? value : true;
        }

        //    public boolean orElseTrue() {
        //        return isPresent ? value : true;
        //    }

        /**
         * Or else throw.
         *
         * @return true, if successful
         * @throws NoSuchElementException the no such element exception
         */
        public boolean orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return true, if successful
         */
        public boolean orElse(boolean other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return true, if successful
         * @throws E the e
         */
        public <E extends Exception> boolean orElseGet(Try.BooleanSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsBoolean();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return true, if successful
         * @throws X the x
         */
        public <X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalBoolean optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Boolean.compare(this.get(), optional.get());
        }

        /**
         * Stream.
         *
         * @return the stream
         */
        public Stream<Boolean> stream() {
            if (isPresent) {
                return Stream.of(value);
            } else {
                return Stream.empty();
            }
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Boolean> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Boolean> empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalBoolean) {
                final OptionalBoolean other = (OptionalBoolean) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalBoolean[%s]", value);
            }

            return "OptionalBoolean.empty";
        }
    }

    /**
     * The Class OptionalChar.
     */
    public static final class OptionalChar implements Comparable<OptionalChar> {

        /** The Constant EMPTY. */
        private static final OptionalChar EMPTY = new OptionalChar();

        /** The Constant CHAR_0. */
        private static final OptionalChar CHAR_0 = new OptionalChar(N.CHAR_0);

        /** The value. */
        private final char value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional char.
         */
        private OptionalChar() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional char.
         *
         * @param value the value
         */
        private OptionalChar(char value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional char
         */
        public static OptionalChar empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional char
         */
        public static OptionalChar of(char value) {
            return value == N.CHAR_0 ? CHAR_0 : new OptionalChar(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional char
         */
        public static OptionalChar ofNullable(Character val) {
            if (val == null) {
                return empty();
            } else {
                return of(val);
            }
        }

        /**
         * Gets the.
         *
         * @return the char
         * @throws NoSuchElementException the no such element exception
         */
        public char get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar ifPresent(Try.CharConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent()) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional char
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalChar ifPresentOrElse(Try.CharConsumer<E> action, Try.Runnable<E2> emptyAction) throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar filter(Try.CharPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent() && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar map(final Try.CharUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalChar.of(mapper.applyAsChar(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<Character, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.CharFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar flatMap(Try.CharFunction<OptionalChar, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar or(Try.Supplier<OptionalChar, E> supplier) throws E {
            if (isPresent()) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the char
         */
        public char orZero() {
            return isPresent() ? value : 0;
        }

        //    public char orElseZero() {
        //        return isPresent() ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the char
         * @throws NoSuchElementException the no such element exception
         */
        public char orElseThrow() throws NoSuchElementException {
            if (isPresent()) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the char
         */
        public char orElse(char other) {
            return isPresent() ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the char
         * @throws E the e
         */
        public <E extends Exception> char orElseGet(Try.CharSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent()) {
                return value;
            } else {
                return other.getAsChar();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the char
         * @throws X the x
         */
        public <X extends Throwable> char orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent()) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalChar optional) {
            if (optional == null || optional.isPresent() == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Character.compare(this.get(), optional.get());
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Character> boxed() {
            if (isPresent()) {
                return Optional.of(value);
            } else {
                return Optional.<Character> empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalChar) {
                final OptionalChar other = (OptionalChar) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent()) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent()) {
                return String.format("OptionalChar[%s]", value);
            }

            return "OptionalChar.empty";
        }
    }

    /**
     * The Class OptionalByte.
     */
    public static final class OptionalByte implements Comparable<OptionalByte> {

        /** The Constant EMPTY. */
        private static final OptionalByte EMPTY = new OptionalByte();

        /** The Constant POOL. */
        private static final OptionalByte[] POOL = new OptionalByte[256];

        static {
            for (int i = 0; i < 256; i++) {
                POOL[i] = new OptionalByte((byte) (i - 128));
            }
        }

        /** The value. */
        private final byte value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional byte.
         */
        private OptionalByte() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional byte.
         *
         * @param value the value
         */
        private OptionalByte(byte value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional byte
         */
        public static OptionalByte empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional byte
         */
        public static OptionalByte of(byte value) {
            return POOL[value - Byte.MIN_VALUE];
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional byte
         */
        public static OptionalByte ofNullable(Byte val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalByte.of(val);
            }
        }

        /**
         * Gets the.
         *
         * @return the byte
         * @throws NoSuchElementException the no such element exception
         */
        public byte get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte ifPresent(Try.ByteConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional byte
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalByte ifPresentOrElse(Try.ByteConsumer<E> action, Try.Runnable<E2> emptyAction) throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte filter(Try.BytePredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte map(final Try.ByteUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalByte.of(mapper.applyAsByte(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<Byte, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.ByteFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte flatMap(Try.ByteFunction<OptionalByte, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte or(Try.Supplier<OptionalByte, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the byte
         */
        public byte orZero() {
            return isPresent ? value : 0;
        }

        //    public byte orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the byte
         * @throws NoSuchElementException the no such element exception
         */
        public byte orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the byte
         */
        public byte orElse(byte other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the byte
         * @throws E the e
         */
        public <E extends Exception> byte orElseGet(Try.ByteSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsByte();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the byte
         * @throws X the x
         */
        public <X extends Throwable> byte orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalByte optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Byte.compare(this.get(), optional.get());
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Byte> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Byte> empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalByte) {
                final OptionalByte other = (OptionalByte) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalByte[%s]", value);
            }

            return "OptionalByte.empty";
        }
    }

    /**
     * The Class OptionalShort.
     */
    public static final class OptionalShort implements Comparable<OptionalShort> {

        /** The Constant EMPTY. */
        private static final OptionalShort EMPTY = new OptionalShort();

        /** The value. */
        private final short value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional short.
         */
        private OptionalShort() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional short.
         *
         * @param value the value
         */
        private OptionalShort(short value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional short
         */
        public static OptionalShort empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional short
         */
        public static OptionalShort of(short value) {
            return new OptionalShort(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional short
         */
        public static OptionalShort ofNullable(Short val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalShort.of(val);
            }
        }

        /**
         * Gets the.
         *
         * @return the short
         * @throws NoSuchElementException the no such element exception
         */
        public short get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort ifPresent(Try.ShortConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional short
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalShort ifPresentOrElse(Try.ShortConsumer<E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort filter(Try.ShortPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort map(final Try.ShortUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalShort.of(mapper.applyAsShort(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<Short, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.ShortFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort flatMap(Try.ShortFunction<OptionalShort, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort or(Try.Supplier<OptionalShort, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the short
         */
        public short orZero() {
            return isPresent ? value : 0;
        }

        //    public short orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the short
         * @throws NoSuchElementException the no such element exception
         */
        public short orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the short
         */
        public short orElse(short other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the short
         * @throws E the e
         */
        public <E extends Exception> short orElseGet(Try.ShortSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsShort();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the short
         * @throws X the x
         */
        public <X extends Throwable> short orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalShort optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Short.compare(this.get(), optional.get());
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Short> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Short> empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalShort) {
                final OptionalShort other = (OptionalShort) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalShort[%s]", value);
            }

            return "OptionalShort.empty";
        }
    }

    /**
     * The Class OptionalInt.
     */
    public static final class OptionalInt implements Comparable<OptionalInt> {

        /** The Constant EMPTY. */
        private static final OptionalInt EMPTY = new OptionalInt();

        /** The Constant MIN_CACHED_VALUE. */
        private static final int MIN_CACHED_VALUE = -128;

        /** The Constant MAX_CACHED_VALUE. */
        private static final int MAX_CACHED_VALUE = 1025;

        /** The Constant POOL. */
        private static final OptionalInt[] POOL = new OptionalInt[MAX_CACHED_VALUE - MIN_CACHED_VALUE];

        static {
            for (int i = 0, to = MAX_CACHED_VALUE - MIN_CACHED_VALUE; i < to; i++) {
                POOL[i] = new OptionalInt(i + MIN_CACHED_VALUE);
            }
        }

        /** The value. */
        private final int value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional int.
         */
        private OptionalInt() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional int.
         *
         * @param value the value
         */
        private OptionalInt(int value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional int
         */
        public static OptionalInt empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional int
         */
        public static OptionalInt of(int value) {
            return value >= MIN_CACHED_VALUE && value < MAX_CACHED_VALUE ? POOL[value - MIN_CACHED_VALUE] : new OptionalInt(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional int
         */
        public static OptionalInt ofNullable(Integer val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalInt.of(val);
            }
        }

        /**
         * From.
         *
         * @param op the op
         * @return the optional int
         */
        public static OptionalInt from(java.util.OptionalInt op) {
            if (op.isPresent()) {
                return of(op.getAsInt());
            } else {
                return empty();
            }
        }

        /**
         * Gets the.
         *
         * @return the int
         * @throws NoSuchElementException the no such element exception
         */
        public int get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt ifPresent(Try.IntConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional int
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalInt ifPresentOrElse(Try.IntConsumer<E> action, Try.Runnable<E2> emptyAction) throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt filter(Try.IntPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt map(final Try.IntUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to long.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong mapToLong(final Try.ToLongFunction<Integer, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return OptionalLong.empty();
            }
        }

        /**
         * Map to double.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDouble(final Try.ToDoubleFunction<Integer, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.IntFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt flatMap(Try.IntFunction<OptionalInt, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt or(Try.Supplier<OptionalInt, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the int
         */
        public int orZero() {
            return isPresent ? value : 0;
        }

        //    public int orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the int
         * @throws NoSuchElementException the no such element exception
         */
        public int orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the int
         */
        public int orElse(int other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the int
         * @throws E the e
         */
        public <E extends Exception> int orElseGet(Try.IntSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsInt();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the int
         * @throws X the x
         */
        public <X extends Throwable> int orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalInt optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Integer.compare(this.get(), optional.get());
        }

        /**
         * Stream.
         *
         * @return the int stream
         */
        public IntStream stream() {
            if (isPresent) {
                return IntStream.of(value);
            } else {
                return IntStream.empty();
            }
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Integer> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Integer> empty();
            }
        }

        /**
         * .
         *
         * @return the java.util. optional int
         */
        public java.util.OptionalInt __() {
            if (isPresent) {
                return java.util.OptionalInt.of(value);
            } else {
                return java.util.OptionalInt.empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalInt) {
                final OptionalInt other = (OptionalInt) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalInt[%s]", value);
            }

            return "OptionalInt.empty";
        }
    }

    /**
     * The Class OptionalLong.
     */
    public static final class OptionalLong implements Comparable<OptionalLong> {

        /** The Constant EMPTY. */
        private static final OptionalLong EMPTY = new OptionalLong();

        /** The value. */
        private final long value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional long.
         */
        private OptionalLong() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional long.
         *
         * @param value the value
         */
        private OptionalLong(long value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional long
         */
        public static OptionalLong empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional long
         */
        public static OptionalLong of(long value) {
            return new OptionalLong(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional long
         */
        public static OptionalLong ofNullable(Long val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalLong.of(val);
            }
        }

        /**
         * From.
         *
         * @param op the op
         * @return the optional long
         */
        public static OptionalLong from(java.util.OptionalLong op) {
            if (op.isPresent()) {
                return of(op.getAsLong());
            } else {
                return empty();
            }
        }

        /**
         * Gets the.
         *
         * @return the long
         * @throws NoSuchElementException the no such element exception
         */
        public long get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong ifPresent(Try.LongConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional long
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalLong ifPresentOrElse(Try.LongConsumer<E> action, Try.Runnable<E2> emptyAction) throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong filter(Try.LongPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong map(final Try.LongUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<Long, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to double.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDouble(final Try.ToDoubleFunction<Long, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.LongFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong flatMap(Try.LongFunction<OptionalLong, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong or(Try.Supplier<OptionalLong, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the long
         */
        public long orZero() {
            return isPresent ? value : 0;
        }

        //    public long orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the long
         * @throws NoSuchElementException the no such element exception
         */
        public long orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the long
         */
        public long orElse(long other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the long
         * @throws E the e
         */
        public <E extends Exception> long orElseGet(Try.LongSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsLong();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the long
         * @throws X the x
         */
        public <X extends Throwable> long orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalLong optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Long.compare(this.get(), optional.get());
        }

        /**
         * Stream.
         *
         * @return the long stream
         */
        public LongStream stream() {
            if (isPresent) {
                return LongStream.of(value);
            } else {
                return LongStream.empty();
            }
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Long> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Long> empty();
            }
        }

        /**
         * .
         *
         * @return the java.util. optional long
         */
        public java.util.OptionalLong __() {
            if (isPresent) {
                return java.util.OptionalLong.of(value);
            } else {
                return java.util.OptionalLong.empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalLong) {
                final OptionalLong other = (OptionalLong) obj;

                return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalLong[%s]", value);
            }

            return "OptionalLong.empty";
        }
    }

    /**
     * The Class OptionalFloat.
     */
    public static final class OptionalFloat implements Comparable<OptionalFloat> {

        /** The Constant EMPTY. */
        private static final OptionalFloat EMPTY = new OptionalFloat();

        /** The value. */
        private final float value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional float.
         */
        private OptionalFloat() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional float.
         *
         * @param value the value
         */
        private OptionalFloat(float value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional float
         */
        public static OptionalFloat empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional float
         */
        public static OptionalFloat of(float value) {
            return new OptionalFloat(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional float
         */
        public static OptionalFloat ofNullable(Float val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalFloat.of(val);
            }
        }

        /**
         * Gets the.
         *
         * @return the float
         * @throws NoSuchElementException the no such element exception
         */
        public float get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat ifPresent(Try.FloatConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional float
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalFloat ifPresentOrElse(Try.FloatConsumer<E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat filter(Try.FloatPredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat map(final Try.FloatUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalFloat.of(mapper.applyAsFloat(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to double.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDouble(final Try.ToDoubleFunction<Float, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.FloatFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat flatMap(Try.FloatFunction<OptionalFloat, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat or(Try.Supplier<OptionalFloat, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the float
         */
        public float orZero() {
            return isPresent ? value : 0;
        }

        //    public float orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the float
         * @throws NoSuchElementException the no such element exception
         */
        public float orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the float
         */
        public float orElse(float other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the float
         * @throws E the e
         */
        public <E extends Exception> float orElseGet(Try.FloatSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsFloat();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the float
         * @throws X the x
         */
        public <X extends Throwable> float orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalFloat optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Float.compare(this.get(), optional.get());
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Float> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Float> empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalFloat) {
                final OptionalFloat other = (OptionalFloat) obj;

                return (isPresent && other.isPresent) ? N.equals(value, other.value) : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalFloat[%s]", value);
            }

            return "OptionalFloat.empty";
        }
    }

    /**
     * The Class OptionalDouble.
     */
    public static final class OptionalDouble implements Comparable<OptionalDouble> {

        /** The Constant EMPTY. */
        private static final OptionalDouble EMPTY = new OptionalDouble();

        /** The value. */
        private final double value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new optional double.
         */
        private OptionalDouble() {
            this.value = 0;
            this.isPresent = false;
        }

        /**
         * Instantiates a new optional double.
         *
         * @param value the value
         */
        private OptionalDouble(double value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @return the optional double
         */
        public static OptionalDouble empty() {
            return EMPTY;
        }

        /**
         * Of.
         *
         * @param value the value
         * @return the optional double
         */
        public static OptionalDouble of(double value) {
            return new OptionalDouble(value);
        }

        /**
         * Of nullable.
         *
         * @param val the val
         * @return the optional double
         */
        public static OptionalDouble ofNullable(Double val) {
            if (val == null) {
                return empty();
            } else {
                return OptionalDouble.of(val);
            }
        }

        /**
         * From.
         *
         * @param op the op
         * @return the optional double
         */
        public static OptionalDouble from(java.util.OptionalDouble op) {
            if (op.isPresent()) {
                return of(op.getAsDouble());
            } else {
                return empty();
            }
        }

        /**
         * Gets the.
         *
         * @return the double
         * @throws NoSuchElementException the no such element exception
         */
        public double get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Checks if is present.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble ifPresent(Try.DoubleConsumer<E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return the optional double
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> OptionalDouble ifPresentOrElse(Try.DoubleConsumer<E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble filter(Try.DoublePredicate<E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble map(final Try.DoubleUnaryOperator<E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<Double, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to long.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong mapToLong(final Try.ToLongFunction<Double, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return OptionalLong.empty();
            }
        }

        /**
         * Map to obj.
         *
         * @param <T> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <T, E extends Exception> Nullable<T> mapToObj(final Try.DoubleFunction<T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Nullable.of(mapper.apply(value));
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble flatMap(Try.DoubleFunction<OptionalDouble, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble or(Try.Supplier<OptionalDouble, E> supplier) throws E {
            if (isPresent) {
                return this;
            } else {
                return Objects.requireNonNull(supplier.get());
            }
        }

        /**
         * Or zero.
         *
         * @return the double
         */
        public double orZero() {
            return isPresent ? value : 0;
        }

        //    public double orElseZero() {
        //        return isPresent ? value : 0;
        //    }

        /**
         * Or else throw.
         *
         * @return the double
         * @throws NoSuchElementException the no such element exception
         */
        public double orElseThrow() throws NoSuchElementException {
            if (isPresent) {
                return value;
            } else {
                throw new NoSuchElementException("No value present");
            }
        }

        /**
         * Or else.
         *
         * @param other the other
         * @return the double
         */
        public double orElse(double other) {
            return isPresent ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the double
         * @throws E the e
         */
        public <E extends Exception> double orElseGet(Try.DoubleSupplier<E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent) {
                return value;
            } else {
                return other.getAsDouble();
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the double
         * @throws X the x
         */
        public <X extends Throwable> double orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Compare to.
         *
         * @param optional the optional
         * @return the int
         */
        @Override
        public int compareTo(OptionalDouble optional) {
            if (optional == null || optional.isPresent == false) {
                return isPresent ? 1 : 0;
            }

            if (isPresent == false) {
                return -1;
            }

            return Double.compare(this.get(), optional.get());
        }

        /**
         * Stream.
         *
         * @return the double stream
         */
        public DoubleStream stream() {
            if (isPresent) {
                return DoubleStream.of(value);
            } else {
                return DoubleStream.empty();
            }
        }

        /**
         * Boxed.
         *
         * @return the optional
         */
        public Optional<Double> boxed() {
            if (isPresent) {
                return Optional.of(value);
            } else {
                return Optional.<Double> empty();
            }
        }

        /**
         * .
         *
         * @return the java.util. optional double
         */
        public java.util.OptionalDouble __() {
            if (isPresent) {
                return java.util.OptionalDouble.of(value);
            } else {
                return java.util.OptionalDouble.empty();
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof OptionalDouble) {
                final OptionalDouble other = (OptionalDouble) obj;

                return (isPresent && other.isPresent) ? N.equals(value, other.value) : isPresent == other.isPresent;
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (isPresent) {
                return String.format("OptionalDouble[%s]", value);
            }

            return "OptionalDouble.empty";
        }
    }

    /**
     * The Class Nullable.
     *
     * @param <T> the generic type
     */
    public static final class Nullable<T> {

        /** The Constant EMPTY. */
        private static final Nullable<?> EMPTY = new Nullable<>();

        /** The value. */
        private final T value;

        /** The is present. */
        private final boolean isPresent;

        /**
         * Instantiates a new nullable.
         */
        private Nullable() {
            this.value = null;
            this.isPresent = false;
        }

        /**
         * Instantiates a new nullable.
         *
         * @param value the value
         */
        private Nullable(T value) {
            this.value = value;
            this.isPresent = true;
        }

        /**
         * Empty.
         *
         * @param <T> the generic type
         * @return the nullable
         */
        public static <T> Nullable<T> empty() {
            return (Nullable<T>) EMPTY;
        }

        /**
         * Of.
         *
         * @param <T> the generic type
         * @param value the value
         * @return the nullable
         */
        public static <T> Nullable<T> of(T value) {
            return new Nullable<>(value);
        }

        /**
         * From.
         *
         * @param <T> the generic type
         * @param optional the optional
         * @return the nullable
         */
        public static <T> Nullable<T> from(Optional<T> optional) {
            if (optional.isPresent()) {
                return new Nullable<>(optional.get());
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * From.
         *
         * @param <T> the generic type
         * @param optional the optional
         * @return the nullable
         */
        public static <T> Nullable<T> from(java.util.Optional<T> optional) {
            if (optional.isPresent()) {
                return new Nullable<>(optional.get());
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Gets the.
         *
         * @return the t
         * @throws NoSuchElementException the no such element exception
         */
        public T get() throws NoSuchElementException {
            return orElseThrow();
        }

        /**
         * Returns {@code true} if the value is present, otherwise returns {@code false}.
         *
         * @return true, if is present
         */
        public boolean isPresent() {
            return isPresent;
        }

        /**
         * Returns {@code true} if the value is not present, otherwise returns {@code false}.
         *
         * @return true, if is not present
         */
        public boolean isNotPresent() {
            return isPresent == false;
        }

        /**
         * Returns {@code true} if the value is not present, otherwise returns {@code false}.
         *
         * @return true, if is empty
         * @deprecated replaced by {@link #isNotPresent()}
         */
        @Deprecated
        public boolean isEmpty() {
            return isPresent == false;
        }

        /**
         * Returns {@code true} if the value is not present, or it is present but it's {@code null}, otherwise returns {@code false}.
         *
         * @return true, if is null
         */
        public boolean isNull() {
            return value == null;
        }

        /**
         * Returns {@code true} if the value is present and it's not {@code null}, otherwise returns {@code false}.
         *
         * @return true, if is not null
         */
        public boolean isNotNull() {
            return value != null;
        }

        /**
         * If present.
         *
         * @param <E> the element type
         * @param action the action
         * @return itself
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> ifPresent(Try.Consumer<? super T, E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isPresent()) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If present or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return itself
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> Nullable<T> ifPresentOrElse(Try.Consumer<? super T, E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isPresent()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * If not null.
         *
         * @param <E> the element type
         * @param action the action
         * @return itself
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> ifNotNull(Try.Consumer<? super T, E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isNotNull()) {
                action.accept(value);
            }

            return this;
        }

        /**
         * If not null or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @return itself
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> Nullable<T> ifNotNullOrElse(Try.Consumer<? super T, E> action, Try.Runnable<E2> emptyAction)
                throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isNotNull()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }

            return this;
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the nullable
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> filter(Try.Predicate<? super T, E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isPresent() && predicate.test(value)) {
                return this;
            } else {
                return empty();
            }
        }

        /**
         * Filter if not null.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional
         * @throws E the e
         */
        public <E extends Exception> Optional<T> filterIfNotNull(Try.Predicate<? super T, E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isNotNull() && predicate.test(value)) {
                return Optional.of(value);
            } else {
                return Optional.empty();
            }
        }

        /**
         * Map.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> map(Try.Function<? super T, ? extends U, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Nullable.of((U) mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to boolean.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean mapToBoolean(final Try.ToBooleanFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalBoolean.of(mapper.applyAsBoolean(value));
            } else {
                return OptionalBoolean.empty();
            }
        }

        /**
         * Map to char.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar mapToChar(final Try.ToCharFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalChar.of(mapper.applyAsChar(value));
            } else {
                return OptionalChar.empty();
            }
        }

        /**
         * Map to byte.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte mapToByte(final Try.ToByteFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalByte.of(mapper.applyAsByte(value));
            } else {
                return OptionalByte.empty();
            }
        }

        /**
         * Map to short.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort mapToShort(final Try.ToShortFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalShort.of(mapper.applyAsShort(value));
            } else {
                return OptionalShort.empty();
            }
        }

        /**
         * Map to int.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToInt(final Try.ToIntFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to long.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong mapToLong(final Try.ToLongFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return OptionalLong.empty();
            }
        }

        /**
         * Map to float.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat mapToFloat(final Try.ToFloatFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalFloat.of(mapper.applyAsFloat(value));
            } else {
                return OptionalFloat.empty();
            }
        }

        /**
         * Map to double.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDouble(final Try.ToDoubleFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Map if not null.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> mapIfNotNull(Try.Function<? super T, ? extends U, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return Nullable.of((U) mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Map to boolean if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional boolean
         * @throws E the e
         */
        public <E extends Exception> OptionalBoolean mapToBooleanIfNotNull(final Try.ToBooleanFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalBoolean.of(mapper.applyAsBoolean(value));
            } else {
                return OptionalBoolean.empty();
            }
        }

        /**
         * Map to char if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional char
         * @throws E the e
         */
        public <E extends Exception> OptionalChar mapToCharIfNotNull(final Try.ToCharFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalChar.of(mapper.applyAsChar(value));
            } else {
                return OptionalChar.empty();
            }
        }

        /**
         * Map to byte if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional byte
         * @throws E the e
         */
        public <E extends Exception> OptionalByte mapToByteIfNotNull(final Try.ToByteFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalByte.of(mapper.applyAsByte(value));
            } else {
                return OptionalByte.empty();
            }
        }

        /**
         * Map to short if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional short
         * @throws E the e
         */
        public <E extends Exception> OptionalShort mapToShortIfNotNull(final Try.ToShortFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalShort.of(mapper.applyAsShort(value));
            } else {
                return OptionalShort.empty();
            }
        }

        /**
         * Map to int if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional int
         * @throws E the e
         */
        public <E extends Exception> OptionalInt mapToIntIfNotNull(final Try.ToIntFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalInt.of(mapper.applyAsInt(value));
            } else {
                return OptionalInt.empty();
            }
        }

        /**
         * Map to long if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional long
         * @throws E the e
         */
        public <E extends Exception> OptionalLong mapToLongIfNotNull(final Try.ToLongFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalLong.of(mapper.applyAsLong(value));
            } else {
                return OptionalLong.empty();
            }
        }

        /**
         * Map to float if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional float
         * @throws E the e
         */
        public <E extends Exception> OptionalFloat mapToFloatIfNotNull(final Try.ToFloatFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalFloat.of(mapper.applyAsFloat(value));
            } else {
                return OptionalFloat.empty();
            }
        }

        /**
         * Map to double if not null.
         *
         * @param <E> the element type
         * @param mapper the mapper
         * @return the optional double
         * @throws E the e
         */
        public <E extends Exception> OptionalDouble mapToDoubleIfNotNull(final Try.ToDoubleFunction<? super T, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return OptionalDouble.of(mapper.applyAsDouble(value));
            } else {
                return OptionalDouble.empty();
            }
        }

        /**
         * Flat map.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> flatMap(Try.Function<? super T, Nullable<U>, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isPresent()) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Flat map if not null.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> flatMapIfNotNull(Try.Function<? super T, Nullable<U>, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return Objects.requireNonNull(mapper.apply(value));
            } else {
                return empty();
            }
        }

        /**
         * Or.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the nullable
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> or(Try.Supplier<Nullable<? extends T>, E> supplier) throws E {
            N.checkArgNotNull(supplier, "supplier");

            if (isPresent()) {
                return this;
            } else {
                return Objects.requireNonNull((Nullable<T>) supplier.get());
            }
        }

        /**
         * Or if null.
         *
         * @param <E> the element type
         * @param supplier the supplier
         * @return the nullable
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> orIfNull(Try.Supplier<Nullable<? extends T>, E> supplier) throws E {
            N.checkArgNotNull(supplier, "supplier");

            if (isNotNull()) {
                return this;
            } else {
                return Objects.requireNonNull((Nullable<T>) supplier.get());
            }
        }

        /**
         * Or null.
         *
         * @return the t
         */
        public T orNull() {
            return isPresent() ? value : null;
        }

        //    public T orElseNull() {
        //        return isPresent() ? value : null;
        //    }

        /**
         * Or else.
         *
         * @param other the other
         * @return the t
         */
        public T orElse(T other) {
            return isPresent() ? value : other;
        }

        /**
         * Or else get.
         *
         * @param <E> the element type
         * @param other the other
         * @return the t
         * @throws E the e
         */
        public <E extends Exception> T orElseGet(Try.Supplier<? extends T, E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isPresent()) {
                return value;
            } else {
                return other.get();
            }
        }

        /**
         * Or else throw.
         *
         * @return the t
         * @throws NoSuchElementException the no such element exception
         */
        public T orElseThrow() throws NoSuchElementException {
            if (isPresent()) {
                return value;
            } else {
                throw new NoSuchElementException("No value is present");
            }
        }

        /**
         * Or else throw.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the t
         * @throws X the x
         */
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isPresent()) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Or else if null.
         *
         * @param other the other
         * @return the t
         */
        public T orElseIfNull(T other) {
            return isNotNull() ? value : other;
        }

        /**
         * Or else get if null.
         *
         * @param <E> the element type
         * @param other the other
         * @return the t
         * @throws E the e
         */
        public <E extends Exception> T orElseGetIfNull(Try.Supplier<? extends T, E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isNotNull()) {
                return value;
            } else {
                return other.get();
            }
        }

        /**
         * Or else throw if null.
         *
         * @return the t
         * @throws NoSuchElementException the no such element exception
         */
        public T orElseThrowIfNull() throws NoSuchElementException {
            if (isNotNull()) {
                return value;
            } else {
                throw new NoSuchElementException("No value is present");
            }
        }

        /**
         * Or else throw if null.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the t
         * @throws X the x
         */
        public <X extends Throwable> T orElseThrowIfNull(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isNotNull()) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Stream.
         *
         * @return the stream
         */
        public Stream<T> stream() {
            if (isPresent()) {
                return Stream.of(value);
            } else {
                return Stream.<T> empty();
            }
        }

        /**
         * Stream if not null.
         *
         * @return the stream
         */
        public Stream<T> streamIfNotNull() {
            if (isNotNull()) {
                return Stream.of(value);
            } else {
                return Stream.<T> empty();
            }
        }

        /**
         * To list.
         *
         * @return the list
         */
        public List<T> toList() {
            if (isPresent()) {
                return N.asList(value);
            } else {
                return new ArrayList<>();
            }
        }

        /**
         * To list if not null.
         *
         * @return the list
         */
        public List<T> toListIfNotNull() {
            if (isNotNull()) {
                return N.asList(value);
            } else {
                return new ArrayList<>();
            }
        }

        /**
         * To set.
         *
         * @return the sets the
         */
        public Set<T> toSet() {
            if (isPresent()) {
                return N.asSet(value);
            } else {
                return new HashSet<>();
            }
        }

        /**
         * To set if not null.
         *
         * @return the sets the
         */
        public Set<T> toSetIfNotNull() {
            if (isNotNull()) {
                return N.asSet(value);
            } else {
                return new HashSet<>();
            }
        }

        /**
         * To immutable list.
         *
         * @return the immutable list
         */
        public ImmutableList<T> toImmutableList() {
            if (isPresent()) {
                return ImmutableList.of(value);
            } else {
                return ImmutableList.empty();
            }
        }

        /**
         * To immutable list if not null.
         *
         * @return the immutable list
         */
        public ImmutableList<T> toImmutableListIfNotNull() {
            if (isNotNull()) {
                return ImmutableList.of(value);
            } else {
                return ImmutableList.empty();
            }
        }

        /**
         * To immutable set.
         *
         * @return the immutable set
         */
        public ImmutableSet<T> toImmutableSet() {
            if (isPresent()) {
                return ImmutableSet.of(value);
            } else {
                return ImmutableSet.empty();
            }
        }

        /**
         * To immutable set if not null.
         *
         * @return the immutable set
         */
        public ImmutableSet<T> toImmutableSetIfNotNull() {
            if (isNotNull()) {
                return ImmutableSet.of(value);
            } else {
                return ImmutableSet.empty();
            }
        }

        /**
         * To optional.
         *
         * @return the optional
         */
        public Optional<T> toOptional() {
            if (value == null) {
                return Optional.<T> empty();
            } else {
                return Optional.of(value);
            }
        }

        /**
         * To jdk optional.
         *
         * @return the java.util. optional
         */
        public java.util.Optional<T> toJdkOptional() {
            if (value == null) {
                return java.util.Optional.<T> empty();
            } else {
                return java.util.Optional.of(value);
            }
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof Nullable) {
                final Nullable<?> other = (Nullable<?>) obj;

                return N.equals(isPresent, other.isPresent) && N.equals(value, other.value);
            }

            return false;
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return N.hashCode(isPresent) * 31 + N.hashCode(value);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (value == null) {
                return isPresent ? "Nullable[null]" : "Nullable.empty";
            } else {
                return String.format("Nullable[%s]", N.toString(value));
            }
        }
    }

    /**
     * The Class Holder.
     *
     * @param <T> the generic type
     */
    public static final class Holder<T> extends Reference<T, Holder<T>> {

        /**
         * Instantiates a new holder.
         */
        public Holder() {
            this(null);
        }

        /**
         * Instantiates a new holder.
         *
         * @param value the value
         */
        Holder(T value) {
            super(value);
        }

        /**
         * Of.
         *
         * @param <T> the generic type
         * @param value the value
         * @return the holder
         */
        public static <T> Holder<T> of(T value) {
            return new Holder<>(value);
        }
    }

    /**
     * The Class R.
     *
     * @param <T> the generic type
     */
    public static final class R<T> extends Reference<T, R<T>> {

        /**
         * Instantiates a new r.
         */
        public R() {
            this(null);
        }

        /**
         * Instantiates a new r.
         *
         * @param value the value
         */
        R(T value) {
            super(value);
        }

        /**
         * Of.
         *
         * @param <T> the generic type
         * @param value the value
         * @return the r
         */
        public static <T> R<T> of(T value) {
            return new R<>(value);
        }
    }

    /**
     * The Class Reference.
     *
     * @param <T> the generic type
     * @param <H> the generic type
     */
    static abstract class Reference<T, H extends Reference<T, H>> {

        /** The value. */
        private T value;

        /**
         * Instantiates a new reference.
         */
        protected Reference() {
            this(null);
        }

        /**
         * Instantiates a new reference.
         *
         * @param value the value
         */
        protected Reference(T value) {
            this.value = value;
        }

        /**
         * Value.
         *
         * @return the t
         */
        public T value() {
            return value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         * @deprecated replace by {@link #value()}.
         */
        @Deprecated
        public T getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value the new value
         */
        public void setValue(final T value) {
            this.value = value;
        }

        /**
         * Gets the and set.
         *
         * @param value the value
         * @return the and set
         */
        public T getAndSet(final T value) {
            final T result = this.value;
            this.value = value;
            return result;
        }

        /**
         * Sets the and get.
         *
         * @param value the value
         * @return the t
         */
        public T setAndGet(final T value) {
            this.value = value;
            return this.value;
        }

        /**
         * Gets the and update.
         *
         * @param <E> the element type
         * @param updateFunction the update function
         * @return the and update
         * @throws E the e
         */
        public final <E extends Exception> T getAndUpdate(Try.UnaryOperator<T, E> updateFunction) throws E {
            final T res = value;
            this.value = updateFunction.apply(value);
            return res;
        }

        /**
         * Update and get.
         *
         * @param <E> the element type
         * @param updateFunction the update function
         * @return the t
         * @throws E the e
         */
        public final <E extends Exception> T updateAndGet(Try.UnaryOperator<T, E> updateFunction) throws E {
            this.value = updateFunction.apply(value);
            return value;
        }

        /**
         * Set with the specified new value and returns <code>true</code> if <code>predicate</code> returns true.
         * Otherwise just return <code>false</code> without setting the value to new value.
         *
         * @param <E> the element type
         * @param newValue the new value
         * @param predicate - test the current value.
         * @return true, if successful
         * @throws E the e
         */
        public <E extends Exception> boolean setIf(final T newValue, final Try.Predicate<? super T, E> predicate) throws E {
            if (predicate.test(value)) {
                this.value = newValue;
                return true;
            }

            return false;
        }

        /**
         * Set with the specified new value and returns <code>true</code> if <code>predicate</code> returns true.
         * Otherwise just return <code>false</code> without setting the value to new value.
         *
         * @param <E> the element type
         * @param newValue the new value
         * @param predicate the first parameter is the current value, the second parameter is the new value.
         * @return true, if successful
         * @throws E the e
         */
        public <E extends Exception> boolean setIf(final T newValue, final Try.BiPredicate<? super T, ? super T, E> predicate) throws E {
            if (predicate.test(value, newValue)) {
                this.value = newValue;
                return true;
            }

            return false;
        }

        /**
         * Checks if is null.
         *
         * @return true, if is null
         */
        public boolean isNull() {
            return value == null;
        }

        /**
         * Checks if is not null.
         *
         * @return true, if is not null
         */
        public boolean isNotNull() {
            return value != null;
        }

        /**
         * If not null.
         *
         * @param <E> the element type
         * @param action the action
         * @throws E the e
         */
        public <E extends Exception> void ifNotNull(Try.Consumer<? super T, E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isNotNull()) {
                action.accept(value);
            }
        }

        /**
         * If not null or else.
         *
         * @param <E> the element type
         * @param <E2> the generic type
         * @param action the action
         * @param emptyAction the empty action
         * @throws E the e
         * @throws E2 the e2
         */
        public <E extends Exception, E2 extends Exception> void ifNotNullOrElse(Try.Consumer<? super T, E> action, Try.Runnable<E2> emptyAction) throws E, E2 {
            N.checkArgNotNull(action, "action");
            N.checkArgNotNull(emptyAction, "emptyAction");

            if (isNotNull()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }
        }

        /**
         * Accept.
         *
         * @param <E> the element type
         * @param action the action
         * @throws E the e
         */
        public <E extends Exception> void accept(final Try.Consumer<? super T, E> action) throws E {
            action.accept(value);
        }

        /**
         * Accept if not null.
         *
         * @param <E> the element type
         * @param action the action
         * @throws E the e
         */
        @Deprecated
        public <E extends Exception> void acceptIfNotNull(final Try.Consumer<? super T, E> action) throws E {
            N.checkArgNotNull(action, "action");

            if (isNotNull()) {
                action.accept(value);
            }
        }

        /**
         * Map.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the u
         * @throws E the e
         */
        public <U, E extends Exception> U map(final Try.Function<? super T, ? extends U, E> mapper) throws E {
            return mapper.apply(value);
        }

        /**
         * Map if not null.
         *
         * @param <U> the generic type
         * @param <E> the element type
         * @param mapper the mapper
         * @return the nullable
         * @throws E the e
         */
        public <U, E extends Exception> Nullable<U> mapIfNotNull(final Try.Function<? super T, ? extends U, E> mapper) throws E {
            N.checkArgNotNull(mapper, "mapper");

            if (isNotNull()) {
                return Nullable.of((U) mapper.apply(value));
            } else {
                return Nullable.<U> empty();
            }
        }

        /**
         * Filter.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the nullable
         * @throws E the e
         */
        public <E extends Exception> Nullable<T> filter(final Try.Predicate<? super T, E> predicate) throws E {
            if (predicate.test(value)) {
                return Nullable.of(value);
            } else {
                return Nullable.<T> empty();
            }
        }

        /**
         * Filter if not null.
         *
         * @param <E> the element type
         * @param predicate the predicate
         * @return the optional
         * @throws E the e
         */
        public <E extends Exception> Optional<T> filterIfNotNull(final Try.Predicate<? super T, E> predicate) throws E {
            N.checkArgNotNull(predicate, "predicate");

            if (isNotNull() && predicate.test(value)) {
                return Optional.of(value);
            } else {
                return Optional.<T> empty();
            }
        }

        /**
         * Stream.
         *
         * @return the stream
         */
        public Stream<T> stream() {
            return Stream.of(value);
        }

        /**
         * Stream if not null.
         *
         * @return the stream
         */
        public Stream<T> streamIfNotNull() {
            if (isNotNull()) {
                return Stream.of(value);
            } else {
                return Stream.<T> empty();
            }
        }

        /**
         * Or else if null.
         *
         * @param other the other
         * @return the t
         */
        public T orElseIfNull(T other) {
            return isNotNull() ? value : other;
        }

        /**
         * Or else get if null.
         *
         * @param <E> the element type
         * @param other the other
         * @return the t
         * @throws E the e
         */
        public <E extends Exception> T orElseGetIfNull(Try.Supplier<? extends T, E> other) throws E {
            N.checkArgNotNull(other, "other");

            if (isNotNull()) {
                return value;
            } else {
                return other.get();
            }
        }

        /**
         * Or else throw if null.
         *
         * @param <X> the generic type
         * @param exceptionSupplier the exception supplier
         * @return the t
         * @throws X the x
         */
        public <X extends Throwable> T orElseThrowIfNull(Supplier<? extends X> exceptionSupplier) throws X {
            N.checkArgNotNull(exceptionSupplier, "exceptionSupplier");

            if (isNotNull()) {
                return value;
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Returns a non-empty {@code Nullable} with the {@code value}.
         *
         * @return the nullable
         */
        public Nullable<T> toNullable() {
            return Nullable.of(value);
        }

        /**
         * Returns an {@code Optional} with the {@code value} if {@code value} is not null, otherwise an empty {@code Optional} is returned.
         *
         * @return the optional
         */
        public Optional<T> toOptional() {
            return Optional.ofNullable(value);
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return (value == null) ? 0 : value.hashCode();
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(final Object obj) {
            return this == obj || (obj instanceof Reference && N.equals(((Reference) obj).value, value));
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (value == null) {
                return "Reference[null]";
            } else {
                return String.format("Reference[%s]", N.toString(value));
            }
        }
    }

    //    public static final class t extends u {
    //        private t() {
    //            // utility class
    //        }
    //    }
    //
    //    public static final class m extends u {
    //        private m() {
    //            // utility class
    //        }
    //    }
}