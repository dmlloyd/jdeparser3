package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

/**
 * A creator for declarations that can have documentation comments.
 * <p>
 * The type parameter {@code D} determines which level of doc comment
 * content is available:
 * <ul>
 * <li>{@link DocCommentCreator} — full Javadoc with block tags
 * ({@code @return}, {@code @throws}, {@code @see}, etc.), used
 * for declarations whose documentation is a standalone Javadoc
 * comment.</li>
 * <li>{@link DocInlineCreator} — inline content only (text,
 * {@code {@code}}, {@code {@link}}, etc.), used for declarations
 * whose documentation is contributed as inline content within a
 * block tag of an enclosing element's Javadoc comment.</li>
 * </ul>
 *
 * @param <D> the doc comment creator type
 */
public sealed interface DocCommentableCreator<D extends DocInlineCreator>
        permits EnumConstantCreator, ModifiableCreator, RecordComponentCreator, TypeParamCreator {

    /**
     * Configures documentation for this declaration.
     * <p>
     * The callback receives a creator whose type depends on the declaration
     * kind: a {@link DocCommentCreator} for standalone Javadoc comments, or a
     * {@link DocInlineCreator} for inline content that will be contributed to
     * an enclosing element's documentation.
     *
     * @param builder the callback to configure the doc comment content
     */
    void docComment(Consumer<D> builder);
}
