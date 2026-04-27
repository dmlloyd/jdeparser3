/**
 * JDeparser 3: a Java source code generation library.
 * <p>
 * This module provides a programmatic API for generating syntactically correct
 * Java source files, supporting all Java language constructs
 * with configurable source version validation.
 */
module io.smallrye.jdeparser {
    requires java.compiler;
    requires io.smallrye.common.constraint;

    exports io.smallrye.jdeparser;
    exports io.smallrye.jdeparser.creator;
    exports io.smallrye.jdeparser.format;
    exports io.smallrye.jdeparser.impl to io.smallrye.jdeparser.test;
}
