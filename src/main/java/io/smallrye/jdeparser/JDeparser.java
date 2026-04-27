package io.smallrye.jdeparser;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.format.Filer;
import io.smallrye.jdeparser.format.FormatPreferences;
import io.smallrye.jdeparser.impl.SourcesImpl;

/**
 * Entry point for the JDeparser 3 source code generation library.
 * <p>
 * Use {@link #createSources} to obtain a {@link Sources} instance,
 * then define source files using the borrow-pattern API.
 *
 * <pre>{@code
 * Sources sources = JDeparser.createSources(filer, format, SourceVersion.JAVA_17);
 *
 * sources.createSourceFile("com.example", "Greeter", sf -> {
 *     sf.class_("Greeter", cc -> {
 *         cc.public_();
 *         // ... define class members
 *     });
 * });
 *
 * sources.writeSources();
 * }</pre>
 */
public final class JDeparser {

    private JDeparser() {
    }

    /**
     * Creates a new source file collection with the given configuration.
     *
     * @param filer the filer used to create output files
     * @param preferences the formatting preferences
     * @param sourceVersion the target Java source version for feature validation
     * @return a new source file collection
     */
    public static Sources createSources(final Filer filer, final FormatPreferences preferences,
            final SourceVersion sourceVersion) {
        Assert.checkNotNullParam("filer", filer);
        Assert.checkNotNullParam("preferences", preferences);
        Assert.checkNotNullParam("sourceVersion", sourceVersion);
        return new SourcesImpl(filer, preferences, sourceVersion);
    }
}
