package com.example.model;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * A generic container that holds items. Supports serialization and comparison.
 *
 * @see String
 * @since 1.0
 * @author JDeparser Test Suite
 * @version 1.0.0
 * @param <T> the element type
 */
public final class Container<T extends Comparable> implements Serializable, Comparable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The default initial capacity.
     */
    public static final int DEFAULT_CAPACITY = 16;

    /**
     * The backing list of items.
     */
    private final List items;

    /**
     * The container name.
     */
    private String name;

    /**
     * Whether the container is locked.
     */
    private volatile boolean locked;

    static {
        // static initializer
        System.setProperty("container.version", "1.0");
    }

    {
        locked = false;
    }

    /**
     * Creates a container with the default capacity.
     */
    public Container() {
        this("unnamed", DEFAULT_CAPACITY);
    }

    /**
     * Creates a container with the given name and capacity.
     *
     * @param name the container name
     * @param capacity the initial capacity
     */
    public Container(String name, int capacity) throws IllegalArgumentException {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.name = java.util.Objects.requireNonNull(name);
        this.items = new ArrayList(capacity);
    }

    /**
     * Creates a container with the given items.
     *
     * @param items the initial items
     */
    @SuppressWarnings("unchecked")
    public Container(T... items) {
        this("unnamed", items.length);
        for (T item : items) {
            this.items.add(item);
        }
    }

    /**
     * Returns the container name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the container name.
     *
     * @throws IllegalStateException if the container is locked
     * @param name the new name
     */
    public void setName(String name) throws IllegalStateException {
        checkNotLocked();
        this.name = name;
    }

    /**
     * Adds an item to the container.
     *
     * @return true if the item was added
     * @param item the item to add
     */
    public synchronized boolean add(T item) {
        checkNotLocked();
        return items.add(item);
    }

    /**
     * Transforms all items using the given function.
     *
     * @return a new list of transformed items
     * @param fn the transformation function
     */
    public <R extends Comparable> List transform(java.util.function.Function fn) {
        List result = new ArrayList(items.size());
        for (T item : items) {
            result.add(fn.apply(item));
        }
        return result;
    }

    /**
     * Creates an empty container.
     *
     * @return a new empty container
     */
    public static <E extends Comparable> Container empty() {
        return new Container();
    }

    /**
     * Checks that the container is not locked.
     *
     * @throws IllegalStateException if the container is locked
     */
    private void checkNotLocked() {
        if (locked) {
            throw new IllegalStateException("container is locked");
        }
    }

    @Override
    public int compareTo(Container other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return "Container{".concat(name).concat("}");
    }

    /**
     * Container lifecycle states.
     */
    public enum State {
        OPEN,
        CLOSED,
        LOCKED;
    }
}
