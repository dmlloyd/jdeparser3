package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.DocInlineCreator;
import org.jboss.jdeparser.creator.RecordComponentCreator;

/**
 * Implementation of {@link RecordComponentCreator} that collects component
 * configuration and writes the component declaration within a record's
 * component list.
 * <p>
 * Writes the form: {@code [annotations] Type name}.
 * <p>
 * Record components support annotations and documentation but do not
 * have modifiers of their own.
 */
public final class RecordComponentCreatorImpl extends AbstractCreator implements RecordComponentCreator, Writable {

    /** The component name. */
    private final String name;

    /** The component type. */
    private final JType type;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional inline doc comment. */
    private DocInlineCreatorImpl docComment;

    /**
     * Constructs a new record component creator.
     *
     * @param version the source version
     * @param name    the component name
     * @param type    the component type
     */
    public RecordComponentCreatorImpl(final SourceVersion version, final String name, final JType type) {
        super(version);
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the optional inline doc comment creator, if one was configured.
     *
     * @return the inline doc comment creator, or {@code null} if none
     */
    public DocInlineCreatorImpl docComment() {
        return docComment;
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType, final Consumer<AnnotationCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("annotationType", annotationType);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(annotationType);
        final AnnotationCreatorImpl ac = new AnnotationCreatorImpl(version(), annotationType);
        ac.sourceFile(sourceFile());
        nest(() -> builder.accept(ac));
        ac.finish();
        annotations.add(ac);
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType) {
        checkActive();
        Assert.checkNotNullParam("annotationType", annotationType);
        registerUsedType(annotationType);
        annotations.add(new AnnotationCreatorImpl(version(), annotationType));
    }

    /** {@inheritDoc} */
    @Override
    public void docComment(final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final DocInlineCreatorImpl dc = getOrCreateDocComment();
        nest(() -> builder.accept(dc));
        dc.finish();
    }

    /**
     * Returns the existing inline doc comment creator, or creates one on demand.
     * <p>
     * If a creator already exists from a prior call, it is reopened for
     * further configuration.
     *
     * @return the inline doc comment creator
     */
    private DocInlineCreatorImpl getOrCreateDocComment() {
        DocInlineCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocInlineCreatorImpl(version(), sourceFile(), null);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // [annotations] Type name
        annotations.writeWithSpaces(writer);
        AbstractJExpr.writeType(writer, type);
        writer.sp();
        writer.writeName(name);
    }
}
