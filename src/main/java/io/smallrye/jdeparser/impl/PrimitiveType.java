package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;

/**
 * Represents a Java primitive type ({@code int}, {@code boolean}, {@code void}, etc.).
 * <p>
 * Instances are pre-allocated as static constants and should be accessed via
 * those constants rather than being constructed directly. Most type operations
 * that are only meaningful for reference types (e.g., {@link #typeArg},
 * {@link #nestedType}, {@link #field}) throw {@link IllegalStateException}.
 */
public final class PrimitiveType extends AbstractType {

    /** The {@code void} pseudo-type. */
    public static final PrimitiveType VOID = new PrimitiveType("void", null);

    /** The {@code boolean} primitive type. */
    public static final PrimitiveType BOOLEAN = new PrimitiveType("boolean", "java.lang.Boolean");

    /** The {@code byte} primitive type. */
    public static final PrimitiveType BYTE = new PrimitiveType("byte", "java.lang.Byte");

    /** The {@code short} primitive type. */
    public static final PrimitiveType SHORT = new PrimitiveType("short", "java.lang.Short");

    /** The {@code int} primitive type. */
    public static final PrimitiveType INT = new PrimitiveType("int", "java.lang.Integer");

    /** The {@code long} primitive type. */
    public static final PrimitiveType LONG = new PrimitiveType("long", "java.lang.Long");

    /** The {@code float} primitive type. */
    public static final PrimitiveType FLOAT = new PrimitiveType("float", "java.lang.Float");

    /** The {@code double} primitive type. */
    public static final PrimitiveType DOUBLE = new PrimitiveType("double", "java.lang.Double");

    /** The {@code char} primitive type. */
    public static final PrimitiveType CHAR = new PrimitiveType("char", "java.lang.Character");

    /** The Java keyword for this primitive type (e.g., {@code "int"}). */
    private final String keyword;

    /**
     * The fully qualified name of the corresponding wrapper class,
     * or {@code null} for {@link #VOID} which has no boxed form.
     */
    private final String boxedName;

    /**
     * Constructs a new primitive type representation.
     *
     * @param keyword the Java keyword (e.g., {@code "int"})
     * @param boxedName the qualified name of the wrapper class (e.g., {@code "java.lang.Integer"}),
     *        or {@code null} for {@code void}
     */
    private PrimitiveType(final String keyword, final String boxedName) {
        this.keyword = keyword;
        this.boxedName = boxedName;
    }

    /**
     * Returns the Java keyword for this primitive type.
     *
     * @return the keyword string (e.g., {@code "int"}, {@code "boolean"})
     */
    public String keyword() {
        return keyword;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this primitive has a corresponding wrapper class, returns a new
     * {@link ReferenceType} for that wrapper; otherwise (for {@code void})
     * returns {@code this}.
     */
    @Override
    public Type box() {
        if (boxedName != null) {
            return new ReferenceType(boxedName);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Primitive types are already unboxed, so this returns {@code this}.
     */
    @Override
    public Type unbox() {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot have type arguments
     */
    @Override
    public Type typeArg(final List<Type> args) {
        throw new IllegalStateException("Primitive types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot have nested types
     */
    @Override
    public Type nestedType(final String name) {
        throw new IllegalStateException("Primitive types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot be used as wildcard bounds
     */
    @Override
    public Type wildcardExtends() {
        throw new IllegalStateException("Primitive types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot be used as wildcard bounds
     */
    @Override
    public Type wildcardSuper() {
        throw new IllegalStateException("Primitive types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types do not have fields
     */
    @Override
    public Var field(final String name) {
        throw new IllegalStateException("Primitive types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types do not have methods
     */
    @Override
    public Expr call(final String name, final List<Expr> args) {
        throw new IllegalStateException("Primitive types do not have methods");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.write(switch (keyword) {
            case "void" -> Tokens.$KW.VOID;
            case "boolean" -> Tokens.$KW.BOOLEAN;
            case "byte" -> Tokens.$KW.BYTE;
            case "short" -> Tokens.$KW.SHORT;
            case "int" -> Tokens.$KW.INT;
            case "long" -> Tokens.$KW.LONG;
            case "float" -> Tokens.$KW.FLOAT;
            case "double" -> Tokens.$KW.DOUBLE;
            case "char" -> Tokens.$KW.CHAR;
            default -> throw new AssertionError("Unknown primitive: " + keyword);
        });
    }
}
