package org.spruce.compiler.bootstrap.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.ASTLiteral;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;
import org.spruce.compiler.bootstrap.scanner.TokenType;

import static org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary.Type.*;
import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>ExpressionsParser</code> is a <code>BasicParser</code> that parses
 * expressions.
 */
public class ExpressionsParser extends BasicParser {
    // If we get a bad Primary, don't consume these when attempting to parse
    // beyond it.  Expression stoppers and operators.
    public static final List<TokenType> PRIMARY_STOPPERS = Arrays.asList(
            CLOSE_PARENTHESIS, CLOSE_BRACE,
            COMMA, SEMICOLON,
            LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL, EXCLAMATION_EQUAL,
            DOUBLE_AMPERSAND,  DOUBLE_PIPE, EXCLAMATION,
            PLUS, MINUS, AS, ISA,
            EOF
    );

    // If we get a bad Expression, don't consume these when attempting to parse
    // beyond it.  Primary stoppers without the binary operators.  Switch
    // expression parts and statement starters.
    public static final List<TokenType> EXPRESSION_STOPPERS = Arrays.asList(
            CLOSE_PARENTHESIS, CLOSE_BRACE,
            COMMA, SEMICOLON,
            BREAK, CONTINUE, FOR, IF, RETURN, USE, WHILE,
            EOF
    );

    /**
     * Constructs an <code>ExpressionsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ExpressionsParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * Parses an <code>Expression</code>.
     * <em>
     * Expression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression
     * </em>
     * @return An <code>ASTExpression</code> that could be an <code>ASTLambdaExpression</code>
     *     or an <code>ASTValueExpression</code>.
     */
    public ASTExpression parseExpression() {
        if (isValueExpression(curr())) {
            return parseValueExpression();
        }
        else {
            error(curr().getLocation(), "Expected primary expression.  Got \"" + curr().getValue() + "\".");
            return parseBadPrimary(EXPRESSION_STOPPERS);
        }
    }

    /**
     * Parses a <code>ValueExpression</code>.
     * <em>
     * ValueExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BinaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IsaExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary
     * </em>
     * @return An implementation of <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseValueExpression() {
        if (isValueExpression(curr())) {
            return parseLogicalOrExpression();
        }
        else {
            error(curr().getLocation(), "Expected a value expression.");
            return parseBadPrimary(EXPRESSION_STOPPERS);
        }
    }

    /**
     * Parses an <code>LogicalOrExpression</code>; they are left-
     * associative with each other.
     * <em>
     * LogicalOrExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression || LogicalAndExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseLogicalOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(DOUBLE_PIPE),
                this::parseLogicalAndExpression
        );
    }

    /**
     * Parses a <code>LogicalAndExpression</code>; they are left-
     * associative with each other.
     * <em>
     * LogicalAndExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression &amp;&amp; RelationalExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseLogicalAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(DOUBLE_AMPERSAND),
                this::parseRelationalExpression
        );
    }

    /**
     * Parses a <code>RelationalExpression</code>; they are left-
     * associative with each other.
     * <em>
     * RelationalExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt; AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt;= AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt; AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt;= AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression == AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression != AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression isa DataType
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseRelationalExpression() {
        Location loc = curr().getLocation();
        ASTValueExpression result = parseAdditiveExpression();
        TokenType curr;
        while ( (curr = isAcceptedOperator(Arrays.asList(
                LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL,
                EXCLAMATION_EQUAL, ISA)) ) != null) {
            accept(curr);
            if (curr == ISA) {
                result = new ASTIsaExpression(loc, result, parseIsaTarget());
            }
            else {
                result = new ASTBinaryExpression(loc, result, parseAdditiveExpression(), curr);
            }
        }
        return result;
    }

    /**
     * Parses an <code>IsaExpression</code>.
     * <em>
     * IsaTarget:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType
     * </em>
     * @return An <code>ASTIsaTarget</code>.
     */
    public ASTIsaTarget parseIsaTarget() {
        return getTypesParser().parseDataType();
    }

