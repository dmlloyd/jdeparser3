package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.JDocReference;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.DocInlineCreatorImpl;

/**
 * A creator for inline Javadoc content: plain text, {@code {@code}},
 * {@code {@link}}, and {@code {@linkplain}} tags, and other inline tags.
 * <p>
 * This interface represents the subset of doc comment content that may
 * appear inside block tag bodies (such as {@code @return}, {@code @throws},
 * and {@code @deprecated}), where nested block tags are not permitted.
 * <p>
 * {@link DocCommentCreator} extends this interface to add block tag
 * methods for top-level doc comments.
 *
 * @see DocCommentCreator
 */
public sealed interface DocInlineCreator permits DocCommentCreator, DocInlineCreatorImpl {

    /**
     * Adds a block of text to the doc comment body.
     *
     * @param text the text (may contain HTML)
     */
    void text(String text);

    /**
     * Adds an inline {@code {@code ...}} tag to the current text.
     *
     * @param code the code text
     */
    void code(String code);

    /**
     * Adds an inline {@code {@docRoot}} tag, which resolves to the
     * relative path to the generated documentation root directory.
     */
    void docRoot();

    /**
     * Adds an inline {@code {@index ...}} tag to declare an index entry.
     * <p>
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     *
     * @param term the word or phrase to index
     */
    void index(String term);

    /**
     * Adds an inline {@code {@index ...}} tag to declare an index entry
     * with a description.
     * <p>
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     *
     * @param term        the word or phrase to index
     * @param description the short description of the index entry
     */
    void index(String term, String description);

    /**
     * Adds an inline {@code {@inheritDoc}} tag to inherit the corresponding
     * documentation from the overridden method.
     * <p>
     * Only valid in method documentation comments.
     */
    void inheritDoc();

    /**
     * Adds an inline {@code {@inheritDoc supertype}} tag to inherit the
     * corresponding documentation from the specified supertype's method.
     * <p>
     * Only valid in method documentation comments.
     *
     * @param supertype the supertype whose method documentation to inherit
     */
    void inheritDoc(JType supertype);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a type.
     *
     * @param type the type to link to
     */
    void link(JType type);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a type with display text.
     *
     * @param type  the type to link to
     * @param label the display text for the link
     */
    void link(JType type, String label);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a program element.
     *
     * @param ref the program element reference
     */
    void link(JDocReference ref);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a program element
     * with display text.
     *
     * @param ref   the program element reference
     * @param label the display text for the link
     */
    void link(JDocReference ref, String label);

    /**
     * Adds an inline {@code {@linkplain ...}} tag referencing a type.
     * <p>
     * Unlike {@link #link(JType)}, {@code {@linkplain}} renders
     * the label in plain text rather than code font.
     *
     * @param type the type to link to
     */
    void linkPlain(JType type);

    /**
     * Adds an inline {@code {@linkplain ...}} tag referencing a type with display text.
     * <p>
     * Unlike {@link #link(JType, String)}, {@code {@linkplain}} renders
     * the label in plain text rather than code font.
     *
     * @param type  the type to link to
     * @param label the display text for the link
     */
    void linkPlain(JType type, String label);

    /**
     * Adds an inline {@code {@linkplain ...}} tag referencing a program element.
     * <p>
     * Unlike {@link #link(JDocReference)}, {@code {@linkplain}} renders
     * the label in plain text rather than code font.
     *
     * @param ref the program element reference
     */
    void linkPlain(JDocReference ref);

    /**
     * Adds an inline {@code {@linkplain ...}} tag referencing a program element
     * with display text.
     * <p>
     * Unlike {@link #link(JDocReference, String)}, {@code {@linkplain}}
     * renders the label in plain text rather than code font.
     *
     * @param ref   the program element reference
     * @param label the display text for the link
     */
    void linkPlain(JDocReference ref, String label);

    /**
     * Adds an inline {@code {@literal ...}} tag that displays text without
     * interpreting it as HTML markup or nested Javadoc tags.
     *
     * @param text the literal text
     */
    void literal(String text);

    /**
     * Adds an inline {@code {@summary ...}} tag to explicitly mark the
     * summary sentence of a documentation comment.
     * <p>
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_10 10}
     * or later.
     *
     * @param text the summary text
     */
    void summary(String text);

    /**
     * Adds an inline {@code {@systemProperty ...}} tag identifying a
     * system property name.
     * <p>
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_12 12}
     * or later.
     *
     * @param propertyName the system property name
     */
    void systemProperty(String propertyName);

    /**
     * Adds an inline {@code {@value}} tag that displays the value of the
     * enclosing static field's compile-time constant.
     */
    void value();

    /**
     * Adds an inline {@code {@value ref}} tag that displays the value of
     * the specified static field's compile-time constant.
     *
     * @param ref the program element reference to the static field
     */
    void value(JDocReference ref);
}
