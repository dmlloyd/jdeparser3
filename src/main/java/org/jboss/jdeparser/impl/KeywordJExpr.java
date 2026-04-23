package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a Java keyword used as an expression
 * ({@code this}, {@code super}, or {@code null}).
 * <p>
 * Instances are obtained from the {@link #THIS}, {@link #SUPER}, and
 * {@link #NULL} constants; the constructor is private to enforce a
 * fixed set of instances.
 */
public final class KeywordJExpr extends AbstractJExpr {

    /** The {@code this} keyword expression. */
    public static final KeywordJExpr THIS = new KeywordJExpr(Tokens.$KW.THIS);

    /** The {@code super} keyword expression. */
    public static final KeywordJExpr SUPER = new KeywordJExpr(Tokens.$KW.SUPER);

    /** The {@code null} literal expression. */
    public static final KeywordJExpr NULL = new KeywordJExpr(Tokens.$KW.NULL);

    /** The keyword token for this expression. */
    private final Tokens.$KW token;

    /**
     * Constructs a new keyword expression.
     *
     * @param token the keyword token (must not be {@code null})
     */
    private KeywordJExpr(final Tokens.$KW token) {
        this.token = token;
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

    /**
     * Returns the keyword token of this expression.
     *
     * @return the keyword token (never {@code null})
     */
    public Tokens.$KW token() {
        return token;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(token);
    }
}