    /**
     * Parses an <code>AdditiveExpression</code>; they are left-
     * associative with each other.
     * <em>
     * AdditiveExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression + CastExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression - CastExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseAdditiveExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(PLUS, MINUS),
                this::parseCastExpression
        );
    }

    /**
     * Parses a <code>CastExpression</code>; they are left-associative with
     * each other.
     * <em>
     * CastExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression as IntersectionType<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseCastExpression() {
        Location loc = curr().getLocation();
        ASTValueExpression result = parseUnaryExpression();
        while (isCurr(AS)) {
            accept(AS);
            result = new ASTCastExpression(loc, result, getTypesParser().parseIntersectionType());
        }
        return result;
    }

    /**
     * Parses a <code>UnaryExpression</code>.
     * <em>
     * UnaryExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;! UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;- UnaryExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseUnaryExpression() {
        Location loc = curr().getLocation();
        if (isCurr(EXCLAMATION)) {
            accept(EXCLAMATION);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), EXCLAMATION);
        }
        else if (isCurr(MINUS)) {
            accept(MINUS);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), MINUS);
        }
        else {
            return parsePrimary();
        }
    }

    private ASTPrimary parseBadPrimary(List<TokenType> stoppers) {
        Location loc = curr().getLocation();
        ASTKeywordNode badChild;
        if (!test(curr(), stoppers)) {
            badChild = parseModifier(Arrays.asList(curr().getType()),
                    curr().getType().getRepresentation()
            );
        }
        else {
            badChild = new ASTKeywordNode(loc, UNKNOWN);
        }
        return new ASTPrimary(loc, new ASTPrimary.ASTBadPrimary(loc, badChild), BAD);
    }

    /**
     * Parses a <code>Primary</code>.
     * <em>
     * Primary:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Literal</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ClassLiteral</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>self</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . self<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>( Expression )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ClassInstanceCreationExpression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * </em>
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary() {
        Location loc = curr().getLocation();
        ASTPrimary primary;
        if (!isPrimary(curr())) {
            error(loc, "Expected: literal, expression name, or array or class instance creation expression.");
            primary = parseBadPrimary(PRIMARY_STOPPERS);
        }
        else if (isLiteral(curr())) {
            // Literal
            ASTLiteral literal = getLiteralsParser().parseLiteral();
            primary = new ASTPrimary(loc, literal, LITERAL);
        }
        else if (isCurr(IDENTIFIER)) {
            if (isNext(OPEN_PARENTHESIS)) {
                // id(args)
                // MethodInvocation
                ASTIdentifier methodName = getNamesParser().parseIdentifier();
                primary = new ASTPrimary(loc, parseMethodInvocation(methodName), METHOD_INVOCATION);
            }
            else {
                ASTDataType dataType = getTypesParser().parseDataType();
                if (isCurr(DOT) && isNext(CLASS)) {
                    // ClassLiteral
                    return new ASTPrimary(loc, parseClassLiteral(dataType), CLASS_LITERAL);
                }

                ASTExpressionName exprName;
                if (dataType.canConvertToExpressionName()) {
                    exprName = dataType.convertToExpressionName();
                }
                else {
                    error(dataType.getLocation(),"Expected expression name.");
                    exprName = ASTExpressionName.badExpressionName(dataType.getLocation());
                }
                primary = parsePrimary(exprName);
            }
        }
        else if (isCurr(TokenType.SELF)) {
            // self
            ASTKeywordNode keywordSelf = parseSelf();
            ASTSelf self = new ASTSelf(loc, keywordSelf);
            primary = new ASTPrimary(loc, self, ASTPrimary.Type.SELF);
        }
        else if (isCurr(SUPER)) {
            ASTKeywordNode sooper = parseSuper();
            if (accept(DOT) == null) {
                error(curr().getLocation(),
                        "Expected '.'.");
            }
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                // MethodInvocation
                primary = new ASTPrimary(loc, parseMethodInvocationSuper(sooper), METHOD_INVOCATION);
            }
            else {
                // FieldAccess
                primary = new ASTPrimary(loc, parseFieldAccessSuper(sooper), FIELD_ACCESS);
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // Expression
            accept(OPEN_PARENTHESIS);
            ASTExpression expression = parseExpression();
            if (accept(CLOSE_PARENTHESIS) == null) {
                error(curr().getLocation(), "Expected close parenthesis ')'.");
            }
            primary = new ASTPrimary(loc, expression, PAREN_EXPR);
        }
        else if (isCurr(NEW)) {
            accept(NEW);
            // Assume an identifier is next.
            ASTTypeToInstantiate tti = parseTypeToInstantiate();

            // Assume '(' is next.
            // ClassInstanceCreationExpression
            primary = new ASTPrimary(loc, parseClassInstanceCreationExpression(tti),
                    ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR);
        }
        else {
            primary = parseBadPrimary(PRIMARY_STOPPERS);
        }

        // Qualified Class Instance Creation, Element Access, and Method Invocations may chain up.
        // E.g. new Foo()[i].method1()[j].method2().new Bar()
        return parsePrimary(primary);
    }

    /**
     * Parses a <code>Primary</code>, given an already parsed
     * <code>ASTExpressionName</code>.
     * <em>
     * Primary:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Literal<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ExpressionName</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;self<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . self</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;( Expression )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * </em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTExpressionName exprName) {
        ASTPrimary primary;
        Location loc = exprName.getLocation();

        // If exprName is a simple identifier, then it is the method name.
        List<ASTIdentifier> children = exprName.getTypedChildren();
        if (children.size() == 1 && isCurr(OPEN_PARENTHESIS)) {
            // id( -> method invocation
            // MethodInvocation
            ASTIdentifier methodName = children.get(0);
            primary = new ASTPrimary(loc, parseMethodInvocation(methodName), METHOD_INVOCATION);
            return parsePrimary(primary);
        }

        if (isCurr(DOT) && isNext(TokenType.SELF)) {
            // TypeName.self
            ASTTypeName tn = exprName.convertToTypeName();
            accept(DOT);
            ASTTypenameSelf typenameSelf = new ASTTypenameSelf(tn.getLocation(), tn, parseSelf());
            primary = new ASTPrimary(loc, typenameSelf, ASTPrimary.Type.TYPENAME_SELF);
        }
        else if (isCurr(DOT) && isNext(SUPER) && !isPeek(OPEN_PARENTHESIS)) {
            // TypeName.super.methodInvocation()
            // TypeName.super.fieldAccess
            // NOT Expression.super() -- Constructor invocation.
            accept(DOT);
            ASTKeywordNode sooper = parseSuper();

            if (isCurr(DOT)) {
                accept(DOT);
                if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                    // MethodInvocation
                    // TypeName.super.<TypeArgs>methodName(args)
                    primary = new ASTPrimary(loc, parseMethodInvocationSuper(exprName, sooper), METHOD_INVOCATION);
                }
                else {
                    // TypeName.super.<TypeArgs>fieldName
                    // FieldAccess
                    primary = new ASTPrimary(loc, parseFieldAccessSuper(exprName, sooper), FIELD_ACCESS);
                }
            }
            else {
                error(loc, "Expected method invocation, or field access (.).");
                primary = parseBadPrimary(PRIMARY_STOPPERS);
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // MethodInvocation
            // ExprNameExceptForMethodName.methodName(args)
            //children = exprName.getTypedChildren();
            ASTIdentifier methodName = children.getLast();
            children.removeLast();
            ASTExpressionName actualExprName = new ASTExpressionName(exprName.getLocation(), children);
            primary = new ASTPrimary(loc, parseMethodInvocationExprNameIdentifier(actualExprName, methodName), METHOD_INVOCATION);
        }
        else {
            // ExpressionName
            primary = new ASTPrimary(loc, exprName, EXPR_NAME);
        }
        return parsePrimary(primary);
    }

    /**
     * <p>Parses an <code>ASTPrimary</code>, given an already parsed
     * <code>ASTPrimary</code>.  This accounts for circularity in the Primary
     * productions, where method invocations, element access, field access, and
     * qualified class instance creations can be chained.</p>
     * <em>
     * Primary:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Literal<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;self<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . self<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;( Expression )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ClassInstanceCreationExpression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTPrimary primary) {
        Location loc = primary.getLocation();

        // Qualified Class Instance Creation, Element Access, Field Access, and
        // Method Invocations may chain up.
        // E.g. new Foo()[i].method1()[j].method2().new Bar()
        while ( (isCurr(DOT) && isNext(NEW)) ||
                //(isCurr(DOT) && isNext(LESS_THAN)) ||
                (isCurr(DOT) && isNext(IDENTIFIER))
        ) {
            if (isCurr(DOT) && isNext(NEW)) {
                // ClassInstanceCreationExpression
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, cice, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR);
            }
            if (isCurr(DOT) && isNext(IDENTIFIER)) {
                accept(DOT);
                if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                    // MethodInvocation
                    ASTMethodInvocation mi = parseMethodInvocation(primary);
                    primary = new ASTPrimary(loc, mi, METHOD_INVOCATION);
                }
                else {
                    // FieldAccess
                    ASTFieldAccess fa = parseFieldAccess(primary);
                    primary = new ASTPrimary(loc, fa, FIELD_ACCESS);
                }
            }
        }
        return primary;
    }

    /**
     * Parses a <code>MethodInvocation</code>, given an <code>ASTIdentifier</code>
     * that has already been parsed and its <code>Location</code>.
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Identifier ( ArgumentList )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier ( ArgumentList )
     * </em>
     * @param identifier An already parsed <code>ASTIdentifier</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTIdentifier identifier) {
        if (accept(OPEN_PARENTHESIS) == null) {
            throw internalError(OPEN_PARENTHESIS);
        }
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(identifier.getLocation())
                .setIdentifier(identifier);
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * <p>Parses a <code>MethodInvocation</code>, given an <code>ASTPrimary</code>
     * that has already been parsed.</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Primary . [TypeArguments] Identifier ( ArgumentList )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . [TypeArguments] Identifier ( ArgumentList )
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTPrimary primary) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(primary.getLocation())
                .setPrimary(primary);
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * <p>Parses a <code>MethodInvocation</code>, given an <code>ASTExpressionName</code>
     * that has already been parsed and broken up into the given <code>ASTExpressionName</code>
     * and an <code>ASTIdentifier</code> serving as the method name.</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ExpressionName . [TypeArguments] Identifier ( ArgumentList )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . [TypeArguments] Identifier ( ArgumentList )
     * </em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationExprNameIdentifier(ASTExpressionName exprName, ASTIdentifier methodName) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setExprName(exprName)
                .setIdentifier(methodName);
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * <p>Parses a <code>MethodInvocation</code>, given an already parsed
     * <code>super</code>.  The DOT following "super" has been parsed also.</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super . Identifier ( ArgumentList )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier ( ArgumentList )
     * </em>
     * @param sooper An already parsed <code>super</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTKeywordNode sooper) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(sooper.getLocation())
                .setSooper(sooper);
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * <p>Parses a <code>MethodInvocation</code>, with <code>super</code>,
     * starting with an already parsed <code>ASTExpressionName</code>, which is
     * converted to an <code>ASTTypeName</code>, and an already parsed
     * <code>ASTKeywordNode</code> of keyword <code>super</code>.
     * DOT has already been parsed after "super".</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . super . Identifier ( ArgumentList )</strong>
     * </em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @param sooper An already parsed <code>ASTKeywordNode</code> of type <code>super</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTExpressionName exprName, ASTKeywordNode sooper) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setTypeName(exprName.convertToTypeName())
                .setSooper(sooper);
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * Parses an <code>ArgumentList</code>.
     * <em>
     * ArgumentList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression {, Expression}
     * </em>
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList parseArgumentList() {
        return parseList(
                BasicParser::isExpression,
                "Expected an expression.",
                COMMA,
                this::parseExpression,
                ASTArgumentList::new,
                false);
    }

    /**
     * Parses a <em>ClassInstanceCreationExpression</em>.
     * <em>
     * ClassInstanceCreationExpression:
     * &nbsp;&nbsp;&nbsp;&nbsp;UnqualifiedClassInstanceCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . UnqualifiedClassInstanceCreationExpression
     * </em>
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression() {
        return new ASTClassInstanceCreationExpression(curr().getLocation(), parseUnqualifiedClassInstanceCreationExpression());
    }

    /**
     * Parses a <code>ClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTPrimary</code>.  It is expected that the parser
     * is at ". new" in the Scanner.
     * <em>
     * ClassInstanceCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UnqualifiedClassInstanceCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Primary . UnqualifiedClassInstanceCreationExpression</strong>
     * </em>
     * @param alreadyParsed An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTPrimary alreadyParsed) {
        Location loc = alreadyParsed.getLocation();
        if (isCurr(DOT) && isNext(NEW)) {
            accept(DOT);
            ASTUnqualifiedClassInstanceCreationExpression ucice = parseUnqualifiedClassInstanceCreationExpression();
            return new ASTClassInstanceCreationExpression(loc, alreadyParsed, ucice);
        }
        else {
            throw internalError("'.' then 'new'");
        }
    }

    /**
     * Parses a <code>ClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "(" in
     * the Scanner.
     * <em>
     * ClassInstanceCreationExpression:
     * &nbsp;&nbsp;&nbsp;&nbsp;UnqualifiedClassInstanceCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . UnqualifiedClassInstanceCreationExpression
     * </em>
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        return new ASTClassInstanceCreationExpression(alreadyParsed.getLocation(),
                parseUnqualifiedClassInstanceCreationExpression(alreadyParsed));
    }

    /**
     * Parses an <code>UnqualifiedClassInstanceCreationExpression</code>.
     * <em>
     * UnqualifiedClassInstanceCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate ( [ArgumentList] )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate ( [ArgumentList] ) ClassBody
     * </em>
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression() {
        Location loc = curr().getLocation();
        if (accept(NEW) == null) {
            throw internalError(NEW);
        }
        ASTTypeToInstantiate tti = parseTypeToInstantiate();
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTArgumentList argumentList = parseArgumentList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTUnqualifiedClassInstanceCreationExpression(loc, tti, argumentList);
    }

    /**
     * Parses an <code>UnqualifiedClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "(" in
     * the Scanner.
     * <em>
     * UnqualifiedClassInstanceCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate ( [ArgumentList] )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate ( [ArgumentList] ) ClassBody
     * </em>
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected \"(\".");
        }
        ASTArgumentList argumentList = parseArgumentList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected \")\".");
        }
        return new ASTUnqualifiedClassInstanceCreationExpression(alreadyParsed.getLocation(), alreadyParsed, argumentList);
    }

    /**
     * Parses a <code>TypeToInstantiate</code>.
     * <em>
     * TypeToInstantiate:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName
     * </em>
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate parseTypeToInstantiate() {
        Location loc = curr().getLocation();
        ASTTypeName typeName = getNamesParser().parseTypeName();
        return new ASTTypeToInstantiate(loc, typeName);
    }

    /**
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTPrimary</code>.  DOT has also already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Primary . Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccess(ASTPrimary primary) {
        Location loc = primary.getLocation();
        return new ASTFieldAccess(loc, primary, getNamesParser().parseIdentifier());
    }

    /**
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTKeywordNode</code> of keyword <code>super</code>.  DOT has also
     * already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super . Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
     * </em>
     * @param sooper An already parsed <code>ASTKeywordNode</code> of type <code>super</code>
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTKeywordNode sooper) {
        Location loc = sooper.getLocation();
        return new ASTFieldAccess(loc, sooper, getNamesParser().parseIdentifier());
    }

    /**
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTExpressionName</code> and an already parsed <code>ASTKeywordNode</code>
     * of keyword <code>super</code>. DOT has also already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . super . Identifier</strong>
     * </em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @param sooper An already parsed <code>ASTKeywordNode</code> of type <code>super</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTExpressionName exprName, ASTKeywordNode sooper) {
        Location loc = exprName.getLocation();
        return new ASTFieldAccess(loc, exprName.convertToTypeName(), sooper, getNamesParser().parseIdentifier());
    }

    /**
     * Parses a <code>ClassLiteral</code>, given an already parsed
     * <code>ASTDataType</code>.
     * <em>
     * ClassLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType . class
     * </em>
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTClassLiteral</code>.
     */
    public ASTClassLiteral parseClassLiteral(ASTDataType dt) {
        Location loc = dt.getLocation();
        if (accept(DOT) == null || accept(CLASS) == null) {
            throw internalError("'.' then 'class'");
        }
        return new ASTClassLiteral(loc, dt);
    }

