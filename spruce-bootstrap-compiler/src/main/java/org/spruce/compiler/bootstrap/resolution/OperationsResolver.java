package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * An <code>OperationsResolver</code> is a <code>BasicResolver</code> that
 * resolves method invocations, constructor invocations, and expressions
 * involving operators in the expressions portion of an AST and Symbol Table.
 * All other expressions are resolved in the <code>ExpressionsResolver</code>.
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
