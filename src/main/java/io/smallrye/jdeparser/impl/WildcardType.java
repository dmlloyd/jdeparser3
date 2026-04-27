package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;

/**
 * Represents a wildcard type: either unbounded ({@code ?}),
 * upper-bounded ({@code ? extends X}), or lower-bounded ({@code ? super X}).
 * <p>
 * Wildcard types are used exclusively as type arguments and cannot be
 * used directly as types for most operations.  Consequently, most methods
 * inherited from {@link AbstractType} are overridden to throw
 * {@link IllegalStateException}.
 */
public final class WildcardType extends AbstractType {

    /** The singleton unbounded wildcard type ({@code ?}). */
    public static final WildcardType UNBOUNDED = new WildcardType(true, null);

    /**
     * {@code true} for an upper-bounded wildcard ({@code ? extends X})
     * or an unbounded wildcard ({@code ?}),
     * {@code false} for a lower-bounded wildcard ({@code ? super X}).
     */
    private final boolean extends_;

    /** The bound type, or {@code null} for an unbounded wildcard. */
    private final Type bound;

    /**
     * Constructs a new wildcard type.
     *
     * @param extends_ {@code true} for {@code ? extends bound} or unbounded,
     *                 {@code false} for {@code ? super bound}
     * @param bound    the bound type, or {@code null} for an unbounded wildcard
     */
    public WildcardType(final boolean extends_, final Type bound) {
        this.extends_ = extends_;
        this.bound = bound;
    }

    /**
     * Returns whether this is an upper-bounded or unbounded wildcard.
     *
     * @return {@code true} for extends or unbounded, {@code false} for super
     */
    public boolean isExtends() {
        return extends_;
    }

    /**
     * Returns the bound type, or {@code null} for an unbounded wildcard.
     *
     * @return the bound type, or {@code null}
     */
    public Type bound() {
        return bound;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot form array types
     */
    @Override
    public Type array() {
        throw new IllegalStateException("Wildcard types cannot form array types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have type arguments
     */
    @Override
    public Type typeArg(final List<Type> args) {
        throw new IllegalStateException("Wildcard types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be boxed
     */
    @Override
    public Type box() {
        throw new IllegalStateException("Wildcard types cannot be boxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be unboxed
     */
    @Override
    public Type unbox() {
        throw new IllegalStateException("Wildcard types cannot be unboxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have an erasure
     */
    @Override
    public Type erasure() {
        throw new IllegalStateException("Wildcard types do not have an erasure");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be further bounded
     */
    @Override
    public Type wildcardExtends() {
        throw new IllegalStateException("Wildcard types cannot be further bounded");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be further bounded
     */
    @Override
    public Type wildcardSuper() {
        throw new IllegalStateException("Wildcard types cannot be further bounded");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have nested types
     */
    @Override
    public Type nestedType(final String name) {
        throw new IllegalStateException("Wildcard types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have fields
     */
    @Override
    public Var field(final String name) {
        throw new IllegalStateException("Wildcard types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have methods
     */
    @Override
    public Expr call(final String name, final List<Expr> args) {
        throw new IllegalStateException("Wildcard types do not have methods");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have class literals
     */
    @Override
    public Expr class_() {
        throw new IllegalStateException("Wildcard types cannot have class literals");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$PUNCT.Q);
        if (bound != null) {
            // space between ? and the keyword, since $PUNCT is not in $KW's spacing check
            writer.sp();
            if (extends_) {
                writer.write(Tokens.$KW.EXTENDS);
            } else {
                writer.write(Tokens.$KW.SUPER);
            }
            AbstractExpr.writeType(writer, bound);
        }
    }
}