    /**
     * Parses the keyword <code>self</code>.
     * @return An <code>ASTKeywordNode</code> of keyword <code>self</code>.
     */
    public ASTKeywordNode parseSelf() {
        Token t;
        if ((t = accept(TokenType.SELF)) != null) {
            return new ASTKeywordNode(t.getLocation(), TokenType.SELF);
        }
        else {
            throw internalError(TokenType.SELF);
        }
    }

    /**
     * Parses the keyword <code>super</code>.
     * @return An <code>ASTKeywordNode</code> of keyword <code>super</code>.
     */
    public ASTKeywordNode parseSuper() {
        Token t;
        if ((t = accept(SUPER)) != null) {
            return new ASTKeywordNode(t.getLocation(), SUPER);
        }
        else {
            throw internalError(SUPER);
        }
    }

    /**
     * Helper method to avoid duplicating code for parsing binary expressions
     * that are left-associative.
     *
     * @param isOnInitialToken Determines whether a given token is a valid
     *                         token on which to start parsing the desired node.
     * @param acceptedTokens   A <code>List</code> of accepted <code>TokenTypes</code>
     *                         that can serve as operators.
     * @param childParser      Parses and returns the child node (operand).
     * @return Either an <code>ASTValueExpression</code> of the child type or an
     *     <code>ASTBinaryExpression</code> containing left-associative children.
     */
    private ASTValueExpression parseBinaryExpressionLeftAssociative(Predicate<Token> isOnInitialToken,
                                                                    List<TokenType> acceptedTokens, Supplier<? extends ASTValueExpression> childParser) {
        Location loc = curr().getLocation();
        ASTValueExpression result;
        if (isOnInitialToken.test(curr())) {
            result = childParser.get();
        }
        else {
            error(curr().getLocation(), "Expected a literal or expression name.");
            result = parseBadPrimary(EXPRESSION_STOPPERS);
        }
        TokenType curr;
        while ( (curr = isAcceptedOperator(acceptedTokens) ) != null) {
            accept(curr);
            if (isOnInitialToken.test(curr())) {
                result = new ASTBinaryExpression(loc, result, childParser.get(), curr);
            }
            else {
                error(curr().getLocation(), "Expected a literal or expression name.");
                result = new ASTBinaryExpression(loc, result, parseBadPrimary(EXPRESSION_STOPPERS), curr);
            }
        }
        return result;
    }
}
