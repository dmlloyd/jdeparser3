package io.smallrye.jdeparser;

import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.SwitchCreator;
import io.smallrye.jdeparser.impl.AbstractExpr;
import io.smallrye.jdeparser.impl.BlockCreatorImpl;
import io.smallrye.jdeparser.impl.BooleanExpr;
import io.smallrye.jdeparser.impl.CallExpr;
import io.smallrye.jdeparser.impl.CharExpr;
import io.smallrye.jdeparser.impl.DoubleExpr;
import io.smallrye.jdeparser.impl.FloatExpr;
import io.smallrye.jdeparser.impl.IntegerExpr;
import io.smallrye.jdeparser.impl.KeywordExpr;
import io.smallrye.jdeparser.impl.LambdaExpr;
import io.smallrye.jdeparser.impl.LiteralArrayExpr;
import io.smallrye.jdeparser.impl.LongExpr;
import io.smallrye.jdeparser.impl.MethodRefExpr;
import io.smallrye.jdeparser.impl.NamedVar;
import io.smallrye.jdeparser.impl.Prec;
import io.smallrye.jdeparser.impl.StringExpr;
import io.smallrye.jdeparser.impl.SwitchCreatorImpl;
import io.smallrye.jdeparser.impl.SwitchExpr;
import io.smallrye.jdeparser.impl.TextBlockExpr;
import io.smallrye.jdeparser.impl.Tokens;
import io.smallrye.jdeparser.impl.UnaryExpr;

/**
 * The core expression interface for representing Java expressions in generated source code.
 * <p>
 * Expressions are composable values: each method returns a new {@link Expr} (or {@link Var})
 * representing the compound expression. Predefined constant expressions are available for
 * common literals and keywords.
 */
public sealed interface Expr permits Var, AbstractExpr {

    /**
     * The boolean literal {@code true}.
     */
    Expr TRUE = BooleanExpr.TRUE;

    /**
     * The boolean literal {@code false}.
     */
    Expr FALSE = BooleanExpr.FALSE;

    /**
     * The keyword expression {@code this}.
     */
    Expr THIS = KeywordExpr.THIS;

    /**
     * The keyword expression {@code super}.
     */
    Expr SUPER = KeywordExpr.SUPER;

    /**
     * The null literal {@code null}.
     */
    Expr NULL = KeywordExpr.NULL;

    /**
     * The integer literal {@code 0}.
     */
    Expr ZERO = IntegerExpr.ZERO;

    /**
     * The integer literal {@code 1}.
     */
    Expr ONE = IntegerExpr.ONE;

    /**
     * Creates a decimal {@code int} literal.
     *
     * @param value the integer value
     * @return the literal expression
     */
    static Expr decimal(final int value) {
        return Integer.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : switch (value) {
            case 0 -> IntegerExpr.ZERO;
            case 1 -> IntegerExpr.ONE;
            default -> new IntegerExpr(value, 10);
        };
    }

    /**
     * Creates a decimal {@code long} literal.
     *
     * @param value the long value
     * @return the literal expression
     */
    static Expr decimal(final long value) {
        return Long.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : new LongExpr(value, 10);
    }

    /**
     * Creates a decimal {@code float} literal.
     *
     * @param value the float value
     * @return the literal expression
     */
    static Expr decimal(final float value) {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new FloatExpr(value, false);
    }

    /**
     * Creates a decimal {@code double} literal.
     *
     * @param value the double value
     * @return the literal expression
     */
    static Expr decimal(final double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new DoubleExpr(value, false);
    }

    /**
     * Creates a hexadecimal {@code int} literal (e.g., {@code 0xFF}).
     * Note that hexadecimal literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static Expr hex(final int value) {
        return new IntegerExpr(value, 16);
    }

    /**
     * Creates a hexadecimal {@code long} literal (e.g., {@code 0xFFL}).
     * Note that hexadecimal literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static Expr hex(final long value) {
        return new LongExpr(value, 16);
    }

    /**
     * Creates an octal {@code int} literal (e.g., {@code 077}).
     * Note that octal literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static Expr octal(final int value) {
        return new IntegerExpr(value, 8);
    }

    /**
     * Creates an octal {@code long} literal (e.g., {@code 077L}).
     * Note that octal literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static Expr octal(final long value) {
        return new LongExpr(value, 8);
    }

    /**
     * Creates a binary {@code int} literal (e.g., {@code 0b1010}).
     * Note that binary literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    static Expr binary(final int value) {
        return new IntegerExpr(value, 2);
    }

    /**
     * Creates a binary {@code long} literal (e.g., {@code 0b1010L}).
     * Note that binary literals are never negative (use with {@link Expr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    static Expr binary(final long value) {
        return new LongExpr(value, 2);
    }

    /**
     * Creates a hexadecimal {@code float} literal (e.g., {@code 0x1.0p0f}).
     *
     * @param value the float value
     * @return the literal expression
     */
    static Expr hex(final float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new FloatExpr(value, true);
    }

