package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A method call expression: {@code receiver.method(args)} or {@code method(args)}.
 * <p>
 * When the receiver is {@code null}, this represents an unqualified method call.
 */
public final class CallExpr extends AbstractExpr {

    private final Expr receiver;
    private final String method;
    private final List<Expr> args;

    /**
     * Constructs a new method call expression.
     *
     * @param receiver the receiver expression, or {@code null} for unqualified calls
     * @param method the method name
     * @param args the argument expressions
     */
    public CallExpr(final Expr receiver, final String method, final List<Expr> args) {
        this.receiver = receiver;
        this.method = method;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the receiver expression.
     *
     * @return the receiver, or {@code null} for unqualified calls
     */
    public Expr receiver() {
        return receiver;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String method() {
        return method;
    }

    /**
     * Returns the argument expressions.
     *
     * @return an unmodifiable list of arguments
     */
    public List<Expr> args() {
        return args;
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
        // receiver.method(arg1, arg2) or method(arg1, arg2)
        if (receiver != null) {
            writeSubExpr(writer, receiver, Prec.POSTFIX, Assoc.LEFT, Assoc.LEFT);
            writer.write(Tokens.$PUNCT.DOT);
        }
        writer.writeName(method);
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
