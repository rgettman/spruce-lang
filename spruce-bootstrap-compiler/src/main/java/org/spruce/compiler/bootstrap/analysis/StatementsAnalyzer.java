package org.spruce.compiler.bootstrap.analysis;

import org.spruce.compiler.bootstrap.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * A <code>StatementsAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * the data flow within blocks and statements.
 */
public class StatementsAnalyzer extends BasicAnalyzer {

    /**
     * Constructs a <code>StatementsAnalyzer</code>.
     * @param analyzer An <code>Analyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public StatementsAnalyzer(Analyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * <p>Analyzes a <code>Block</code>.</p>
     * <p>If it's a constructor block, enforce a maximum
     * of one constructor invocation.  A constructor cannot invoke itself,
     * directly or indirectly.  Before an explicit constructor invocation:</p>
     * <ul>
     * <li>It cannot reference <code>self</code>,</li>
     * <li>It cannot access a field of a superclass.</li>
     * <li>It cannot call a method in the superclass or the superclass of an
     * enclosing class.</li>
     * <li>It cannot contain a <code>return</code> statement.</li>
     * <li>Assignment to an instance field is permitted, as long as it isn't
     * already initialized in its own declaration.</li>
     * </ul>
     * <p>If it's a method block:</p>
     * <ul>
     * <li>All execution paths in a non-void method must return something.</li>
     * <li>Every local variable must be assigned before it is referenced.</li>
     * <li>Every statement must be reachable.</li>
     * </ul>
     * @param block An <code>ASTBlock</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeBlock(ASTBlock block, AnalysisContext ctx) {
        boolean allPathsHaveReturn = analyzeBlockHelper(block, ctx);
        if (!allPathsHaveReturn && !ctx.isReturningVoid()) {
            error(block.getEndLocation(), "Missing return statement.");
        }
    }

    /**
     * A helper method to analyze a <code>Block</code>, everything besides
     * erroring for a missing <code>return</code> statement.
     * @param block An <code>ASTBlock</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return Whether all execution paths withing the block have a
     *     <code>return</code> statements.
     */
    public boolean analyzeBlockHelper(ASTBlock block, AnalysisContext ctx) {
        boolean allPathsHaveReturnSoFar = false;
        boolean onlyUnreachablesLeft = false;
        boolean unreachableErrorProduced = false;

        for (ASTBlockStatement blockStmt : block.getBlockStmts().getTypedChildren()) {
            if (onlyUnreachablesLeft && !unreachableErrorProduced && (!ctx.isInConstructor() || ctx.isPastPrologue())) {
                error(blockStmt.getLocation(), "Unreachable statement.");
                unreachableErrorProduced = true;
            }

            allPathsHaveReturnSoFar |= switch (blockStmt) {
            case ASTBlock subBlock -> analyzeSubBlock(subBlock, ctx);
            case ASTBasicForStatement basicForStmt -> analyzeBasicForStatement(basicForStmt, ctx);
            case ASTHasTarget continueStmt -> {
                onlyUnreachablesLeft = true;
                yield analyzeHasTarget(continueStmt, ctx);
            }
            case ASTConstructorInvocation constrInvocation -> {
                // TODO: Consider moving this if statement into analyzeConstructorInvocation.
                if (!ctx.isInConstructor()) {
                    error(constrInvocation.getLocation(), "Constructor invocation not allowed in a method body.");
                }
                else if (ctx.isPastPrologue()) {
                    error(constrInvocation.getLocation(), "Only one explicit constructor invocation allowed.");
                }
                ctx = ctx.pastPrologue();
                yield analyzeConstructorInvocation(constrInvocation, ctx);
            }
            case ASTEnhancedForStatement enhancedForStmt -> analyzeEnhancedForStatement(enhancedForStmt, ctx);
            case ASTExpressionStatement expressionStmt -> analyzeStatementExpression(expressionStmt.getStmtExpr(), ctx);
            case ASTIfStatement ifStmt -> analyzeIfStatement(ifStmt, ctx);
            case ASTLocalVariableDeclarationStatement localVarDeclStmt ->
                    analyzeLocalVariableDeclarationStatement(localVarDeclStmt, ctx);
            case ASTReturnStatement returnStmt -> {
                onlyUnreachablesLeft = true;
                yield analyzeReturnStatement(returnStmt, ctx);
            }
            case ASTWhileStatement whileStmt -> analyzeWhileStatement(whileStmt, ctx);
            };
        }

        return allPathsHaveReturnSoFar;
    }

    /**
     * Analyzes a sub-<code>Block</code>.
     * @param subBlock An <code>ASTBlock</code> representing a sub-block.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return Whether all execution paths in the given block have a return
     *     statement.
     */
    public boolean analyzeSubBlock(ASTBlock subBlock, AnalysisContext ctx) {
        AnalysisContext subBlockCtx = ctx.withSubscope();
        return analyzeBlockHelper(subBlock, subBlockCtx);
    }

