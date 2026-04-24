package org.jboss.jdeparser;

import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.SwitchCreator;
import org.jboss.jdeparser.impl.AbstractJExpr;
import org.jboss.jdeparser.impl.BlockCreatorImpl;
import org.jboss.jdeparser.impl.BooleanJExpr;
import org.jboss.jdeparser.impl.CallJExpr;
import org.jboss.jdeparser.impl.CharJExpr;
import org.jboss.jdeparser.impl.DoubleJExpr;
import org.jboss.jdeparser.impl.FloatJExpr;
import org.jboss.jdeparser.impl.IntegerJExpr;
import org.jboss.jdeparser.impl.KeywordJExpr;
import org.jboss.jdeparser.impl.LambdaJExpr;
import org.jboss.jdeparser.impl.LiteralArrayJExpr;
import org.jboss.jdeparser.impl.LongJExpr;
import org.jboss.jdeparser.impl.MethodRefJExpr;
import org.jboss.jdeparser.impl.NameJExpr;
import org.jboss.jdeparser.impl.Prec;
import org.jboss.jdeparser.impl.StringJExpr;
import org.jboss.jdeparser.impl.SwitchCreatorImpl;
import org.jboss.jdeparser.impl.SwitchJExpr;
import org.jboss.jdeparser.impl.TextBlockJExpr;
import org.jboss.jdeparser.impl.Tokens;
import org.jboss.jdeparser.impl.UnaryJExpr;

/**
 * The core expression interface for representing Java expressions in generated source code.
 * <p>
 * Expressions are composable values: each method returns a new {@link JExpr} (or {@link JVar})
 * representing the compound expression. Predefined constant expressions are available for
 * common literals and keywords.
 */
public sealed interface JExpr permits JVar, AbstractJExpr {

    /**
     * The boolean literal {@code true}.
     */
    JExpr TRUE = BooleanJExpr.TRUE;

    /**
     * The boolean literal {@code false}.
     */
    JExpr FALSE = BooleanJExpr.FALSE;

    /**
     * The keyword expression {@code this}.
     */
    JExpr THIS = KeywordJExpr.THIS;

    /**
     * The keyword expression {@code super}.
     */
    JExpr SUPER = KeywordJExpr.SUPER;

    /**
     * The null literal {@code null}.
     */
    JExpr NULL = KeywordJExpr.NULL;

    /**
     * The integer literal {@code 0}.
     */
    JExpr ZERO = IntegerJExpr.ZERO;

    /**
     * The integer literal {@code 1}.
     */
    JExpr ONE = IntegerJExpr.ONE;

    /**
     * Creates a decimal {@code int} literal.
     *
     * @param value the integer value
     * @return the literal expression
     */
    static JExpr decimal(final int value) {
        return Integer.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : switch (value) {
            case 0 -> IntegerJExpr.ZERO;
            case 1 -> IntegerJExpr.ONE;
            default -> new IntegerJExpr(value, 10);
        };
    }

    /**
     * Creates a decimal {@code long} literal.
     *
     * @param value the long value
     * @return the literal expression
     */
    static JExpr decimal(final long value) {
        return Long.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : new LongJExpr(value, 10);
    }

    /**
     * Creates a decimal {@code float} literal.
     *
     * @param value the float value
     * @return the literal expression
     */
    static JExpr decimal(final float value) {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new FloatJExpr(value, false);
    }

    /**
     * Creates a decimal {@code double} literal.
     *
     * @param value the double value
     * @return the literal expression
     */
    static JExpr decimal(final double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new DoubleJExpr(value, false);
    }

    /**
     * Creates a hexadecimal {@code int} literal (e.g., {@code 0xFF}).
     * Note that hexadecimal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static JExpr hex(final int value) {
        return new IntegerJExpr(value, 16);
    }

    /**
     * Creates a hexadecimal {@code long} literal (e.g., {@code 0xFFL}).
     * Note that hexadecimal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static JExpr hex(final long value) {
        return new LongJExpr(value, 16);
    }

    /**
     * Creates an octal {@code int} literal (e.g., {@code 077}).
     * Note that octal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static JExpr octal(final int value) {
        return new IntegerJExpr(value, 8);
    }

    /**
     * Creates an octal {@code long} literal (e.g., {@code 077L}).
     * Note that octal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static JExpr octal(final long value) {
        return new LongJExpr(value, 8);
    }

    /**
     * Creates a binary {@code int} literal (e.g., {@code 0b1010}).
     * Note that binary literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static JExpr binary(final int value) {
        return new IntegerJExpr(value, 2);
    }

    /**
     * Creates a binary {@code long} literal (e.g., {@code 0b1010L}).
     * Note that binary literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static JExpr binary(final long value) {
        return new LongJExpr(value, 2);
    }

    /**
     * Creates a hexadecimal {@code float} literal (e.g., {@code 0x1.0p0f}).
     *
     * @param value the float value
     * @return the literal expression
     */
    static JExpr hex(final float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new FloatJExpr(value, true);
    }

