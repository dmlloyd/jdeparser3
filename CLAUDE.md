# JDeparser 3

A Java source code generation library using the borrow pattern.

## Build

```
mvn clean verify
```

Requires JDK 25+, compiles with `--release 17`.

## Architecture

- `io.smallrye.jdeparser` — public API: entry point (`JDeparser`), core types (`Type`, `Expr`, `Var`, `Exprs`, `Types`), source management (`Sources`), version control (`SourceVersion`, `LanguageFeature`)
- `io.smallrye.jdeparser.creator` — borrow-pattern API interfaces (`BlockCreator`, `ClassCreator`, `MethodCreator`, etc.)
- `io.smallrye.jdeparser.format` — public formatting configuration (`FormatPreferences`, `Filer`)
- `io.smallrye.jdeparser.impl` — internal implementations (not exported from module)

## Design Principles

- Borrow pattern (consumer/callback API) for all structural constructs
- Expressions are composable values; structural constructs use `Consumer<XxxCreator>` callbacks
- Version-gated feature validation via `SourceVersion.supports(LanguageFeature)`
- Formatting engine adapted from jdeparser2
