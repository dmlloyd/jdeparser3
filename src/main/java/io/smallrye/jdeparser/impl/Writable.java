package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * An element that can write itself to a {@link SourceFileWriter}.
 * <p>
 * Implemented by all AST nodes (types, expressions, statements) and tokens.
 * The formatting engine calls {@link #write(SourceFileWriter)} to emit source
 * code; implementations use the writer's spacing, indentation, and token
 * state machine methods to produce correctly formatted output.
 * <p>
 * Writing an element <em>should not</em> mutate its internal state; some elements
 * may be written more than once.
 */
@FunctionalInterface
public interface Writable {

    /**
     * Writes this element to the given source file writer.
     *
     * @param writer the writer to emit source code to
     * @throws IOException if an I/O error occurs
     */
    void write(SourceFileWriter writer) throws IOException;

    /**
     * Returns the vertical spacing constant to use before this member
     * when it appears in a type body member list.
     * <p>
     * Returns {@code null} by default, meaning no specific spacing is applied
     * (a simple newline is used).  Overridden by method, constructor, and
     * type declaration writables to return the appropriate constant.
     *
     * @return the spacing constant, or {@code null} for default newline spacing
     */
    default FormatPreferences.Space memberSpacing() {
        return null;
    }
}
