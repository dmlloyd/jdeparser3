package org.jboss.jdeparser.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.impl.LambdaJExpr;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comprehensive whole-file comparison tests that exercise as many features
 * as possible for each type kind (class, interface, enum, record, annotation).
 * <p>
 * Each test generates a large source file and compares it against a
 * pre-created expected output file in {@code src/test/resources/expected/}.
 */
class ComprehensiveWholeFileTest extends AbstractGeneratingTestCase {

    // ── Comprehensive class ─────────────────────────────────────────────

    /**
     * Generates a comprehensive class exercising: generics, superclass,
     * interfaces, fields (static/instance/final), static and instance
     * initializers, multiple constructors (including varargs and throws),
     * multiple methods (static, final, synchronized, with type params),
     * annotations, doc comments, nested types, and diverse statements.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void comprehensiveClass() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JType arrayListType = JTypes.typeNamed("java.util.ArrayList");
        final JType serializableType = JTypes.typeNamed("java.io.Serializable");
        final JType comparableType = JTypes.typeNamed("java.lang.Comparable");
        final JType overrideType = JTypes.typeNamed("java.lang.Override");
        final JType suppressWarningsType = JTypes.typeNamed("java.lang.SuppressWarnings");
        final JType illegalArgType = JTypes.typeNamed("java.lang.IllegalArgumentException");
        final JType illegalStateType = JTypes.typeNamed("java.lang.IllegalStateException");
        final JType objectsType = JTypes.typeNamed("java.util.Objects");

        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.model", "Container", sf -> {
            sf.import_(listType);
            sf.import_(arrayListType);
            sf.import_(serializableType);

            sf.class_("Container", cc -> {
                cc.public_();
                cc.final_();
                cc.docComment(dc -> {
                    dc.text("A generic container that holds items.");
                    dc.text(" Supports serialization and comparison.");
                    dc.see(JType.STRING);
                    dc.since("1.0");
                    dc.author("JDeparser Test Suite");
                    dc.version("1.0.0");
                });
                cc.typeParam("T", tp -> {
                    tp.extends_(comparableType);
                    tp.docComment(dc -> dc.text("the element type"));
                });
                cc.implements_(serializableType);
                cc.implements_(comparableType);

                // static field
                cc.field("serialVersionUID", fc -> {
                    fc.private_();
                    fc.static_();
                    fc.final_();
                    fc.type(JType.LONG);
                    fc.init(JExprs.decimal(1L));
                    fc.docComment(dc -> dc.text("Serial version UID."));
                });

                // static field with annotation
                cc.field("DEFAULT_CAPACITY", fc -> {
                    fc.public_();
                    fc.static_();
                    fc.final_();
                    fc.type(JType.INT);
                    fc.init(JExprs.decimal(16));
                    fc.docComment(dc -> dc.text("The default initial capacity."));
                });

                // instance fields
                cc.field("items", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(listType);
                    fc.docComment(dc -> dc.text("The backing list of items."));
                });

                cc.field("name", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                    fc.docComment(dc -> dc.text("The container name."));
                });

                cc.field("locked", fc -> {
                    fc.private_();
                    fc.volatile_();
                    fc.type(JType.BOOLEAN);
                    fc.docComment(dc -> dc.text("Whether the container is locked."));
                });

                // static initializer
                cc.staticInit(b -> {
                    b.lineComment("static initializer");
                    b.emit(JExprs.callStatic(
                        JTypes.typeNamed("java.lang.System"), "setProperty",
                        JExprs.str("container.version"), JExprs.str("1.0")));
                });

                // instance initializer
                cc.instanceInit(b -> {
                    b.emit(JExprs.$v("locked").assign(JExpr.FALSE));
                });

                // default constructor
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the default capacity.");
                    });
                    ctor.body(b -> {
                        b.callThis(JExprs.str("unnamed"), JExprs.$v("DEFAULT_CAPACITY"));
                    });
                });

                // parameterized constructor with throws
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the given name and capacity.");
                    });
                    ctor.param("name", JType.STRING, p -> {
                        p.docComment(dc -> dc.text("the container name"));
                    });
                    ctor.param("capacity", JType.INT, p -> {
                        p.docComment(dc -> dc.text("the initial capacity"));
                    });
                    ctor.throws_(illegalArgType);
                    ctor.body(b -> {
                        b.if_(JExprs.$v("capacity").le(JExpr.ZERO), then -> {
                            then.throw_(JExprs.new_(illegalArgType, JExprs.str("capacity must be positive")));
                        });
                        b.emit(JExpr.THIS.field("name").assign(
                            JExprs.callStatic(objectsType, "requireNonNull", JExprs.$v("name"))));
                        b.emit(JExpr.THIS.field("items").assign(
                            JExprs.new_(arrayListType, JExprs.$v("capacity"))));
                    });
                });

                // varargs constructor
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.annotate(suppressWarningsType, a -> {
                        a.value(JExprs.str("unchecked"));
                    });
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the given items.");
                    });
                    ctor.varargParam("items", JTypes.typeNamed("T"), p -> {
                        p.docComment(dc -> dc.text("the initial items"));
                    });
                    ctor.body(b -> {
                        b.callThis(JExprs.str("unnamed"), JExprs.$v("items").field("length"));
                        b.forEach(JTypes.typeNamed("T"), "item", JExprs.$v("items"), loop -> {
                            loop.emit(JExpr.THIS.field("items").call("add", JExprs.$v("item")));
                        });
                    });
                });

                // getter method
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.docComment(dc -> {
                        dc.text("Returns the container name.");
                        dc.return_("the name");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("name"));
                    });
                });

                // setter method with validation
                cc.method("setName", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Sets the container name.");
                        dc.throws_(illegalStateType, "if the container is locked");
                    });
                    mc.param("name", JType.STRING, p -> {
                        p.docComment(dc -> dc.text("the new name"));
                    });
                    mc.throws_(illegalStateType);
                    mc.body(b -> {
                        b.emit(JExprs.call("checkNotLocked"));
                        b.emit(JExpr.THIS.field("name").assign(JExprs.$v("name")));
                    });
                });

                // synchronized method
                cc.method("add", mc -> {
                    mc.public_();
                    mc.synchronized_();
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Adds an item to the container.");
                        dc.return_("true if the item was added");
                    });
                    mc.param("item", JTypes.typeNamed("T"), p -> {
                        p.docComment(dc -> dc.text("the item to add"));
                    });
                    mc.body(b -> {
                        b.emit(JExprs.call("checkNotLocked"));
                        b.return_(JExprs.$v("items").call("add", JExprs.$v("item")));
                    });
                });

                // method with type parameter
                cc.method("transform", mc -> {
                    mc.public_();
                    mc.typeParam("R", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(listType);
                    mc.docComment(dc -> {
                        dc.text("Transforms all items using the given function.");
                        dc.return_(c -> {
                            c.text("a new list of transformed items");
                        });
                    });
                    mc.param("fn", JTypes.typeNamed("java.util.function.Function"), p -> {
                        p.docComment(dc -> dc.text("the transformation function"));
                    });
                    mc.body(b -> {
                        b.var(listType, "result", JExprs.new_(arrayListType, JExprs.$v("items").call("size")));
                        b.forEach(JTypes.typeNamed("T"), "item", JExprs.$v("items"), loop -> {
                            loop.emit(JExprs.$v("result").call("add", JExprs.$v("fn").call("apply", JExprs.$v("item"))));
                        });
                        b.return_(JExprs.$v("result"));
                    });
                });

                // static factory method
                cc.method("empty", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("E", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(JTypes.typeNamed("com.example.model.Container"));
                    mc.docComment(dc -> {
                        dc.text("Creates an empty container.");
                        dc.return_("a new empty container");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.new_(JTypes.typeNamed("com.example.model.Container")));
                    });
                });

                // private helper method
                cc.method("checkNotLocked", mc -> {
                    mc.private_();
                    mc.docComment(dc -> {
                        dc.text("Checks that the container is not locked.");
                        dc.throws_(illegalStateType, "if the container is locked");
                    });
                    mc.body(b -> {
                        b.if_(JExprs.$v("locked"), then -> {
                            then.throw_(JExprs.new_(illegalStateType, JExprs.str("container is locked")));
                        });
                    });
                });

                // compareTo
                cc.method("compareTo", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(JType.INT);
                    mc.param("other", JTypes.typeNamed("com.example.model.Container"));
                    mc.body(b -> {
                        b.return_(JExprs.$v("name").call("compareTo", JExprs.$v("other").field("name")));
                    });
                });

                // toString
                cc.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.str("Container{").call("concat", JExprs.$v("name"))
                            .call("concat", JExprs.str("}")));
                    });
                });

                // nested enum
                cc.enum_("State", ec -> {
                    ec.public_();
                    ec.docComment(dc -> dc.text("Container lifecycle states."));
                    ec.constant("OPEN", c -> {});
                    ec.constant("CLOSED", c -> {});
                    ec.constant("LOCKED", c -> {});
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Container");
    }

    // ── Comprehensive interface ─────────────────────────────────────────

    /**
     * Generates a comprehensive sealed interface exercising: generics,
     * superinterfaces, permits, abstract methods, default methods,
     * static methods, constant fields, doc comments, and annotations.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void comprehensiveInterface() throws IOException {
        final JType iterableType = JTypes.typeNamed("java.lang.Iterable");
        final JType streamType = JTypes.typeNamed("java.util.stream.Stream");
        final JType optionalType = JTypes.typeNamed("java.util.Optional");
        final JType comparatorType = JTypes.typeNamed("java.util.Comparator");
        final JType overrideType = JTypes.typeNamed("java.lang.Override");

        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.spi", "Repository", sf -> {
            sf.import_(streamType);
            sf.import_(optionalType);
            sf.import_(comparatorType);

            sf.interface_("Repository", ic -> {
                ic.public_();
                ic.sealed_();
                ic.docComment(dc -> {
                    dc.text("A sealed repository interface for entity persistence.");
                    dc.text(" Supports CRUD operations and querying.");
                    dc.since("2.0");
                });
                ic.typeParam("T", tp -> {});
                ic.typeParam("ID", tp -> {
                    tp.extends_(JTypes.typeNamed("java.io.Serializable"));
                });
                ic.extends_(iterableType);
                ic.permits(JTypes.typeNamed("com.example.spi.AbstractRepository"));
                ic.permits(JTypes.typeNamed("com.example.spi.ReadOnlyRepository"));

                // constant fields
                ic.field("DEFAULT_PAGE_SIZE", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExprs.decimal(25));
                    fc.docComment(dc -> dc.text("Default page size for paginated queries."));
                });

                ic.field("MAX_BATCH_SIZE", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExprs.decimal(1000));
                    fc.docComment(dc -> dc.text("Maximum batch size for bulk operations."));
                });

                // abstract methods (no body)
                ic.method("findById", mc -> {
                    mc.returning(optionalType);
                    mc.docComment(dc -> {
                        dc.text("Finds an entity by its identifier.");
                        dc.return_("an optional containing the entity, or empty");
                    });
                    mc.param("id", JTypes.typeNamed("ID"), p -> {
                        p.docComment(dc -> dc.text("the entity identifier"));
                    });
                });

                ic.method("save", mc -> {
                    mc.returning(JTypes.typeNamed("T"));
                    mc.docComment(dc -> {
                        dc.text("Saves the given entity.");
                        dc.return_("the saved entity");
                    });
                    mc.param("entity", JTypes.typeNamed("T"), p -> {
                        p.docComment(dc -> dc.text("the entity to save"));
                    });
                });

                ic.method("deleteById", mc -> {
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Deletes an entity by its identifier.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the entity was deleted");
                        });
                    });
                    mc.param("id", JTypes.typeNamed("ID"), p -> {
                        p.docComment(dc -> dc.text("the entity identifier"));
                    });
                });

                ic.method("findAll", mc -> {
                    mc.returning(streamType);
                    mc.docComment(dc -> {
                        dc.text("Returns all entities as a stream.");
                        dc.return_("a stream of all entities");
                    });
                });

                ic.method("count", mc -> {
                    mc.returning(JType.LONG);
                    mc.docComment(dc -> {
                        dc.text("Returns the total number of entities.");
                        dc.return_("the entity count");
                    });
                });

                // default methods
                ic.method("existsById", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether an entity with the given ID exists.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the entity exists");
                        });
                    });
                    mc.param("id", JTypes.typeNamed("ID"), p -> {
                        p.docComment(dc -> dc.text("the entity identifier"));
                    });
                    mc.body(b -> {
                        b.return_(JExprs.call("findById", JExprs.$v("id")).call("isPresent"));
                    });
                });

                ic.method("isEmpty", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether the repository is empty.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the repository has no entities");
                        });
                    });
                    mc.body(b -> {
                        b.return_(JExprs.call("count").eq(JExpr.ZERO));
                    });
                });

                // static method
                ic.method("naturalOrder", mc -> {
                    mc.static_();
                    mc.typeParam("E", tp -> {
                        tp.extends_(JTypes.typeNamed("java.lang.Comparable"));
                    });
                    mc.returning(comparatorType);
                    mc.docComment(dc -> {
                        dc.text("Returns a comparator that uses natural ordering.");
                        dc.return_("a natural-order comparator");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.methodRef(JTypes.typeNamed("Comparable"), "compareTo"));
                    });
                });

                // nested interface
                ic.interface_("Listener", lic -> {
                    lic.public_();
                    lic.docComment(dc -> dc.text("Listener for repository change events."));
                    lic.typeParam("E", tp -> {});

                    lic.method("onSave", mc -> {
                        mc.docComment(dc -> dc.text("Called after an entity is saved."));
                        mc.param("entity", JTypes.typeNamed("E"));
                    });

                    lic.method("onDelete", mc -> {
                        mc.docComment(dc -> dc.text("Called after an entity is deleted."));
                        mc.param("id", JType.OBJECT);
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Repository");
    }

    // ── Comprehensive enum ──────────────────────────────────────────────

    /**
     * Generates a comprehensive enum exercising: interfaces, constants
     * with arguments and anonymous class bodies, fields, multiple
     * constructors, methods (abstract overrides in constants, instance
     * methods, static methods), doc comments, and annotations.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void comprehensiveEnum() throws IOException {
        final JType overrideType = JTypes.typeNamed("java.lang.Override");

        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.lang", "Operator", sf -> {
            sf.enum_("Operator", ec -> {
                ec.public_();
                ec.docComment(dc -> {
                    dc.text("Arithmetic operators with symbolic representation");
                    dc.text(" and evaluation logic.");
                    dc.since("1.0");
                });
                ec.implements_(JTypes.typeNamed("java.util.function.BinaryOperator"));

                // constants with args and anonymous class bodies
                ec.constant("ADD", c -> {
                    c.arg(JExprs.str("+"));
                    c.arg(JExprs.decimal(1));
                    c.docComment(dc -> dc.text("Addition operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(JType.OBJECT);
                            mc.param("a", JType.OBJECT);
                            mc.param("b", JType.OBJECT);
                            mc.body(b -> {
                                b.return_(JExprs.$v("a").cast(JTypes.typeNamed("Number"))
                                    .call("doubleValue")
                                    .add(JExprs.$v("b").cast(JTypes.typeNamed("Number"))
                                        .call("doubleValue")).paren().cast(JType.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("SUBTRACT", c -> {
                    c.arg(JExprs.str("-"));
                    c.arg(JExprs.decimal(1));
                    c.docComment(dc -> dc.text("Subtraction operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(JType.OBJECT);
                            mc.param("a", JType.OBJECT);
                            mc.param("b", JType.OBJECT);
                            mc.body(b -> {
                                b.return_(JExprs.$v("a").cast(JTypes.typeNamed("Number"))
                                    .call("doubleValue")
                                    .sub(JExprs.$v("b").cast(JTypes.typeNamed("Number"))
                                        .call("doubleValue")).paren().cast(JType.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("MULTIPLY", c -> {
                    c.arg(JExprs.str("*"));
                    c.arg(JExprs.decimal(2));
                    c.docComment(dc -> dc.text("Multiplication operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(JType.OBJECT);
                            mc.param("a", JType.OBJECT);
                            mc.param("b", JType.OBJECT);
                            mc.body(b -> {
                                b.return_(JExprs.$v("a").cast(JTypes.typeNamed("Number"))
                                    .call("doubleValue")
                                    .mul(JExprs.$v("b").cast(JTypes.typeNamed("Number"))
                                        .call("doubleValue")).paren().cast(JType.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("DIVIDE", c -> {
                    c.arg(JExprs.str("/"));
                    c.arg(JExprs.decimal(2));
                    c.docComment(dc -> dc.text("Division operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(JType.OBJECT);
                            mc.param("a", JType.OBJECT);
                            mc.param("b", JType.OBJECT);
                            mc.body(b -> {
                                b.return_(JExprs.$v("a").cast(JTypes.typeNamed("Number"))
                                    .call("doubleValue")
                                    .div(JExprs.$v("b").cast(JTypes.typeNamed("Number"))
                                        .call("doubleValue")).paren().cast(JType.OBJECT));
                            });
                        });
                    });
                });

                // fields
                ec.field("symbol", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.STRING);
                    fc.docComment(dc -> dc.text("The operator symbol."));
                });

                ec.field("precedence", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.INT);
                    fc.docComment(dc -> dc.text("The operator precedence level."));
                });

                // constructor
                ec.constructor(ctor -> {
                    ctor.docComment(dc -> dc.text("Constructs an operator."));
                    ctor.param("symbol", JType.STRING, p -> {
                        p.docComment(dc -> dc.text("the symbol"));
                    });
                    ctor.param("precedence", JType.INT, p -> {
                        p.docComment(dc -> dc.text("the precedence"));
                    });
                    ctor.body(b -> {
                        b.emit(JExpr.THIS.field("symbol").assign(JExprs.$v("symbol")));
                        b.emit(JExpr.THIS.field("precedence").assign(JExprs.$v("precedence")));
                    });
                });

                // instance methods
                ec.method("symbol", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.docComment(dc -> {
                        dc.text("Returns the operator symbol.");
                        dc.return_("the symbol string");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("symbol"));
                    });
                });

                ec.method("precedence", mc -> {
                    mc.public_();
                    mc.returning(JType.INT);
                    mc.docComment(dc -> {
                        dc.text("Returns the operator precedence.");
                        dc.return_("the precedence level");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("precedence"));
                    });
                });

                // static method
                ec.method("fromSymbol", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.returning(JTypes.typeNamed("com.example.lang.Operator"));
                    mc.docComment(dc -> {
                        dc.text("Returns the operator for the given symbol.");
                        dc.return_("the matching operator");
                        dc.throws_(JTypes.typeNamed("java.lang.IllegalArgumentException"),
                            "if no operator matches");
                    });
                    mc.param("symbol", JType.STRING, p -> {
                        p.docComment(dc -> dc.text("the operator symbol"));
                    });
                    mc.throws_(JTypes.typeNamed("java.lang.IllegalArgumentException"));
                    mc.body(b -> {
                        b.forEach(JTypes.typeNamed("com.example.lang.Operator"), "op",
                            JExprs.call("values"), loop -> {
                                loop.if_(JExprs.$v("op").field("symbol").call("equals", JExprs.$v("symbol")), then -> {
                                    then.return_(JExprs.$v("op"));
                                });
                            });
                        b.throw_(JExprs.new_(JTypes.typeNamed("java.lang.IllegalArgumentException"),
                            JExprs.str("Unknown operator: ").call("concat", JExprs.$v("symbol"))));
                    });
                });

                // toString override
                ec.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.$v("symbol"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Operator");
    }

    // ── Comprehensive record ────────────────────────────────────────────

    /**
     * Generates a comprehensive record exercising: components, compact
     * constructor with validation, interfaces, type parameters, static
     * fields, custom methods, doc comments, and annotations.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void comprehensiveRecord() throws IOException {
        final JType overrideType = JTypes.typeNamed("java.lang.Override");
        final JType comparableType = JTypes.typeNamed("java.lang.Comparable");
        final JType illegalArgType = JTypes.typeNamed("java.lang.IllegalArgumentException");

        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.data", "Interval", sf -> {
            sf.record_("Interval", rc -> {
                rc.public_();
                rc.docComment(dc -> {
                    dc.text("An immutable interval between two comparable bounds.");
                    dc.text(" The lower bound must be less than or equal to the upper bound.");
                    dc.since("3.0");
                });
                rc.typeParam("T", tp -> {
                    tp.extends_(comparableType);
                });
                rc.implements_(comparableType);

                // components
                rc.component("lower", JTypes.typeNamed("T"), comp -> {
                    comp.docComment(dc -> dc.text("the lower bound (inclusive)"));
                });
                rc.component("upper", JTypes.typeNamed("T"), comp -> {
                    comp.docComment(dc -> dc.text("the upper bound (inclusive)"));
                });

                // static fields
                rc.field("EMPTY_COUNT", fc -> {
                    fc.private_();
                    fc.static_();
                    fc.type(JType.INT);
                    fc.init(JExpr.ZERO);
                    fc.docComment(dc -> dc.text("Counter for empty interval creation."));
                });

                // compact constructor
                rc.compactConstructor(b -> {
                    b.if_(JExprs.$v("lower").call("compareTo", JExprs.$v("upper")).gt(JExpr.ZERO), then -> {
                        then.throw_(JExprs.new_(illegalArgType,
                            JExprs.str("lower bound must not exceed upper bound")));
                    });
                });

                // instance methods
                rc.method("contains", mc -> {
                    mc.public_();
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether this interval contains the given value.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the value is within the interval");
                        });
                    });
                    mc.param("value", JTypes.typeNamed("T"), p -> {
                        p.docComment(dc -> dc.text("the value to test"));
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("lower").call("compareTo", JExprs.$v("value")).le(JExpr.ZERO)
                            .and(JExprs.$v("upper").call("compareTo", JExprs.$v("value")).ge(JExpr.ZERO)));
                    });
                });

                rc.method("overlaps", mc -> {
                    mc.public_();
                    mc.returning(JType.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether this interval overlaps with another.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the intervals overlap");
                        });
                    });
                    mc.param("other", JTypes.typeNamed("com.example.data.Interval"), p -> {
                        p.docComment(dc -> dc.text("the other interval"));
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("lower").call("compareTo", JExprs.$v("other").call("upper")).le(JExpr.ZERO)
                            .and(JExprs.$v("upper").call("compareTo", JExprs.$v("other").call("lower")).ge(JExpr.ZERO)));
                    });
                });

                rc.method("compareTo", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(JType.INT);
                    mc.param("other", JTypes.typeNamed("com.example.data.Interval"));
                    mc.body(b -> {
                        b.var(JType.INT, "cmp", JExprs.$v("lower").call("compareTo", JExprs.$v("other").call("lower")));
                        b.if_(JExprs.$v("cmp").ne(JExpr.ZERO), then -> {
                            then.return_(JExprs.$v("cmp"));
                        });
                        b.return_(JExprs.$v("upper").call("compareTo", JExprs.$v("other").call("upper")));
                    });
                });

                // static factory methods
                rc.method("of", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("C", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(JTypes.typeNamed("com.example.data.Interval"));
                    mc.docComment(dc -> {
                        dc.text("Creates an interval from the given bounds.");
                        dc.return_("a new interval");
                    });
                    mc.param("lower", JTypes.typeNamed("C"));
                    mc.param("upper", JTypes.typeNamed("C"));
                    mc.body(b -> {
                        b.return_(JExprs.new_(JTypes.typeNamed("com.example.data.Interval"),
                            JExprs.$v("lower"), JExprs.$v("upper")));
                    });
                });

                rc.method("singleton", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("C", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(JTypes.typeNamed("com.example.data.Interval"));
                    mc.docComment(dc -> {
                        dc.text("Creates a single-point interval.");
                        dc.return_("an interval where lower equals upper");
                    });
                    mc.param("value", JTypes.typeNamed("C"));
                    mc.body(b -> {
                        b.return_(JExprs.new_(JTypes.typeNamed("com.example.data.Interval"),
                            JExprs.$v("value"), JExprs.$v("value")));
                    });
                });

                // toString override
                rc.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.str("[").call("concat", JExprs.$v("lower").call("toString"))
                            .call("concat", JExprs.str(".."))
                            .call("concat", JExprs.$v("upper").call("toString"))
                            .call("concat", JExprs.str("]")));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Interval");
    }

    // ── Comprehensive annotation interface ──────────────────────────────

    /**
     * Generates a comprehensive annotation interface exercising: elements
     * with defaults (string, int, boolean, class, enum, annotation, array),
     * elements without defaults, constant fields, and doc comments.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void comprehensiveAnnotation() throws IOException {
        final JType retentionType = JTypes.typeNamed("java.lang.annotation.Retention");
        final JType retentionPolicyType = JTypes.typeNamed("java.lang.annotation.RetentionPolicy");
        final JType targetType = JTypes.typeNamed("java.lang.annotation.Target");
        final JType elementTypeType = JTypes.typeNamed("java.lang.annotation.ElementType");
        final JType documentedType = JTypes.typeNamed("java.lang.annotation.Documented");

        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.annotation", "RestEndpoint", sf -> {
            sf.import_(retentionType);
            sf.import_(retentionPolicyType);
            sf.import_(targetType);
            sf.import_(elementTypeType);
            sf.import_(documentedType);

            sf.annotationInterface_("RestEndpoint", ac -> {
                ac.public_();
                ac.docComment(dc -> {
                    dc.text("Marks a method as a REST endpoint with routing");
                    dc.text(" and content-type configuration.");
                    dc.since("2.0");
                });
                ac.annotate(documentedType);
                ac.annotate(retentionType, a -> {
                    a.value(JExprs.$v("RetentionPolicy").field("RUNTIME"));
                });
                ac.annotate(targetType, a -> {
                    a.value(JExprs.$v("ElementType").field("METHOD"));
                });

                // elements without defaults
                ac.element("path", JType.STRING);

                // elements with defaults
                ac.element("method", JType.STRING, JExprs.str("GET"));
                ac.element("produces", JType.STRING, JExprs.str("application/json"));
                ac.element("consumes", JType.STRING, JExprs.str("application/json"));
                ac.element("timeout", JType.INT, JExprs.decimal(30));
                ac.element("async", JType.BOOLEAN, JExpr.FALSE);

                // constant
                ac.constant("VERSION", fc -> {
                    fc.type(JType.STRING);
                    fc.init(JExprs.str("2.0"));
                    fc.docComment(dc -> dc.text("The API version."));
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("RestEndpoint");
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    /**
     * Loads an expected output file from test resources and compares it
     * against the generated source.
     *
     * @param fileName the file name without extension
     * @throws IOException if the resource cannot be loaded
     */
    private void assertMatchesExpected(final String fileName) throws IOException {
        final String actual = getSource(switch (fileName) {
            case "Container" -> "com.example.model";
            case "Repository" -> "com.example.spi";
            case "Operator" -> "com.example.lang";
            case "Interval" -> "com.example.data";
            case "RestEndpoint" -> "com.example.annotation";
            default -> throw new IllegalArgumentException("Unknown file: " + fileName);
        }, fileName);
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
        try (InputStream is = ComprehensiveWholeFileTest.class.getModule().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Expected resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
