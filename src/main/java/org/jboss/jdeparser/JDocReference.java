package org.jboss.jdeparser;

import io.smallrye.common.constraint.Assert;
import org.jboss.jdeparser.impl.DocReferenceImpl;

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
 * Instances are created via {@link JType#docRef(String)}.
 *
 * @see JType#docRef(String)
 */
public sealed interface JDocReference permits DocReferenceImpl {
    /**
     * Create a rootless program element reference.
     *
     * @param member the member to reference (must not be {@code null})
     * @return the doc reference (not {@code null})
     */
    static JDocReference of(String member) {
        Assert.checkNotNullParam("member", member);
        return new DocReferenceImpl(null, member);
    }
}
