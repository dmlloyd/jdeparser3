package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.impl.AnnotationInterfaceCreatorImpl;

/**
 * A creator for building an annotation type declaration.
 */
public sealed interface AnnotationInterfaceCreator extends ModifiableCreator permits AnnotationInterfaceCreatorImpl {

    /**
     * Defines an annotation element (method-like member).
     *
     * @param name         the element name
     * @param type         the element type
     * @param defaultValue the default value expression, or {@code null} for no default
     */
    void element(String name, Type type, Expr defaultValue);

    /**
     * Defines an annotation element without a default value.
     *
     * @param name the element name
     * @param type the element type
     */
    void element(String name, Type type);

    /**
     * Defines a constant in this annotation type.
     *
     * @param name    the constant name
     * @param builder the callback to configure the constant field
     */
    void constant(String name, Consumer<FieldCreator> builder);
}
