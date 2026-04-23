package org.jboss.jdeparser.creator;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.AnnotationCreatorImpl;

/**
 * A creator for configuring annotation member values.
 * <p>
 * Used within the callback of {@link AnnotatableCreator#annotate(JType, Consumer)}.
 */
public sealed interface AnnotationCreator permits AnnotationCreatorImpl {

    /**
     * Sets the default {@code value()} member of the annotation.
     *
     * @param value the value expression
     */
    void value(JExpr value);

    /**
     * Sets a named member of the annotation.
     *
     * @param name  the member name
     * @param value the value expression
     */
    void member(String name, JExpr value);

    /**
     * Sets a named member of the annotation to an array of values.
     *
     * @param name   the member name
     * @param values the value expressions forming the array initializer
     */
    default void memberArray(String name, JExpr... values) {
        memberArray(name, List.of(values));
    }

    /**
     * Sets a named member of the annotation to an array of values.
     *
     * @param name   the member name
     * @param values the value expressions forming the array initializer as a list
     */
    void memberArray(String name, List<JExpr> values);
}
