package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Type;

/**
 * A class literal expression: {@code Type.class}.
 */
public final class ClassLiteralExpr extends AbstractExpr {

    private final Type type;

    /**
     * Constructs a new class literal expression.
     *
     * @param type the type whose class literal to produce
     */
    public ClassLiteralExpr(final Type type) {
        this.type = type;
    }

    /**
     * Returns the type of this class literal.
     *
     * @return the type
     */
    public Type type() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.POSTFIX;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.LEFT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // Type.class
        writeType(writer, type);
        writer.write(Tokens.$PUNCT.DOT);
        writer.write(Tokens.$KW.CLASS);
    }
}
