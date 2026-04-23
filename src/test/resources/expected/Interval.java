package com.example.data;

/**
 * An immutable interval between two comparable bounds. The lower bound must be less than or equal to the upper bound.
 *
 * @since 3.0
 * @param lower the lower bound (inclusive)
 * @param upper the upper bound (inclusive)
 */
public record Interval<T extends Comparable>(T lower, T upper) implements Comparable {
    /**
     * Counter for empty interval creation.
     */
    private static int EMPTY_COUNT = 0;

    Interval {
        if (lower.compareTo(upper) > 0) {
            throw new IllegalArgumentException("lower bound must not exceed upper bound");
        }
    }

    /**
     * Checks whether this interval contains the given value.
     *
     * @return {@code true} if the value is within the interval
     * @param value the value to test
     */
    public boolean contains(T value) {
        return lower.compareTo(value) <= 0 && upper.compareTo(value) >= 0;
    }

    /**
     * Checks whether this interval overlaps with another.
     *
     * @return {@code true} if the intervals overlap
     * @param other the other interval
     */
    public boolean overlaps(Interval other) {
        return lower.compareTo(other.upper()) <= 0 && upper.compareTo(other.lower()) >= 0;
    }

    @Override
    public int compareTo(Interval other) {
        int cmp = lower.compareTo(other.lower());
        if (cmp != 0) {
            return cmp;
        }
        return upper.compareTo(other.upper());
    }

    /**
     * Creates an interval from the given bounds.
     *
     * @return a new interval
     */
    public static <C extends Comparable> Interval of(C lower, C upper) {
        return new Interval(lower, upper);
    }

    /**
     * Creates a single-point interval.
     *
     * @return an interval where lower equals upper
     */
    public static <C extends Comparable> Interval singleton(C value) {
        return new Interval(value, value);
    }

    @Override
    public String toString() {
        return "[".concat(lower.toString()).concat("..").concat(upper.toString()).concat("]");
    }
}
