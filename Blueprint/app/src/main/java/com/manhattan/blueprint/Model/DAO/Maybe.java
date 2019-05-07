package com.manhattan.blueprint.Model.DAO;

import java.util.function.Function;

// A bridged version of Java 8's Optional type that requires API 24
public final class Maybe<T> {
    public static <T> Maybe<T> of(T value) {
        if (value == null) {
            return Maybe.empty();
        }

        return new Maybe<>(value);
    }

    public static <T> Maybe<T> empty() {
        return new Maybe<>(null);
    }

    private final T value;

    private Maybe(T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        return value;
    }

    public Maybe<T> ifPresent(Consumer<? super T> consumer) {
        if (value != null) {
            consumer.consume(value);
        }
        return Maybe.of(value);
    }

    public Maybe<T> ifNotPresent(Consumer<Void> consumer) {
        if (value == null) {
            consumer.consume(null);
        }
        return Maybe.of(value);
    }

    public T withDefault(T def){
        return isPresent() ? value : def;
    }

    // If value is present, unwrap the result, apply the function, and wrap back in a maybe
    // If value isn't present, do nothing
    public <S> Maybe<S> map(Function<? super T, ? extends S> f) {
        if (isPresent()) {
            return Maybe.of(f.apply(value));
        }
        return Maybe.empty();
    }

    public <S> Maybe<S> bind(Function<? super T, ? extends Maybe<S>> f) {
        if (isPresent()) {
            return f.apply(value);
        }
        return Maybe.empty();
    }
}