    /**
     * Creates a hexadecimal {@code double} literal (e.g., {@code 0x1.0p0}).
     *
     * @param value the double value
     * @return the literal expression
     */
    static JExpr hex(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new DoubleJExpr(value, true);
    }

    /**
     * Creates a {@code String} literal.
     *
     * @param value the string value (will be properly escaped in the output)
     * @return the literal expression
     */
    static JExpr str(final String value) {
        Assert.checkNotNullParam("value", value);
        return new StringJExpr(value);
    }

    /**
     * Creates a text block literal (Java 15+).
     *
     * @param value the text block content
     * @return the literal expression
     */
    static JExpr textBlock(final String value) {
        Assert.checkNotNullParam("value", value);
        return new TextBlockJExpr(value);
    }

    /**
     * Creates a {@code char} literal from a character value.
     *
     * @param c the character value
     * @return the literal expression
     */
    static JExpr ch(final int c) {
        Assert.checkMinimumParameter("c", 0, c);
        Assert.checkMaximumParameter("c", Character.MAX_VALUE, c);
        return new CharJExpr(c);
    }

    /**
     * Creates a variable expression referencing a local variable or parameter by name.
     *
     * @param name the variable name
     * @return the variable expression
     */
    static JVar $v(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new NameJExpr(name);
    }

    /**
     * Creates an unqualified method call: {@code method(args)}.
     *
     * @param name the method name
     * @param args the argument expressions
     * @return the call expression
     */
    static JExpr callPlain(final String name, final JExpr... args) {
        return callPlain(name, List.of(args));
    }

