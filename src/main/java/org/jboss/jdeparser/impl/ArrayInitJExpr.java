package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * An array creation expression with an initializer: {@code new int[] {1, 2, 3}}.
 */
public final class ArrayInitJExpr extends AbstractJExpr {

    private final JType arrayType;
    private final LiteralArrayJExpr elements;

    /**
     * Construct a new instance.
     *
     * @param arrayType the array type (must not be {@code null})
     * @param elements the array elements as a literal (must not be {@code null})
     */
    public ArrayInitJExpr(final JType arrayType, final LiteralArrayJExpr elements) {
        this.arrayType = arrayType;
        this.elements = elements;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // new Type[] { e1, e2, e3 }
        writer.write(Tokens.$KW.NEW);
        writeType(writer, arrayType);
        writeExpr(writer, elements);
    }
}
