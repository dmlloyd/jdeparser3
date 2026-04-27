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
        final Type listType = Type.named("java.util.List");
        final Type arrayListType = Type.named("java.util.ArrayList");
        final Type serializableType = Type.named("java.io.Serializable");
        final Type comparableType = Type.named("java.lang.Comparable");
        final Type overrideType = Type.named("java.lang.Override");
        final Type suppressWarningsType = Type.named("java.lang.SuppressWarnings");
        final Type illegalArgType = Type.named("java.lang.IllegalArgumentException");
        final Type illegalStateType = Type.named("java.lang.IllegalStateException");
        final Type objectsType = Type.named("java.util.Objects");

        final Sources sources = createSources(SourceVersion.JAVA_17);
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
                    dc.see(Type.STRING);
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
                    fc.type(Type.LONG);
                    fc.init(Expr.decimal(1L));
                    fc.docComment(dc -> dc.text("Serial version UID."));
                });

                // static field with annotation
                cc.field("DEFAULT_CAPACITY", fc -> {
                    fc.public_();
                    fc.static_();
                    fc.final_();
                    fc.type(Type.INT);
                    fc.init(Expr.decimal(16));
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
                    fc.type(Type.STRING);
                    fc.docComment(dc -> dc.text("The container name."));
                });

                cc.field("locked", fc -> {
                    fc.private_();
                    fc.volatile_();
                    fc.type(Type.BOOLEAN);
                    fc.docComment(dc -> dc.text("Whether the container is locked."));
                });

                // static initializer
                cc.staticInit(b -> {
                    b.lineComment("static initializer");
                    final Type type = Type.named("java.lang.System");
                    b.emit(type.call("setProperty", Expr.str("container.version"), Expr.str("1.0")));
                });

                // instance initializer
                cc.instanceInit(b -> {
                    b.emit(Expr.$v("locked").assign(Expr.FALSE));
                });

                // default constructor
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the default capacity.");
                    });
                    ctor.body(b -> {
                        b.callThis(Expr.str("unnamed"), Expr.$v("DEFAULT_CAPACITY"));
                    });
                });

                // parameterized constructor with throws
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the given name and capacity.");
                    });
                    ctor.param("name", Type.STRING, p -> {
                        p.docComment(dc -> dc.text("the container name"));
                    });
                    ctor.param("capacity", Type.INT, p -> {
                        p.docComment(dc -> dc.text("the initial capacity"));
                    });
                    ctor.throws_(illegalArgType);
                    ctor.body(b -> {
                        b.if_(Expr.$v("capacity").le(Expr.ZERO), then -> {
                            then.throw_(illegalArgType.new_(List.of(new Expr[] { Expr.str("capacity must be positive") })));
                        });
                        b.emit(Expr.THIS.field("name").assign(
                                objectsType.call("requireNonNull", Expr.$v("name"))));
                        b.emit(Expr.THIS.field("items").assign(
                                arrayListType.new_(List.of(new Expr[] { Expr.$v("capacity") }))));
                    });
                });

                // varargs constructor
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.annotate(suppressWarningsType, a -> {
                        a.value(Expr.str("unchecked"));
                    });
                    ctor.docComment(dc -> {
                        dc.text("Creates a container with the given items.");
                    });
                    ctor.varargParam("items", Type.named("T"), p -> {
                        p.docComment(dc -> dc.text("the initial items"));
                    });
                    ctor.body(b -> {
                        b.callThis(Expr.str("unnamed"), Expr.$v("items").field("length"));
                        b.forEach(Type.named("T"), "item", Expr.$v("items"), loop -> {
                            loop.emit(Expr.THIS.field("items").call("add", Expr.$v("item")));
                        });
                    });
                });

                // getter method
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.docComment(dc -> {
                        dc.text("Returns the container name.");
                        dc.return_("the name");
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("name"));
                    });
                });

                // setter method with validation
                cc.method("setName", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Sets the container name.");
                        dc.throws_(illegalStateType, "if the container is locked");
                    });
                    mc.param("name", Type.STRING, p -> {
                        p.docComment(dc -> dc.text("the new name"));
                    });
                    mc.throws_(illegalStateType);
                    mc.body(b -> {
                        b.emit(Expr.callPlain("checkNotLocked"));
                        b.emit(Expr.THIS.field("name").assign(Expr.$v("name")));
                    });
                });

                // synchronized method
                cc.method("add", mc -> {
                    mc.public_();
                    mc.synchronized_();
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Adds an item to the container.");
                        dc.return_("true if the item was added");
                    });
                    mc.param("item", Type.named("T"), p -> {
                        p.docComment(dc -> dc.text("the item to add"));
                    });
                    mc.body(b -> {
                        b.emit(Expr.callPlain("checkNotLocked"));
                        b.return_(Expr.$v("items").call("add", Expr.$v("item")));
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
                    mc.param("fn", Type.named("java.util.function.Function"), p -> {
                        p.docComment(dc -> dc.text("the transformation function"));
                    });
                    mc.body(b -> {
                        b.var(listType, "result", arrayListType.new_(List.of(new Expr[] { Expr.$v("items").call("size") })));
                        b.forEach(Type.named("T"), "item", Expr.$v("items"), loop -> {
                            loop.emit(Expr.$v("result").call("add", Expr.$v("fn").call("apply", Expr.$v("item"))));
                        });
                        b.return_(Expr.$v("result"));
                    });
                });

                // static factory method
                cc.method("empty", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("E", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(Type.named("com.example.model.Container"));
                    mc.docComment(dc -> {
                        dc.text("Creates an empty container.");
                        dc.return_("a new empty container");
                    });
                    mc.body(b -> {
                        final Type type = Type.named("com.example.model.Container");
                        b.return_(type.new_(List.of(new Expr[] {})));
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
                        b.if_(Expr.$v("locked"), then -> {
                            then.throw_(illegalStateType.new_(List.of(new Expr[] { Expr.str("container is locked") })));
                        });
                    });
                });

                // compareTo
                cc.method("compareTo", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(Type.INT);
                    mc.param("other", Type.named("com.example.model.Container"));
                    mc.body(b -> {
                        b.return_(Expr.$v("name").call("compareTo", Expr.$v("other").field("name")));
                    });
                });

                // toString
                cc.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.str("Container{").call("concat", Expr.$v("name"))
                                .call("concat", Expr.str("}")));
                    });
                });

                // nested enum
                cc.enum_("State", ec -> {
                    ec.public_();
                    ec.docComment(dc -> dc.text("Container lifecycle states."));
                    ec.constant("OPEN", c -> {
                    });
                    ec.constant("CLOSED", c -> {
                    });
                    ec.constant("LOCKED", c -> {
                    });
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
        final Type iterableType = Type.named("java.lang.Iterable");
        final Type streamType = Type.named("java.util.stream.Stream");
        final Type optionalType = Type.named("java.util.Optional");
        final Type comparatorType = Type.named("java.util.Comparator");
        Type.named("java.lang.Override");

        final Sources sources = createSources(SourceVersion.JAVA_17);
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
                ic.typeParam("T", tp -> {
                });
                ic.typeParam("ID", tp -> {
                    tp.extends_(Type.named("java.io.Serializable"));
                });
                ic.extends_(iterableType);
                ic.permits(Type.named("com.example.spi.AbstractRepository"));
                ic.permits(Type.named("com.example.spi.ReadOnlyRepository"));

                // constant fields
                ic.field("DEFAULT_PAGE_SIZE", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.decimal(25));
                    fc.docComment(dc -> dc.text("Default page size for paginated queries."));
                });

                ic.field("MAX_BATCH_SIZE", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.decimal(1000));
                    fc.docComment(dc -> dc.text("Maximum batch size for bulk operations."));
                });

                // abstract methods (no body)
                ic.method("findById", mc -> {
                    mc.returning(optionalType);
                    mc.docComment(dc -> {
                        dc.text("Finds an entity by its identifier.");
                        dc.return_("an optional containing the entity, or empty");
                    });
                    mc.param("id", Type.named("ID"), p -> {
                        p.docComment(dc -> dc.text("the entity identifier"));
                    });
                });

                ic.method("save", mc -> {
                    mc.returning(Type.named("T"));
                    mc.docComment(dc -> {
                        dc.text("Saves the given entity.");
                        dc.return_("the saved entity");
                    });
                    mc.param("entity", Type.named("T"), p -> {
                        p.docComment(dc -> dc.text("the entity to save"));
                    });
                });

                ic.method("deleteById", mc -> {
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Deletes an entity by its identifier.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the entity was deleted");
                        });
                    });
                    mc.param("id", Type.named("ID"), p -> {
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
                    mc.returning(Type.LONG);
                    mc.docComment(dc -> {
                        dc.text("Returns the total number of entities.");
                        dc.return_("the entity count");
                    });
                });

                // default methods
                ic.method("existsById", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether an entity with the given ID exists.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the entity exists");
                        });
                    });
                    mc.param("id", Type.named("ID"), p -> {
                        p.docComment(dc -> dc.text("the entity identifier"));
                    });
                    mc.body(b -> {
                        b.return_(Expr.callPlain("findById", Expr.$v("id")).call("isPresent"));
                    });
                });

                ic.method("isEmpty", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether the repository is empty.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the repository has no entities");
                        });
                    });
                    mc.body(b -> {
                        b.return_(Expr.callPlain("count").eq(Expr.ZERO));
                    });
                });

                // static method
                ic.method("naturalOrder", mc -> {
                    mc.static_();
                    mc.typeParam("E", tp -> {
                        tp.extends_(Type.named("java.lang.Comparable"));
                    });
                    mc.returning(comparatorType);
                    mc.docComment(dc -> {
                        dc.text("Returns a comparator that uses natural ordering.");
                        dc.return_("a natural-order comparator");
                    });
                    mc.body(b -> {
                        final Type type = Type.named("Comparable");
                        b.return_(type.methodRef("compareTo"));
                    });
                });

                // nested interface
                ic.interface_("Listener", lic -> {
                    lic.public_();
                    lic.docComment(dc -> dc.text("Listener for repository change events."));
                    lic.typeParam("E", tp -> {
                    });

                    lic.method("onSave", mc -> {
                        mc.docComment(dc -> dc.text("Called after an entity is saved."));
                        mc.param("entity", Type.named("E"));
                    });

                    lic.method("onDelete", mc -> {
                        mc.docComment(dc -> dc.text("Called after an entity is deleted."));
                        mc.param("id", Type.OBJECT);
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
        final Type overrideType = Type.named("java.lang.Override");

        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example.lang", "Operator", sf -> {
            sf.enum_("Operator", ec -> {
                ec.public_();
                ec.docComment(dc -> {
                    dc.text("Arithmetic operators with symbolic representation");
                    dc.text(" and evaluation logic.");
                    dc.since("1.0");
                });
                ec.implements_(Type.named("java.util.function.BinaryOperator"));

                // constants with args and anonymous class bodies
                ec.constant("ADD", c -> {
                    c.arg(Expr.str("+"));
                    c.arg(Expr.decimal(1));
                    c.docComment(dc -> dc.text("Addition operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(Type.OBJECT);
                            mc.param("a", Type.OBJECT);
                            mc.param("b", Type.OBJECT);
                            mc.body(b -> {
                                b.return_(Expr.$v("a").cast(Type.named("Number"))
                                        .call("doubleValue")
                                        .add(Expr.$v("b").cast(Type.named("Number"))
                                                .call("doubleValue"))
                                        .paren().cast(Type.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("SUBTRACT", c -> {
                    c.arg(Expr.str("-"));
                    c.arg(Expr.decimal(1));
                    c.docComment(dc -> dc.text("Subtraction operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(Type.OBJECT);
                            mc.param("a", Type.OBJECT);
                            mc.param("b", Type.OBJECT);
                            mc.body(b -> {
                                b.return_(Expr.$v("a").cast(Type.named("Number"))
                                        .call("doubleValue")
                                        .sub(Expr.$v("b").cast(Type.named("Number"))
                                                .call("doubleValue"))
                                        .paren().cast(Type.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("MULTIPLY", c -> {
                    c.arg(Expr.str("*"));
                    c.arg(Expr.decimal(2));
                    c.docComment(dc -> dc.text("Multiplication operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(Type.OBJECT);
                            mc.param("a", Type.OBJECT);
                            mc.param("b", Type.OBJECT);
                            mc.body(b -> {
                                b.return_(Expr.$v("a").cast(Type.named("Number"))
                                        .call("doubleValue")
                                        .mul(Expr.$v("b").cast(Type.named("Number"))
                                                .call("doubleValue"))
                                        .paren().cast(Type.OBJECT));
                            });
                        });
                    });
                });

                ec.constant("DIVIDE", c -> {
                    c.arg(Expr.str("/"));
                    c.arg(Expr.decimal(2));
                    c.docComment(dc -> dc.text("Division operator."));
                    c.body(body -> {
                        body.method("apply", mc -> {
                            mc.public_();
                            mc.annotate(overrideType);
                            mc.returning(Type.OBJECT);
                            mc.param("a", Type.OBJECT);
                            mc.param("b", Type.OBJECT);
                            mc.body(b -> {
                                b.return_(Expr.$v("a").cast(Type.named("Number"))
                                        .call("doubleValue")
                                        .div(Expr.$v("b").cast(Type.named("Number"))
                                                .call("doubleValue"))
                                        .paren().cast(Type.OBJECT));
                            });
                        });
                    });
                });

                // fields
                ec.field("symbol", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.STRING);
                    fc.docComment(dc -> dc.text("The operator symbol."));
                });

                ec.field("precedence", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.INT);
                    fc.docComment(dc -> dc.text("The operator precedence level."));
                });

                // constructor
                ec.constructor(ctor -> {
                    ctor.docComment(dc -> dc.text("Constructs an operator."));
                    ctor.param("symbol", Type.STRING, p -> {
                        p.docComment(dc -> dc.text("the symbol"));
                    });
                    ctor.param("precedence", Type.INT, p -> {
                        p.docComment(dc -> dc.text("the precedence"));
                    });
                    ctor.body(b -> {
                        b.emit(Expr.THIS.field("symbol").assign(Expr.$v("symbol")));
                        b.emit(Expr.THIS.field("precedence").assign(Expr.$v("precedence")));
                    });
                });

                // instance methods
                ec.method("symbol", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.docComment(dc -> {
                        dc.text("Returns the operator symbol.");
                        dc.return_("the symbol string");
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("symbol"));
                    });
                });

                ec.method("precedence", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.docComment(dc -> {
                        dc.text("Returns the operator precedence.");
                        dc.return_("the precedence level");
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("precedence"));
                    });
                });

                // static method
                ec.method("fromSymbol", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.returning(Type.named("com.example.lang.Operator"));
                    mc.docComment(dc -> {
                        dc.text("Returns the operator for the given symbol.");
                        dc.return_("the matching operator");
                        dc.throws_(Type.named("java.lang.IllegalArgumentException"),
                                "if no operator matches");
                    });
                    mc.param("symbol", Type.STRING, p -> {
                        p.docComment(dc -> dc.text("the operator symbol"));
                    });
                    mc.throws_(Type.named("java.lang.IllegalArgumentException"));
                    mc.body(b -> {
                        b.forEach(Type.named("com.example.lang.Operator"), "op",
                                Expr.callPlain("values"), loop -> {
                                    loop.if_(Expr.$v("op").field("symbol").call("equals", Expr.$v("symbol")), then -> {
                                        then.return_(Expr.$v("op"));
                                    });
                                });
                        final Type type = Type.named("java.lang.IllegalArgumentException");
                        b.throw_(type.new_(
                                List.of(new Expr[] { Expr.str("Unknown operator: ").call("concat", Expr.$v("symbol")) })));
                    });
                });

                // toString override
                ec.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.$v("symbol"));
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
        final Type overrideType = Type.named("java.lang.Override");
        final Type comparableType = Type.named("java.lang.Comparable");
        final Type illegalArgType = Type.named("java.lang.IllegalArgumentException");

        final Sources sources = createSources(SourceVersion.JAVA_17);
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
                rc.component("lower", Type.named("T"), comp -> {
                    comp.docComment(dc -> dc.text("the lower bound (inclusive)"));
                });
                rc.component("upper", Type.named("T"), comp -> {
                    comp.docComment(dc -> dc.text("the upper bound (inclusive)"));
                });

                // static fields
                rc.field("EMPTY_COUNT", fc -> {
                    fc.private_();
                    fc.static_();
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
                    fc.docComment(dc -> dc.text("Counter for empty interval creation."));
                });

                // compact constructor
                rc.compactConstructor(b -> {
                    b.if_(Expr.$v("lower").call("compareTo", Expr.$v("upper")).gt(Expr.ZERO), then -> {
                        then.throw_(illegalArgType
                                .new_(List.of(new Expr[] { Expr.str("lower bound must not exceed upper bound") })));
                    });
                });

                // instance methods
                rc.method("contains", mc -> {
                    mc.public_();
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether this interval contains the given value.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the value is within the interval");
                        });
                    });
                    mc.param("value", Type.named("T"), p -> {
                        p.docComment(dc -> dc.text("the value to test"));
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("lower").call("compareTo", Expr.$v("value")).le(Expr.ZERO)
                                .and(Expr.$v("upper").call("compareTo", Expr.$v("value")).ge(Expr.ZERO)));
                    });
                });

                rc.method("overlaps", mc -> {
                    mc.public_();
                    mc.returning(Type.BOOLEAN);
                    mc.docComment(dc -> {
                        dc.text("Checks whether this interval overlaps with another.");
                        dc.return_(c -> {
                            c.code("true");
                            c.text(" if the intervals overlap");
                        });
                    });
                    mc.param("other", Type.named("com.example.data.Interval"), p -> {
                        p.docComment(dc -> dc.text("the other interval"));
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("lower").call("compareTo", Expr.$v("other").call("upper")).le(Expr.ZERO)
                                .and(Expr.$v("upper").call("compareTo", Expr.$v("other").call("lower")).ge(Expr.ZERO)));
                    });
                });

                rc.method("compareTo", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(Type.INT);
                    mc.param("other", Type.named("com.example.data.Interval"));
                    mc.body(b -> {
                        b.var(Type.INT, "cmp", Expr.$v("lower").call("compareTo", Expr.$v("other").call("lower")));
                        b.if_(Expr.$v("cmp").ne(Expr.ZERO), then -> {
                            then.return_(Expr.$v("cmp"));
                        });
                        b.return_(Expr.$v("upper").call("compareTo", Expr.$v("other").call("upper")));
                    });
                });

                // static factory methods
                rc.method("of", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("C", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(Type.named("com.example.data.Interval"));
                    mc.docComment(dc -> {
                        dc.text("Creates an interval from the given bounds.");
                        dc.return_("a new interval");
                    });
                    mc.param("lower", Type.named("C"));
                    mc.param("upper", Type.named("C"));
                    mc.body(b -> {
                        final Type type = Type.named("com.example.data.Interval");
                        b.return_(type.new_(List.of(new Expr[] { Expr.$v("lower"), Expr.$v("upper") })));
                    });
                });

                rc.method("singleton", mc -> {
                    mc.public_();
                    mc.static_();
                    mc.typeParam("C", tp -> {
                        tp.extends_(comparableType);
                    });
                    mc.returning(Type.named("com.example.data.Interval"));
                    mc.docComment(dc -> {
                        dc.text("Creates a single-point interval.");
                        dc.return_("an interval where lower equals upper");
                    });
                    mc.param("value", Type.named("C"));
                    mc.body(b -> {
                        final Type type = Type.named("com.example.data.Interval");
                        b.return_(type.new_(List.of(new Expr[] { Expr.$v("value"), Expr.$v("value") })));
                    });
                });

                // toString override
                rc.method("toString", mc -> {
                    mc.public_();
                    mc.annotate(overrideType);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.str("[").call("concat", Expr.$v("lower").call("toString"))
                                .call("concat", Expr.str(".."))
                                .call("concat", Expr.$v("upper").call("toString"))
                                .call("concat", Expr.str("]")));
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
        final Type retentionType = Type.named("java.lang.annotation.Retention");
        final Type retentionPolicyType = Type.named("java.lang.annotation.RetentionPolicy");
        final Type targetType = Type.named("java.lang.annotation.Target");
        final Type elementTypeType = Type.named("java.lang.annotation.ElementType");
        final Type documentedType = Type.named("java.lang.annotation.Documented");

        final Sources sources = createSources(SourceVersion.JAVA_17);
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
                    a.value(Expr.$v("RetentionPolicy").field("RUNTIME"));
                });
                ac.annotate(targetType, a -> {
                    a.value(Expr.$v("ElementType").field("METHOD"));
                });

                // elements without defaults
                ac.element("path", Type.STRING);

                // elements with defaults
                ac.element("method", Type.STRING, Expr.str("GET"));
                ac.element("produces", Type.STRING, Expr.str("application/json"));
                ac.element("consumes", Type.STRING, Expr.str("application/json"));
                ac.element("timeout", Type.INT, Expr.decimal(30));
                ac.element("async", Type.BOOLEAN, Expr.FALSE);

                // constant
                ac.constant("VERSION", fc -> {
                    fc.type(Type.STRING);
                    fc.init(Expr.str("2.0"));
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
