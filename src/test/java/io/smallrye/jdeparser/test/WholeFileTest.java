package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.ModifierFlag;

/**
 * Tests that compare complete generated source files against pre-created
 * expected output files stored in test resources.
 */
class WholeFileTest extends AbstractGeneratingTestCase {

    /**
     * Generates a simple POJO class with field, constructor, and getter,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void personClass() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Person", sf -> {
            sf.class_("Person", cc -> {
                cc.public_();
                cc.final_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.STRING);
                });
                cc.constructor(con -> {
                    con.public_();
                    con.param("name", Type.STRING);
                    con.body(b -> {
                        b.emit(Expr.THIS.field("name").assign(Expr.$v("name")));
                    });
                });
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.$v("name"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Person");
    }

    /**
     * Generates an enum with constants, fields, constructor, and method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void seasonEnum() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Season", sf -> {
            sf.enum_("Season", ec -> {
                ec.public_();
                ec.constant("SPRING", c -> c.arg(Expr.str("warm")));
                ec.constant("SUMMER", c -> c.arg(Expr.str("hot")));
                ec.constant("AUTUMN", c -> c.arg(Expr.str("cool")));
                ec.constant("WINTER", c -> c.arg(Expr.str("cold")));
                ec.field("description", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.STRING);
                });
                ec.constructor(con -> {
                    con.param("description", Type.STRING);
                    con.body(b -> {
                        b.emit(Expr.THIS.field("description").assign(Expr.$v("description")));
                    });
                });
                ec.method("getDescription", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.$v("description"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Season");
    }

    /**
     * Generates a generic interface with an abstract method and a default method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void converterInterface() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Converter", sf -> {
            sf.interface_("Converter", ic -> {
                ic.public_();
                ic.typeParam("F", tp -> {
                });
                ic.typeParam("T", tp -> {
                });
                ic.method("convert", mc -> {
                    mc.returning(Type.named("T"));
                    mc.param("input", Type.named("F"));
                });
                ic.method("identity", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(Type.named("F"));
                    mc.param("input", Type.named("F"));
                    mc.body(b -> {
                        b.return_(Expr.$v("input"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Converter");
    }

    /**
     * Generates a record with compact constructor and method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void rangeRecord() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Range", sf -> {
            sf.record_("Range", rc -> {
                rc.public_();
                rc.component("min", Type.INT);
                rc.component("max", Type.INT);
                rc.compactConstructor(b -> {
                    b.if_(Expr.$v("min").gt(Expr.$v("max")), then -> {
                        final Type type = Type.named("java.lang.IllegalArgumentException");
                        then.throw_(type.new_(List.of(new Expr[] { Expr.str("min > max") })));
                    });
                });
                rc.method("size", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.body(b -> {
                        b.return_(Expr.$v("max").sub(Expr.$v("min")));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Range");
    }

    /**
     * Loads an expected output file from test resources and compares it
     * against the generated source.
     *
     * @param fileName the file name (without extension for the generated file,
     *        with .java suffix for the resource)
     * @throws IOException if the resource cannot be loaded
     */
    private void assertMatchesExpected(final String fileName) throws IOException {
        final String actual = getSource("com.example", fileName);
        final String expected = loadExpected(fileName + ".java");
        assertEquals(expected, actual, "Generated output for " + fileName + " should match expected file");
    }

    /**
     * Loads an expected file from the {@code expected/} test resource directory.
     *
     * @param resourceName the file name within the {@code expected/} directory
     * @return the file contents as a string
     * @throws IOException if the resource cannot be read
     */
    private static String loadExpected(final String resourceName) throws IOException {
        final String path = "expected/" + resourceName;
        try (InputStream is = WholeFileTest.class.getModule().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Expected resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
