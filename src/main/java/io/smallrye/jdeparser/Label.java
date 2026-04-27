package io.smallrye.jdeparser;

import java.util.function.BiConsumer;

import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.impl.LabelImpl;

/**
 * A statement label that can be used as a target for {@code break} and {@code continue}.
 * <p>
 * Labels are created by {@link BlockCreator#labeled(String, BiConsumer)
 * BlockCreator.labeled()} and can be referenced by
 * {@link BlockCreator#break_(Label) break_()} and
 * {@link BlockCreator#continue_(Label) continue_()}.
 */
public sealed interface Label permits LabelImpl {
    /**
     * Returns the name of this label.
     *
     * @return the label name (never {@code null})
     */
    String name();
}
