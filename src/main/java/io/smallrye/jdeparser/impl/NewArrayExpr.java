package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;

/**
 * An array creation expression with dimension expressions: {@code new int[n]}.
 */
public final class NewArrayExpr extends AbstractExpr {

    private final ArrayType arrayType;
    private final List<Expr> sizes;

    /**
     * Constructs a new array creation expression.
     *
     * @param arrayType the array type
     * @param sizes the dimension size expressions
     */
    public NewArrayExpr(final ArrayType arrayType, final List<Expr> sizes) {
        this.arrayType = arrayType;
        this.sizes = List.copyOf(sizes);
    }

    /**
     * Returns the dimension size expressions.
     *
     * @return an unmodifiable list of dimension expressions
     */
    public List<Expr> sizes() {
        return sizes;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.UNARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // new Type[dim1][dim2][][]
        writer.write(Tokens.$KW.NEW);
        writeType(writer, arrayType.elementType());
        int dims = arrayType.dimensions();
        for (int i = 0; i < dims; i++) {
            final Expr dim = sizes.get(i);
            writer.write(Tokens.$BRACKET.OPEN);
            writeExpr(writer, dim);
            writer.write(Tokens.$BRACKET.CLOSE);
            if (i == sizes.size() - 1) {
                for (i++; i < dims; i++) {
                }
                writer.write(Tokens.$BRACKET.OPEN);
                writer.write(Tokens.$BRACKET.CLOSE);
            }
        }
    }
}
