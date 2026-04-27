package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.common.constraint.Assert;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Represents an intersection type ({@code A & B}), used in cast expressions
 * and type parameter bounds.
 * <p>
 * Intersection types have restricted usage in Java: they may only appear
 * in cast expressions and as upper bounds of type parameters.  Consequently,
 * most type operations are overridden to throw {@link IllegalStateException}.
 * <p>
 * The constructor validates that at least two types are provided and that
 * none of the constituent types are themselves intersection types (no nesting).
 */
public final class IntersectionType extends AbstractType {

    /** The constituent types, stored as an unmodifiable list. */
    private final List<Type> types;

    /**
     * Constructs a new intersection type from the given constituent types.
     *
     * @param types the constituent types (defensively copied to an unmodifiable list)
     * @throws IllegalArgumentException if fewer than two types are provided,
     *                                  or if any of the types is itself an {@link IntersectionType}
     */
    public IntersectionType(final List<Type> types) {
        Assert.checkNotNullParam("types", types);
        if (types.size() < 2) {
            throw new IllegalArgumentException("Intersection types require at least 2 types");
        }
        for (Type type : types) {
            if (type instanceof IntersectionType) {
                throw new IllegalArgumentException("Intersection types cannot be nested");
            }
        }
        this.types = List.copyOf(types);
    }

    /**
     * Returns the constituent types as an unmodifiable list.
     *
     * @return the types forming this intersection (at least two elements)
     */
    public List<Type> types() {
        return types;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot form array types
     */
    @Override
    public Type array() {
        throw new IllegalStateException("Intersection types cannot form array types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have type arguments
     */
    @Override
    public Type typeArg(final List<Type> args) {
        throw new IllegalStateException("Intersection types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be boxed
     */
    @Override
    public Type box() {
        throw new IllegalStateException("Intersection types cannot be boxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be unboxed
     */
    @Override
    public Type unbox() {
        throw new IllegalStateException("Intersection types cannot be unboxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have a single erasure
     */
    @Override
    public Type erasure() {
        throw new IllegalStateException("Intersection types do not have a single erasure");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be used as wildcard bounds
     */
    @Override
    public Type wildcardExtends() {
        throw new IllegalStateException("Intersection types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be used as wildcard bounds
     */
    @Override
    public Type wildcardSuper() {
        throw new IllegalStateException("Intersection types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have nested types
     */
    @Override
    public Type nestedType(final String name) {
        throw new IllegalStateException("Intersection types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have fields
     */
    @Override
    public Var field(final String name) {
        throw new IllegalStateException("Intersection types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have methods
     */
    @Override
    public Expr call(final String name, final List<Expr> args) {
        throw new IllegalStateException("Intersection types do not have methods");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have class literals
     */
    @Override
    public Expr class_() {
        throw new IllegalStateException("Intersection types cannot have class literals");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractExpr.writeList(writer, types, FormatPreferences.Space.AROUND_TYPE_BOUND_AND, Tokens.$PUNCT.AMP, FormatPreferences.Space.AROUND_TYPE_BOUND_AND);
    }
}
