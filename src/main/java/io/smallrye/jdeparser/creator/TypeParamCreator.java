package io.smallrye.jdeparser.creator;

import java.util.List;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.impl.TypeParamCreatorImpl;

/**
 * A creator for configuring a type parameter on a generic type or method.
 * <p>
 * Extends {@link DocCommentableCreator DocCommentableCreator&lt;DocInlineCreator&gt;}
 * so that documentation set here is contributed as inline content
 * within the enclosing element's Javadoc comment.
 */
public sealed interface TypeParamCreator extends DocCommentableCreator<DocInlineCreator> permits TypeParamCreatorImpl {

    /**
     * Adds an upper bound to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bound the bound type
     */
    default void extends_(Type bound) {
        extends_(List.of(bound));
    }

    /**
     * Adds upper bounds to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bounds the bound types
     */
    default void extends_(Type... bounds) {
        extends_(List.of(bounds));
    }

    /**
     * Adds upper bounds to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bounds the bound types
     */
    void extends_(List<Type> bounds);
}
