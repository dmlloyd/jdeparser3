package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.AccessLevel;
import io.smallrye.jdeparser.creator.AnnotationCreator;
import io.smallrye.jdeparser.creator.DocCommentCreator;
import io.smallrye.jdeparser.creator.ModifierFlag;
import io.smallrye.jdeparser.creator.ModifierLocation;
import io.smallrye.jdeparser.creator.ParamCreator;

/**
 * Implementation of {@link ParamCreator} that collects parameter configuration
 * (annotations, modifiers, documentation) and writes the parameter declaration.
 * <p>
 * Parameters support the {@code final} modifier and annotations. Documentation
 * set here is propagated to the enclosing method/constructor's Javadoc as a
 * {@code @param} tag.
 */
public final class ParamCreatorImpl extends AbstractCreator implements ParamCreator, Writable {

    /** The parameter name. */
    private final String name;

    /** The parameter type. */
    private final Type type;

    /** Whether this is a varargs parameter. */
    private final boolean varargs;

    /** The modifier holder for this parameter. */
    private final ModifierHolder modifiers = new ModifierHolder(ModifierLocation.PARAMETER);

    /** The annotation holder for this parameter. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional inline doc comment for this parameter. */
    private DocInlineCreatorImpl docComment;

    /**
     * Constructs a new parameter creator.
     *
     * @param version the source version
     * @param name the parameter name
     * @param type the parameter type
     * @param varargs whether this is a varargs parameter
     */
    public ParamCreatorImpl(final SourceVersion version, final String name, final Type type, final boolean varargs) {
        super(version);
        this.name = name;
        this.type = type;
        this.varargs = varargs;
    }

    /**
     * Returns the parameter name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the inline doc comment creator, if documentation was configured.
     *
     * @return the inline doc comment, or {@code null}
     */
    public DocInlineCreatorImpl docComment() {
        return docComment;
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return ModifierLocation.PARAMETER;
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
    public void annotate(final Type annotationType, final Consumer<AnnotationCreator> builder) {
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
    public void annotate(final Type annotationType) {
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
        final DocInlineCreatorImpl dc = getOrCreateDocComment();
        nest(() -> builder.accept((DocCommentCreator) dc));
        dc.finish();
    }

    /**
     * Returns the existing inline doc comment creator, or creates one on demand.
     * <p>
     * A {@link DocCommentCreatorImpl} is created (to satisfy the
     * {@link DocCommentCreator} API), but stored as {@link DocInlineCreatorImpl}
     * since only the inline content is used for {@code @param} tag generation.
     * If a creator already exists from a prior call, it is reopened for
     * further configuration.
     *
     * @return the inline doc comment creator
     */
    private DocInlineCreatorImpl getOrCreateDocComment() {
        DocInlineCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), null);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // annotations modifiers type name  or  annotations modifiers type... name
        annotations.writeWithSpaces(writer);
        modifiers.write(writer);
        AbstractExpr.writeType(writer, type);
        if (varargs) {
            writer.write(Tokens.$PUNCT.ELLIPSIS);
        }
        writer.sp();
        writer.writeName(name);
    }
}