    /**
     * Creates a hexadecimal {@code double} literal (e.g., {@code 0x1.0p0}).
     *
     * @param value the double value
     * @return the literal expression
     */
    static Expr hex(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new DoubleExpr(value, true);
    }

    /**
     * Creates a {@code String} literal.
     *
     * @param value the string value (will be properly escaped in the output)
     * @return the literal expression
     */
    static Expr str(final String value) {
        Assert.checkNotNullParam("value", value);
        return new StringExpr(value);
    }

    /**
     * Creates a text block literal (Java 15+).
     *
     * @param value the text block content
     * @return the literal expression
     */
    static Expr textBlock(final String value) {
        Assert.checkNotNullParam("value", value);
        return new TextBlockExpr(value);
    }

    /**
     * Creates a {@code char} literal from a character value.
     *
     * @param c the character value
     * @return the literal expression
     */
    static Expr ch(final int c) {
        Assert.checkMinimumParameter("c", 0, c);
        Assert.checkMaximumParameter("c", Character.MAX_VALUE, c);
        return new CharExpr(c);
    }

    /**
     * Creates a variable expression referencing a local variable or parameter by name.
     *
     * @param name the variable name
     * @return the variable expression
     */
    static Var $v(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new NamedVar(name);
    }

    /**
     * Creates an unqualified method call: {@code method(args)}.
     *
     * @param name the method name
     * @param args the argument expressions
     * @return the call expression
     */
    static Expr callPlain(final String name, final Expr... args) {
        return callPlain(name, List.of(args));
    }

