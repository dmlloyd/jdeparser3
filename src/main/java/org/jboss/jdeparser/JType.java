package org.jboss.jdeparser;

import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.smallrye.common.constraint.Assert;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.impl.AbstractJType;
import org.jboss.jdeparser.impl.AnonymousClassJExpr;
import org.jboss.jdeparser.impl.ClassCreatorImpl;
import org.jboss.jdeparser.impl.IntersectionJType;
import org.jboss.jdeparser.impl.MethodRefJExpr;
import org.jboss.jdeparser.impl.NewJExpr;
import org.jboss.jdeparser.impl.PrimitiveJType;
import org.jboss.jdeparser.impl.ReferenceJType;
import org.jboss.jdeparser.impl.WildcardJType;

/**
 * The core type interface for representing Java types in generated source code.
 * <p>
 * Types are composable: methods such as {@link #array()}, {@link #typeArg(JType...)}, and {@link #nestedType(String)}
 * return new {@link JType} instances representing the derived type. Predefined constants are available for
 * all primitive types and for commonly used reference types.
 */
public sealed interface JType permits AbstractJType {

    /**
     * The primitive type {@code void}.
     */
    JType VOID = PrimitiveJType.VOID;

    /**
     * The primitive type {@code boolean}.
     */
    JType BOOLEAN = PrimitiveJType.BOOLEAN;

    /**
     * The primitive type {@code byte}.
     */
    JType BYTE = PrimitiveJType.BYTE;

    /**
     * The primitive type {@code short}.
     */
    JType SHORT = PrimitiveJType.SHORT;

    /**
     * The primitive type {@code int}.
     */
    JType INT = PrimitiveJType.INT;

    /**
     * The primitive type {@code long}.
     */
    JType LONG = PrimitiveJType.LONG;

    /**
     * The primitive type {@code float}.
     */
    JType FLOAT = PrimitiveJType.FLOAT;

    /**
     * The primitive type {@code double}.
     */
    JType DOUBLE = PrimitiveJType.DOUBLE;

    /**
     * The primitive type {@code char}.
     */
    JType CHAR = PrimitiveJType.CHAR;

    /**
     * The reference type {@code java.lang.String}.
     */
    JType STRING = ReferenceJType.STRING;

    /**
     * The reference type {@code java.lang.Object}.
     */
    JType OBJECT = ReferenceJType.OBJECT;

    /**
     * The unbounded wildcard type ({@code ?}).
     */
    JType WILDCARD = WildcardJType.UNBOUNDED;

