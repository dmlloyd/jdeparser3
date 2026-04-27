package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Type;

/**
 * A creator for declarations that can be annotated.
 * <p>
 * Provides methods to add annotations to the declaration being built.
 */
public sealed interface AnnotatableCreator
        permits EnumConstantCreator, LocalVarCreator, ModifiableCreator, RecordComponentCreator {

    /**
     * Adds an annotation with member values configured via the given callback.
     *
     * @param annotationType the annotation type
     * @param builder the callback to configure annotation members
     */
    void annotate(Type annotationType, Consumer<AnnotationCreator> builder);

    /**
     * Adds a marker annotation (one with no members).
     *
     * @param annotationType the annotation type
     */
    void annotate(Type annotationType);
}
