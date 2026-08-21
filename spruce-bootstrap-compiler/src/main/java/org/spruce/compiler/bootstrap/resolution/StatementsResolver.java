package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;

/**
 * A <code>StatementsResolver</code> is a <code>BasicResolver</code> that
 * resolves data types in the statements portion of an AST and Symbol Table:
 * local variable declarations, including in the "init"s in if, for, and while
 * statements.
 */
public class StatementsResolver extends BasicResolver {
    /**
     * Constructs a <code>StatementsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public StatementsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve all symbols in a <code>Block</code>.
     * @param block An <code>ASTBlock</code>.
     * @param ctx A <code>ResolutionContext</code> representing a method, a
     *            constructor, or an enclosing block.
     */
    public void resolveBlock(ASTBlock block, ResolutionContext ctx) {
        for (ASTBlockStatement blockStmt : block.getBlockStmts().getTypedChildren()) {
            // Some of these will need a new ResolutionContext for the scope that they
            // introduce, but others will not.
            switch (blockStmt) {
            case ASTLocalVariableDeclarationStatement localVarDeclStmt ->
                    resolveLocalVariableDeclarationStatement(localVarDeclStmt, ctx);
            case ASTBlock subBlock -> resolveSubBlock(subBlock, ctx);
            case ASTBasicForStatement basicForStmt -> resolveBasicForStatement(basicForStmt, ctx);
            case ASTConstructorInvocation constrInvocation -> getOperationsResolver().resolveConstructorInvocation(
                    constrInvocation, ctx);
            case ASTEnhancedForStatement enhancedForStmt -> resolveEnhancedForStatement(enhancedForStmt, ctx);
            case ASTExpressionStatement exprStmt -> resolveExpressionStatement(exprStmt, ctx);
            case ASTIfStatement ifStmt -> resolveIfStatement(ifStmt, ctx);
            case ASTReturnStatement returnStmt -> resolveReturnStatement(returnStmt, ctx);
            case ASTWhileStatement whileStmt -> resolveWhileStatement(whileStmt, ctx);
            // Any other statements with symbols that need resolution go above.

            // All other statements don't have any symbols to resolve.
            default -> {}
            }
        }
    }