    /**
     * Creates an unqualified method call: {@code method(args)}.
     *
     * @param name the method name
     * @param args the argument expressions as a list
     * @return the call expression
     */
    static Expr callPlain(final String name, final List<Expr> args) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("args", args);
        return new CallExpr(null, name, args);
    }

    /**
     * Creates a method reference on an expression: {@code expr::method}.
     *
     * @param name the method name
     * @return the method reference expression
     */
    default Expr methodRef(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new MethodRefExpr(this, name);
    }

    /**
     * Creates a switch expression on this value (Java 14+): {@code switch (thisValue) { /* cases *\u200D/ }}.
     *
     * @param version  the source version for feature validation
     * @param builder  the switch case builder
     * @return the switch expression
     */
    default Expr switch_(final SourceVersion version, final Consumer<SwitchCreator> builder) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("builder", builder);
        version.require(LanguageFeature.SWITCH_EXPRESSIONS);
        final SwitchCreatorImpl sc = new SwitchCreatorImpl(version);
        builder.accept(sc);
        sc.finish();
        return new SwitchExpr(this, sc);
    }

    /**
     * Creates an expression-body lambda with a single untyped parameter: {@code x -> expr}.
     *
     * @param param the parameter name
     * @param body  the expression body
     * @return the lambda expression
     */
    static Expr lambda(final String param, final Expr body) {
        Assert.checkNotNullParam("param", param);
        Assert.checkNotEmptyParam("param", param);
        Assert.checkNotNullParam("body", body);
        return new LambdaExpr(List.of(new LambdaExpr.LambdaParam(param)), body);
    }

    /**
     * Creates an expression-body lambda with multiple untyped parameters: {@code (x, y) -> expr}.
     *
     * @param params the parameter names
     * @param body   the expression body
     * @return the lambda expression
     */
    static Expr lambda(final List<String> params, final Expr body) {
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        return new LambdaExpr(
            params.stream().map(LambdaExpr.LambdaParam::new).toList(),
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
    static Expr lambda(final SourceVersion version, final String param,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("param", param);
        Assert.checkNotEmptyParam("param", param);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaExpr(List.of(new LambdaExpr.LambdaParam(param)), bc);
    }

    /**
     * Creates a block-body lambda with multiple untyped parameters: {@code (x, y) -> /* statements *\u200D/}.
     *
     * @param version the source version for feature validation
     * @param params  the parameter names
     * @param body    the block body builder
     * @return the lambda expression
     */
    static Expr lambda(final SourceVersion version, final List<String> params,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaExpr(
            params.stream().map(LambdaExpr.LambdaParam::new).toList(),
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
    static Expr lambdaTyped(final List<LambdaExpr.LambdaParam> params, final Expr body) {
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        return new LambdaExpr(params, body);
    }

    /**
     * Creates a block-body lambda with typed parameters: {@code (Type1 x, Type2 y) -> { }/* statements *\u200D/ }}.
     *
     * @param version the source version for feature validation
     * @param params  the typed parameters (name-type pairs)
     * @param body    the block body builder
     * @return the lambda expression
     */
    static Expr lambdaTyped(final SourceVersion version, final List<LambdaExpr.LambdaParam> params,
        final Consumer<BlockCreator> body) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("params", params);
        Assert.checkNotEmptyParam("params", params);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaExpr(params, bc);
    }

    // Arithmetic operations

    /**
     * Returns an expression representing the addition of this expression and the given operand ({@code this + operand}).
     *
     * @param operand the right-hand operand
     * @return the addition expression
     */
    Expr add(Expr operand);

    /**
     * Returns an expression representing the subtraction of the given operand from this expression ({@code this - operand}).
     *
     * @param operand the right-hand operand
     * @return the subtraction expression
     */
    Expr sub(Expr operand);

    /**
     * Returns an expression representing the multiplication of this expression and the given operand ({@code this * operand}).
     *
     * @param operand the right-hand operand
     * @return the multiplication expression
     */
    Expr mul(Expr operand);

    /**
     * Returns an expression representing the division of this expression by the given operand ({@code this / operand}).
     *
     * @param operand the right-hand operand
     * @return the division expression
     */
    Expr div(Expr operand);

    /**
     * Returns an expression representing the remainder of this expression divided by the given operand ({@code this % operand}).
     *
     * @param operand the right-hand operand
     * @return the remainder expression
     */
    Expr mod(Expr operand);

    /**
     * Returns an expression representing the arithmetic negation of this expression ({@code -this}).
     *
     * @return the negation expression
     */
    Expr neg();

    /**
     * Returns an expression representing the unary plus of this expression ({@code +this}).
     *
     * @return the unary plus expression
     */
    Expr pos();

    // Bitwise operations

    /**
     * Returns an expression representing the bitwise AND of this expression and the given operand ({@code this & operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise AND expression
     */
    Expr bitAnd(Expr operand);

    /**
     * Returns an expression representing the bitwise OR of this expression and the given operand ({@code this | operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise OR expression
     */
    Expr bitOr(Expr operand);

    /**
     * Returns an expression representing the bitwise XOR of this expression and the given operand ({@code this ^ operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise XOR expression
     */
    Expr bitXor(Expr operand);

    /**
     * Returns an expression representing the bitwise complement of this expression ({@code ~this}).
     *
     * @return the bitwise complement expression
     */
    Expr comp();

    // Shift operations

    /**
     * Returns an expression representing the left shift of this expression by the given operand ({@code this << operand}).
     *
     * @param operand the shift distance
     * @return the left shift expression
     */
    Expr shl(Expr operand);

    /**
     * Returns an expression representing the signed right shift of this expression by the given operand ({@code this >> operand}).
     *
     * @param operand the shift distance
     * @return the signed right shift expression
     */
    Expr shr(Expr operand);

    /**
     * Returns an expression representing the unsigned right shift of this expression by the given operand ({@code this >>> operand}).
     *
     * @param operand the shift distance
     * @return the unsigned right shift expression
     */
    Expr ushr(Expr operand);

    // Relational operations

    /**
     * Returns an expression representing the equality comparison of this expression and the given operand ({@code this == operand}).
     *
     * @param operand the right-hand operand
     * @return the equality expression
     */
    Expr eq(Expr operand);

    /**
     * Returns an expression representing the inequality comparison of this expression and the given operand ({@code this != operand}).
     *
     * @param operand the right-hand operand
     * @return the inequality expression
     */
    Expr ne(Expr operand);

    /**
     * Returns an expression representing the less-than comparison of this expression and the given operand ({@code this < operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than expression
     */
    Expr lt(Expr operand);

    /**
     * Returns an expression representing the greater-than comparison of this expression and the given operand ({@code this > operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than expression
     */
    Expr gt(Expr operand);

    /**
     * Returns an expression representing the less-than-or-equal comparison of this expression and the given operand ({@code this <= operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than-or-equal expression
     */
    Expr le(Expr operand);

    /**
     * Returns an expression representing the greater-than-or-equal comparison of this expression and the given operand ({@code this >= operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than-or-equal expression
     */
    Expr ge(Expr operand);

    // Logical operations

    /**
     * Returns an expression representing the logical AND of this expression and the given operand ({@code this && operand}).
     *
     * @param operand the right-hand operand
     * @return the logical AND expression
     */
    Expr and(Expr operand);

    /**
     * Returns an expression representing the logical OR of this expression and the given operand ({@code this || operand}).
     *
     * @param operand the right-hand operand
     * @return the logical OR expression
     */
    Expr or(Expr operand);

    /**
     * Returns an expression representing the logical negation of this expression ({@code !this}).
     *
     * @return the logical negation expression
     */
    Expr not();

    // Cast

    /**
     * Returns an expression representing a type cast of this expression to the given type ({@code (type) this}).
     *
     * @param type the target type to cast to
     * @return the cast expression
     */
    Expr cast(Type type);

    // Instanceof

    /**
     * Returns an expression representing a plain {@code instanceof} test ({@code this instanceof type}).
     *
     * @param type the type to test against
     * @return the {@code instanceof} expression
     */
    Expr instanceof_(Type type);

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
    Expr instanceof_(Type type, String bindingVar);

    // Ternary

    /**
     * Returns an expression representing the ternary conditional operator ({@code this ? ifTrue : ifFalse}).
     *
     * @param ifTrue the expression to evaluate if this expression is {@code true}
     * @param ifFalse the expression to evaluate if this expression is {@code false}
     * @return the ternary conditional expression
     */
    Expr cond(Expr ifTrue, Expr ifFalse);

    // Grouping

    /**
     * Returns an expression representing this expression enclosed in parentheses ({@code (this)}).
     *
     * @return the parenthesized expression
     */
    Expr paren();

    // Member access

    /**
     * Returns a variable expression representing an instance field access on this expression ({@code this.name}).
     *
     * @param name the field name
     * @return the field access variable expression
     */
    Var field(String name);

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the method call expression
     */
    default Expr call(String name, Expr... args) {
        return call(name, List.of(args));
    }

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the method call expression
     */
    Expr call(String name, List<Expr> args);

    // Arrays

    /**
     * Returns a variable expression representing an array element access on this expression ({@code this[index]}).
     *
     * @param index the index expression
     * @return the array element access variable expression
     */
    Var idx(Expr index);

    /**
     * Create a literal array expression, e.g. {@code { 1, 2, 3 }}.
     *
     * @param items the items within the expression, which should all be the same type (must not be {@code null})
     * @return the array literal expression (not {@code null})
     */
    static Expr arrayLiteral(Expr... items) {
        Assert.checkNotNullParam("items", items);
        return arrayLiteral(List.of(items));
    }

    /**
     * Create a literal array expression, e.g. {@code { 1, 2, 3 }}.
     *
     * @param items the items within the expression, which should all be the same type (must not be {@code null})
     * @return the array literal expression (not {@code null})
     */
    static Expr arrayLiteral(List<Expr> items) {
        Assert.checkNotNullParam("items", items);
        return new LiteralArrayExpr(items);
    }

    // Increment and decrement

    /**
     * Returns an expression representing the postfix increment of this expression ({@code this++}).
     *
     * @return the postfix increment expression
     */
    Expr inc();

    /**
     * Returns an expression representing the postfix decrement of this expression ({@code this--}).
     *
     * @return the postfix decrement expression
     */
    Expr dec();

    /**
     * Returns an expression representing the prefix increment of the given expression ({@code ++expr}).
     *
     * @param expr the expression to increment (must not be {@code null})
     * @return the prefix increment expression
     */
    static Expr inc(Expr expr) {
        Assert.checkNotNullParam("expr", expr);
        return new UnaryExpr(Tokens.$UNOP.PP, expr, true, Prec.UNARY);
    }

    /**
     * Returns an expression representing the prefix decrement of the given expression ({@code --expr}).
     *
     * @param expr the expression to decrement (must not be {@code null})
     * @return the prefix decrement expression
     */
    static Expr dec(Expr expr) {
        Assert.checkNotNullParam("expr", expr);
        return new UnaryExpr(Tokens.$UNOP.MM, expr, true, Prec.UNARY);
    }
}
