package org.spruce.compiler.bootstrap.resolution;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.ASTLiteral;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

/**
 * An <code>ExpressionsResolver</code> is a <code>BasicResolver</code> that
 * resolves data types, expressions, value expressions, and primaries in the
 * expressions portion of an AST and Symbol Table.  All expressions <em>except
 * for Method Invocations</em> are resolved here.  Method invocations have
 * their own Resolver.
 */
public class ExpressionsResolver extends BasicResolver {
    /**
     * Constructs an <code>ExpressionsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The global <code>TypeLookup</code>.
     */
    public ExpressionsResolver(Resolver resolver, MessageProducer msgProducer, TypeLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolves symbols in an <code>Expression</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveExpression(ASTExpression expr, ResolutionContext ctx) {
        switch(expr) {
            case ASTValueExpression valueExpr -> resolveValueExpression(valueExpr, ctx);
        }
    }

    /**
     * Resolves symbols in a <code>ValueExpression</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing scope.
     */
    public void resolveValueExpression(ASTValueExpression valueExpr, ResolutionContext ctx) {
        switch (valueExpr) {
        case ASTBinaryExpression binaryExpr -> resolveBinaryExpression(binaryExpr, ctx);
        case ASTIsaExpression isaExpr -> resolveIsaExpression(isaExpr, ctx);
        case ASTUnaryExpression unaryExpr -> resolveUnaryExpression(unaryExpr, ctx);
        case ASTPrimary primary -> resolvePrimary(primary, ctx);
        }
    }

    /**
     * Resolves symbols in a <code>BinaryExpression</code>.
     * @param binaryExpr An <code>ASTBinaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveBinaryExpression(ASTBinaryExpression binaryExpr, ResolutionContext ctx) {
        resolveValueExpression(binaryExpr.getFirst(), ctx);
        resolveValueExpression(binaryExpr.getSecond(), ctx);
    }

    /**
     * Resolves symbols in an <code>IsaExpression</code>.
     * @param isaExpr An <code>ASTIsaExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveIsaExpression(ASTIsaExpression isaExpr, ResolutionContext ctx) {
        resolveExpression(isaExpr.getExpr(), ctx);
        resolveIsaTarget(isaExpr.getIsaTarget(), ctx);
    }

    /**
     * Resolves symbols in an <code>IsaTarget</code>.
     * @param isaTarget An <code>ASTIsaTarget</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveIsaTarget(ASTIsaTarget isaTarget, ResolutionContext ctx) {
        switch(isaTarget) {
        case ASTDataType dt -> getTypesResolver().resolveDataType(dt, ctx);
        }
    }

    /**
     * Resolves symbols in a <code>UnaryExpression</code>.
     * @param unaryExpr An <code>ASTUnaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveUnaryExpression(ASTUnaryExpression unaryExpr, ResolutionContext ctx) {
        resolveValueExpression(unaryExpr.getFirst(), ctx);
    }

    /**
     * Resolves symbols in a <code>Primary</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolvePrimary(ASTPrimary primary, ResolutionContext ctx) {
        switch(primary.getType()) {
        case CLASS_INSTANCE_CREATION_EXPR ->
                resolveClassInstanceCreationExpression((ASTClassInstanceCreationExpression) primary.getChild(), ctx);
        case CLASS_LITERAL -> resolveClassLiteral((ASTClassLiteral) primary.getChild(), ctx);
        case EXPR_NAME -> resolveExpressionName((ASTExpressionName) primary.getChild(), ctx);
        case FIELD_ACCESS -> resolveFieldAccess((ASTFieldAccess) primary.getChild(), ctx);
        case LITERAL -> resolveLiteral((ASTLiteral) primary.getChild(), ctx);
        case METHOD_INVOCATION -> {
            // TODO: Resolve the method invocation in a Methods Resolver!
        }
        case PAREN_EXPR -> resolveParenthesizedExpression((ASTExpression) primary.getChild(), ctx);
        case SELF -> resolveSelf((ASTKeywordNode) primary.getChild(), ctx);
        case TYPENAME_SELF -> resolveTypeNameSelf((ASTTypenameSelf) primary.getChild(), ctx);
        case BAD -> throw internalError("Shouldn't get here!  Can't resolve symbols in a BAD primary! " + primary);
        }
    }

    /**
     * Resolves symbols in a <code>LeftHandSide</code>.
     * @param lhs An <code>ASTLeftHandSide</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveLeftHandSide(ASTLeftHandSide lhs, ResolutionContext ctx) {
        switch(lhs) {
        case ASTExpressionName exprName -> resolveExpressionName(exprName, ctx);
        case ASTFieldAccess fieldAccess -> resolveFieldAccess(fieldAccess, ctx);
        }
    }

    public void resolveClassInstanceCreationExpression(ASTClassInstanceCreationExpression cice, ResolutionContext ctx) {

    }

    public void resolveClassLiteral(ASTClassLiteral classLiteral, ResolutionContext ctx) {

    }

    public void resolveExpressionName(ASTExpressionName exprName, ResolutionContext ctx) {

    }

    public void resolveFieldAccess(ASTFieldAccess fieldAccess, ResolutionContext ctx) {

    }

    public void resolveLiteral(ASTLiteral literal, ResolutionContext ctx) {

    }

    public void resolveParenthesizedExpression(ASTExpression parenthesized, ResolutionContext ctx) {

    }

    public void resolveSelf(ASTKeywordNode self, ResolutionContext ctx) {

    }

    public void resolveTypeNameSelf(ASTTypenameSelf typeNameSelf, ResolutionContext ctx) {

    }
}