    /**
     * Resolve all symbols in a <code>LocalVariableDeclarationStatement</code>.
     * @param localVarDeclStmt An <code>ASTLocalVariableDeclarationStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveLocalVariableDeclarationStatement(ASTLocalVariableDeclarationStatement localVarDeclStmt,
                                                         ResolutionContext ctx) {
        resolveLocalVariableDeclaration(localVarDeclStmt.getLocalVarDecl(), ctx);
    }

    /**
     * Resolve all symbols in a <code>LocalVariableDeclaration</code>.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveLocalVariableDeclaration(ASTLocalVariableDeclaration localVarDecl, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        ExpressionsResolver exprResolver = getExpressionsResolver();
        Optional<ASTDataType> optDt = localVarDecl.getLocalVarType().getDataType();
        if (optDt.isPresent()) {
            ASTDataType dt = optDt.get();
            typesResolver.resolveDataType(dt, ctx);

            for (ASTVariableDeclarator varDecl : localVarDecl.getVarDeclList().getTypedChildren()) {
                varDecl.getDeclSymbol().setDataType(dt.getResolvedDataType());
                varDecl.getVarInitializer().ifPresent(expr -> exprResolver.resolveExpression(expr, ctx));
            }
        }
        else {
            throw internalError("Resolution of auto data type is not implemented yet!");
        }
    }

    /**
     * Resolve all symbols in a sub-<code>Block</code>.
     * @param subBlock An <code>ASTBlock</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveSubBlock(ASTBlock subBlock, ResolutionContext ctx) {
        resolveBlock(subBlock, ctx.withEnclosingSymbol(subBlock.getDeclSymbol()));
    }

    /**
     * Resolve all symbols in a <code>BasicForStatement</code>.
     * @param basicForStmt An <code>ASTBasicForStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveBasicForStatement(ASTBasicForStatement basicForStmt, ResolutionContext ctx) {
        ResolutionContext forCtx = ctx.withEnclosingSymbol(basicForStmt.getDeclSymbol());
        if (basicForStmt.getInit().isPresent()) {
            ASTInit init = basicForStmt.getInit().get();
            resolveInit(init, forCtx);
        }
        if (basicForStmt.getValueExpr().isPresent()) {
            ASTValueExpression condition = basicForStmt.getValueExpr().get();
            getExpressionsResolver().resolveValueExpression(condition, forCtx);
        }
        resolveStatementExpressionList(basicForStmt.getStmtExprList(), forCtx);
        resolveBlock(basicForStmt.getBlock(), forCtx);
    }

    /**
     * Resolve all symbols in an <code>EnhancedForStatement</code>.
     * @param enhancedForStmt An <code>ASTEnhancedForStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveEnhancedForStatement(ASTEnhancedForStatement enhancedForStmt, ResolutionContext ctx) {
        ResolutionContext forCtx = ctx.withEnclosingSymbol(enhancedForStmt.getDeclSymbol());
        resolveLocalVariableDeclaration(enhancedForStmt.getLocalVarDecl(), forCtx);
        ASTValueExpression iterable = enhancedForStmt.getValueExpr();
        getExpressionsResolver().resolveValueExpression(iterable, ctx);
        resolveBlock(enhancedForStmt.getBlock(), forCtx);
    }

    /**
     * Resolve all symbols in an <code>ExpressionStatement</code>.
     * @param exprStmt An <code>ASTExpressionStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveExpressionStatement(ASTExpressionStatement exprStmt, ResolutionContext ctx) {
        resolveStatementExpression(exprStmt.getStmtExpr(), ctx);
    }

    /**
     * Resolve all symbols in an <code>IfStatement</code>.
     * @param ifStmt An <code>ASTIfStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveIfStatement(ASTIfStatement ifStmt, ResolutionContext ctx) {
        ResolutionContext ifCtx = ctx.withEnclosingSymbol(ifStmt.getDeclSymbol());
        if (ifStmt.getInit().isPresent()) {
            resolveInit(ifStmt.getInit().get(), ifCtx);
        }
        ASTValueExpression condition = ifStmt.getCondExpr();
        getExpressionsResolver().resolveValueExpression(condition, ifCtx);

        ASTBlock ifBlock = ifStmt.getIfBlock();
        resolveBlock(ifBlock, ctx.withEnclosingSymbol(ifBlock.getDeclSymbol()));

        if (ifStmt.getElseBlock().isPresent()) {
            ASTBlock elseBlock = ifStmt.getElseBlock().get();
            resolveBlock(elseBlock, ctx.withEnclosingSymbol(elseBlock.getDeclSymbol()));
        }
        else if (ifStmt.getElseIf().isPresent()){
            ASTIfStatement elseIfStmt = ifStmt.getElseIf().get();
            resolveIfStatement(elseIfStmt, ifCtx);
        }
    }

    /**
     * Resolve all symbols in a <code>ReturnStatement</code>.
     * @param returnStmt An <code>ASTReturnStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveReturnStatement(ASTReturnStatement returnStmt, ResolutionContext ctx) {
        if (returnStmt.getExpr().isPresent()) {
            ASTExpression expr = returnStmt.getExpr().get();
            getExpressionsResolver().resolveExpression(expr, ctx);
        }
    }

    /**
     * Resolve all symbols in a <code>WhileStatement</code>.
     * @param whileStmt An <code>ASTWhileStatement</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveWhileStatement(ASTWhileStatement whileStmt, ResolutionContext ctx) {
        ResolutionContext whileCtx = ctx.withEnclosingSymbol(whileStmt.getDeclSymbol());
        if (whileStmt.getInit().isPresent()) {
            resolveInit(whileStmt.getInit().get(), whileCtx);
        }
        ASTValueExpression condition = whileStmt.getValueExpr();
        getExpressionsResolver().resolveValueExpression(condition, whileCtx);

        resolveBlock(whileStmt.getBlock(), whileCtx);
    }

    /**
     * Resolve all symbols in an <code>Init</code>.
     * @param init An <code>ASTInit</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveInit(ASTInit init, ResolutionContext ctx) {
        switch (init) {
        case ASTLocalVariableDeclaration localVarDecl -> resolveLocalVariableDeclaration(localVarDecl, ctx);
        case ASTStatementExpressionList stmtExprList -> resolveStatementExpressionList(stmtExprList, ctx);
        }
    }

    /**
     * Resolve all symbols in a <code>StatementExpressionList</code>.
     * @param stmtExprList An <code>ASTStatementExpressionList</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveStatementExpressionList(ASTStatementExpressionList stmtExprList, ResolutionContext ctx) {
        for (ASTStatementExpression stmtExpr : stmtExprList.getTypedChildren()) {
            resolveStatementExpression(stmtExpr, ctx);
        }
    }

    /**
     * Resolve all symbols in a <code>StatementExpression</code>.
     * @param stmtExpr An <code>ASTStatementExpression</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveStatementExpression(ASTStatementExpression stmtExpr, ResolutionContext ctx) {
        switch(stmtExpr) {
        case ASTAssignment assignment -> resolveAssignment(assignment, ctx);
        case ASTMethodInvocation methodInvocation -> getOperationsResolver().resolveMethodInvocation(
                methodInvocation, ctx);
        case ASTClassInstanceCreationExpression cice ->
                getExpressionsResolver().resolveClassInstanceCreationExpression(cice, ctx);
        }
    }

    /**
     * Resolve all symbols in an <code>Assignment</code>.
     * @param assignment An <code>ASTAssignment</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveAssignment(ASTAssignment assignment, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        ASTLeftHandSide lhs = assignment.getLeftHandSide();
        exprResolver.resolveLeftHandSide(lhs, ctx);

        ASTExpression rightHandSide = assignment.getExpr();
        exprResolver.resolveExpression(rightHandSide, ctx);

        // TODO: In the analyzer, determine if this assignment is legal.
    }
}
