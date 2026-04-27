package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.LanguageFeature;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A switch expression used in expression context (Java 14+):
 * {@code switch (selector) &#123; case ... &#125;}.
 * <p>
 * Switch expressions produce a value and can be used anywhere an expression
 * is expected. The cases typically use arrow syntax ({@code ->}) and/or
 * {@code yield} to produce the value.
 *
 * @see LanguageFeature#SWITCH_EXPRESSIONS
 */
public final class SwitchExpr extends AbstractExpr {

    private final Expr selector;
    private final SwitchCreatorImpl cases;

    /**
     * Constructs a new switch expression.
     *
     * @param selector the selector expression
     * @param cases the switch cases creator
     */
    public SwitchExpr(final Expr selector, final SwitchCreatorImpl cases) {
        this.selector = selector;
        this.cases = cases;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#PRIMARY}
     */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#NONE}
     */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // switch (selector) { cases }
        writer.write(Tokens.$KW.SWITCH);
        writer.write(FormatPreferences.Space.BEFORE_PAREN_SWITCH);
        writer.write(Tokens.$PAREN.OPEN);
        writer.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
        writeExpr(writer, selector);
        writer.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_SWITCH);
        cases.writeBlock(writer);
    }
}
