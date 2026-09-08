package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;
import java.util.stream.Collectors;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.ASTLiteral;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTIntersectionType;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.spruce.compiler.bootstrap.resolution.TypesResolver.BOOLEAN_TYPE;

/**
 * An <code>ExpressionsResolver</code> is a <code>BasicResolver</code> that
 * resolves data types, expressions, value expressions, and primaries in the
 * expressions portion of an AST and Symbol Table.  All expressions <em>except
 * for Method Invocations, Constructors, and Operators</em> are resolved here.
 * Those operations are resolved in the <code>OperationsResolver</code>.
 */
public class ExpressionsResolver extends BasicResolver {
    /**
     * Constructs an <code>ExpressionsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public ExpressionsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
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
        case ASTBinaryExpression binaryExpr -> getOperatorsResolver().resolveBinaryExpression(binaryExpr, ctx);
        case ASTCastExpression castExpr -> resolveCastExpression(castExpr, ctx);
        case ASTIsaExpression isaExpr -> resolveIsaExpression(isaExpr, ctx);
        case ASTUnaryExpression unaryExpr -> getOperatorsResolver().resolveUnaryExpression(unaryExpr, ctx);
        case ASTPrimary primary -> resolvePrimary(primary, ctx);
        }
    }

    /**
     * Resolves symbols in a <code>CastExpression</code>.
     * @param castExpr An <code>ASTCastExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveCastExpression(ASTCastExpression castExpr, ResolutionContext ctx) {
        resolveExpression(castExpr.getExpr(), ctx);
        ASTIntersectionType interType = castExpr.getIntersectionType();
        getTypesResolver().resolveIntersectionType(interType, ctx);
        for (ASTDataType dt : interType.getTypedChildren()) {
            Optional<TypeSymbol> optResolved = Optional.ofNullable(dt.getResolvedDataType());
            if (optResolved.isPresent()) {
                TypeSymbol resolved = optResolved.get();
                if (castExpr.getResolvedDataType(resolved.getName()).isPresent()) {
                    error(dt.getLocation(),
                            "Cannot repeat datatype in cast expression: " + resolved.getName());
                }
                castExpr.addResolvedDataType(resolved);
            }
        }
    }

    /**
     * Resolves symbols in an <code>IsaExpression</code>.
     * @param isaExpr An <code>ASTIsaExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveIsaExpression(ASTIsaExpression isaExpr, ResolutionContext ctx) {
        // any, type -> boolean
        resolveExpression(isaExpr.getExpr(), ctx);
        resolveIsaTarget(isaExpr.getIsaTarget(), ctx);
        // TODO: Is it feasible to determine if the condition CAN succeed?
        // If it can't succeed, produce an error?
        // Check if the expression type is a subtype of the target type
        // or if the target type is a subtype of the expression type.
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName(BOOLEAN_TYPE, ctx);
        isaExpr.setResolvedDataType(symbol);
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
     * Resolves symbols in a <code>Primary</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolvePrimary(ASTPrimary primary, ResolutionContext ctx) {
        switch(primary.getChild()) {
        case ASTClassInstanceCreationExpression cice ->
                resolveClassInstanceCreationExpression(cice, ctx);
        case ASTClassLiteral classLiteral -> resolveClassLiteral(classLiteral, ctx);
        case ASTExpressionName exprName -> getNamesResolver().resolveExpressionName(exprName, ctx);
        case ASTFieldAccess fieldAccess -> resolveFieldAccess(fieldAccess, ctx);
        case ASTLiteral literal -> getLiteralsResolver().resolveLiteral(literal, ctx);
        case ASTMethodInvocation methodInvocation -> getMethodsResolver().resolveInvocation(
                methodInvocation, ctx);
        // Parenthesized expression.
        case ASTExpression expr -> resolveExpression(expr, ctx);
        case ASTSelf self -> resolveSelf(self, ctx);
        case ASTTypenameSelf typenameSelf -> resolveTypenameSelf(typenameSelf, ctx);
        case ASTPrimary.ASTBadPrimary ignored -> throw internalError(
                "Shouldn't get here!  Can't resolve symbols in a BAD primary! " + primary);
        }
        // The datatype of the primary is the datatype of its child.
        Optional<TypeSymbol> optResolved = Optional.ofNullable(primary.getChild().getResolvedDataType());
        optResolved.ifPresent(primary::setResolvedDataType);
    }

    /**
     * Resolves symbols in a <code>LeftHandSide</code>.
     * @param lhs An <code>ASTLeftHandSide</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveLeftHandSide(ASTLeftHandSide lhs, ResolutionContext ctx) {
        switch(lhs) {
        case ASTExpressionName exprName -> getNamesResolver().resolveExpressionName(exprName, ctx);
        case ASTFieldAccess fieldAccess -> resolveFieldAccess(fieldAccess, ctx);
        }
    }

    /**
     * Resolves symbols in a <code>ClassInstanceCreationExpression</code>.
     * @param cice An <code>ASTClassInstanceCreationExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveClassInstanceCreationExpression(ASTClassInstanceCreationExpression cice,
                                                       ResolutionContext ctx) {
        // The primary, if present, will be the enclosing instance, and the
        // UCICE's TypeToInstantiate then must resolve to an inner type with
        // respect to this primary.
        // Else, the UCICE will represent a "normal" class instance creation.
        Optional<ASTPrimary> optPrimary = cice.getPrimary();

        ASTUnqualifiedClassInstanceCreationExpression ucice = cice.getUcice();
        if (optPrimary.isPresent()) {
            ASTPrimary primary = optPrimary.get();
            resolvePrimary(primary, ctx);

            ResolutionContext ciceCtx = ctx.withEnclosingSymbol(primary.getResolvedDataType());
            resolveQualifiedClassInstanceCreationExpression(ucice, ciceCtx);
        }
        else {
            resolveUnqualifiedClassInstanceCreationExpression(ucice, ctx);
        }
        cice.setResolvedDataType(ucice.getResolvedDataType());
        // Must pull up the UCICE's resolved
    }

    /**
     * Resolves symbols in a "regular" <code>UnqualifiedClassInstanceCreationExpression</code>,
     * e.g., <code>new TypeToInstantiate ( )</code>.
     * @param ucice An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveUnqualifiedClassInstanceCreationExpression(
            ASTUnqualifiedClassInstanceCreationExpression ucice, ResolutionContext ctx) {
        resolveTypeToInstantiate(ucice.getTti(), ctx);
        resolveArgumentList(ucice.getArgumentList(), ctx);
        // TODO: Constructor resolution!
    }

    /**
     * Resolves symbols in an <code>UnqualifiedClassInstanceCreationExpression</code>
     * that is part of a qualified class instance creation expression, e.g.,
     * <code>Primary . new TypeToInstantiate ( )</code>.
     * @param ucice An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @param ctx A <code>TypeSymbol</code> representing the type of the
     *                  enclosing instance.
     */
    public void resolveQualifiedClassInstanceCreationExpression(
            ASTUnqualifiedClassInstanceCreationExpression ucice, ResolutionContext ctx)  {
        // Must be a simple name.
        ASTTypeToInstantiate tti = ucice.getTti();
        ASTTypeName tn = tti.getTypeName();
        if (tn.getTypedChildren().size() > 1) {
            error(tn.getLocation(), "Cannot have qualified name with qualified new.");
        }
        // Must be a direct child of the enclosing type of the enclosing instance.
        String simpleName = tn.get(0).getValue();
        SymbolTable enclosingTable = ctx.enclosingSymbol().getTable();
        if (enclosingTable.containsType(simpleName)) {
            TypeSymbol resolved = (TypeSymbol) enclosingTable.get(simpleName);
            tn.setResolvedDataType(resolved);
            tti.setResolvedDataType(resolved);
        }
        else {
            errorSymbolNotFound(tn.getLocation(), simpleName);
        }

        resolveArgumentList(ucice.getArgumentList(), ctx);
        // TODO: Constructor resolution!
    }

