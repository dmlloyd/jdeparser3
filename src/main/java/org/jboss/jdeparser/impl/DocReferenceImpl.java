package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.JDocReference;
import org.jboss.jdeparser.JType;

/**
 * Implementation of {@link JDocReference} that holds a type and a member
 * identifier for use in Javadoc program element references.
 *
 * @param type   the type being referenced
 * @param member the member identifier (e.g., {@code "length()"} or
 *               {@code "CASE_INSENSITIVE_ORDER"})
 */
public record DocReferenceImpl(JType type, String member) implements JDocReference {
}