    /**
     * Creates an unqualified method call: {@code method(args)}.
     *
     * @param name the method name
     * @param args the argument expressions as a list
     * @return the call expression
     */
    static JExpr callPlain(final String name, final List<JExpr> args) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("args", args);
        return new CallJExpr(null, name, args);
    }

    /**
     * Creates a method reference on an expression: {@code expr::method}.
     *
     * @param name the method name
     * @return the method reference expression
     */
    default JExpr methodRef(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new MethodRefJExpr(this, name);
    }

    /**
     * Creates a switch expression on this value (Java 14+): {@code switch (thisValue) { /* cases *\u200D/ }}.
     *
     * @param version  the source version for feature validation
     * @param builder  the switch case builder
     * @return the switch expression
     */
    default JExpr switch_(final SourceVersion version, final Consumer<SwitchCreator> builder) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("builder", builder);
        version.require(LanguageFeature.SWITCH_EXPRESSIONS);
        final SwitchCreatorImpl sc = new SwitchCreatorImpl(version);
        builder.accept(sc);
        sc.finish();
        return new SwitchJExpr(this, sc);
    }

    /**
     * Creates an expression-body lambda with a single untyped parameter: {@code x -> expr}.
     *
     * @param param the parameter name
     * @param body  the expression body
     * @return the lambda expression
     */
    static JExpr lambda(final String param, final JExpr body) {
        Assert.checkNotNullParam("param", param);
        Assert.checkNotEmptyParam("param", param);
        Assert.checkNotNullParam("body", body);
        return new LambdaJExpr(List.of(new LambdaJExpr.LambdaParam(param)), body);
    }

    /**
     * Creates an expression-body lambda with multiple untyped parameters: {@code (x, y) -> expr}.
     *
     * @param params the parameter names
     * @param body   the expression body
     * @return the lambda expression
     */
    static JExpr lambda(final List<String> params, final JExpr body) {
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        return new LambdaJExpr(
            params.stream().map(LambdaJExpr.LambdaParam::new).toList(),
            body
        );
    }

    /**
     * Creates a block-body lambda with a single untyped parameter: {@code x -> { /* statements *\u200D/ }}.
     *
     * @param version the source version for feature validation
     * @param param   the parameter name
     * @param body    the block body builder
     * @return the lambda expression
     */
    static JExpr lambda(final SourceVersion version, final String param,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("param", param);
        Assert.checkNotEmptyParam("param", param);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(List.of(new LambdaJExpr.LambdaParam(param)), bc);
    }

    /**
     * Creates a block-body lambda with multiple untyped parameters: {@code (x, y) -> /* statements *\u200D/}.
     *
     * @param version the source version for feature validation
     * @param params  the parameter names
     * @param body    the block body builder
     * @return the lambda expression
     */
    static JExpr lambda(final SourceVersion version, final List<String> params,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(
            params.stream().map(LambdaJExpr.LambdaParam::new).toList(),
            bc
        );
    }

    /**
     * Creates an expression-body lambda with typed parameters: {@code (Type1 x, Type2 y) -> expr}.
     *
     * @param params the typed parameters (name-type pairs)
     * @param body   the expression body
     * @return the lambda expression
     */
    static JExpr lambdaTyped(final List<LambdaJExpr.LambdaParam> params, final JExpr body) {
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        return new LambdaJExpr(params, body);
    }

    /**
     * Creates a block-body lambda with typed parameters: {@code (Type1 x, Type2 y) -> { }/* statements *\u200D/ }}.
     *
     * @param version the source version for feature validation
     * @param params  the typed parameters (name-type pairs)
     * @param body    the block body builder
     * @return the lambda expression
     */
    static JExpr lambdaTyped(final SourceVersion version, final List<LambdaJExpr.LambdaParam> params,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(params, bc);
    }

    // Arithmetic operations

    /**
     * Returns an expression representing the addition of this expression and the given operand ({@code this + operand}).
     *
     * @param operand the right-hand operand
     * @return the addition expression
     */
    JExpr add(JExpr operand);

    /**
     * Returns an expression representing the subtraction of the given operand from this expression ({@code this - operand}).
     *
     * @param operand the right-hand operand
     * @return the subtraction expression
     */
    JExpr sub(JExpr operand);

    /**
     * Returns an expression representing the multiplication of this expression and the given operand ({@code this * operand}).
     *
     * @param operand the right-hand operand
     * @return the multiplication expression
     */
    JExpr mul(JExpr operand);

    /**
     * Returns an expression representing the division of this expression by the given operand ({@code this / operand}).
     *
     * @param operand the right-hand operand
     * @return the division expression
     */
    JExpr div(JExpr operand);

    /**
     * Returns an expression representing the remainder of this expression divided by the given operand ({@code this % operand}).
     *
     * @param operand the right-hand operand
     * @return the remainder expression
     */
    JExpr mod(JExpr operand);

    /**
     * Returns an expression representing the arithmetic negation of this expression ({@code -this}).
     *
     * @return the negation expression
     */
    JExpr neg();

    /**
     * Returns an expression representing the unary plus of this expression ({@code +this}).
     *
     * @return the unary plus expression
     */
    JExpr pos();

    // Bitwise operations

    /**
     * Returns an expression representing the bitwise AND of this expression and the given operand ({@code this & operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise AND expression
     */
    JExpr bitAnd(JExpr operand);

    /**
     * Returns an expression representing the bitwise OR of this expression and the given operand ({@code this | operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise OR expression
     */
    JExpr bitOr(JExpr operand);

    /**
     * Returns an expression representing the bitwise XOR of this expression and the given operand ({@code this ^ operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise XOR expression
     */
    JExpr bitXor(JExpr operand);

    /**
     * Returns an expression representing the bitwise complement of this expression ({@code ~this}).
     *
     * @return the bitwise complement expression
     */
    JExpr comp();

    // Shift operations

    /**
     * Returns an expression representing the left shift of this expression by the given operand ({@code this << operand}).
     *
     * @param operand the shift distance
     * @return the left shift expression
     */
    JExpr shl(JExpr operand);

    /**
     * Returns an expression representing the signed right shift of this expression by the given operand ({@code this >> operand}).
     *
     * @param operand the shift distance
     * @return the signed right shift expression
     */
    JExpr shr(JExpr operand);

    /**
     * Returns an expression representing the unsigned right shift of this expression by the given operand ({@code this >>> operand}).
     *
     * @param operand the shift distance
     * @return the unsigned right shift expression
     */
    JExpr ushr(JExpr operand);

    // Relational operations

    /**
     * Returns an expression representing the equality comparison of this expression and the given operand ({@code this == operand}).
     *
     * @param operand the right-hand operand
     * @return the equality expression
     */
    JExpr eq(JExpr operand);

    /**
     * Returns an expression representing the inequality comparison of this expression and the given operand ({@code this != operand}).
     *
     * @param operand the right-hand operand
     * @return the inequality expression
     */
    JExpr ne(JExpr operand);

    /**
     * Returns an expression representing the less-than comparison of this expression and the given operand ({@code this < operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than expression
     */
    JExpr lt(JExpr operand);

    /**
     * Returns an expression representing the greater-than comparison of this expression and the given operand ({@code this > operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than expression
     */
    JExpr gt(JExpr operand);

    /**
     * Returns an expression representing the less-than-or-equal comparison of this expression and the given operand ({@code this <= operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than-or-equal expression
     */
    JExpr le(JExpr operand);

    /**
     * Returns an expression representing the greater-than-or-equal comparison of this expression and the given operand ({@code this >= operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than-or-equal expression
     */
    JExpr ge(JExpr operand);

    // Logical operations

    /**
     * Returns an expression representing the logical AND of this expression and the given operand ({@code this && operand}).
     *
     * @param operand the right-hand operand
     * @return the logical AND expression
     */
    JExpr and(JExpr operand);

    /**
     * Returns an expression representing the logical OR of this expression and the given operand ({@code this || operand}).
     *
     * @param operand the right-hand operand
     * @return the logical OR expression
     */
    JExpr or(JExpr operand);

    /**
     * Returns an expression representing the logical negation of this expression ({@code !this}).
     *
     * @return the logical negation expression
     */
    JExpr not();

    // Cast

    /**
     * Returns an expression representing a type cast of this expression to the given type ({@code (type) this}).
     *
     * @param type the target type to cast to
     * @return the cast expression
     */
    JExpr cast(JType type);

    // Instanceof

    /**
     * Returns an expression representing a plain {@code instanceof} test ({@code this instanceof type}).
     *
     * @param type the type to test against
     * @return the {@code instanceof} expression
     */
    JExpr instanceof_(JType type);

    /**
     * Returns an expression representing a pattern-matching {@code instanceof} test with a binding variable
     * ({@code this instanceof type bindingVar}).
     * <p>
     * This form requires Java 16 or later.
     *
     * @param type the type to test against
     * @param bindingVar the name of the pattern binding variable
     * @return the pattern-matching {@code instanceof} expression
     */
    JExpr instanceof_(JType type, String bindingVar);

    // Ternary

    /**
     * Returns an expression representing the ternary conditional operator ({@code this ? ifTrue : ifFalse}).
     *
     * @param ifTrue the expression to evaluate if this expression is {@code true}
     * @param ifFalse the expression to evaluate if this expression is {@code false}
     * @return the ternary conditional expression
     */
    JExpr cond(JExpr ifTrue, JExpr ifFalse);

    // Grouping

    /**
     * Returns an expression representing this expression enclosed in parentheses ({@code (this)}).
     *
     * @return the parenthesized expression
     */
    JExpr paren();

    // Member access

    /**
     * Returns a variable expression representing an instance field access on this expression ({@code this.name}).
     *
     * @param name the field name
     * @return the field access variable expression
     */
    JVar field(String name);

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the method call expression
     */
    default JExpr call(String name, JExpr... args) {
        return call(name, List.of(args));
    }

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the method call expression
     */
    JExpr call(String name, List<JExpr> args);

    // Arrays

    /**
     * Returns a variable expression representing an array element access on this expression ({@code this[index]}).
     *
     * @param index the index expression
     * @return the array element access variable expression
     */
    JVar idx(JExpr index);

    /**
     * Create a literal array expression, e.g. {@code { 1, 2, 3 }}.
     *
     * @param items the items within the expression, which should all be the same type (must not be {@code null})
     * @return the array literal expression (not {@code null})
     */
    static JExpr arrayLiteral(JExpr... items) {
        Assert.checkNotNullParam("items", items);
        return arrayLiteral(List.of(items));
    }

    /**
     * Create a literal array expression, e.g. {@code { 1, 2, 3 }}.
     *
     * @param items the items within the expression, which should all be the same type (must not be {@code null})
     * @return the array literal expression (not {@code null})
     */
    static JExpr arrayLiteral(List<JExpr> items) {
        Assert.checkNotNullParam("items", items);
        return new LiteralArrayJExpr(items);
    }

    // Increment and decrement

    /**
     * Returns an expression representing the postfix increment of this expression ({@code this++}).
     *
     * @return the postfix increment expression
     */
    JExpr inc();

    /**
     * Returns an expression representing the postfix decrement of this expression ({@code this--}).
     *
     * @return the postfix decrement expression
     */
    JExpr dec();

    /**
     * Returns an expression representing the prefix increment of the given expression ({@code ++expr}).
     *
     * @param expr the expression to increment (must not be {@code null})
     * @return the prefix increment expression
     */
    static JExpr inc(JExpr expr) {
        Assert.checkNotNullParam("expr", expr);
        return new UnaryJExpr(Tokens.$UNOP.PP, expr, true, Prec.UNARY);
    }

    /**
     * Returns an expression representing the prefix decrement of the given expression ({@code --expr}).
     *
     * @param expr the expression to decrement (must not be {@code null})
     * @return the prefix decrement expression
     */
    static JExpr dec(JExpr expr) {
        Assert.checkNotNullParam("expr", expr);
        return new UnaryJExpr(Tokens.$UNOP.MM, expr, true, Prec.UNARY);
    }
}
