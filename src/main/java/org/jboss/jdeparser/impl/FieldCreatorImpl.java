package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.FieldCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;

/**
 * Implementation of {@link FieldCreator} that collects field configuration
 * and writes the complete field declaration.
 * <p>
 * Writes the form: {@code [annotations] [modifiers] Type name [= init];}.
 */
public final class FieldCreatorImpl extends AbstractCreator implements FieldCreator, Writable {

    /** The field name. */
    private final String name;

    /** The modifier location for this field. */
    private final ModifierLocation location;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** The field type (may be set after construction). */
    private JType type;

    /** Optional initializer expression. */
    private JExpr init;

    /**
     * Constructs a new field creator.
     *
     * @param version  the source version
     * @param name     the field name
     * @param location the modifier location (e.g., {@link ModifierLocation#FIELD} or
     *                 {@link ModifierLocation#INTERFACE_FIELD})
     */
    public FieldCreatorImpl(final SourceVersion version, final String name, final ModifierLocation location) {
        super(version);
        this.name = name;
        this.location = location;
        this.modifiers = new ModifierHolder(location);
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return location;
    }

    /** {@inheritDoc} */
    @Override
    public void type(final JType type) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public void init(final JExpr init) {
        checkActive();
        Assert.checkNotNullParam("init", init);
        this.init = init;
    }

    /** {@inheritDoc} */
    @Override
    public void setAccess(final AccessLevel access) {
        checkActive();
        Assert.checkNotNullParam("access", access);
        modifiers.setAccess(access);
    }

    /** {@inheritDoc} */
    @Override
    public void addFlag(final ModifierFlag flag) {
        checkActive();
        Assert.checkNotNullParam("flag", flag);
        modifiers.addFlag(flag);
    }

    /** {@inheritDoc} */
    @Override
    public void removeFlag(final ModifierFlag flag) {
        checkActive();
        Assert.checkNotNullParam("flag", flag);
        modifiers.removeFlag(flag);
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
    public void docComment(final Consumer<DocCommentCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final DocCommentCreatorImpl dc = getOrCreateDocComment();
        nest(() -> builder.accept(dc));
        dc.finish();
    }

    /**
     * Returns the existing doc comment creator, or creates one on demand.
     * <p>
     * If a creator already exists from a prior call, it is reopened
     * for further configuration.
     *
     * @return the doc comment creator
     */
    private DocCommentCreatorImpl getOrCreateDocComment() {
        DocCommentCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), DocContext.FIELD);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // [javadoc] [annotations] [modifiers] Type name [= init];
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        if (type != null) {
            AbstractJExpr.writeType(writer, type);
            writer.sp();
        }
        writer.writeName(name);
        if (init != null) {
            writer.write(Tokens.$BINOP.ASSIGN);
            AbstractJExpr.writeExpr(writer, init);
        }
        writer.write(Tokens.$PUNCT.SEMI);
    }
}
