package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.ForCreatorImpl;

/**
 * A creator for configuring a traditional {@code for} loop.
 * <p>
 * The loop is structured as: {@code for (init; condition; update) body}.
 */
public sealed interface ForCreator permits ForCreatorImpl {

    /**
     * Sets the loop initializer, declaring a new variable.
     *
     * @param type the variable type
     * @param name the variable name
     * @param init the initializer expression
     * @return the loop variable, for use in the condition, update, and body
     */
    Var init(Type type, String name, Expr init);

    /**
     * Sets the loop condition.
     *
     * @param condition the condition expression
     */
    void condition(Expr condition);

    /**
     * Sets the loop update expression.
     *
     * @param update the update expression
     */
    void update(Expr update);

    /**
     * Defines the loop body.
     *
     * @param body the callback for the body statements
     */
    void body(Consumer<BlockCreator> body);
}