    /**
     * Resolves symbols in a <code>TypeToInstantiate</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveTypeToInstantiate(ASTTypeToInstantiate tti, ResolutionContext ctx) {
        ASTTypeName tn = tti.getTypeName();
        getNamesResolver().resolveTypeName(tn, ctx);
        tti.setResolvedDataType(tn.getResolvedDataType());
    }

    /**
     * Resolves symbols in an <code>ArgumentList</code>.
     * @param argList An <code>ASTArgumentList</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveArgumentList(ASTArgumentList argList, ResolutionContext ctx) {
        for (ASTExpression arg : argList.getTypedChildren()) {
            resolveExpression(arg, ctx);
        }
    }

    /**
     * Resolves the datatype of a <code>ClassLiteral</code>.
     * @param classLiteral An <code>ASTClassLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveClassLiteral(ASTClassLiteral classLiteral, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol symbol = typesResolver.resolveBuiltInDataTypeByName("Class", ctx);
        classLiteral.setResolvedDataType(symbol);
        typesResolver.resolveDataType(classLiteral.getDataType(), ctx);
    }

    /**
     * Resolves the field and the datatype of a <code>FieldAccess</code>.
     * @param fieldAccess An <code>ASTFieldAccess</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveFieldAccess(ASTFieldAccess fieldAccess, ResolutionContext ctx) {
        TypeSymbol direct = getTypesResolver().findEnclosingType(ctx.enclosingSymbol());

        if (fieldAccess.getSooper().isPresent()) {
            if (fieldAccess.getTypeName().isPresent()) {
                // TypeName.super.Identifier
                ASTTypeName tn = fieldAccess.getTypeName().get();
                getNamesResolver().resolveTypeName(tn, ctx);
                Optional<TypeSymbol> optTypeName = Optional.ofNullable(tn.getResolvedDataType());

                // Ensure that the resolved type is an enclosing type, direct or indirect.
                if (optTypeName.isPresent()) {
                    TypeSymbol resolved = optTypeName.get();
                    if (getTypesResolver().isEnclosingType(resolved, direct)) {
                        Optional<TypeSymbol> optSupertype = resolved.getSuperclass();
                        if (optSupertype.isPresent()) {
                            resolveFieldGivenType(optSupertype.get(), fieldAccess);
                        }
                        else {
                            String typeName = tn.getTypedChildren().stream()
                                    .map(ASTIdentifier::getValue)
                                    .collect(Collectors.joining("."));
                            error(tn.getLocation(), typeName + " does not have a superclass.");
                        }
                    }
                    else {
                        String typeName = tn.getTypedChildren().stream()
                                .map(ASTIdentifier::getValue)
                                .collect(Collectors.joining("."));
                        error(tn.getLocation(), typeName + " is not an enclosing type.");
                    }
                }
                // Else an error would have been generated already, when
                // resolving the typename.
            }
            else {
                // super.Identifier
                Optional<TypeSymbol> optSupertype = direct.getSuperclass();
                if (optSupertype.isPresent()) {
                    resolveFieldGivenType(optSupertype.get(), fieldAccess);
                }
                else {
                    error(fieldAccess.getLocation(), direct.getName() + " does not have a superclass.");
                }
            }
        }
        else {
            // Primary.Identifier
            Optional<ASTPrimary> optFieldPrimary = fieldAccess.getPrimary();
            if (optFieldPrimary.isEmpty()) {
                throw internalError("FieldAccess had no super keyword or primary! " + fieldAccess);
            }

            // Resolve nested primary.
            ASTPrimary fieldPrimary = optFieldPrimary.get();
            resolvePrimary(fieldPrimary, ctx);

            Optional<TypeSymbol> optDatatype = Optional.ofNullable(fieldPrimary.getResolvedDataType());
            optDatatype.ifPresent(ts -> resolveFieldGivenType(ts, fieldAccess));
            // Else an error would have been generated already, when
            // resolving the primary datatype.
        }
    }

    /**
     * If the type symbol has already been identified, then search the type
     * symbol for a field of a particular name given by the <code>FieldAccess</code>.
     * @param type A <code>TypeSymbol</code>.
     * @param fieldAccess An <code>ASTFieldAccess</code>.
     */
    private void resolveFieldGivenType(TypeSymbol type, ASTFieldAccess fieldAccess) {
        ASTIdentifier idField = fieldAccess.getIdentifier();
        String fieldName = idField.getValue();
        SymbolTable table = type.getTable();
        if (table.containsField(fieldName)) {
            VariableSymbol field = (VariableSymbol) table.get(fieldName);
            fieldAccess.setResolvedEntity(field);
        }
        else {
            errorSymbolNotFound(idField.getLocation(), fieldName);
        }
    }

