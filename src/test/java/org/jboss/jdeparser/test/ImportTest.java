package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for automatic import resolution and {@code java.lang} auto-importing.
 * <p>
 * Verifies that:
 * <ul>
 *   <li>{@code java.lang} types are automatically written with simple names
 *       and do not produce import statements</li>
 *   <li>Explicitly imported types are written with simple names and produce
 *       the corresponding import statement</li>
 *   <li>Non-imported types from other packages remain fully qualified</li>
 *   <li>Same-package types are written with simple names without import
 *       statements</li>
 * </ul>
 */
class ImportTest extends AbstractGeneratingTestCase {

    // ── java.lang auto-import ───────────────────────────────────────────

    /**
     * Verifies that {@code java.lang.String} is written with its simple
     * name {@code String} without requiring an explicit import.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void javaLangStringUsesSimpleName() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeting", sf -> {
            sf.class_("Greeting", cc -> {
                cc.public_();
                cc.field("message", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.STRING);
                });
                cc.method("getMessage", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.$v("message"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeting");
        assertNotNull(source);
        assertTrue(source.contains("private final String message;"),
                "java.lang.String field should use simple name 'String', got:\n" + source);
        assertTrue(source.contains("public String getMessage()"),
                "java.lang.String return type should use simple name 'String', got:\n" + source);
        assertFalse(source.contains("java.lang.String"),
                "java.lang.String should not appear fully qualified, got:\n" + source);
    }

    /**
     * Verifies that {@code java.lang.Object} is written with its simple
     * name {@code Object} without requiring an explicit import.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void javaLangObjectUsesSimpleName() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Container", sf -> {
            sf.class_("Container", cc -> {
                cc.public_();
                cc.field("value", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Container");
        assertTrue(source.contains("private Object value;"),
                "java.lang.Object field should use simple name 'Object', got:\n" + source);
        assertFalse(source.contains("java.lang.Object"),
                "java.lang.Object should not appear fully qualified, got:\n" + source);
    }

    /**
     * Verifies that boxed primitive types from {@code java.lang} (such as
     * {@code Integer}) are written with their simple names.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void javaLangBoxedTypesUseSimpleNames() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BoxedFields", sf -> {
            sf.class_("BoxedFields", cc -> {
                cc.public_();
                cc.field("count", fc -> {
                    fc.private_();
                    fc.type(JType.INT.box());
                });
                cc.field("flag", fc -> {
                    fc.private_();
                    fc.type(JType.BOOLEAN.box());
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "BoxedFields");
        assertTrue(source.contains("private Integer count;"),
                "java.lang.Integer should use simple name 'Integer', got:\n" + source);
        assertTrue(source.contains("private Boolean flag;"),
                "java.lang.Boolean should use simple name 'Boolean', got:\n" + source);
        assertFalse(source.contains("java.lang."),
                "No java.lang types should appear fully qualified, got:\n" + source);
    }

    /**
     * Verifies that no import statement is generated for {@code java.lang} types
     * since they are implicitly imported.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void javaLangTypesDoNotGenerateImportStatements() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NoImports", sf -> {
            sf.class_("NoImports", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                });
                cc.field("value", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NoImports");
        assertFalse(source.contains("import java.lang."),
                "No import statement should be generated for java.lang types, got:\n" + source);
    }

    /**
     * Verifies that explicitly importing a {@code java.lang} type does not
     * produce a redundant import statement, but still uses the simple name.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitJavaLangImportIsRedundantButHarmless() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Redundant", sf -> {
            sf.import_(JType.STRING);
            sf.class_("Redundant", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Redundant");
        assertTrue(source.contains("private String name;"),
                "Should still use simple name 'String', got:\n" + source);
        assertFalse(source.contains("import java.lang.String;"),
                "Should not generate redundant import for java.lang.String, got:\n" + source);
    }

    // ── Explicit imports ────────────────────────────────────────────────

    /**
     * Verifies that an explicitly imported type from another package is
     * written with its simple name and produces a corresponding import statement.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitImportUsesSimpleName() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "WithImport", sf -> {
            sf.import_(listType);
            sf.class_("WithImport", cc -> {
                cc.public_();
                cc.field("items", fc -> {
                    fc.private_();
                    fc.type(listType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "WithImport");
        assertTrue(source.contains("import java.util.List;"),
                "Should contain import statement for java.util.List, got:\n" + source);
        assertTrue(source.contains("private List items;"),
                "Imported type should use simple name 'List', got:\n" + source);
        assertFalse(source.contains("java.util.List items"),
                "Imported type should not appear fully qualified in body, got:\n" + source);
    }

    /**
     * Verifies that a type which is NOT imported remains fully qualified
     * in the generated output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void nonImportedTypeRemainsFullyQualified() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NoImport", sf -> {
            sf.class_("NoImport", cc -> {
                cc.public_();
                cc.field("items", fc -> {
                    fc.private_();
                    fc.type(listType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NoImport");
        assertTrue(source.contains("private java.util.List items;"),
                "Non-imported type should remain fully qualified, got:\n" + source);
        assertFalse(source.contains("import java.util.List;"),
                "Should not contain import statement for non-imported type, got:\n" + source);
    }

    /**
     * Verifies that multiple explicit imports all produce import statements
     * and use simple names.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void multipleExplicitImports() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JType mapType = JTypes.typeNamed("java.util.Map");
        final JType setType = JTypes.typeNamed("java.util.Set");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MultiImport", sf -> {
            sf.import_(listType);
            sf.import_(mapType);
            sf.import_(setType);
            sf.class_("MultiImport", cc -> {
                cc.public_();
                cc.field("list", fc -> {
                    fc.private_();
                    fc.type(listType);
                });
                cc.field("map", fc -> {
                    fc.private_();
                    fc.type(mapType);
                });
                cc.field("set", fc -> {
                    fc.private_();
                    fc.type(setType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "MultiImport");
        assertTrue(source.contains("import java.util.List;"),
                "Should contain import for List, got:\n" + source);
        assertTrue(source.contains("import java.util.Map;"),
                "Should contain import for Map, got:\n" + source);
        assertTrue(source.contains("import java.util.Set;"),
                "Should contain import for Set, got:\n" + source);
        assertTrue(source.contains("private List list;"),
                "List should use simple name, got:\n" + source);
        assertTrue(source.contains("private Map map;"),
                "Map should use simple name, got:\n" + source);
        assertTrue(source.contains("private Set set;"),
                "Set should use simple name, got:\n" + source);
    }

    /**
     * Verifies that an imported parameterized type uses simple names for
     * both the raw type and its type arguments when all are imported or
     * from {@code java.lang}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void importedParameterizedTypeUsesSimpleNames() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "GenericField", sf -> {
            sf.import_(listType);
            sf.class_("GenericField", cc -> {
                cc.public_();
                cc.field("names", fc -> {
                    fc.private_();
                    fc.type(listType.typeArg(JType.STRING));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "GenericField");
        assertTrue(source.contains("import java.util.List;"),
                "Should contain import for List, got:\n" + source);
        assertTrue(source.contains("private List<String> names;"),
                "Parameterized type should use simple names for both raw type and type argument, got:\n" + source);
    }

    /**
     * Verifies that a type imported via {@link Class} object uses simple
     * names in the generated output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void importViaClassObjectUsesSimpleName() throws IOException {
        final JType listType = JTypes.typeOf(java.util.List.class);
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ClassImport", sf -> {
            sf.import_(java.util.List.class);
            sf.class_("ClassImport", cc -> {
                cc.public_();
                cc.field("items", fc -> {
                    fc.private_();
                    fc.type(listType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ClassImport");
        assertTrue(source.contains("import java.util.List;"),
                "Should contain import for java.util.List, got:\n" + source);
        assertTrue(source.contains("private List items;"),
                "Class-imported type should use simple name, got:\n" + source);
    }

    // ── Same-package types ──────────────────────────────────────────────

    /**
     * Verifies that a type in the same package as the source file is written
     * with its simple name and does not produce an import statement.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void samePackageTypeUsesSimpleName() throws IOException {
        final JType peerType = JTypes.typeNamed("com.example.Peer");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SamePackage", sf -> {
            sf.class_("SamePackage", cc -> {
                cc.public_();
                cc.field("peer", fc -> {
                    fc.private_();
                    fc.type(peerType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SamePackage");
        assertTrue(source.contains("private Peer peer;"),
                "Same-package type should use simple name 'Peer', got:\n" + source);
        assertFalse(source.contains("import com.example.Peer;"),
                "Same-package type should not generate an import statement, got:\n" + source);
    }

    // ── Import precedence and conflicts (JLS §6.5.2, §7.5.1) ─────────

    /**
     * Verifies that an explicit single-type-import shadows a {@code java.lang}
     * type with the same simple name.
     * <p>
     * Per JLS §7.5.1, a single-type-import declaration shadows types inherited
     * from {@code java.lang}.  When {@code com.example.String} is explicitly
     * imported, {@code java.lang.String} must be written fully qualified.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitImportShadowsJavaLangType() throws IOException {
        final JType customString = JTypes.typeNamed("com.example.String");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("org.test", "Shadowed", sf -> {
            sf.import_(customString);
            sf.class_("Shadowed", cc -> {
                cc.public_();
                // explicit import: should use simple name
                cc.field("custom", fc -> {
                    fc.private_();
                    fc.type(customString);
                });
                // java.lang.String: shadowed, must be fully qualified
                cc.field("standard", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("org.test", "Shadowed");
        assertTrue(source.contains("import com.example.String;"),
                "Should contain import for com.example.String, got:\n" + source);
        assertTrue(source.contains("private String custom;"),
                "Explicitly imported type should use simple name, got:\n" + source);
        assertTrue(source.contains("private java.lang.String standard;"),
                "Shadowed java.lang.String must remain fully qualified, got:\n" + source);
    }

    /**
     * Verifies that an explicit single-type-import shadows a same-package
     * type with the same simple name.
     * <p>
     * Per JLS §7.5.1, a single-type-import declaration shadows types declared
     * in the same package.  When {@code com.other.Peer} is explicitly imported
     * into package {@code com.example}, a same-package {@code com.example.Peer}
     * must be written fully qualified.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitImportShadowsSamePackageType() throws IOException {
        final JType importedPeer = JTypes.typeNamed("com.other.Peer");
        final JType samePackagePeer = JTypes.typeNamed("com.example.Peer");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "PeerConflict", sf -> {
            sf.import_(importedPeer);
            sf.class_("PeerConflict", cc -> {
                cc.public_();
                // explicit import: should use simple name
                cc.field("imported", fc -> {
                    fc.private_();
                    fc.type(importedPeer);
                });
                // same-package type: shadowed by import, must be fully qualified
                cc.field("local", fc -> {
                    fc.private_();
                    fc.type(samePackagePeer);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "PeerConflict");
        assertTrue(source.contains("import com.other.Peer;"),
                "Should contain import for com.other.Peer, got:\n" + source);
        assertTrue(source.contains("private Peer imported;"),
                "Explicitly imported type should use simple name, got:\n" + source);
        assertTrue(source.contains("private com.example.Peer local;"),
                "Shadowed same-package type must remain fully qualified, got:\n" + source);
    }

    /**
     * Verifies that an explicit import of a type whose simple name matches
     * a {@code java.lang} type causes only the imported type to use the simple
     * name, while other {@code java.lang} types that do NOT conflict remain
     * unaffected.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void shadowingOnlyAffectsConflictingName() throws IOException {
        final JType customString = JTypes.typeNamed("com.example.String");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("org.test", "PartialShadow", sf -> {
            sf.import_(customString);
            sf.class_("PartialShadow", cc -> {
                cc.public_();
                // java.lang.String is shadowed
                cc.field("standard", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                });
                // java.lang.Object is NOT shadowed — no conflict
                cc.field("obj", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
                // the explicitly imported type
                cc.field("custom", fc -> {
                    fc.private_();
                    fc.type(customString);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("org.test", "PartialShadow");
        assertTrue(source.contains("private java.lang.String standard;"),
                "Shadowed java.lang.String must be fully qualified, got:\n" + source);
        assertTrue(source.contains("private Object obj;"),
                "Non-conflicting java.lang.Object should still use simple name, got:\n" + source);
        assertTrue(source.contains("private String custom;"),
                "Explicitly imported type should use simple name, got:\n" + source);
    }

    /**
     * Verifies that when both a same-package type and a {@code java.lang} type
     * share the same simple name and there is no explicit single-type-import,
     * the same-package type takes precedence and uses the simple name.
     * <p>
     * Per JLS §6.5.2, same-package types are resolved at step 3, before
     * on-demand imports ({@code java.lang.*}) at step 4.  The same-package
     * type shadows the {@code java.lang} type, which must be fully qualified.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void samePackageTypeShadowsJavaLangType() throws IOException {
        final JType samePackageObject = JTypes.typeNamed("com.example.Object");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SamePackageWins", sf -> {
            sf.class_("SamePackageWins", cc -> {
                cc.public_();
                // same-package type: takes precedence per JLS step 3
                cc.field("local", fc -> {
                    fc.private_();
                    fc.type(samePackageObject);
                });
                // java.lang.Object: shadowed by same-package type, must be fully qualified
                cc.field("standard", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SamePackageWins");
        assertTrue(source.contains("private Object local;"),
                "Same-package type should use simple name (JLS step 3 precedence), got:\n" + source);
        assertTrue(source.contains("private java.lang.Object standard;"),
                "Shadowed java.lang type must be fully qualified, got:\n" + source);
    }

    /**
     * Verifies that an explicit single-type-import of a same-package type
     * that conflicts with a {@code java.lang} type produces the same result
     * as implicit same-package resolution: the same-package type uses the
     * simple name and the {@code java.lang} type must be fully qualified.
     * <p>
     * Per JLS §6.5.2, same-package types already take precedence over
     * {@code java.lang} on-demand imports (step 3 before step 4).  An
     * explicit single-type-import of the same-package type is redundant
     * but harmless, and the result should be the same.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitImportOfSamePackageTypeWithJavaLangConflict() throws IOException {
        final JType samePackageObject = JTypes.typeNamed("com.example.Object");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Disambiguated", sf -> {
            sf.import_(samePackageObject);
            sf.class_("Disambiguated", cc -> {
                cc.public_();
                // explicitly imported same-package type: should use simple name
                cc.field("local", fc -> {
                    fc.private_();
                    fc.type(samePackageObject);
                });
                // java.lang.Object: shadowed, must be fully qualified
                cc.field("standard", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Disambiguated");
        assertTrue(source.contains("private Object local;"),
                "Same-package type should use simple name, got:\n" + source);
        assertTrue(source.contains("private java.lang.Object standard;"),
                "Shadowed java.lang.Object must be fully qualified, got:\n" + source);
    }

    /**
     * Verifies that explicitly importing {@code java.lang.Object} itself
     * resolves an ambiguity with a same-package type of the same simple name.
     * <p>
     * The explicit import of the {@code java.lang} type should cause it to use
     * the simple name, while the same-package type must be fully qualified.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void explicitJavaLangImportResolvesAmbiguityWithSamePackage() throws IOException {
        final JType samePackageObject = JTypes.typeNamed("com.example.Object");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "PreferLang", sf -> {
            // explicitly import java.lang.Object to disambiguate
            sf.import_(JType.OBJECT);
            sf.class_("PreferLang", cc -> {
                cc.public_();
                // java.lang.Object: explicitly imported, should use simple name
                cc.field("standard", fc -> {
                    fc.private_();
                    fc.type(JType.OBJECT);
                });
                // same-package type: shadowed by explicit import, must be fully qualified
                cc.field("local", fc -> {
                    fc.private_();
                    fc.type(samePackageObject);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "PreferLang");
        assertTrue(source.contains("private Object standard;"),
                "Explicitly imported java.lang.Object should use simple name, got:\n" + source);
        assertTrue(source.contains("private com.example.Object local;"),
                "Shadowed same-package type must be fully qualified, got:\n" + source);
    }

    // ── Mixed scenarios ─────────────────────────────────────────────────

    /**
     * Verifies that a source file with a mix of {@code java.lang} types,
     * explicitly imported types, same-package types, and non-imported types
     * all resolve correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void mixedImportResolution() throws IOException {
        final JType listType = JTypes.typeNamed("java.util.List");
        final JType fileType = JTypes.typeNamed("java.io.File");
        final JType peerType = JTypes.typeNamed("com.example.Peer");
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Mixed", sf -> {
            sf.import_(listType);
            sf.class_("Mixed", cc -> {
                cc.public_();
                // java.lang type: should use simple name automatically
                cc.field("name", fc -> {
                    fc.private_();
                    fc.type(JType.STRING);
                });
                // explicitly imported type: should use simple name
                cc.field("items", fc -> {
                    fc.private_();
                    fc.type(listType);
                });
                // same-package type: should use simple name
                cc.field("peer", fc -> {
                    fc.private_();
                    fc.type(peerType);
                });
                // non-imported type: should remain fully qualified
                cc.field("file", fc -> {
                    fc.private_();
                    fc.type(fileType);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Mixed");
        // java.lang
        assertTrue(source.contains("private String name;"),
                "java.lang.String should use simple name, got:\n" + source);
        assertFalse(source.contains("java.lang.String"),
                "java.lang.String should not appear fully qualified, got:\n" + source);
        // explicit import
        assertTrue(source.contains("import java.util.List;"),
                "Should have import for List, got:\n" + source);
        assertTrue(source.contains("private List items;"),
                "Imported List should use simple name, got:\n" + source);
        // same-package
        assertTrue(source.contains("private Peer peer;"),
                "Same-package type should use simple name, got:\n" + source);
        assertFalse(source.contains("import com.example.Peer;"),
                "Same-package type should not have import, got:\n" + source);
        // non-imported
        assertTrue(source.contains("private java.io.File file;"),
                "Non-imported type should remain fully qualified, got:\n" + source);
    }
}
