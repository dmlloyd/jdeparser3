package org.jboss.jdeparser.impl;

import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.CaseCreator;

/**
 * Implementation of {@link CaseCreator} for type-pattern switch cases
 * with optional guard conditions.
 */
public final class CaseCreatorImpl extends AbstractCreator implements CaseCreator {

    /** The optional guard expression. */
    private JExpr guard;

    /** The case body. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new case creator.
     *
     * @param version the source version
     */
    public CaseCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void when_(final JExpr guard) {
        checkActive();
        Assert.checkNotNullParam("guard", guard);
        this.guard = guard;
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

    /**
     * Returns the guard expression, if set.
     *
     * @return the guard expression, or {@code null}
     */
    public JExpr guard() {
        return guard;
    }

    /**
     * Returns the case body.
     *
     * @return the body, or {@code null} if not set
     */
    public BlockCreatorImpl body() {
        return body;
    }
}
