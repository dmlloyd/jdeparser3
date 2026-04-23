package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.DocInlineCreator;
import org.jboss.jdeparser.creator.TypeParamCreator;

/**
 * Implementation of {@link TypeParamCreator} that collects type parameter
 * bounds and documentation.
 * <p>
 * Writes a type parameter in the form {@code T}, {@code T extends Bound},
 * or {@code T extends Bound1 & Bound2}.  Documentation set via
 * {@link #docComment(Consumer)} is propagated to the enclosing type or
 * method's Javadoc as a {@code @param <T>} tag.
 */
public final class TypeParamCreatorImpl extends AbstractCreator implements TypeParamCreator, Writable {

    /** The type parameter name. */
    private final String name;

    /** The upper bound(s), or empty if unbounded. */
    private JType bounds;

    /** Optional inline doc comment for this type parameter. */
    private DocInlineCreatorImpl docComment;

    /**
     * Constructs a new type parameter creator.
     *
     * @param version the source version
     * @param name    the type parameter name (e.g., {@code "T"})
     */
    public TypeParamCreatorImpl(final SourceVersion version, final String name) {
        super(version);
        this.name = name;
    }

    /**
     * Returns the type parameter name.
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
    public void extends_(List<JType> bounds) {
        checkActive();
        Assert.checkNotNullParam("bounds", bounds);
        bounds = List.copyOf(bounds);
        for (JType bound : bounds) {
            registerUsedType(bound);
        }
        JType ourBounds = this.bounds;
        if (ourBounds == null) {
            this.bounds = switch (bounds.size()) {
                case 0 -> null;
                case 1 -> bounds.get(0);
                default -> new IntersectionJType(bounds);
            };
        } else if (ourBounds instanceof IntersectionJType ourIntersection) {
            this.bounds = switch (bounds.size()) {
                case 0 -> ourIntersection;
                default -> new IntersectionJType(
                    Stream.concat(ourIntersection.types().stream(), bounds.stream()).toList()
                );
            };
        } else {
            this.bounds = switch (bounds.size()) {
                case 0 -> ourBounds;
                default -> new IntersectionJType(
                    Stream.concat(Stream.of(ourBounds), bounds.stream()).toList()
                );
            };
        }
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
        // T or T extends Bound or T extends Bound1 & Bound2
        writer.writeName(name);
        if (bounds != null) {
            writer.write(Tokens.$KW.EXTENDS);
            AbstractJExpr.writeType(writer, bounds);
        }
    }
}
