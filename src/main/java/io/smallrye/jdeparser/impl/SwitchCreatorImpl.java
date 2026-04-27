package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.CaseCreator;
import io.smallrye.jdeparser.creator.SwitchCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link SwitchCreator} that collects switch cases
 * and writes the complete switch block.
 */
public final class SwitchCreatorImpl extends AbstractCreator implements SwitchCreator {

    /** The collected case entries. */
    private final List<Writable> cases = new ArrayList<>();

    /**
     * Constructs a new switch creator.
     *
     * @param version the source version
     */
    public SwitchCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final Expr value, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("value", value);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeExpr(w, value);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final List<Expr> values, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("values", values);
        Assert.checkNotEmptyParam("values", values);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeList(w, values, FormatPreferences.Space.AFTER_COMMA);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final Type type, final String name, final Consumer<CaseCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final CaseCreatorImpl cc = new CaseCreatorImpl(version());
        cc.sourceFile(sourceFile());
        nest(() -> builder.accept(cc));
        cc.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            if (cc.guard() != null) {
                w.write(Tokens.$KW.WHEN);
                AbstractExpr.writeExpr(w, cc.guard());
            }
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            if (cc.body() != null) {
                w.pushIndent(FormatPreferences.Indentation.LINE);
                cc.body().writeBlock(w);
                w.nl();
                w.popIndent(FormatPreferences.Indentation.LINE);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void caseNull(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            w.write(Tokens.$KW.NULL);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void default_(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.DEFAULT);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /**
     * Writes the switch block body (braces enclosing case labels).
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeBlock(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$BRACE.OPEN);
        if (cases.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
            writer.nl();
            writer.pushIndent(FormatPreferences.Indentation.CASE_LABELS);
            for (Writable c : cases) {
                c.write(writer);
            }
            writer.popIndent(FormatPreferences.Indentation.CASE_LABELS);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
