package io.smallrye.jdeparser.impl;

import io.smallrye.common.constraint.Assert;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Var;

/**
 * Abstract base class for all variable (assignable location) implementations.
 * <p>
 * Extends {@link AbstractExpr} and implements {@link Var}, providing
 * default implementations for all assignment operator methods.  Examples of
 * concrete subclasses include local variable references, field references,
 * and array element accesses.
 */
public abstract non-sealed class AbstractVar extends AbstractExpr implements Var {

    /**
     * Sole constructor for subclasses.
     */
    protected AbstractVar() {
    }

    // ── Simple assignment ────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr assign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.ASSIGN, value);
    }

    // ── Compound arithmetic assignment ───────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr addAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.PLUS_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr subAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.MINUS_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr mulAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.TIMES_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr divAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.DIV_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr modAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.MOD_ASSIGN, value);
    }

    // ── Compound bitwise assignment ──────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr bitAndAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.AND_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr bitOrAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.OR_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr bitXorAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.XOR_ASSIGN, value);
    }

    // ── Compound shift assignment ────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr shlAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.SHL_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr shrAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.SHR_ASSIGN, value);
    }

    /** {@inheritDoc} */
    @Override
    public Expr ushrAssign(final Expr value) {
        Assert.checkNotNullParam("value", value);
        return new AssignExpr(this, Tokens.$BINOP.USHR_ASSIGN, value);
    }
}
