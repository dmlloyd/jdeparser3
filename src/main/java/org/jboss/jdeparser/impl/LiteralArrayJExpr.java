package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * An array literal: {@code {1, 2, 3}}.
 */
public final class LiteralArrayJExpr extends AbstractJExpr {

    private final List<JExpr> elements;

    /**
     * Construct a new instance.
     *
     * @param elements the literal element expressions
     */
    public LiteralArrayJExpr(final List<JExpr> elements) {
        this.elements = List.copyOf(elements);
    }

    /**
     * Returns the initializer element expressions.
     *
     * @return an unmodifiable list of elements
     */
    public List<JExpr> elements() {
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
