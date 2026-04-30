package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.LambdaCreatorImpl;

/**
 * A creator for configuring a block-body lambda expression.
 * <p>
 * Parameters are declared via the {@code param} methods. Once the first
 * parameter is added, the lambda is locked into either typed or untyped
 * mode: adding a typed parameter after an untyped one (or vice versa)
 * will throw {@link IllegalStateException}.
 * <p>
 * The lambda body is defined by calling {@link #body(Consumer)}, which
 * provides a {@link BlockCreator} for building the body statements.
 * <p>
 * Zero-parameter lambdas are supported and produce {@code () -> { ... }}.
 *
 * @see io.smallrye.jdeparser.Expr#lambda(Consumer)
 */
public sealed interface LambdaCreator permits LambdaCreatorImpl {

    /**
     * Sets the source version for feature validation within the lambda body.
     * <p>
     * If not called, the source version defaults to {@link SourceVersion#LATEST}.
     * This method must be called before {@link #body(Consumer)}.
     *
     * @param version the source version (must not be {@code null})
     */
    void sourceVersion(SourceVersion version);

    /**
     * Adds an untyped parameter to this lambda.
     * <p>
     * Once an untyped parameter has been added, adding a typed parameter
     * is forbidden.
     *
     * @param name the parameter name (must not be {@code null} or empty)
     * @return a variable expression referencing the declared parameter
     * @throws IllegalStateException if a typed parameter was already added
     */
    Var param(String name);

    /**
     * Adds a typed parameter to this lambda.
     * <p>
     * Once a typed parameter has been added, adding an untyped parameter
     * is forbidden.
     *
     * @param name the parameter name (must not be {@code null} or empty)
     * @param type the parameter type (must not be {@code null})
     * @return a variable expression referencing the declared parameter
     * @throws IllegalStateException if an untyped parameter was already added
     */
    Var param(String name, Type type);

    /**
     * Adds a typed parameter to this lambda with additional configuration
     * (annotations, modifiers) via a {@link ParamCreator} callback.
     * <p>
     * Once a typed parameter has been added, adding an untyped parameter
     * is forbidden.
     *
     * @param name the parameter name (must not be {@code null} or empty)
     * @param type the parameter type (must not be {@code null})
     * @param builder the callback for configuring the parameter (must not be {@code null})
     * @return a variable expression referencing the declared parameter
     * @throws IllegalStateException if an untyped parameter was already added
     */
    Var param(String name, Type type, Consumer<ParamCreator> builder);

    /**
     * Defines the lambda body.
     * <p>
     * This method must be called exactly once. The provided callback receives
     * a {@link BlockCreator} for building the body statements.
     *
     * @param body the callback for the body statements (must not be {@code null})
     */
    void body(Consumer<BlockCreator> body);
}
