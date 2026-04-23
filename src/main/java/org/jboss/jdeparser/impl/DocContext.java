package org.jboss.jdeparser.impl;

/**
 * The declaration context in which a documentation comment appears.
 * <p>
 * Used for eager validation of tag usage: certain Javadoc tags are only
 * valid in specific contexts as defined by the standard doclet specification.
 */
public enum DocContext {

    /** A module declaration. */
    MODULE("module"),

    /** A package declaration. */
    PACKAGE("package"),

    /** A type declaration (class, interface, enum, record, annotation interface). */
    TYPE("type"),

    /** A constructor declaration. */
    CONSTRUCTOR("constructor"),

    /** A method declaration (including annotation interface elements). */
    METHOD("method"),

    /** A field declaration (including enum constants). */
    FIELD("field"),

    /** Overview page or other documentation files. */
    OTHER("overview"),
    ;

    /** The human-readable name for error messages. */
    private final String displayName;

    DocContext(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a human-readable name for use in error messages.
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }
}
