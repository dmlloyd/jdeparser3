package org.jboss.jdeparser.impl;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JVar;

/**
 * Abstract base class for all variable (assignable location) implementations.
 * <p>
 * Extends {@link AbstractJExpr} and implements {@link JVar}, providing
 * default implementations for all assignment operator methods.  Examples of
 * concrete subclasses include local variable references, field references,
 * and array element accesses.
 */
public abstract non-sealed class AbstractJVar extends AbstractJExpr implements JVar {

    /**
     * Sole constructor for subclasses.
     */
    protected AbstractJVar() {
    }

    // ── Simple assignment ────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr assign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.ASSIGN, value);
    }

    // ── Compound arithmetic assignment ───────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr addAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.PLUS_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr subAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.MINUS_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr mulAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.TIMES_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr divAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.DIV_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr modAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.MOD_ASSIGN, value);
    }

    // ── Compound bitwise assignment ──────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr bitAndAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.AND_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr bitOrAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.OR_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr bitXorAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.XOR_ASSIGN, value);
    }

    // ── Compound shift assignment ────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr shlAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.SHL_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr shrAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.SHR_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr ushrAssign(final JExpr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignJExpr(this, Tokens.$BINOP.USHR_ASSIGN, value);
    }
}
