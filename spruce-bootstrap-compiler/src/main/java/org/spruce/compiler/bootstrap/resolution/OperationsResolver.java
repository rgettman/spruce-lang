package org.spruce.compiler.bootstrap.resolution;

import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * An <code>OperationsResolver</code> is a <code>BasicResolver</code> that
 * resolves method invocations, constructor invocations, and expressions
 * involving operators in the expressions portion of an AST and Symbol Table.
 * All other expressions are resolved in the <code>ExpressionsResolver</code>
 * (except expression names, resolved in the <code>NamesResolver</code>).
 */
public class OperationsResolver extends BasicResolver {
    /**
     * Constructs an <code>OperationsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public OperationsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    //
    // Constructor Resolution
    //

    //
    // Method Resolution
    //

    /**
     * Resolves symbols in a <code>MethodInvocation</code>.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveMethodInvocation(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        // 1. Determine the type to search.
        Optional<TypeSymbol> typeToSearch = getTypeToSearch(methodInvocation, ctx);
        if (typeToSearch.isEmpty()) {
            // Error generated already.
            return;
        }

        // 2. Identify potentially applicable methods.
        //    If no methods match, error not found.

        // 3. Choose most specific method, if one such method exists.
        //    If no one method is maximally specific, ambiguous error.

        // 4. Type of the method invocation is the type of the Result.
        //    (Or the enclosing class type for a Constructor.)

    }

    /**
     * 1. Find the "type to search" for the given <code>MethodInvocation</code>,
     *    if it exists.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getTypeToSearch(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        String methodName = methodInvocation.getIdentifier().getValue();
        Optional<ASTKeywordNode> optSuper = methodInvocation.getSooper();
        Optional<ASTExpressionName> optExprName = methodInvocation.getExprName();
        Optional<ASTTypeName> optTypeName = methodInvocation.getTypeName();
        Optional<ASTPrimary> optPrimary = methodInvocation.getPrimary();

        if (optSuper.isPresent()) {
            if (optTypeName.isPresent()) {
                // 1a. Typename.super.methodName
                return getTypeToSearchTypenameSuper(methodInvocation, ctx);
            }
            else {
                // 1b. super.methodName
                return getTypeToSearchSuper(methodInvocation, ctx);
            }
        }
        else {
            if (optPrimary.isPresent()) {
                // 1c. Primary.methodName
                return getTypeToSearchPrimary(methodInvocation, ctx);
            }
            else if (optExprName.isPresent()) {
                // 1d. ExpressionName.methodName
                return getTypeToSearchExpressionName(methodInvocation, ctx);
            }
            else {
                // 1e. methodName
                Optional<TypeSymbol> optTypeToSearch = getTypeToSearchBareMethodName(methodName, ctx);
                if (optTypeToSearch.isEmpty()) {
                    errorSymbolNotFound(methodInvocation.getLocation(), methodInvocation.getIdentifier().getValue());
                }
                return optTypeToSearch;
            }
        }
    }

    // 1a. Typename.super.methodName
    private Optional<TypeSymbol> getTypeToSearchTypenameSuper(ASTMethodInvocation methodInvocation,
                                                              ResolutionContext ctx) {
        NamesResolver namesResolver = getNamesResolver();
        TypesResolver typesResolver = getTypesResolver();

        if (methodInvocation.getTypeName().isEmpty()) {
            throw internalError("type name on method invocation");
        }
        ASTTypeName typeName = methodInvocation.getTypeName().get();
        namesResolver.resolveTypeName(typeName, ctx);
        Optional<TypeSymbol> optResolved = Optional.ofNullable(typeName.getResolvedDataType());
        if (optResolved.isPresent()) {
            TypeSymbol resolved = optResolved.get();
            if (typesResolver.isEnclosingType(resolved, ctx.enclosingSymbol())) {
                Optional<TypeSymbol> optSuperclass = resolved.getSuperclass();
                if (optSuperclass.isEmpty()) {
                    error(methodInvocation.getLocation(),"Type " + resolved.getName() + " has no superclass");
                }
                return optSuperclass;
            }
            else {
                error(methodInvocation.getLocation(), "Type " + resolved.getName() + " is not an enclosing type");
            }
        }
        // else an error was already generated for not found.
        return Optional.empty();
    }

    // 1b. super.methodName
    private Optional<TypeSymbol> getTypeToSearchSuper(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol type = typesResolver.findEnclosingType(ctx.enclosingSymbol());
        Optional<TypeSymbol> optSuperclass = type.getSuperclass();
        if (optSuperclass.isEmpty()) {
            error(methodInvocation.getLocation(), "Type " + type.getName() + " has no superclass");
        }
        return optSuperclass;
    }

    // 1c. Primary.methodName
    private Optional<TypeSymbol> getTypeToSearchPrimary(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        if (methodInvocation.getPrimary().isEmpty()) {
            throw internalError("primary on method invocation");
        }
        ASTPrimary primary = methodInvocation.getPrimary().get();
        getExpressionsResolver().resolvePrimary(primary, ctx);
        return Optional.ofNullable(primary.getResolvedDataType());
    }

    // 1d. ExpressionName.methodName
    private Optional<TypeSymbol> getTypeToSearchExpressionName(ASTMethodInvocation methodInvocation,
                                                               ResolutionContext ctx) {
        NamesResolver namesResolver = getNamesResolver();

        if (methodInvocation.getExprName().isEmpty()) {
            throw internalError("type name on method invocation");
        }
        ASTExpressionName exprName = methodInvocation.getExprName().get();

        Optional<Symbol> optResolved = namesResolver.resolveExpressionOrTypeNameIds(exprName.getTypedChildren(), ctx);
        if (optResolved.isEmpty()) {
            // Errors resolving the symbol already generated.
            return Optional.empty();
        }
        else {
            Symbol resolved = optResolved.get();
            switch(resolved) {
            case TypeSymbol type -> {
                // TypeName.methodName
                return Optional.of(type);
            }
            case VariableSymbol variable -> {
                // ExpressionName.methodName
                return Optional.of(variable.getDataType());
            }
            default -> throw internalError("Unexpected symbol type resolving expression name in method invocation: " +
                    resolved.getClass().getName());
            }
        }
    }

    // 1e. methodName
    private Optional<TypeSymbol> getTypeToSearchBareMethodName(String methodName, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        ParentSymbol parent = typesResolver.findEnclosingType(ctx.enclosingSymbol());

        // Look in the current type and its superclass and superinterfaces hierarchy,
        // before searching an enclosing type and its superclass and superinterface hierarchy.
        // The type to search is the current type, or an enclosing type, even if the
        // name is found in a superclass or a superinterface.

        // 1. Repeat (2) for each enclosing type further up the enclosing type
        //    hierarchy.  Stopping with a match means shadowing possible
        //    matches further up the enclosing type hierarchy.
        while (parent.isType()) {
            TypeSymbol type = (TypeSymbol) parent;

            // 2. Starting with the current type, check the superclass
            //    hierarchy and the superinterface hierarchy.  Finding a match
            //    means that the current type, NOT any superclass or
            //    superinterface, is the type to search.
            boolean found = findMethodName(methodName, type);
            if (found) {
                return Optional.of(type);
            }
            parent = ((ChildSymbolTable) type.getParent()).getParent();
        }

        // 3. Eventually, check shared use statements (they don't exist yet).

        // 4. No method in scope with that name.
        return Optional.empty();
    }

    private boolean findMethodName(String methodName, TypeSymbol type) {
        SymbolTable table = type.getTable();
        if (table.containsMethodName(methodName)) {
            return true;
        }

        // Walk up superclass hierarchy then superinterface hierarchy looking
        // for the symbol name.
        if (type.getSuperclass().isPresent()) {
            TypeSymbol superclass = type.getSuperclass().get();
            boolean foundInSuperclass = findMethodName(methodName, superclass);
            if (foundInSuperclass) {
                return true;
            }
        }

        return findMethodInSuperinterfaces(methodName, type.getSuperinterfaces());
    }

    private boolean findMethodInSuperinterfaces(String methodName, List<TypeSymbol> superinterfaces) {
        for (TypeSymbol superinterface : superinterfaces) {
            ChildSymbolTable child = superinterface.getTable();
            if (child.containsMethodName(methodName)) {
                return true;
            }
            else {
                // Recur on this superinterface's superinterfaces.
                boolean foundInSuperinterfaces = findMethodInSuperinterfaces(
                        methodName, superinterface.getSuperinterfaces());
                if (foundInSuperinterfaces) {
                    return true;
                }
            }
        }
        return false;
    }

    //
    // Operator Resolution
    //

    /**
     * Resolves symbols in a <code>UnaryExpression</code>.
     * @param unaryExpr An <code>ASTUnaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveUnaryExpression(ASTUnaryExpression unaryExpr, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        ASTValueExpression first = unaryExpr.getFirst();
        exprResolver.resolveValueExpression(first, ctx);
        Optional<TypeSymbol> optSymbol = Optional.ofNullable(first.getResolvedDataType());

        if (optSymbol.isPresent()) {
            TypeSymbol symbol = optSymbol.get();
            TypesResolver typesResolver = getTypesResolver();

            switch (unaryExpr.getOperation()) {
            case EXCLAMATION -> {
                if (typesResolver.isBoolean(symbol, ctx)) {
                    unaryExpr.setResolvedDataType(symbol);
                }
                else {
                    errorUnaryOperator(unaryExpr.getLocation(), EXCLAMATION, symbol);
                }
            }
            case MINUS -> {
                if (typesResolver.isNumeric(symbol, ctx)) {
                    unaryExpr.setResolvedDataType(symbol);
                }
                else {
                    errorUnaryOperator(unaryExpr.getLocation(), MINUS, symbol);
                }
            }
            default -> throw internalError("Can't resolve unexpected unary operator '" +
                           unaryExpr.getOperation() + "'!");
            }
        }
        // Else there was an error resolving the operand already.
    }

    private void errorUnaryOperator(Location loc, TokenType operator, TypeSymbol symbol) {
        error(loc, "Operator '" + operator.getRepresentation() + "' cannot have operand '" +
                symbol.getName() + "'");
    }

    /**
     * Resolves symbols in a <code>BinaryExpression</code>.
     * @param binaryExpr An <code>ASTBinaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveBinaryExpression(ASTBinaryExpression binaryExpr, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        ASTValueExpression first = binaryExpr.getFirst();
        ASTValueExpression second = binaryExpr.getSecond();
        exprResolver.resolveValueExpression(first, ctx);
        exprResolver.resolveValueExpression(second, ctx);
        Optional<TypeSymbol> optFirstSymbol = Optional.ofNullable(first.getResolvedDataType());
        Optional<TypeSymbol> optSecondSymbol = Optional.ofNullable(second.getResolvedDataType());

        if (optFirstSymbol.isPresent() && optSecondSymbol.isPresent()) {
            TypeSymbol firstSymbol = optFirstSymbol.get();
            TypeSymbol secondSymbol = optSecondSymbol.get();
            TypesResolver typesResolver = getTypesResolver();
            TypeSymbol bool = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.BOOLEAN_TYPE, ctx);
            TypeSymbol character = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.CHARACTER_TYPE, ctx);
            TypeSymbol integer = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.INTEGER_TYPE, ctx);
            TypeSymbol duble = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.DOUBLE_TYPE, ctx);
            TypeSymbol string = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.STRING_TYPE, ctx);

            TokenType operator = binaryExpr.getOperation();
            switch (operator) {
            // Conditional logical operators.
            case DOUBLE_PIPE, DOUBLE_AMPERSAND -> {
                // boolean, boolean -> boolean.
                if (typesResolver.isBoolean(firstSymbol, ctx) && typesResolver.isBoolean(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(bool);
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // ISA is handled by ExpressionsResolver.resolveIsaExpression.
            // Relational operators.
            case DOUBLE_EQUAL, EXCLAMATION_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,
                 GREATER_THAN, GREATER_THAN_OR_EQUAL -> {
                // numeric, numeric -> boolean
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(bool);
                }
                else {
                    // Yes, we can't even use == or != on non-numerics!
                    // Yes, operator overloading will cover this case, eventually.
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // Addition, numerics or strings with anything else (string conversion).
            case PLUS -> {
                // character, character -> character
                // integer, character/integer -> integer with promotion
                // double, numeric -> double with promotion
                // string, any/string -> string with string conversion
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    if (firstSymbol == character && secondSymbol == character) {
                        binaryExpr.setResolvedDataType(character);
                    }
                    else if (firstSymbol == duble || secondSymbol == duble) {
                        binaryExpr.setResolvedDataType(duble);
                        // Handle promotion later.
                    }
                    else {
                        binaryExpr.setResolvedDataType(integer);
                        // Handle promotion later.
                    }
                }
                else if (typesResolver.isString(firstSymbol, ctx) || typesResolver.isString(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(string);
                    // Handle string conversion later.
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // Subtraction, numerics.
            case MINUS -> {
                // character, character -> character
                // integer, character/integer -> integer
                // double, numeric -> double
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    if (firstSymbol == character && secondSymbol == character) {
                        binaryExpr.setResolvedDataType(character);
                    }
                    else if (firstSymbol == duble || secondSymbol == duble) {
                        binaryExpr.setResolvedDataType(duble);
                    }
                    else {
                        binaryExpr.setResolvedDataType(integer);
                    }
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            default -> throw internalError("Can't resolve unexpected binary operator '" +
                    binaryExpr.getOperation() + "'!");
            }
        }
        // Else there was an error resolving at least one of the operands
        // already.
    }

    private void errorBinaryOperator(Location loc, TokenType operator,
                                     TypeSymbol first, TypeSymbol second) {
        error(loc, "Operator '" + operator.getRepresentation() + "' cannot have operands '" +
                first.getName() + "' and '" + second.getName() + "'");
    }
}
