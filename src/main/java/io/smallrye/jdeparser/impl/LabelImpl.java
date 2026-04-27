package io.smallrye.jdeparser.impl;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Label;

/**
 * Implementation of {@link Label} as a simple record holding the label name.
 *
 * @param name the label name (never {@code null})
 */
public record LabelImpl(String name) implements Label {

    /**
     * Constructs a new label with the given name.
     *
     * @param name the label name
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public LabelImpl {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
    }
}
