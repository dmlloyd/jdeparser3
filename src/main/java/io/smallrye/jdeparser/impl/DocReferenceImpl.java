package io.smallrye.jdeparser.impl;

import static io.smallrye.jdeparser.impl.DocInlineCreatorImpl.typeName;

import java.io.IOException;

import io.smallrye.jdeparser.DocReference;
import io.smallrye.jdeparser.Type;

/**
 * Implementation of {@link DocReference} that holds a type and a member
 * identifier for use in Javadoc program element references.
 *
 * @param type   the type being referenced
 * @param member the member identifier (e.g., {@code "length()"} or
 *               {@code "CASE_INSENSITIVE_ORDER"})
 */
public record DocReferenceImpl(Type type, String member) implements DocReference, Writable {
    public void write(final SourceFileWriter w) throws IOException {
        if (type != null) {
            w.writeUnescaped(w.resolveClassName(typeName(type)));
        }
        w.writeUnescaped("#" + member);
    }
}
