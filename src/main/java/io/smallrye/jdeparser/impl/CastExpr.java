package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A type cast expression of the form {@code (Type) expr}.
 * <p>
 * Cast expressions have {@link Prec#UNARY} precedence and
 * {@link Assoc#RIGHT right-to-left} associativity.
 */
public final class CastExpr extends AbstractExpr {

    /** The target type of the cast. */
    private final Type type;

    /** The expression being cast. */
    private final Expr operand;

    /**
     * Constructs a new cast expression.
     *
     * @param type the target type (never {@code null})
     * @param operand the expression to cast (never {@code null})
     */
    public CastExpr(final Type type, final Expr operand) {
        this.type = type;
        this.operand = operand;
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
        // (Type) operand
        writer.write(FormatPreferences.Space.BEFORE_PAREN_CAST);
        writer.write(Tokens.$PAREN.OPEN);
        writer.write(FormatPreferences.Space.WITHIN_PAREN_CAST);
        writeType(writer, type);
        writer.write(FormatPreferences.Space.WITHIN_PAREN_CAST);
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.AFTER_CAST);
        writeSubExpr(writer, operand, Prec.UNARY, Assoc.RIGHT, Assoc.RIGHT);
    }

    /**
     * Returns the target type of the cast.
     *
     * @return the cast type (never {@code null})
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the expression being cast.
     *
     * @return the operand (never {@code null})
     */
    public Expr operand() {
        return operand;
    }
}
