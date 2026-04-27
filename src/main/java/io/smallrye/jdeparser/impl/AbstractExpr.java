package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.common.constraint.Assert;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Abstract base class for all expression nodes.
 * <p>
 * Provides default implementations for every operator method defined on {@link Expr}.
 * Each method constructs the appropriate concrete expression node, preserving the
 * operator's precedence and associativity so that the formatting engine can later
 * emit the minimum necessary parentheses.
 */
public abstract non-sealed class AbstractExpr implements Expr, Writable {

    /**
     * Sole constructor for subclasses.
     */
    protected AbstractExpr() {
    }

    /**
     * Writes this expression to the given source file writer.
     *
     * @param writer the writer to emit source code to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void write(SourceFileWriter writer) throws IOException;

    /**
     * Writes a sub-expression, inserting parentheses if the sub-expression's
     * precedence requires it relative to the parent operator.
     * <p>
     * Parentheses are added when:
     * <ul>
     *   <li>The sub-expression has lower precedence than the parent</li>
     *   <li>The sub-expression has equal precedence but is on the
     *       non-associative side (e.g., right operand of a left-associative
     *       operator)</li>
     * </ul>
     *
     * @param writer     the source file writer
     * @param sub        the sub-expression to write
     * @param parentPrec the precedence of the enclosing operator
     * @param parentAssoc the associativity of the enclosing operator
     * @param side       which side of the parent this sub-expression is on
     * @throws IOException if an I/O error occurs
     */
    protected static void writeSubExpr(final SourceFileWriter writer, final Expr sub,
                                       final Prec parentPrec, final Assoc parentAssoc,
                                       final Assoc side) throws IOException {
        final AbstractExpr asub = (AbstractExpr) sub;
        final boolean needParens = asub.precedence().ordinal() < parentPrec.ordinal()
            || asub.precedence() == parentPrec && parentAssoc != side;
        if (needParens) {
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_EXPR);
            asub.write(writer);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_EXPR);
            writer.write(Tokens.$PAREN.CLOSE);
        } else {
            asub.write(writer);
        }
    }

    /**
     * Writes an expression, casting to Writable and invoking write().
     *
     * @param writer the source file writer
     * @param expr   the expression to write
     * @throws IOException if an I/O error occurs
     */
    protected static void writeExpr(final SourceFileWriter writer, final Expr expr) throws IOException {
        ((AbstractExpr) expr).write(writer);
    }

    /**
     * Writes a type, casting to Writable and invoking write().
     *
     * @param writer the source file writer
     * @param type   the type to write
     * @throws IOException if an I/O error occurs
     */
    protected static void writeType(final SourceFileWriter writer, final Type type) throws IOException {
        ((AbstractType) type).write(writer);
    }

    /**
     * Writes a comma-separated list of writable items.
     *
     * @param writer the source file writer
     * @param items  the items to write
     * @param space  the spacing rule to apply after each comma
     * @throws IOException if an I/O error occurs
     */
    protected static void writeList(final SourceFileWriter writer, final List<?> items,
                                    final FormatPreferences.Space space) throws IOException {
        writeList(writer, items, Tokens.$PUNCT.COMMA, space);
    }

    /**
     * Writes a comma-separated list of writable items.
     *
     * @param writer the source file writer
     * @param items  the items to write
     * @param token  the token of the delimiter
     * @param space  the spacing rule to apply after each delimiter
     * @throws IOException if an I/O error occurs
     */
    protected static void writeList(final SourceFileWriter writer, final List<?> items, final Token token,
                                    final FormatPreferences.Space space) throws IOException {
        writeList(writer, items, null, token, space);
    }

    /**
     * Writes a comma-separated list of writable items.
     *
     * @param writer      the source file writer
     * @param items       the items to write
     * @param beforeSpace the spacing rule to apply before each delimiter
     * @param token       the token of the delimiter
     * @param afterSpace  the spacing rule to apply after each delimiter
     * @throws IOException if an I/O error occurs
     */
    protected static void writeList(final SourceFileWriter writer, final List<?> items,
                                    final FormatPreferences.Space beforeSpace,
                                    final Token token,
                                    final FormatPreferences.Space afterSpace) throws IOException {
        int size = items.size();
        if (size > 0) {
            ((Writable) items.get(0)).write(writer);
            for (int i = 1; i < size; i++) {
                if (beforeSpace != null) {
                    writer.write(beforeSpace);
                }
                writer.write(token);
                if (afterSpace != null) {
                    writer.write(afterSpace);
                }
                ((Writable) items.get(i)).write(writer);
            }
        }
    }

    /**
     * Writes a comma-separated list of writable items with wrapping support.
     *
     * @param writer   the source file writer
     * @param items    the items to write
     * @param space    the spacing rule to apply after each comma when not wrapping
     * @param wrapping the wrapping context
     * @throws IOException if an I/O error occurs
     */
    protected static void writeList(final SourceFileWriter writer, final List<?> items,
                                    final FormatPreferences.Space space,
                                    final FormatPreferences.Wrapping wrapping) throws IOException {
        writeList(writer, items, null, Tokens.$PUNCT.COMMA, space, wrapping);
    }

    /**
     * Writes a delimiter-separated list of writable items with wrapping support.
     * <p>
     * When the wrapping mode is {@link FormatPreferences.WrappingMode#ALWAYS_WRAP},
     * each element after the first is placed on a new line.  When the mode is
     * {@link FormatPreferences.WrappingMode#WRAP_ONLY_IF_LONG}, elements wrap to
     * a new line when the current column reaches the configured line length.
     * In both wrapping modes, continuation lines are indented by
     * {@link FormatPreferences.Indentation#LINE_CONTINUATION}.
     *
     * @param writer      the source file writer
     * @param items       the items to write
     * @param beforeSpace the spacing rule to apply before each delimiter (may be {@code null})
     * @param token       the delimiter token
     * @param afterSpace  the spacing rule to apply after each delimiter when not wrapping
     * @param wrapping    the wrapping context
     * @throws IOException if an I/O error occurs
     */
    protected static void writeList(final SourceFileWriter writer, final List<?> items,
                                    final FormatPreferences.Space beforeSpace,
                                    final Token token,
                                    final FormatPreferences.Space afterSpace,
                                    final FormatPreferences.Wrapping wrapping) throws IOException {
        int size = items.size();
        if (size == 0) {
            return;
        }
        FormatPreferences.WrappingMode mode = writer.getFormat().getWrapMode(wrapping);
        boolean alwaysWrap = mode == FormatPreferences.WrappingMode.ALWAYS_WRAP;
        boolean wrapIfLong = mode == FormatPreferences.WrappingMode.WRAP_ONLY_IF_LONG;
        if (alwaysWrap || wrapIfLong) {
            writer.pushIndent(FormatPreferences.Indentation.LINE_CONTINUATION);
        }
        ((Writable) items.get(0)).write(writer);
        for (int i = 1; i < size; i++) {
            if (beforeSpace != null) {
                writer.write(beforeSpace);
            }
            writer.write(token);
            if (alwaysWrap || (wrapIfLong && writer.getColumn() >= writer.getLineLength())) {
                writer.nl();
            } else if (afterSpace != null) {
                writer.write(afterSpace);
            }
            ((Writable) items.get(i)).write(writer);
        }
        if (alwaysWrap || wrapIfLong) {
            writer.popIndent(FormatPreferences.Indentation.LINE_CONTINUATION);
        }
    }

    // ── Precedence and associativity ──────────────────────────────────────

    /**
     * Returns the precedence level of this expression.
     *
     * @return the precedence (never {@code null})
     */
    public abstract Prec precedence();

    /**
     * Returns the associativity of this expression.
     *
     * @return the associativity (never {@code null})
     */
    public abstract Assoc associativity();

    // ── Arithmetic ───────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr add(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.PLUS, other, Prec.ADDITIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr sub(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.MINUS, other, Prec.ADDITIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr mul(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.TIMES, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr div(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.DIV, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr mod(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.MOD, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr neg() {
        return new UnaryExpr(Tokens.$UNOP.MINUS, this, true, Prec.UNARY);
    }

    /** {@inheritDoc} */
    @Override
    public Expr pos() {
        return new UnaryExpr(Tokens.$UNOP.PLUS, this, true, Prec.UNARY);
    }

    // ── Bitwise ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr bitAnd(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.BIT_AND, other, Prec.BITWISE_AND, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr bitOr(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.BIT_OR, other, Prec.BITWISE_OR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr bitXor(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.BIT_XOR, other, Prec.BITWISE_XOR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr comp() {
        return new UnaryExpr(Tokens.$UNOP.COMP, this, true, Prec.UNARY);
    }

    // ── Shift ────────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr shl(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.SHL, other, Prec.SHIFT, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr shr(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.SHR, other, Prec.SHIFT, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr ushr(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.USHR, other, Prec.SHIFT, Assoc.LEFT);
    }

    // ── Equality ─────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr eq(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.EQ, other, Prec.EQUALITY, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public Expr ne(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.NE, other, Prec.EQUALITY, Assoc.NONE);
    }

    // ── Relational ───────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr lt(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.LT, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public Expr gt(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.GT, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public Expr le(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.LE, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public Expr ge(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.GE, other, Prec.RELATIONAL, Assoc.NONE);
    }

    // ── Logical ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr and(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.LOGICAL_AND, other, Prec.LOGICAL_AND, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr or(final Expr other) {
        Assert.checkNotNullParam("other", other);
        return new BinaryExpr(this, Tokens.$BINOP.LOGICAL_OR, other, Prec.LOGICAL_OR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public Expr not() {
        return new UnaryExpr(Tokens.$UNOP.NOT, this, true, Prec.UNARY);
    }

    // ── Type operations ──────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr cast(final Type type) {
        Assert.checkNotNullParam("type", type);
        return new CastExpr(type, this);
    }

    /** {@inheritDoc} */
    @Override
    public Expr instanceof_(final Type type) {
        Assert.checkNotNullParam("type", type);
        return new InstanceOfExpr(this, type, null);
    }

    /** {@inheritDoc} */
    @Override
    public Expr instanceof_(final Type type, final String bindingVar) {
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("bindingVar", bindingVar);
        Assert.checkNotEmptyParam("bindingVar", bindingVar);
        return new InstanceOfExpr(this, type, bindingVar);
    }

    // ── Ternary ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr cond(final Expr ifTrue, final Expr ifFalse) {
        Assert.checkNotNullParam("ifTrue", ifTrue);
        Assert.checkNotNullParam("ifFalse", ifFalse);
        return new CondExpr(this, ifTrue, ifFalse);
    }

    // ── Grouping ─────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr paren() {
        return new ParenExpr(this);
    }

    // ── Member access ────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Var field(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new FieldRefVar(this, name);
    }

    // ── Method call ──────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr call(final String name, final List<Expr> args) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("args", args);
        return new CallExpr(this, name, args);
    }

    // ── Array access ─────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Var idx(final Expr index) {
        Assert.checkNotNullParam("index", index);
        return new ArrayLookupVar(this, index);
    }

    // ── Increment / decrement ────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Expr inc() {
        return new UnaryExpr(Tokens.$UNOP.PP, this, false, Prec.POSTFIX);
    }

    /** {@inheritDoc} */
    @Override
    public Expr dec() {
        return new UnaryExpr(Tokens.$UNOP.MM, this, false, Prec.POSTFIX);
    }
}