    /**
     * Resolves the <code>Self</code> to the directly enclosing type.
     * @param self An <code>ASTSelf</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveSelf(ASTSelf self, ResolutionContext ctx) {
        TypeSymbol direct = getTypesResolver().findEnclosingType(ctx.enclosingSymbol());
        self.setResolvedDataType(direct);
    }

    /**
     * Resolves the <code>TypenameSelf</code> to an enclosing type matching the
     * type name.
     * @param typenameSelf An <code>ASTTypenameSelf</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveTypenameSelf(ASTTypenameSelf typenameSelf, ResolutionContext ctx) {
        ASTTypeName tn = typenameSelf.getTypename();
        getNamesResolver().resolveTypeName(tn, ctx);
        Optional<TypeSymbol> optResolved = Optional.ofNullable(tn.getResolvedDataType());

        // Ensure that the resolved type is an enclosing type, direct or indirect.
        if (optResolved.isPresent()) {
            TypeSymbol resolved = optResolved.get();
            TypeSymbol direct = getTypesResolver().findEnclosingType(ctx.enclosingSymbol());
            if (getTypesResolver().isEnclosingType(resolved, direct)) {
                typenameSelf.setResolvedDataType(resolved);
            }
            else {
                String typeName = tn.getTypedChildren().stream()
                    .map(ASTIdentifier::getValue)
                    .collect(Collectors.joining("."));
                error(typenameSelf.getLocation(), typeName + " is not an enclosing type.");
            }
        }
    }
}
