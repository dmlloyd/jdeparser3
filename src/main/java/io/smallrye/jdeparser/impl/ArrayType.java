package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;

/**
 * Represents a Java array type, wrapping an element type.
 * <p>
 * Array types cannot be parameterized with type arguments.  Both
 * {@link #box()} and {@link #unbox()} return {@code this}, since
 * array types are reference types that have no primitive/boxed duality.
 */
public final class ArrayType extends AbstractType {

    /** The element type of this array. */
    private final Type elementType;

    /**
     * Constructs a new array type with the given element type.
     *
     * @param elementType the element type of the array (e.g., {@code int} for {@code int[]})
     */
    public ArrayType(final Type elementType) {
        this.elementType = elementType;
    }

    /**
     * Returns the element type of this array.
     *
     * @return the element type
     */
    public Type elementType() {
        return elementType;
    }

    /**
     * {@return the dimensions of this array type}
     */
    @Override
    public int dimensions() {
        return elementType instanceof ArrayType at ? at.dimensions() + 1 : 1;
    }

    @Override
    public Expr new_(final List<Expr> args) {
        Assert.checkMinimumParameter("args.size()", 1, args.size());
        Assert.checkMaximumParameter("args.size()", dimensions(), args.size());
        return new NewArrayExpr(this, args);
    }

    @Override
    public Expr newArrayInit(final List<Expr> elements) {
        Assert.checkNotNullParam("elements", elements);
        return new ArrayInitExpr(this, (LiteralArrayExpr) Expr.arrayLiteral(elements));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since array types cannot have type arguments
     */
    @Override
    public Type typeArg(final List<Type> args) {
        throw new IllegalStateException("Array types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Array types are already reference types, so boxing returns {@code this}.
     */
    @Override
    public Type box() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Array types do not have a primitive counterpart, so unboxing returns {@code this}.
     */
    @Override
    public Type unbox() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractExpr.writeType(writer, elementType);
        writer.write(Tokens.$BRACKET.OPEN);
        writer.write(Tokens.$BRACKET.CLOSE);
    }
}
