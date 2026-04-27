package io.smallrye.jdeparser;

import io.smallrye.jdeparser.impl.AbstractVar;

/**
 * An expression that represents an assignable variable reference, such as a local variable, parameter,
 * field reference, or array element access.
 * <p>
 * This interface extends {@link Expr} to add compound and simple assignment operations. Each assignment
 * method returns a {@link Expr} representing the assignment expression as a whole.
 */
public sealed interface Var extends Expr permits AbstractVar {

    /**
     * Returns an expression representing a simple assignment to this variable ({@code this = value}).
     *
     * @param value the value to assign
     * @return the assignment expression
     */
    Expr assign(Expr value);

    /**
     * Returns an expression representing an addition assignment to this variable ({@code this += value}).
     *
     * @param value the value to add and assign
     * @return the compound assignment expression
     */
    Expr addAssign(Expr value);

    /**
     * Returns an expression representing a subtraction assignment to this variable ({@code this -= value}).
     *
     * @param value the value to subtract and assign
     * @return the compound assignment expression
     */
    Expr subAssign(Expr value);

    /**
     * Returns an expression representing a multiplication assignment to this variable ({@code this *= value}).
     *
     * @param value the value to multiply and assign
     * @return the compound assignment expression
     */
    Expr mulAssign(Expr value);

    /**
     * Returns an expression representing a division assignment to this variable ({@code this /= value}).
     *
     * @param value the value to divide by and assign
     * @return the compound assignment expression
     */
    Expr divAssign(Expr value);

    /**
     * Returns an expression representing a remainder assignment to this variable ({@code this %= value}).
     *
     * @param value the value to compute the remainder with and assign
     * @return the compound assignment expression
     */
    Expr modAssign(Expr value);

    /**
     * Returns an expression representing a bitwise AND assignment to this variable ({@code this &= value}).
     *
     * @param value the value to bitwise AND with and assign
     * @return the compound assignment expression
     */
    Expr bitAndAssign(Expr value);

    /**
     * Returns an expression representing a bitwise OR assignment to this variable ({@code this |= value}).
     *
     * @param value the value to bitwise OR with and assign
     * @return the compound assignment expression
     */
    Expr bitOrAssign(Expr value);

    /**
     * Returns an expression representing a bitwise XOR assignment to this variable ({@code this ^= value}).
     *
     * @param value the value to bitwise XOR with and assign
     * @return the compound assignment expression
     */
    Expr bitXorAssign(Expr value);

    /**
     * Returns an expression representing a left shift assignment to this variable ({@code this <<= value}).
     *
     * @param value the shift distance to left shift by and assign
     * @return the compound assignment expression
     */
    Expr shlAssign(Expr value);

    /**
     * Returns an expression representing a signed right shift assignment to this variable ({@code this >>= value}).
     *
     * @param value the shift distance to signed right shift by and assign
     * @return the compound assignment expression
     */
    Expr shrAssign(Expr value);

    /**
     * Returns an expression representing an unsigned right shift assignment to this variable ({@code this >>>= value}).
     *
     * @param value the shift distance to unsigned right shift by and assign
     * @return the compound assignment expression
     */
    Expr ushrAssign(Expr value);
}
