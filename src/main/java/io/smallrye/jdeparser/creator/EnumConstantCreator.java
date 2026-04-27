package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.impl.EnumConstantCreatorImpl;

/**
 * A creator for configuring an enum constant.
 * <p>
 * Supports constructor arguments and an optional anonymous class body
 * for overriding methods.
 */
public sealed interface EnumConstantCreator
        extends AnnotatableCreator, DocCommentableCreator<DocCommentCreator> permits EnumConstantCreatorImpl {

    /**
     * Adds a constructor argument to this enum constant.
     *
     * @param value the argument expression
     */
    void arg(Expr value);

    /**
     * Defines an anonymous class body for this enum constant,
     * allowing method overrides and additional members.
     *
     * @param builder the callback to define the class body
     */
    void body(Consumer<ClassCreator> builder);
}
