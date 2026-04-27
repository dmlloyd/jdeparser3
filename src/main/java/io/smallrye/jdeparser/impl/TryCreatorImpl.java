package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.TryCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link TryCreator} that collects try-with-resources,
 * catch blocks, and an optional finally block.
 * <p>
 * Writes the form: {@code try ([resources]) &#123; body &#125;
 * catch (ExType name) &#123; ... &#125; finally &#123; ... &#125;}.
 */
public final class TryCreatorImpl extends AbstractCreator implements TryCreator, Writable {

    /** The try-with-resources resource declarations. */
    private final List<Resource> resources = new ArrayList<>();

    /** The try body. */
    private BlockCreatorImpl body;

    /** The catch clauses. */
    private final List<CatchClause> catches = new ArrayList<>();

    /** The optional finally body. */
    private BlockCreatorImpl finallyBody;

    /**
     * Constructs a new try creator.
     *
     * @param version the source version
     */
    public TryCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void with(final Type type, final String name, final Expr init) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("init", init);
        registerUsedType(type);
        resources.add(new Resource(type, name, init));
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
    public void catch_(final Type exceptionType, final String name, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("exceptionType", exceptionType);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("body", body);
        registerUsedType(exceptionType);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> body.accept(bc));
        bc.finish();
        catches.add(new CatchClause(List.of(exceptionType), name, bc));
    }

    /** {@inheritDoc} */
    @Override
    public void catch_(final List<Type> exceptionTypes, final String name, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("exceptionTypes", exceptionTypes);
        Assert.checkNotEmptyParam("exceptionTypes", exceptionTypes);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("body", body);
        for (Type exceptionType : exceptionTypes) {
            registerUsedType(exceptionType);
        }
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> body.accept(bc));
        bc.finish();
        catches.add(new CatchClause(List.copyOf(exceptionTypes), name, bc));
    }

    /** {@inheritDoc} */
    @Override
    public void finally_(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> body.accept(bc));
        bc.finish();
        this.finallyBody = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$KW.TRY);
        // resources
        if (!resources.isEmpty()) {
            writer.write(FormatPreferences.Space.BEFORE_PAREN_TRY);
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_TRY);
            boolean first = true;
            for (Resource r : resources) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.SEMI);
                    writer.write(FormatPreferences.Space.AFTER_SEMICOLON);
                }
                first = false;
                AbstractExpr.writeType(writer, r.type);
                writer.sp();
                writer.writeName(r.name);
                writer.write(Tokens.$BINOP.ASSIGN);
                AbstractExpr.writeExpr(writer, r.init);
            }
            writer.write(FormatPreferences.Space.WITHIN_PAREN_TRY);
            writer.write(Tokens.$PAREN.CLOSE);
        }
        // body
        writer.write(FormatPreferences.Space.BEFORE_BRACE_TRY);
        if (body != null) {
            body.writeBlock(writer);
        }
        // catches
        for (CatchClause c : catches) {
            writer.write(Tokens.$KW.CATCH);
            writer.write(FormatPreferences.Space.BEFORE_PAREN_CATCH);
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_CATCH);
            AbstractExpr.writeList(writer, c.types, FormatPreferences.Space.AROUND_MULTI_CATCH_OR, Tokens.$PUNCT.BAR,
                    FormatPreferences.Space.AROUND_MULTI_CATCH_OR);
            writer.sp();
            writer.writeName(c.name);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_CATCH);
            writer.write(Tokens.$PAREN.CLOSE);
            writer.write(FormatPreferences.Space.BEFORE_BRACE_CATCH);
            c.body.writeBlock(writer);
        }
        // finally
        if (finallyBody != null) {
            writer.write(Tokens.$KW.FINALLY);
            writer.write(FormatPreferences.Space.BEFORE_BRACE_FINALLY);
            finallyBody.writeBlock(writer);
        }
        writer.nl();
    }

    /**
     * A try-with-resources resource declaration.
     *
     * @param type the resource type
     * @param name the resource variable name
     * @param init the initializer expression
     */
    private record Resource(Type type, String name, Expr init) {
    }

    /**
     * A catch clause with exception types, variable name, and body.
     *
     * @param types the exception types (at least one; multiple for multi-catch)
     * @param name the exception variable name
     * @param body the catch body
     */
    private record CatchClause(List<Type> types, String name, BlockCreatorImpl body) {
    }
}
