package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Type;

/**
 * A qualified {@code this} or {@code super} expression: {@code Outer.this}/{@code Outer.super}.
 */
public final class QualifiedExpr extends AbstractExpr {

    private final Type qualifier;
    private final Tokens.$KW keyword;

    /**
     * Constructs a new qualified this expression.
     *
     * @param qualifier the enclosing type qualifier
     * @param keyword the qualified keyword
     */
    public QualifiedExpr(final Type qualifier, final Tokens.$KW keyword) {
        this.qualifier = qualifier;
        this.keyword = keyword;
    }

    /**
     * Returns the enclosing type qualifier.
     *
     * @return the qualifier type
     */
    public Type qualifier() {
        return qualifier;
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
        // Outer.this
        writeType(writer, qualifier);
        writer.write(Tokens.$PUNCT.DOT);
        writer.write(keyword);
    }
}
