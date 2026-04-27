package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Represents a parameterized (generic) type with type arguments applied,
 * such as {@code List<String>} or {@code Map<String, Integer>}.
 * <p>
 * Once type arguments have been applied, further calls to {@link #typeArg}
 * are rejected with an {@link IllegalStateException}. The {@link #erasure()}
 * operation delegates to the raw type's erasure.
 */
public final class NarrowedType extends AbstractType {

    /** The raw (unparameterized) type. */
    private final Type rawType;

    /** The type arguments, stored as an unmodifiable list. */
    private final List<Type> typeArgs;

    /**
     * Constructs a new parameterized type.
     *
     * @param rawType the raw type to which type arguments are applied
     * @param typeArgs the type arguments (defensively copied to an unmodifiable list)
     */
    public NarrowedType(final Type rawType, final List<Type> typeArgs) {
        this.rawType = rawType;
        this.typeArgs = List.copyOf(typeArgs);
    }

    /**
     * Returns the raw (unparameterized) type.
     *
     * @return the raw type
     */
    public Type rawType() {
        return rawType;
    }

    /**
     * Returns the type arguments as an unmodifiable list.
     *
     * @return the type arguments (never {@code null}, never empty)
     */
    public List<Type> typeArgs() {
        return typeArgs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the raw type's erasure.
     */
    @Override
    public Type erasure() {
        return rawType.erasure();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since type arguments have already been applied
     */
    @Override
    public Type typeArg(final List<Type> args) {
        throw new IllegalStateException("Type arguments already applied");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractExpr.writeType(writer, rawType);
        writer.write(Tokens.$ANGLE.OPEN);
        AbstractExpr.writeList(writer, typeArgs, FormatPreferences.Space.AFTER_COMMA_TYPE_ARGUMENT);
        writer.write(Tokens.$ANGLE.CLOSE);
    }
}
