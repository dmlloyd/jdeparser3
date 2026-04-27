package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * An array literal: {@code {1, 2, 3}}.
 */
public final class LiteralArrayExpr extends AbstractExpr {

    private final List<Expr> elements;

    /**
     * Construct a new instance.
     *
     * @param elements the literal element expressions
     */
    public LiteralArrayExpr(final List<Expr> elements) {
        this.elements = List.copyOf(elements);
    }

    /**
     * Returns the initializer element expressions.
     *
     * @return an unmodifiable list of elements
     */
    public List<Expr> elements() {
        return elements;
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
        // { e1, e2, e3 }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_ARRAY_INIT);
        writer.write(Tokens.$BRACE.OPEN);
        if (elements.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_ARRAY_INIT);
            writeList(writer, elements, FormatPreferences.Space.AFTER_COMMA);
            writer.write(FormatPreferences.Space.WITHIN_BRACES_ARRAY_INIT);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
