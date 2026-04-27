package io.smallrye.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a boolean literal ({@code true} or {@code false}).
 * <p>
 * Instances are obtained from the {@link #TRUE} and {@link #FALSE} constants;
 * the constructor is private to enforce a fixed set of instances.
 */
public final class BooleanExpr extends AbstractExpr {

    /** The boolean literal {@code true}. */
    public static final BooleanExpr TRUE = new BooleanExpr(true);

    /** The boolean literal {@code false}. */
    public static final BooleanExpr FALSE = new BooleanExpr(false);

    private final boolean value;

    /**
     * Constructs a new boolean literal expression.
     *
     * @param value the boolean value
     */
    private BooleanExpr(final boolean value) {
        this.value = value;
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
     * Returns the boolean value of this literal.
     *
     * @return {@code true} or {@code false}
     */
    public boolean value() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(value ? Tokens.$KW.TRUE : Tokens.$KW.FALSE);
    }
}
