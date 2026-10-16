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
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing a method, a
     *            constructor, or an enclosing block.
     */
    public void resolveBlock(ASTBlock block, TypeSymbol targetType, ResolutionContext ctx) {
        for (ASTBlockStatement blockStmt : block.getBlockStmts().getTypedChildren()) {
            // Some of these will need a new ResolutionContext for the scope that they
            // introduce, but others will not.
            // Pass the target type down where matching to it is required.
            switch (blockStmt) {
            case ASTLocalVariableDeclarationStatement localVarDeclStmt ->
                    resolveLocalVariableDeclarationStatement(localVarDeclStmt, ctx);
            case ASTBlock subBlock -> resolveSubBlock(subBlock, targetType, ctx);
            case ASTBasicForStatement basicForStmt -> resolveBasicForStatement(basicForStmt, targetType, ctx);
            case ASTConstructorInvocation constrInvocation -> getConstructorsResolver().resolveInvocation(
                    constrInvocation, ctx);
            case ASTEnhancedForStatement enhancedForStmt -> resolveEnhancedForStatement(enhancedForStmt, targetType, ctx);
            case ASTExpressionStatement exprStmt -> resolveExpressionStatement(exprStmt, ctx);
            case ASTIfStatement ifStmt -> resolveIfStatement(ifStmt, targetType, ctx);
            case ASTReturnStatement returnStmt -> resolveReturnStatement(returnStmt, targetType, ctx);
            case ASTWhileStatement whileStmt -> resolveWhileStatement(whileStmt, targetType, ctx);
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
            TypeSymbol localVarType = dt.getResolvedDataType();

            for (ASTVariableDeclarator varDecl : localVarDecl.getVarDeclList().getTypedChildren()) {
                varDecl.getDeclSymbol().setDataType(dt.getResolvedDataType());
                varDecl.getVarInitializer().ifPresent(expr -> {
                    exprResolver.resolveExpression(expr, ctx);
                    TypeSymbol exprType = expr.getResolvedDataType();
                    if (exprType != null && localVarType != null && !isAssignmentConvertible(exprType, localVarType)) {
                        errorTypeMismatch(expr.getLocation(), localVarType.getFullyQualifiedName(), exprType.getFullyQualifiedName());
                    }
                });
            }
        }
        else {
            throw internalError("Resolution of auto data type is not implemented yet!");
        }
    }

    /**
     * Resolve all symbols in a sub-<code>Block</code>.
     * @param subBlock An <code>ASTBlock</code>.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveSubBlock(ASTBlock subBlock, TypeSymbol targetType, ResolutionContext ctx) {
        resolveBlock(subBlock, targetType, ctx.withEnclosingSymbol(subBlock.getDeclSymbol()));
    }

    /**
     * Resolve all symbols in a <code>BasicForStatement</code>.
     * @param basicForStmt An <code>ASTBasicForStatement</code>.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveBasicForStatement(ASTBasicForStatement basicForStmt, TypeSymbol targetType, ResolutionContext ctx) {
        ResolutionContext forCtx = ctx.withEnclosingSymbol(basicForStmt.getDeclSymbol());
        if (basicForStmt.getInit().isPresent()) {
            ASTInit init = basicForStmt.getInit().get();
            resolveInit(init, forCtx);
        }
        if (basicForStmt.getValueExpr().isPresent()) {
            ASTValueExpression condition = basicForStmt.getValueExpr().get();
            getExpressionsResolver().resolveValueExpression(condition, forCtx);
            checkExpressionBoolean(condition, forCtx);
        }
        resolveStatementExpressionList(basicForStmt.getStmtExprList(), forCtx);
        resolveBlock(basicForStmt.getBlock(), targetType, forCtx);
    }

    /**
     * Resolve all symbols in an <code>EnhancedForStatement</code>.
     * @param enhancedForStmt An <code>ASTEnhancedForStatement</code>.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveEnhancedForStatement(ASTEnhancedForStatement enhancedForStmt, TypeSymbol targetType, ResolutionContext ctx) {
        ResolutionContext forCtx = ctx.withEnclosingSymbol(enhancedForStmt.getDeclSymbol());
        resolveLocalVariableDeclaration(enhancedForStmt.getLocalVarDecl(), forCtx);
        ASTValueExpression iterable = enhancedForStmt.getValueExpr();
        getExpressionsResolver().resolveValueExpression(iterable, ctx);
        TypeSymbol exprType = iterable.getResolvedDataType();
        TypeSymbol iterableType = getTypesResolver().resolveBuiltInDataTypeByName(TypesResolver.ITERABLE_TYPE, ctx);
        if (exprType != null && iterableType != null && !exprType.isSubTypeOf(iterableType)) {
            errorTypeMismatch(iterable.getLocation(), iterableType.getFullyQualifiedName(), exprType.getFullyQualifiedName());
        }
        resolveBlock(enhancedForStmt.getBlock(), targetType, forCtx);
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
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveIfStatement(ASTIfStatement ifStmt, TypeSymbol targetType, ResolutionContext ctx) {
        ResolutionContext ifCtx = ctx.withEnclosingSymbol(ifStmt.getDeclSymbol());
        if (ifStmt.getInit().isPresent()) {
            resolveInit(ifStmt.getInit().get(), ifCtx);
        }
        ASTValueExpression condition = ifStmt.getCondExpr();
        getExpressionsResolver().resolveValueExpression(condition, ifCtx);
        checkExpressionBoolean(condition, ifCtx);

        ASTBlock ifBlock = ifStmt.getIfBlock();
        resolveBlock(ifBlock, targetType, ctx.withEnclosingSymbol(ifBlock.getDeclSymbol()));

        if (ifStmt.getElseBlock().isPresent()) {
            ASTBlock elseBlock = ifStmt.getElseBlock().get();
            resolveBlock(elseBlock, targetType, ctx.withEnclosingSymbol(elseBlock.getDeclSymbol()));
        }
        else if (ifStmt.getElseIf().isPresent()){
            ASTIfStatement elseIfStmt = ifStmt.getElseIf().get();
            resolveIfStatement(elseIfStmt, targetType, ifCtx);
        }
    }

    /**
     * Resolve all symbols in a <code>ReturnStatement</code>.
     * @param returnStmt An <code>ASTReturnStatement</code>.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveReturnStatement(ASTReturnStatement returnStmt, TypeSymbol targetType, ResolutionContext ctx) {
        if (returnStmt.getExpr().isPresent()) {
            ASTExpression expr = returnStmt.getExpr().get();
            getExpressionsResolver().resolveExpression(expr, ctx);
            TypeSymbol returnType = expr.getResolvedDataType();
            if (returnType != null && targetType != null) {
                if (TypeSymbol.VOID.equals(targetType)) {
                    error(expr.getLocation(), "Cannot return an expression in a void method.");
                }
                else if (!isAssignmentConvertible(returnType, targetType)) {
                    errorTypeMismatch(expr.getLocation(), targetType.getFullyQualifiedName(), returnType.getFullyQualifiedName());
                }
            }
        }
        else {
            if (!TypeSymbol.VOID.equals(targetType)) {
                error(returnStmt.getLocation(), "Missing return expression.");
            }
        }
    }

    /**
     * Resolve all symbols in a <code>WhileStatement</code>.
     * @param whileStmt An <code>ASTWhileStatement</code>.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   of the enclosing method/constructor, which may be null
     *                   if there was an error resolving it.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveWhileStatement(ASTWhileStatement whileStmt, TypeSymbol targetType, ResolutionContext ctx) {
        ResolutionContext whileCtx = ctx.withEnclosingSymbol(whileStmt.getDeclSymbol());
        if (whileStmt.getInit().isPresent()) {
            resolveInit(whileStmt.getInit().get(), whileCtx);
        }
        ASTValueExpression condition = whileStmt.getValueExpr();
        getExpressionsResolver().resolveValueExpression(condition, whileCtx);
        checkExpressionBoolean(condition, whileCtx);

        resolveBlock(whileStmt.getBlock(), targetType, whileCtx);
    }

    private void checkExpressionBoolean(ASTExpression expr, ResolutionContext ctx) {
        TypeSymbol exprType = expr.getResolvedDataType();
        if (exprType != null && !getTypesResolver().isBoolean(exprType, ctx)) {
            errorTypeMismatch(expr.getLocation(), "Boolean", exprType.getFullyQualifiedName());
        }
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
        case ASTMethodInvocation methodInvocation -> getMethodsResolver().resolveInvocation(
                methodInvocation, ctx);
        case ASTClassInstanceCreationExpression cice -> getCiceResolver().resolveInvocation(cice, ctx);
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

        ASTExpression rhs = assignment.getExpr();
        exprResolver.resolveExpression(rhs, ctx);

        TypeSymbol targetType = lhs.getResolvedDataType();
        TypeSymbol exprType = rhs.getResolvedDataType();
        if (targetType != null && exprType != null && !isAssignmentConvertible(exprType, targetType)) {
            errorTypeMismatch(rhs.getLocation(), targetType.getFullyQualifiedName(), exprType.getFullyQualifiedName());
        }
    }

    /**
     * <p>Determines whether the expression is assignment-convertible to a target
     * type.</p>
     * <p>
     * Assignment conversion is:
     * <ol>
     * <li>Identity conversion (match type exactly)</li>
     * <li>Reference widening conversion (expression type is a subtype of
     * target type).</li>
     * <li>Primitive widening conversion, e.g. Integer to Long.  (This does
     * not exist in Spruce yet, and it cannot exist until a mechanism is
     * decided upon to represent such an implicit conversion.)</li>
     * </ol>
     * </p>
     * @param exprType A <code>TypeSymbol</code> representing the type of the
     *                 expression.
     * @param targetType A <code>TypeSymbol</code> representing the target type
     *                   to assign an expression to.
     * @return Whether the expression is assignment-convertible to a target type.
     */
    public boolean isAssignmentConvertible(TypeSymbol exprType, TypeSymbol targetType) {
        return canConvertIdentity(exprType, targetType) || canConvertWideningReference(exprType, targetType);
    }
}