    /**
     * Analyzes a <code>BasicForStatement</code>.
     * @param basicForStmt An <code>ASTBasicForStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  Even if there is a <code>return</code>
     *     statement in the block, it's not guaranteed to execute at all.
     */
    public boolean analyzeBasicForStatement(ASTBasicForStatement basicForStmt, AnalysisContext ctx) {
        AnalysisContext basicForCtx = ctx.withTargetedSubscope(basicForStmt);

        if (basicForStmt.getInit().isPresent()) {
            analyzeInit(basicForStmt.getInit().get(), basicForCtx);
        }
        if (basicForStmt.getValueExpr().isPresent()) {
            getExpressionsAnalyzer().analyzeExpression(basicForStmt.getValueExpr().get(), basicForCtx);
        }

        // A local variable may get initialized in the block, and then referred
        // to legally in the statement expression list.
        // Create a new sub-scoped Analysis Context just for the block and the
        // statement expression list.  They may or may not run.
        AnalysisContext subCtx = basicForCtx.withSubscope();
        analyzeBlockHelper(basicForStmt.getBlock(), subCtx);
        analyzeStatementExpressionList(basicForStmt.getStmtExprList(), subCtx);


        // The block is never guaranteed to execute.
        return false;
    }

    /**
     * Analyzes a <code>ConstructorInvocation</code>.
     * @param constrInvocation An <code>ASTConstructorInvocation</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  This isn't the return statement you're
     *     looking for.
     */
    public boolean analyzeConstructorInvocation(ASTConstructorInvocation constrInvocation, AnalysisContext ctx) {
        ExpressionsAnalyzer exprAnalyzer = getExpressionsAnalyzer();
        for (ASTExpression arg : constrInvocation.getArgumentList().getTypedChildren()) {
            exprAnalyzer.analyzeExpression(arg, ctx);
        }

        return false;
    }

    /**
     * Analyzes an <code>EnhancedForStatement</code>.
     * @param enhancedForStmt An <code>ASTEnhancedForStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  Even if there is a <code>return</code>
     *     statement in the block, it's not guaranteed to execute at all.
     */
    public boolean analyzeEnhancedForStatement(ASTEnhancedForStatement enhancedForStmt, AnalysisContext ctx) {
        AnalysisContext enhancedForCtx = ctx.withTargetedSubscope(enhancedForStmt);

        // At this point, it's already validated that there is exactly one
        // variable declarator here.
        ASTVariableDeclarator loopVar = enhancedForStmt.getLocalVarDecl().getVarDeclList().get(0);
        analyzeLocalVariableDeclarator(loopVar, enhancedForCtx);
        // Even though there is no initializer, this variable declaration must
        // be treated as an initialized local variable!
        ctx.initializedLocalVars().add(loopVar.getDeclSymbol());
        getExpressionsAnalyzer().analyzeValueExpression(enhancedForStmt.getValueExpr(), enhancedForCtx);

        // Create a new sub-scoped Analysis Context just for the block, which
        // may or may not run.
        analyzeBlockHelper(enhancedForStmt.getBlock(), enhancedForCtx.withSubscope());

        // The block is never guaranteed to execute.
        return false;
    }

    /**
     * Analyzes a <code>StatementExpressionList</code>.
     * @param stmtExprList An <code>ASTStatementExpressionList</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeStatementExpressionList(ASTStatementExpressionList stmtExprList, AnalysisContext ctx) {
        for (ASTStatementExpression stmtExpr : stmtExprList.getTypedChildren()) {
            analyzeStatementExpression(stmtExpr, ctx);
        }
    }

    /**
     * Analyzes a <code>StatementExpression</code>.
     * @param stmtExpr An <code>ASTStatementExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  This isn't the return statement you're
     *     looking for.
     */
    public boolean analyzeStatementExpression(ASTStatementExpression stmtExpr, AnalysisContext ctx) {
        // TODO: Check constructor prologue restrictions!
        switch (stmtExpr) {
        case ASTAssignment assignment -> analyzeAssignment(assignment, ctx);
        case ASTClassInstanceCreationExpression cice ->
                getExpressionsAnalyzer().analyzeClassInstanceCreationExpression(cice, ctx);
        case ASTMethodInvocation methodInvocation ->
                getExpressionsAnalyzer().analyzeMethodInvocation(methodInvocation, ctx);
        }

        return false;
    }

    /**
     * Analyzes an <code>Assignment</code>.
     * @param assignment An <code>ASTAssignment</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeAssignment(ASTAssignment assignment, AnalysisContext ctx) {
        ExpressionsAnalyzer exprAnalyzer = getExpressionsAnalyzer();
        ASTLeftHandSide lhs = assignment.getLeftHandSide();
        exprAnalyzer.analyzeLeftHandSide(lhs, ctx);
        exprAnalyzer.analyzeExpression(assignment.getExpr(), ctx);

        if (lhs.getResolvedEntity() instanceof VariableSymbol varSymbol && varSymbol.isLocal()) {
            ctx.initializedLocalVars().add(varSymbol);
        }
    }

    /**
     * Analyze a <code>HasTarget</code>, a break or continue statement.
     * @param hasTarget An <code>ASTHasTarget</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  This isn't the return statement you're
     *     looking for.
     */
    public boolean analyzeHasTarget(ASTHasTarget hasTarget, AnalysisContext ctx) {
        if (ctx.getTarget().isPresent()) {
            hasTarget.setTarget(ctx.target());
        }
        else {
            error(hasTarget.getLocation(), "Not allowed outside of for or while statement.");
        }
        return false;
    }

