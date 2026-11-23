package org.spruce.compiler.bootstrap.analysis;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * An <code>ExpressionsAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * the data flow within expressions.
 */
public class ExpressionsAnalyzer extends BasicAnalyzer {

    /**
     * Constructs an <code>ExpressionsAnalyzer</code>.
     * @param analyzer An <code>Analyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ExpressionsAnalyzer(Analyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyzes an <code>Expression</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeExpression(ASTExpression expr, AnalysisContext ctx) {
        switch (expr) {
            case ASTValueExpression valueExpr -> analyzeValueExpression(valueExpr, ctx);
        }
    }

    /**
     * Analyzes a <code>ValueExpression</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeValueExpression(ASTValueExpression valueExpr, AnalysisContext ctx) {
        switch (valueExpr) {
        case ASTBinaryExpression binaryExpr -> analyzeBinaryExpression(binaryExpr, ctx);
        case ASTCastExpression castExpr -> analyzeCastExpression(castExpr, ctx);
        case ASTIsaExpression isaExpr -> analyzeIsaExpression(isaExpr, ctx);
        case ASTPrimary primary -> analyzePrimary(primary, ctx);
        case ASTUnaryExpression unaryExpr -> analyzeUnaryExpression(unaryExpr, ctx);
        }
    }

    /**
     * Analyzes a <code>BinaryExpression</code>.
     * @param binaryExpr An <code>ASTBinaryExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeBinaryExpression(ASTBinaryExpression binaryExpr, AnalysisContext ctx) {
        analyzeValueExpression(binaryExpr.getFirst(), ctx);
        analyzeValueExpression(binaryExpr.getSecond(), ctx);
    }

    /**
     * Analyzes an <code>IsaExpression</code>.
     * @param isaExpr An <code>ASTIsaExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeIsaExpression(ASTIsaExpression isaExpr, AnalysisContext ctx) {
        analyzeExpression(isaExpr, ctx);
    }

    /**
     * Analyzes a <code>CastExpression</code>.
     * @param castExpr An <code>ASTCastExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeCastExpression(ASTCastExpression castExpr, AnalysisContext ctx) {
        analyzeExpression(castExpr.getExpr(), ctx);
    }

    /**
     * Analyzes a <code>UnaryExpression</code>.
     * @param binaryExpr An <code>ASTUnaryExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeUnaryExpression(ASTUnaryExpression binaryExpr, AnalysisContext ctx) {
        analyzeValueExpression(binaryExpr.getFirst(), ctx);
    }

    /**
     * Analyzes a <code>Primary</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzePrimary(ASTPrimary primary, AnalysisContext ctx) {
        // Can't refer to local variables that have not been initialized.
        switch (primary.getChild()) {
        case ASTClassInstanceCreationExpression cice -> analyzeClassInstanceCreationExpression(cice, ctx);
        case ASTExpression parenExpr -> analyzeExpression(parenExpr, ctx);
        case ASTExpressionName exprName -> analyzeExpressionName(exprName, ctx);
        case ASTFieldAccess fieldAccess -> analyzeFieldAccess(fieldAccess, ctx);
        case ASTMethodInvocation methodInvocation -> analyzeMethodInvocation(methodInvocation, ctx);
        case ASTSelf self -> {}
        case ASTTypenameSelf typenameSelf -> {}

        // All primary children types that need analysis go above.
        default -> {}
        }
    }

    /**
     * Analyzes a <code>LeftHandSide</code>.
     * @param lhs An <code>ASTLeftHandSide</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeLeftHandSide(ASTLeftHandSide lhs, AnalysisContext ctx) {
        switch (lhs) {
        case ASTExpressionName exprName -> analyzeExpressionName(exprName, ctx);
        case ASTFieldAccess fieldAccess -> analyzeFieldAccess(fieldAccess, ctx);
        }
    }

    /**
     * Analyzes a <code>FieldAccess</code>.
     * @param fieldAccess An <code>ASTFieldAccess</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeFieldAccess(ASTFieldAccess fieldAccess, AnalysisContext ctx) {
        if (fieldAccess.getPrimary().isPresent()) {
            analyzePrimary(fieldAccess.getPrimary().get(), ctx);
        }
        else if (fieldAccess.getSooper().isPresent() && ctx.isInConstructor() && !ctx.isPastPrologue()) {
            error(fieldAccess.getLocation(), "Superclass field access not allowed in early construction context.");
        }
    }

    /**
     * Analyzes an <code>ExpressionName</code>.
     * @param exprName An <code>ASTExpressionName</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeExpressionName(ASTExpressionName exprName, AnalysisContext ctx) {
        // TODO: Enforce that expression name cannot refer to an uninitialized
        //  local variable.
    }

    /**
     * Analyzes a <code>ClassInstanceCreationExpression</code>.
     * @param cice An <code>ASTClassInstanceCreationExpression</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeClassInstanceCreationExpression(ASTClassInstanceCreationExpression cice, AnalysisContext ctx) {
        if (cice.getPrimary().isPresent()) {
            analyzePrimary(cice.getPrimary().get(), ctx);
        }
        for (ASTExpression arg : cice.getUcice().getArgumentList().getTypedChildren()) {
            analyzeExpression(arg, ctx);
        }
    }

    /**
     * Analyzes a <code>MethodInvocation</code>.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeMethodInvocation(ASTMethodInvocation methodInvocation, AnalysisContext ctx) {
        // TODO: Enforce no super.methodName() or TypeName.super.methodName()
        //  in constructor prologue.

        if (methodInvocation.getPrimary().isPresent()) {
            analyzePrimary(methodInvocation.getPrimary().get(), ctx);
        }
        else if (methodInvocation.getExprName().isPresent()) {
            analyzeExpressionName(methodInvocation.getExprName().get(), ctx);
        }
        for (ASTExpression arg : methodInvocation.getArgumentList().getTypedChildren()) {
            analyzeExpression(arg, ctx);
        }
    }
}
