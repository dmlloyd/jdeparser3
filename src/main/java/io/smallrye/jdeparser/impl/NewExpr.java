package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A constructor call expression: {@code new Type(args)}.
 */
public final class NewExpr extends AbstractExpr {

    private final Type type;
    private final List<Expr> args;

    /**
     * Constructs a new constructor call expression.
     *
     * @param type the type being instantiated
     * @param args the constructor argument expressions
     */
    public NewExpr(final Type type, final List<Expr> args) {
        this.type = type;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the type being instantiated.
     *
     * @return the type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the constructor argument expressions.
     *
     * @return an unmodifiable list of arguments
     */
    public List<Expr> args() {
        return args;
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
        // new Type(arg1, arg2)
        writer.write(Tokens.$KW.NEW);
        writeType(writer, type);
        writer.write(FormatPreferences.Space.BEFORE_PAREN_METHOD_CALL);
        writer.write(Tokens.$PAREN.OPEN);
        if (args.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL_EMPTY);
        } else {
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
            writeList(writer, args, FormatPreferences.Space.AFTER_COMMA,
                    FormatPreferences.Wrapping.ARGUMENT_LIST);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
        }
        writer.write(Tokens.$PAREN.CLOSE);
    }
}
