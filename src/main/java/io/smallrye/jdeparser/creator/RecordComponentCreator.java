package io.smallrye.jdeparser.creator;

import io.smallrye.jdeparser.impl.RecordComponentCreatorImpl;

/**
 * A creator for configuring a record component.
 * <p>
 * Extends {@link AnnotatableCreator} for adding annotations and
 * {@link DocCommentableCreator DocCommentableCreator&lt;DocInlineCreator&gt;}
 * so that documentation set here is contributed as inline content
 * within the enclosing record's Javadoc comment.
 */
public sealed interface RecordComponentCreator extends AnnotatableCreator, DocCommentableCreator<DocInlineCreator> permits RecordComponentCreatorImpl {
}
