package io.smallrye.jdeparser.test;

import java.io.IOException;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.creator.ModifiableCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic tests verifying source file generation produces output.
 */
class BasicSourceFileTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that a minimal empty class generates correct package and class declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void emptyClass() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Empty", sf -> {
            sf.class_("Empty", ModifiableCreator::public_);
        });
        sources.writeSources();
        final String source = getSource("com.example", "Empty");
        assertNotNull(source);
        assertTrue(source.contains("package com.example;"), "should contain package declaration");
        assertTrue(source.contains("public class Empty"), "should contain class declaration");
    }

    /**
     * Verifies that a class with a field generates correct field declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithField() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Holder", sf -> {
            sf.class_("Holder", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Holder");
        assertTrue(source.contains("private final String name;"), "should contain field");
    }

    /**
     * Verifies that a class with a method body generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithMethod() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeter", sf -> {
            sf.class_("Greeter", cc -> {
                cc.public_();
                cc.method("greet", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.str("Hello"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeter");
        assertTrue(source.contains("public String greet()"), "should contain method signature");
        assertTrue(source.contains("return \"Hello\""), "should contain return statement");
    }

    /**
     * Verifies that a constructor with parameters generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithConstructor() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Person", sf -> {
            sf.class_("Person", cc -> {
                cc.public_();
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
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Person");
        assertTrue(source.contains("Person(String name)"), "should contain constructor");
        assertTrue(source.contains("this.name = name;"), "should contain assignment");
    }

    /**
     * Verifies that if/else control flow generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void ifElseStatement() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Flow", sf -> {
            sf.class_("Flow", cc -> {
                cc.method("check", mc -> {
                    mc.returning(Type.STRING);
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.ifElse(
                            Expr.$v("x").gt(Expr.ZERO),
                            then -> then.return_(Expr.str("positive")),
                            else_ -> else_.return_(Expr.str("non-positive"))
                        );
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Flow");
        assertTrue(source.contains("if"), "should contain if");
        assertTrue(source.contains("x > 0"), "should contain condition");
        assertTrue(source.contains("else"), "should contain else");
    }

    /**
     * Verifies that a for loop generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void forLoop() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Loop", sf -> {
            sf.class_("Loop", cc -> {
                cc.method("count", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
                            fb.body(loop -> {
                                loop.emit(Expr.callPlain("println", Expr.$v("i")));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Loop");
        assertTrue(source.contains("for"), "should contain for");
        assertTrue(source.contains("int i = 0"), "should contain init");
        assertTrue(source.contains("i < n"), "should contain condition");
        assertTrue(source.contains("i++"), "should contain update");
    }

    /**
     * Verifies that a lambda expression generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambdas", sf -> {
            sf.class_("Lambdas", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda("x", Expr.$v("x").call("toString")));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambdas");
        assertTrue(source.contains("x -> x.toString()"), "should contain lambda expression");
    }
}
