package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * A qualified {@code this} expression: {@code Outer.this}.
 */
public final class QualifiedJExpr extends AbstractJExpr {

    private final JType qualifier;
    private final Tokens.$KW keyword;

    /**
     * Constructs a new qualified this expression.
     *
     * @param qualifier the enclosing type qualifier
     * @param keyword the qualified keyword
     */
    public QualifiedJExpr(final JType qualifier, final Tokens.$KW keyword) {
        this.qualifier = qualifier;
        this.keyword = keyword;
    }

    /**
     * Returns the enclosing type qualifier.
     *
     * @return the qualifier type
     */
    public JType qualifier() {
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
