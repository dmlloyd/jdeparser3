package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;

/**
 * Represents a qualified reference (class or interface) type, such as
 * {@code java.lang.String} or {@code com.example.MyClass}.
 * <p>
 * This type supports unboxing: if the qualified name corresponds to one of the
 * standard wrapper classes in {@code java.lang}, {@link #unbox()} returns the
 * matching {@link PrimitiveType} constant.
 */
public final class ReferenceType extends AbstractType {

    /** The {@code java.lang.String} type. */
    public static final ReferenceType STRING = new ReferenceType("java.lang.String");

    /** The {@code java.lang.Object} type. */
    public static final ReferenceType OBJECT = new ReferenceType("java.lang.Object");

    /** The fully qualified class name (e.g., {@code "java.lang.String"}). */
    private final String qualifiedName;

    /**
     * Constructs a new reference type for the given qualified class name.
     *
     * @param qualifiedName the fully qualified class name (e.g., {@code "java.lang.String"})
     */
    public ReferenceType(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    /**
     * Returns the fully qualified class name.
     *
     * @return the qualified name (e.g., {@code "java.lang.String"})
     */
    public String qualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns the simple (unqualified) class name, which is the portion
     * after the last {@code '.'} in the qualified name.
     *
     * @return the simple name (e.g., {@code "String"} for {@code "java.lang.String"})
     */
    public String simpleName() {
        int dot = qualifiedName.lastIndexOf('.');
        return dot < 0 ? qualifiedName : qualifiedName.substring(dot + 1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * A reference type is its own erasure.
     */
    @Override
    public Type erasure() {
        return this;
    }

    @Override
    public Expr this_() {
        return new QualifiedExpr(this, Tokens.$KW.THIS);
    }

    @Override
    public Expr super_() {
        return new QualifiedExpr(this, Tokens.$KW.SUPER);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this type represents a standard wrapper class (e.g., {@code java.lang.Integer}),
     * returns the corresponding {@link PrimitiveType} constant.  Otherwise returns
     * {@code this}.
     */
    @Override
    public Type unbox() {
        return switch (qualifiedName) {
            case "java.lang.Boolean" -> PrimitiveType.BOOLEAN;
            case "java.lang.Byte" -> PrimitiveType.BYTE;
            case "java.lang.Short" -> PrimitiveType.SHORT;
            case "java.lang.Integer" -> PrimitiveType.INT;
            case "java.lang.Long" -> PrimitiveType.LONG;
            case "java.lang.Float" -> PrimitiveType.FLOAT;
            case "java.lang.Double" -> PrimitiveType.DOUBLE;
            case "java.lang.Character" -> PrimitiveType.CHAR;
            default -> this;
        };
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.writeClass(qualifiedName);
    }
}
