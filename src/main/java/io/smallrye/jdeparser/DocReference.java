package io.smallrye.jdeparser;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.impl.DocReferenceImpl;

/**
 * Represents a Javadoc program element reference: a type combined with a
 * member identifier, as used in {@code {@link}}, {@code {@linkplain}},
 * and {@code @see} tags.
 * <p>
 * A program element reference has the form {@code Type#member}, where
 * {@code Type} is resolved according to import rules and {@code member}
 * is a field name, method name, or method signature (e.g.,
 * {@code "length()"}, {@code "CASE_INSENSITIVE_ORDER"}).
 * <p>
 * Instances are created via {@link Type#docRef(String)}.
 *
 * @see Type#docRef(String)
 */
public sealed interface DocReference permits DocReferenceImpl {
    /**
     * Create a rootless program element reference.
     *
     * @param member the member to reference (must not be {@code null})
     * @return the doc reference (not {@code null})
     */
    static DocReference of(String member) {
        Assert.checkNotNullParam("member", member);
        return new DocReferenceImpl(null, member);
    }
}