    /**
     * Creates a type from a {@link Class} object.
     *
     * @param clazz the class (must not be {@code null})
     * @return the corresponding type
     */
    static JType of(final Class<?> clazz) {
        Assert.checkNotNullParam("clazz", clazz);
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return VOID;
            if (clazz == boolean.class) return BOOLEAN;
            if (clazz == byte.class) return BYTE;
            if (clazz == short.class) return SHORT;
            if (clazz == int.class) return INT;
            if (clazz == long.class) return LONG;
            if (clazz == float.class) return FLOAT;
            if (clazz == double.class) return DOUBLE;
            if (clazz == char.class) return CHAR;
            throw new IllegalArgumentException("Unknown primitive type: " + clazz);
        }
        if (clazz.isArray()) {
            return of(clazz.getComponentType()).array();
        }
        return new ReferenceJType(clazz.getCanonicalName());
    }

    /**
     * Creates a type from a fully qualified class name string.
     *
     * @param qualifiedName the fully qualified class name (e.g., {@code "com.example.MyClass"})
     * @return the corresponding type
     */
    static JType named(final String qualifiedName) {
        Assert.checkNotNullParam("qualifiedName", qualifiedName);
        Assert.checkNotEmptyParam("qualifiedName", qualifiedName);
        return new ReferenceJType(qualifiedName);
    }

    /**
     * Creates a type from a {@link TypeMirror}, typically used in annotation processors.
     *
     * @param mirror the type mirror (must not be {@code null})
     * @return the corresponding type
     * @throws IllegalArgumentException if the mirror kind is not supported
     */
    static JType of(final TypeMirror mirror) {
        Assert.checkNotNullParam("mirror", mirror);
        return switch (mirror.getKind()) {
            case VOID, NONE -> VOID;
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case SHORT -> SHORT;
            case INT -> INT;
            case LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            case CHAR -> CHAR;
            case ARRAY -> of(((ArrayType) mirror).getComponentType()).array();
            case DECLARED -> {
                var declared = (DeclaredType) mirror;
                var raw = new ReferenceJType(
                    declared.asElement().toString()
                );
                var typeArgs = declared.getTypeArguments();
                if (typeArgs.isEmpty()) {
                    yield raw;
                }
                yield raw.typeArg(typeArgs.stream().map(JType::of).toArray(JType[]::new));
            }
            default -> throw new IllegalArgumentException("Unsupported type mirror kind: " + mirror.getKind());
        };
    }

    /**
     * Returns a type representing an array of this type ({@code this[]}).
     *
     * @return the array type
     */
    JType array();

    /**
     * Returns a parameterized type by applying the given type arguments to this type
     * (e.g., {@code This<A, B>}).
     *
     * @param args the type arguments to apply
     * @return the parameterized type
     */
    default JType typeArg(JType... args) {
        return typeArg(List.of(args));
    }

    /**
     * Returns a parameterized type by applying the given type arguments to this type
     * (e.g., {@code This<A, B>}).
     *
     * @param args the type arguments to apply as a list
     * @return the parameterized type
     */
    JType typeArg(List<JType> args);

    /**
     * Returns the boxed (wrapper) type corresponding to this primitive type.
     * For example, {@code int} becomes {@code java.lang.Integer}.
     *
     * @return the boxed type
     */
    JType box();

    /**
     * Returns the unboxed (primitive) type corresponding to this wrapper type.
     * For example, {@code java.lang.Integer} becomes {@code int}.
     *
     * @return the unboxed type
     */
    JType unbox();

    /**
     * Returns the type erasure of this type, stripping all type arguments.
     *
     * @return the erased type
     */
    JType erasure();

    /**
     * Returns a wildcard type with an upper bound of this type ({@code ? extends This}).
     *
     * @return the upper-bounded wildcard type
     */
    JType wildcardExtends();

    /**
     * Returns a wildcard type with a lower bound of this type ({@code ? super This}).
     *
     * @return the lower-bounded wildcard type
     */
    JType wildcardSuper();

    /**
     * Returns a nested (inner) type within this type ({@code This.Name}).
     *
     * @param name the simple name of the nested type
     * @return the nested type
     */
    JType nestedType(String name);

    /**
     * Returns a variable expression representing a static field reference on this type ({@code This.name}).
     *
     * @param name the field name
     * @return the static field access variable expression
     */
    JVar field(String name);

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the static method call expression
     */
    default JExpr call(String name, JExpr... args) {
        return call(name, List.of(args));
    }

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the static method call expression
     */
    JExpr call(String name, List<JExpr> args);

    /**
     * Returns an expression representing the class literal of this type ({@code This.class}).
     *
     * @return the class literal expression
     */
    JExpr class_();

    /**
     * Creates a qualified {@code this} expression: {@code Type.this}.
     *
     * @return the qualified this expression
     */
    JExpr this_();

    /**
     * Creates a qualified {@code super} expression: {@code Type.super}.
     *
     * @return the qualified this expression
     */
    JExpr super_();

    /**
     * {@return the number of dimensions of this type, if it is an array type, or 0 if it is not an array type}
     */
    default int dimensions() {
        return 0;
    }

    /**
     * Creates a constructor call: {@code new Type(args)}.
     * For array types, this creates a call of the form: {@code new ComponentType[arg0][arg1]...};
     * in this case, the number of arguments must be less than or equal to the number of array dimensions.
     *
     * @param args the constructor argument expressions
     * @return the new expression
     */
    default JExpr new_(final JExpr... args) {
        return new_(List.of(args));
    }

    /**
     * Creates a constructor call: {@code new Type(args)}.
     * For array types, this creates a call of the form: {@code new ComponentType[arg0][arg1]...};
     * in this case, the number of arguments must be less than or equal to the number of array dimensions.
     *
     * @param args the constructor argument expressions as a list
     * @return the new expression
     */
    default JExpr new_(final List<JExpr> args) {
        Assert.checkNotNullParam("args", args);
        return new NewJExpr(this, args);
    }

    /**
     * Creates an anonymous class creation expression: {@code new Type(args) &#123; body &#125;}.
     *
     * @param version the source version for feature validation
     * @param args    the constructor argument expressions
     * @param builder the anonymous class body builder
     * @return the anonymous class expression
     */
    default JExpr new_(final SourceVersion version, final List<JExpr> args,
        final Consumer<ClassCreator> builder) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("args", args);
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version, "", false);
        builder.accept(cc);
        cc.finish();
        return new AnonymousClassJExpr(this, args, cc);
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     * Only valid for array types.
     *
     * @param elements the initializer element expressions as a list
     * @return the array initializer expression
     */
    default JExpr newArrayInit(JExpr... elements) {
        return newArrayInit(List.of(elements));
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     * Only valid for array types.
     *
     * @param elements the initializer element expressions as a list
     * @return the array initializer expression
     */
    JExpr newArrayInit(List<JExpr> elements);

    /**
     * Creates a method reference on a type: {@code Type::method}.
     *
     * @param name the method name (or {@code "new"} for constructor references)
     * @return the method reference expression
     */
    default JExpr methodRef(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new MethodRefJExpr(this, name);
    }

    /**
     * Returns a Javadoc program element reference for a member of this type
     * ({@code This#member}).
     * <p>
     * The returned reference can be used in {@code {@link}}, {@code {@linkplain}},
     * and {@code @see} tags via the {@link org.jboss.jdeparser.creator.DocCommentCreator}
     * API.
     *
     * @param member the member identifier (e.g., {@code "length()"},
     *               {@code "CASE_INSENSITIVE_ORDER"})
     * @return the doc reference
     */
    JDocReference docRef(String member);

    /**
     * Returns an intersection type composed of all the given types ({@code A & B & C}).
     *
     * @param types the types to intersect
     * @return the intersection type
     */
    static JType allOf(JType... types) {
        return allOf(List.of(types));
    }

    /**
     * Returns an intersection type composed of all the given types ({@code A & B & C}).
     *
     * @param types the types to intersect as a list
     * @return the intersection type
     */
    static JType allOf(List<JType> types) {
        return new IntersectionJType(types);
    }
}
