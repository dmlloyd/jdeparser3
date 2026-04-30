package io.smallrye.jdeparser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.LambdaCreator;
import io.smallrye.jdeparser.creator.ParamCreator;

/**
 * Implementation of {@link LambdaCreator} that collects lambda parameter
 * declarations and a body, producing a {@link LambdaExpr}.
 * <p>
 * Parameters are tracked in a list of {@link LambdaExpr.LambdaParam} entries.
 * A three-state mode field enforces that typed and untyped parameters are
 * not mixed within the same lambda.
 */
public final class LambdaCreatorImpl extends AbstractCreator implements LambdaCreator {

    /** No parameters have been added yet. */
    private static final int MODE_UNDECIDED = 0;

    /** Untyped parameters have been added. */
    private static final int MODE_UNTYPED = 1;

    /** Typed parameters have been added. */
    private static final int MODE_TYPED = 2;

    /** The collected lambda parameters. */
    private final List<LambdaExpr.LambdaParam> params = new ArrayList<>();

    /** The parameter mode: undecided, untyped, or typed. */
    private int paramMode = MODE_UNDECIDED;

    /** The overridden source version, or {@code null} to use the default. */
    private SourceVersion overriddenVersion;

    /** The constructed block body. */
    private BlockCreatorImpl blockBody;

    /**
     * Constructs a new lambda creator with default source version.
     */
    public LambdaCreatorImpl() {
        super(SourceVersion.LATEST);
    }

    /** {@inheritDoc} */
    @Override
    public void sourceVersion(final SourceVersion version) {
        checkActive();
        Assert.checkNotNullParam("version", version);
        this.overriddenVersion = version;
    }

    /** {@inheritDoc} */
    @Override
    public Var param(final String name) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        if (paramMode == MODE_TYPED) {
            throw new IllegalStateException("Cannot add untyped parameter after typed parameter");
        }
        paramMode = MODE_UNTYPED;
        params.add(new LambdaExpr.LambdaParam(name));
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public Var param(final String name, final Type type) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        if (paramMode == MODE_UNTYPED) {
            throw new IllegalStateException("Cannot add typed parameter after untyped parameter");
        }
        paramMode = MODE_TYPED;
        registerUsedType(type);
        final ParamCreatorImpl pc = new ParamCreatorImpl(effectiveVersion(), name, type, false);
        pc.sourceFile(sourceFile());
        pc.finish();
        params.add(new LambdaExpr.LambdaParam(name, pc));
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public Var param(final String name, final Type type, final Consumer<ParamCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("builder", builder);
        if (paramMode == MODE_UNTYPED) {
            throw new IllegalStateException("Cannot add typed parameter after untyped parameter");
        }
        paramMode = MODE_TYPED;
        registerUsedType(type);
        final ParamCreatorImpl pc = new ParamCreatorImpl(effectiveVersion(), name, type, false);
        pc.sourceFile(sourceFile());
        nest(() -> builder.accept(pc));
        pc.finish();
        params.add(new LambdaExpr.LambdaParam(name, pc));
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl bc = new BlockCreatorImpl(effectiveVersion());
        bc.sourceFile(sourceFile());
        nest(() -> body.accept(bc));
        bc.finish();
        this.blockBody = bc;
    }

    /**
     * Returns the effective source version, using the overridden version
     * if one was set, or the default otherwise.
     *
     * @return the effective source version
     */
    private SourceVersion effectiveVersion() {
        return overriddenVersion != null ? overriddenVersion : version();
    }

    /**
     * Returns the collected parameters.
     *
     * @return the lambda parameters
     */
    public List<LambdaExpr.LambdaParam> params() {
        return params;
    }

    /**
     * Returns the constructed block body.
     *
     * @return the block body, or {@code null} if {@link #body(Consumer)} was not called
     */
    public BlockCreatorImpl blockBody() {
        return blockBody;
    }
}
