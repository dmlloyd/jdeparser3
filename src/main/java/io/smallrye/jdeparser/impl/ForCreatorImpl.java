package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.ForCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link ForCreator} that collects the for-loop components
 * (initializer, condition, update, body) and writes the complete statement.
 * <p>
 * Writes the form: {@code for (Type name = init; condition; update) &#123; body &#125;}.
 */
public final class ForCreatorImpl extends AbstractCreator implements ForCreator, Writable {

    /** The initializer variable type, or {@code null}. */
    private Type initType;

    /** The initializer variable name. */
    private String initName;

    /** The initializer expression. */
    private Expr initExpr;

    /** The loop condition expression. */
    private Expr condition;

    /** The loop update expression. */
    private Expr update;

    /** The loop body. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new for-loop creator.
     *
     * @param version the source version
     */
    public ForCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public Var init(final Type type, final String name, final Expr init) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("init", init);
        registerUsedType(type);
        this.initType = type;
        this.initName = name;
        this.initExpr = init;
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public void condition(final Expr condition) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        this.condition = condition;
    }

    /** {@inheritDoc} */
    @Override
    public void update(final Expr update) {
        checkActive();
        Assert.checkNotNullParam("update", update);
        this.update = update;
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> body.accept(bc));
        bc.finish();
        this.body = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$KW.FOR);
        writer.write(FormatPreferences.Space.BEFORE_PAREN_FOR);
        writer.write(Tokens.$PAREN.OPEN);
        writer.write(FormatPreferences.Space.WITHIN_PAREN_FOR);
        // init
        if (initType != null) {
            AbstractExpr.writeType(writer, initType);
            writer.sp();
            writer.writeName(initName);
            writer.write(Tokens.$BINOP.ASSIGN);
            AbstractExpr.writeExpr(writer, initExpr);
        }
        writer.write(Tokens.$PUNCT.SEMI);
        writer.write(condition != null
                ? FormatPreferences.Space.AFTER_SEMICOLON
                : FormatPreferences.Space.AFTER_SEMICOLON_EMPTY);
        // condition
        if (condition != null) {
            AbstractExpr.writeExpr(writer, condition);
        }
        writer.write(Tokens.$PUNCT.SEMI);
        writer.write(update != null
                ? FormatPreferences.Space.AFTER_SEMICOLON
                : FormatPreferences.Space.AFTER_SEMICOLON_EMPTY);
        // update
        if (update != null) {
            AbstractExpr.writeExpr(writer, update);
        }
        writer.write(FormatPreferences.Space.WITHIN_PAREN_FOR);
        writer.write(Tokens.$PAREN.CLOSE);
        if (body != null) {
            body.writeStatementBody(writer, FormatPreferences.Space.BEFORE_BRACE_FOR);
        }
        writer.nl();
    }
}
