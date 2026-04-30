package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * A lambda expression: {@code (params) -> body}.
 * <p>
 * All lambdas are internally represented with a block body. When the block
 * body consists of a single {@code return expr;} statement and the
 * {@link FormatPreferences.Opt#LAMBDA_ALWAYS_BLOCK_BODY} option is not
 * enabled, the lambda is rendered in expression form ({@code params -> expr}).
 * <p>
 * Parameters may be untyped ({@code x}) or typed ({@code int x}).
 * Typed parameters are backed by a {@link ParamCreatorImpl} that may carry
 * annotations and modifiers. A single untyped parameter omits the parentheses.
 */
public final class LambdaExpr extends AbstractExpr {

    /** The lambda parameters. */
    private final List<LambdaParam> params;

    /** The block body. */
    private final BlockCreatorImpl blockBody;

    /**
     * Constructs a lambda expression with a block body.
     *
     * @param params the lambda parameters
     * @param blockBody the block body creator
     */
    public LambdaExpr(final List<LambdaParam> params, final BlockCreatorImpl blockBody) {
        this.params = List.copyOf(params);
        this.blockBody = blockBody;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#ASSIGNMENT} (lambda has very low precedence)
     */
    @Override
    public Prec precedence() {
        return Prec.ASSIGNMENT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#RIGHT}
     */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // single untyped parameter: no parens needed
        if (params.size() == 1 && params.get(0).paramCreator() == null) {
            writer.writeName(params.get(0).name());
        } else {
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_DECLARATION);
            boolean first = true;
            for (LambdaParam p : params) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.COMMA);
                    writer.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                if (p.paramCreator() != null) {
                    p.paramCreator().write(writer);
                } else {
                    writer.writeName(p.name());
                }
            }
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_DECLARATION);
            writer.write(Tokens.$PAREN.CLOSE);
        }
        writer.write(Tokens.$BINOP.ARROW);
        // check if we can render as expression form
        Expr singleReturn = blockBody.singleReturnExpr();
        if (singleReturn != null
                && !writer.getFormat().hasOption(FormatPreferences.Opt.LAMBDA_ALWAYS_BLOCK_BODY)) {
            writeExpr(writer, singleReturn);
        } else {
            writer.write(FormatPreferences.Space.BEFORE_BRACE_LAMBDA);
            blockBody.writeBlock(writer);
        }
    }

    /**
     * A lambda parameter with an optional typed parameter creator.
     * <p>
     * For untyped parameters, only the {@code name} is set and
     * {@code paramCreator} is {@code null}. For typed parameters,
     * the {@link ParamCreatorImpl} holds the type, annotations,
     * and modifiers.
     *
     * @param name the parameter name
     * @param paramCreator the typed parameter creator, or {@code null} for an inferred-type parameter
     */
    public record LambdaParam(String name, ParamCreatorImpl paramCreator) {

        /**
         * Creates an untyped lambda parameter.
         *
         * @param name the parameter name
         */
        public LambdaParam(final String name) {
            this(name, null);
        }
    }
}
