package io.smallrye.jdeparser.creator;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.impl.FieldCreatorImpl;

/**
 * A creator for configuring a field declaration.
 */
public sealed interface FieldCreator extends ModifiableCreator permits FieldCreatorImpl {

    /**
     * Sets the type of this field.
     *
     * @param type the field type
     */
    void type(Type type);

    /**
     * Sets the initializer expression for this field.
     *
     * @param init the initializer expression
     */
    void init(Expr init);

    /**
     * Adds the {@code volatile} modifier.
     */
    default void volatile_() {
        addFlag(ModifierFlag.VOLATILE);
    }

    /**
     * Adds the {@code transient} modifier.
     */
    default void transient_() {
        addFlag(ModifierFlag.TRANSIENT);
    }
}