    /**
     * Analyzes an <code>IfStatement</code>.
     * @param ifStmt An <code>ASTIfStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return Whether all execution paths have a return statement.
     */
    public boolean analyzeIfStatement(ASTIfStatement ifStmt, AnalysisContext ctx) {
        AnalysisContext ifCtx = ctx.withSubscope();

        if (ifStmt.getInit().isPresent()) {
            analyzeInit(ifStmt.getInit().get(), ifCtx);
        }
        getExpressionsAnalyzer().analyzeValueExpression(ifStmt.getCondExpr(), ifCtx);

        // Create a new sub-scoped Analysis Context for each block, which may
        // or may not run.
        boolean allPathsHaveReturnSoFar = analyzeBlockHelper(ifStmt.getIfBlock(), ifCtx.withSubscope());
        if (ifStmt.getElseBlock().isPresent()) {
            allPathsHaveReturnSoFar |= analyzeBlockHelper(ifStmt.getElseBlock().get(), ifCtx.withSubscope());
        }
        else if (ifStmt.getElseIf().isPresent()) {
            allPathsHaveReturnSoFar |= analyzeIfStatement(ifStmt.getElseIf().get(), ifCtx.withSubscope());
        }

        return allPathsHaveReturnSoFar;
    }

    /**
     * Analyzes <code>LocalVariableDeclarationStatement</code>.
     * @param localVarDeclStmt An <code>ASTLocalVariableDeclarationStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  This isn't the return statement you're
     *     looking for.
     */
    public boolean analyzeLocalVariableDeclarationStatement(ASTLocalVariableDeclarationStatement localVarDeclStmt,
                                                            AnalysisContext ctx) {
        for (ASTVariableDeclarator varDecl : localVarDeclStmt.getLocalVarDecl().getVarDeclList().getTypedChildren()) {
            analyzeLocalVariableDeclarator(varDecl, ctx);
        }

        return false;
    }

    /**
     * Analyzes a <code>LocalVariableDeclarator</code>.  Keeps track of
     * initialized local variables that are initialized.
     * @param varDecl An <code>ASTVariableDeclarator</code> representing a
     *                local variable declaration.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeLocalVariableDeclarator(ASTVariableDeclarator varDecl, AnalysisContext ctx) {
        ExpressionsAnalyzer exprAnalyzer = getExpressionsAnalyzer();
        if (varDecl.getVarInitializer().isPresent()) {
            ctx.initializedLocalVars().add(varDecl.getDeclSymbol());
            exprAnalyzer.analyzeExpression(varDecl.getVarInitializer().get(), ctx);
        }
    }

    /**
     * Analyzes a <code>WhileStatement</code>.
     * @param whileStmt An <code>ASTWhileStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>false</code>.  Even if there is a <code>return</code>
     *     statement in the block, it's not guaranteed to execute at all.
     */
    public boolean analyzeWhileStatement(ASTWhileStatement whileStmt, AnalysisContext ctx) {
        AnalysisContext whileCtx = ctx.withTargetedSubscope(whileStmt);

        if (whileStmt.getInit().isPresent()) {
            analyzeInit(whileStmt.getInit().get(), whileCtx);
        }
        getExpressionsAnalyzer().analyzeValueExpression(whileStmt.getValueExpr(), whileCtx);

        // Create a new sub-scoped Analysis Context just for the block, which
        // may or may not run.
        analyzeBlockHelper(whileStmt.getBlock(), whileCtx.withSubscope());

        // The block is never guaranteed to execute.
        return false;
    }

    /**
     * Analyzes an <code>Init</code>.
     * @param init An <code>ASTInit</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeInit(ASTInit init, AnalysisContext ctx) {
        switch (init) {
        case ASTLocalVariableDeclaration localVarDecl -> {
            for (ASTVariableDeclarator varDecl : localVarDecl.getVarDeclList().getTypedChildren()) {
                analyzeLocalVariableDeclarator(varDecl, ctx);
            }
        }
        case ASTStatementExpressionList stmtExprList -> analyzeStatementExpressionList(stmtExprList, ctx);
        }
    }

    /**
     * Analyzes a <code>ReturnStatement</code>.
     * @param returnStmt An <code>ASTReturnStatement</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     * @return <code>true</code>.  This <em>is</em> the <code>return</code>
     *     statement you're looking for.
     */
    public boolean analyzeReturnStatement(ASTReturnStatement returnStmt, AnalysisContext ctx) {
        if (ctx.isInConstructor() && !ctx.isPastPrologue()) {
            error(returnStmt.getLocation(), "Return not allowed in early construction context.");
        }
        if (returnStmt.getExpr().isPresent()) {
            getExpressionsAnalyzer().analyzeExpression(returnStmt.getExpr().get(), ctx);
        }
        return true;
    }
}
