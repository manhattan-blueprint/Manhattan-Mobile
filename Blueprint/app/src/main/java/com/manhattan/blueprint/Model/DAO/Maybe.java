package com.manhattan.blueprint.Model.DAO;

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

    public void ifPresent(Consumer<? super T> consumer){
        if (value != null) consumer.consume(value);
    }
}
