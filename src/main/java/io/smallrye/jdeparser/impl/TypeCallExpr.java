package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A static method call on a type: {@code TypeName.method(args)}.
 */
public final class TypeCallExpr extends AbstractExpr {

    private final Type type;
    private final String method;
    private final List<Expr> args;

    /**
     * Constructs a new static method call expression.
     *
     * @param type   the type on which the method is called
     * @param method the method name
     * @param args   the argument expressions
     */
    public TypeCallExpr(final Type type, final String method, final List<Expr> args) {
        this.type = type;
        this.method = method;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the type on which the method is called.
     *
     * @return the target type
     */
    public Type type() {
        return type;
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
        // TypeName.method(arg1, arg2)
        writeType(writer, type);
        writer.write(Tokens.$PUNCT.DOT);
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
