package io.smallrye.jdeparser.impl;

import java.io.IOException;

import io.smallrye.jdeparser.Type;

/**
 * Represents a nested (inner or static member) type reference,
 * such as {@code Outer.Inner}.
 * <p>
 * A nested type is formed by qualifying an inner type name with its
 * enclosing type.  Standard type operations like {@link #array()},
 * {@link #typeArg}, and {@link #nestedType} are inherited from
 * {@link AbstractType} and work as expected.
 */
public final class NestedType extends AbstractType {

    /** The enclosing (outer) type. */
    private final Type outer;

    /** The simple name of the nested type. */
    private final String name;

    /**
     * Constructs a new nested type reference.
     *
     * @param outer the enclosing type
     * @param name  the simple name of the nested type (e.g., {@code "Entry"})
     */
    public NestedType(final Type outer, final String name) {
        this.outer = outer;
        this.name = name;
    }

    /**
     * Returns the enclosing (outer) type.
     *
     * @return the outer type
     */
    public Type outer() {
        return outer;
    }

    /**
     * Returns the simple name of the nested type.
     *
     * @return the nested type name (e.g., {@code "Entry"})
     */
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractExpr.writeType(writer, outer);
        writer.write(Tokens.$PUNCT.DOT);
        writer.writeClass(name);
    }
}
