package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.names.ASTAmbiguousName;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTTypeArguments;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>ExpressionsParser</code> is a <code>BasicParser</code> that parses
 * expressions.
 */
public class ExpressionsParser extends BasicParser {
    /**
     * Constructs a <code>ExpressionsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public ExpressionsParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression parseExpression() {
        Location loc = curr().getLocation();
        if (test(curr(), IDENTIFIER) && test(next(), ARROW)) {
            return new ASTExpression(loc, Arrays.asList(parseLambdaExpression()));
        }
        if (isPrimary(curr())) {
            return new ASTExpression(loc, Arrays.asList(parseConditionalExpression()));
        }
        else if (isCurr(PIPE) || isCurr(DOUBLE_PIPE)) {
            return new ASTExpression(loc, Arrays.asList(parseLambdaExpression()));
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected primary or lambda expression.  Got \"" + curr() + "\".");
        }
    }

    /**
     * Parses an <code>ASTLambdaExpression</code>.
     * @return An <code>ASTLambdaExpression</code>.
     */
    public ASTLambdaExpression parseLambdaExpression() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseLambdaParameters());
        if (accept(ARROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"->\".");
        }
        children.add(parseLambdaBody());
        ASTLambdaExpression lambda = new ASTLambdaExpression(loc, children);
        lambda.setOperation(ARROW);
        return lambda;
    }

    /**
     * Parses an <code>ASTLambdaParameters</code>.
     * @return An <code>ASTLambdaParameters</code>.
     */
    public ASTLambdaParameters parseLambdaParameters() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        TokenType type = null;
        switch (curr().getType()) {
        case DOUBLE_PIPE -> {
            accept(DOUBLE_PIPE);
            children.add(new ASTLambdaParameterList(loc, Collections.emptyList()));
            type = PIPE;
        }
        case PIPE -> {
            accept(PIPE);
            if (!test(curr(), PIPE)) {
                children.add(parseLambdaParameterList());
            }
            else {
                children.add(new ASTLambdaParameterList(loc, Collections.emptyList()));
            }
            if (accept(PIPE) == null) {
                throw new CompileException(curr().getLocation(), "Expected \"|\".");
            }
            type = PIPE;
        }
        case IDENTIFIER -> children.add(getNamesParser().parseIdentifier());
        default -> throw new CompileException(curr().getLocation(), "Expected lambda parameters.");
        }
        ASTLambdaParameters lambdaParams = new ASTLambdaParameters(loc, children);
        lambdaParams.setOperation(type);
        return lambdaParams;
    }

    /**
     * Parses an <code>ASTLambdaParameterList</code>.
     * @return An <code>ASTLambdaParameterList</code>.
     */
    public ASTLambdaParameterList parseLambdaParameterList() {
        Location loc = curr().getLocation();
        return switch(curr().getType()) {
            case VAR, MUT, TAKE -> new ASTLambdaParameterList(loc, Arrays.asList(getClassesParser().parseFormalParameterList()));
            case IDENTIFIER ->
                switch(next().getType()) {
                    case COMMA, PIPE -> new ASTLambdaParameterList(loc, Arrays.asList(parseInferredParameterList()));
                    default -> new ASTLambdaParameterList(loc, Arrays.asList(getClassesParser().parseFormalParameterList()));
                };

            default -> throw new CompileException(curr().getLocation(), "Expected lambda parameter(s), got \"" + curr() + "\".");
        };
    }

    /**
     * Parses an <code>ASTInferredParameterList</code>.
     * @return An <code>ASTInferredParameterList</code>.
     */
    public ASTInferredParameterList parseInferredParameterList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                COMMA,
                getNamesParser()::parseIdentifier,
                ASTInferredParameterList::new
        );
    }

    /**
     * Parses a <code>LambdaBody</code>.
     * @return A <code>LambdaBody</code>.
     */
    public ASTLambdaBody parseLambdaBody() {
        Location loc = curr().getLocation();
        if (test(curr(), OPEN_BRACE)) {
            return new ASTLambdaBody(loc, Arrays.asList(getStatementsParser().parseBlock()));
        }
        else {
            return new ASTLambdaBody(loc, Arrays.asList(parseExpression()));
        }
    }

    /**
     * Parses an <code>ASTLeftHandSide</code>.
     * @return An <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide parseLeftHandSide() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            ASTPrimary primary = parsePrimary();
            if (isCurr(OPEN_BRACKET)) {
                return new ASTLeftHandSide(loc, Arrays.asList(parseElementAccess(loc, primary)));
            }
            else {
                return primary.getLeftHandSide();
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Element access or identifier expected.");
        }
    }

    /**
     * Parses an <code>ASTConditionalExpression</code>; they are right-
     * associative with each other.
     * @return An <code>ASTConditionalExpression</code>.
     */
    public ASTConditionalExpression parseConditionalExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(3);
            children.add(parseLogicalOrExpression());
            ASTConditionalExpression node = new ASTConditionalExpression(loc, children);

            if (isCurr(QUESTION_MARK)) {
                accept(QUESTION_MARK);
                children.add(parseLogicalOrExpression());
                node.setOperation(QUESTION_MARK);

                if (isCurr(COLON)) {
                    accept(COLON);
                    children.add(parseConditionalExpression());
                }
                else {
                    throw new CompileException(curr().getLocation(), "Expected colon.");
                }
            }
            return node;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTLogicalOrExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalOrExpression</code>.
     */
    public ASTLogicalOrExpression parseLogicalOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(PIPE_COLON, DOUBLE_PIPE),
                this::parseLogicalXorExpression,
                ASTLogicalOrExpression::new
        );
    }

    /**
     * Parses an <code>ASTLogicalXorExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalXorExpression</code>.
     */
    public ASTLogicalXorExpression parseLogicalXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(CARET_COLON),
                this::parseLogicalAndExpression,
                ASTLogicalXorExpression::new
        );
    }

    /**
     * Parses an <code>ASTLogicalAndExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalAndExpression</code>.
     */
    public ASTLogicalAndExpression parseLogicalAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(AMPERSAND_COLON, DOUBLE_AMPERSAND),
                this::parseRelationalExpression,
                ASTLogicalAndExpression::new
        );
    }

    /**
     * Parses an <code>ASTRelationalExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTRelationalExpression</code>.
     */
    public ASTRelationalExpression parseRelationalExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseCompareExpression());
            ASTRelationalExpression node = new ASTRelationalExpression(loc, children);

            TokenType curr;
            while ( (curr = isAcceptedOperator(Arrays.asList(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL, NOT_EQUAL, ISA, IS, ISNT)) ) != null) {
                accept(curr);
                children = new ArrayList<>(2);
                children.add(node);
                if (curr == ISA) {
                    children.add(getTypesParser().parseDataType());
                }
                else {
                    children.add(parseCompareExpression());
                }
                node = new ASTRelationalExpression(loc, children);
                node.setOperation(curr);
            }
            return node;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTCompareExpression</code>; they are NOT associative
     * with each other.
     * @return An <code>ASTCompareExpression</code>.
     */
    public ASTCompareExpression parseCompareExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseBitwiseOrExpression());
            ASTCompareExpression node = new ASTCompareExpression(loc, children);

            if (isCurr(COMPARISON)) {
                accept(COMPARISON);
                children.add(parseBitwiseOrExpression());
                node.setOperation(COMPARISON);
            }
            return node;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTBitwiseOrExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseOrExpression</code>.
     */
    public ASTBitwiseOrExpression parseBitwiseOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(PIPE),
                this::parseBitwiseXorExpression,
                ASTBitwiseOrExpression::new
        );
    }

    /**
     * Parses an <code>ASTBitwiseXorExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseXorExpression</code>.
     */
    public ASTBitwiseXorExpression parseBitwiseXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(CARET),
                this::parseBitwiseAndExpression,
                ASTBitwiseXorExpression::new
        );
    }

    /**
     * Parses an <code>ASTBitwiseAndExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseAndExpression</code>.
     */
    public ASTBitwiseAndExpression parseBitwiseAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(AMPERSAND),
                this::parseShiftExpression,
                ASTBitwiseAndExpression::new
        );
    }

    /**
     * Parses an <code>ASTShiftExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTShiftExpression</code>.
     */
    public ASTShiftExpression parseShiftExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(SHIFT_LEFT, SHIFT_RIGHT),
                this::parseAdditiveExpression,
                ASTShiftExpression::new
        );
    }

    /**
     * Parses an <code>ASTAdditiveExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTAdditiveExpression</code>.
     */
    public ASTAdditiveExpression parseAdditiveExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(PLUS, MINUS),
                this::parseMultiplicativeExpression,
                ASTAdditiveExpression::new
        );
    }

    /**
     * Parses an <code>ASTMultiplicativeExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTMultiplicativeExpression</code>.
     */
    public ASTMultiplicativeExpression parseMultiplicativeExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(STAR, SLASH, PERCENT),
                this::parseCastExpression,
                ASTMultiplicativeExpression::new
        );
    }

    /**
     * Parses an <code>ASTCastExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTCastExpression</code>.
     */
    public ASTCastExpression parseCastExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseUnaryExpression());
            ASTCastExpression node = new ASTCastExpression(loc, children);

            while (isCurr(AS)) {
                accept(AS);
                if (children.size() == 1) {
                    children.add(getTypesParser().parseIntersectionType());
                    node.setOperation(AS);
                }
                else {
                    List<ASTNode> siblings = new ArrayList<>(2);
                    siblings.add(node);
                    siblings.add(getTypesParser().parseIntersectionType());
                    node = new ASTCastExpression(loc, siblings);
                    node.setOperation(AS);
                }
            }
            return node;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTUnaryExpression</code>.
     * @return An <code>ASTUnaryExpression</code>.
     */
    public ASTUnaryExpression parseUnaryExpression() {
        Location loc = curr().getLocation();
        if (isCurr(EXCLAMATION)) {
            accept(EXCLAMATION);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), EXCLAMATION);
        }
        else if (isCurr(TILDE)) {
            accept(TILDE);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), TILDE);
        }
        else if (isCurr(MINUS)) {
            accept(MINUS);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), MINUS);
        }
        else if (isCurr(SWITCH)) {
            return new ASTUnaryExpression(loc, parseSwitchExpression(), SWITCH);
        }
        else {
            return new ASTUnaryExpression(loc, parsePrimary());
        }
    }

    /**
     * Parses a <code>ASTSwitchExpression</code>.
     * @return A <code>ASTSwitchExpression</code>.
     */
    public ASTSwitchExpression parseSwitchExpression() {
        Location loc = curr().getLocation();
        if (accept(SWITCH) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"switch\".");
        }
        List<ASTNode> children = Arrays.asList(
            parseConditionalExpression(),
            parseSwitchExpressionBlock());
        ASTSwitchExpression se = new ASTSwitchExpression(loc, children);
        se.setOperation(SWITCH);
        return se;
    }

    /**
     * Parses an <code>ASTSwitchExpressionBlock</code>.
     * @return A <code>ASTSwitchExpressionBlock</code>.
     */
    public ASTSwitchExpressionBlock parseSwitchExpressionBlock() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"{\".");
        }
        List<ASTNode> children = Arrays.asList(parseSwitchExpressionRules());
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"}\".");
        }
        return new ASTSwitchExpressionBlock(loc, children);
    }

    /**
     * Parses an <code>ASTSwitchExpressionRules</code>.
     * @return A <code>ASTSwitchExpressionRules</code>.
     */
    public ASTSwitchExpressionRules parseSwitchExpressionRules() {
        return parseMultiple(
                t -> test(t, CASE, DEFAULT, MUT, VAR, IDENTIFIER) || isPrimary(curr()),
                "Expected a switch case.",
                this::parseSwitchExpressionRule,
                ASTSwitchExpressionRules::new
        );
    }

    /**
     * Parses an <code>ASTSwitchExpressionRule</code>.
     * @return A <code>ASTSwitchExpressionRule</code>.
     */
    public ASTSwitchExpressionRule parseSwitchExpressionRule() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseSwitchLabel());
        if (accept(ARROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected arrow (->).");
        }
        switch(curr().getType()) {
        case OPEN_BRACE -> children.add(getStatementsParser().parseBlock());
        case THROW ->  children.add(getStatementsParser().parseThrowStatement());
        default -> {
            children.add(parseExpression());
            if (accept(SEMICOLON) == null) {
                throw new CompileException(curr().getLocation(), "Expected semicolon.");
            }
        }
        }
        ASTSwitchExpressionRule ser = new ASTSwitchExpressionRule(loc, children);
        ser.setOperation(ARROW);
        return ser;
    }

    /**
     * Parses a <code>ASTSwitchLabel</code>.
     * @return A <code>ASTSwitchLabel</code>.
     */
    public ASTSwitchLabel parseSwitchLabel() {
        Location loc = curr().getLocation();
        return switch (curr().getType()) {
            case CASE -> {
                accept(CASE);
                ASTSwitchLabel temp = new ASTSwitchLabel(loc, Arrays.asList(parseCaseConstants()));
                temp.setOperation(CASE);
                yield temp;
            }
            case DEFAULT -> {
                accept(DEFAULT);
                ASTSwitchLabel temp = new ASTSwitchLabel(loc, Collections.emptyList());
                temp.setOperation(DEFAULT);
                yield temp;
            }
            default -> {
                List<ASTNode> children = new ArrayList<>(2);
                children.add(parsePattern());
                if (test(curr(), WHEN)) {
                    children.add(parseGuard());
                }
                yield new ASTSwitchLabel(loc, children);
            }
        };
    }

    /**
     * Parses a <code>ASTCaseConstants</code>.
     * @return A <code>ASTCaseConstants</code>.
     */
    public ASTCaseConstants parseCaseConstants() {
        return parseList(
                ExpressionsParser::isPrimary,
                "Expected conditional expression.",
                COMMA,
                this::parseConditionalExpression,
                ASTCaseConstants::new
        );
    }

    /**
     * Parses a <code>ASTPattern</code>.
     * @return A <code>ASTPattern</code>.
     */
    public ASTPattern parsePattern() {
        Location loc = curr().getLocation();
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            return new ASTPattern(loc, Arrays.asList(parseTypePattern()));
        }
        else if (isCurr(IDENTIFIER)) {
            ASTDataType dataType = getTypesParser().parseDataType();
            if (isCurr(OPEN_PARENTHESIS)) {
                return new ASTPattern(loc, Arrays.asList(parseRecordPattern(dataType)));
            }
            else {
                return new ASTPattern(loc, Arrays.asList(parseTypePattern(dataType)));
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a record pattern or a type pattern.");
        }
    }

    /**
     * Parses an <code>ASTGuard</code>.
     * @return A <code>ASTGuard</code>.
     */
    public ASTGuard parseGuard() {
        Location loc = curr().getLocation();
        if (accept(WHEN) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"when\".");
        }
        accept(WHEN);
        return new ASTGuard(loc, Arrays.asList(parseConditionalExpression()), WHEN);
    }

    /**
     * Parses an <code>ASTPatternList</code>, <code>ASTPattern</code>s separated by a comma.
     * @return An <code>ASTPatternList</code>.
     */
    public ASTPatternList parsePatternList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected type pattern or record pattern",
                COMMA,
                this::parsePattern,
                ASTPatternList::new
        );
    }

    /**
     * Parses a <code>ASTRecordPattern</code> with the already parsed
     * <code>ASTDataType</code>.
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTRecordPattern</code>.
     */
    public ASTRecordPattern parseRecordPattern(ASTDataType dataType) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(dataType);
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parsePatternList());
            if (accept(CLOSE_PARENTHESIS) == null) {
                throw new CompileException(curr().getLocation(), "Expected \")\".");
            }
        }
        return new ASTRecordPattern(loc, children);
    }

    /**
     * Parses a <code>ASTTypePattern</code>.
     * @return A <code>ASTTypePattern</code>.
     */
    public ASTTypePattern parseTypePattern() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            children.add(getStatementsParser().parseVariableModifierList());
        }
        children.add(getTypesParser().parseDataType());
        children.add(getNamesParser().parseIdentifier());
        return new ASTTypePattern(loc, children);
    }

    /**
     * Parses a <code>ASTTypePattern</code> with the already parsed
     * <code>ASTDataType</code>.
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTTypePattern</code>.
     */
    public ASTTypePattern parseTypePattern(ASTDataType dataType) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(dataType);
        children.add(getNamesParser().parseIdentifier());
        return new ASTTypePattern(loc, children);
    }

    /**
     * Parses an <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary() {
        Location loc = curr().getLocation();
        ASTPrimary primary;
        if (isLiteral(curr())) {
            ASTLiteral literal = getLiteralsParser().parseLiteral();
            primary = new ASTPrimary(loc, Arrays.asList(literal));
        }
        else if (isCurr(IDENTIFIER)) {
            if (isNext(OPEN_PARENTHESIS)) {
                // id(args)
                ASTIdentifier methodName = getNamesParser().parseIdentifier();
                primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            }
            else {
                ASTDataType dataType = getTypesParser().parseDataType();
                if (isCurr(DOUBLE_COLON)) {
                    return new ASTPrimary(loc, Arrays.asList(parseMethodReference(dataType)));
                }
                else if (isCurr(DOT) && isNext(CLASS)) {
                    // Get the class literal and get out.
                    return new ASTPrimary(loc, Arrays.asList(parseClassLiteral(dataType)));
                }
                ASTExpressionName expressionName = dataType.convertToExpressionName();
                primary = parsePrimary(expressionName);
            }
        }
        else if (isCurr(SELF)) {
            ASTSelf keywordSelf = parseSelf();
            primary = new ASTPrimary(loc, Arrays.asList(keywordSelf));
        }
        else if (isCurr(SUPER)) {
            ASTSuper sooper = parseSuper();
            if (isCurr(DOUBLE_COLON)) {
                // Method references don't chain.
                return new ASTPrimary(loc, Arrays.asList(parseMethodReferenceSuper(sooper)));
            }
            else {
                if (accept(DOT) == null) {
                    throw new CompileException(curr().getLocation(), "Expected '.'.");
                }
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(sooper)));
                }
                else {
                    // Field access
                    primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(sooper)));
                }
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            accept(OPEN_PARENTHESIS);
            ASTExpression expression = parseExpression();
            Token closeParen = accept(CLOSE_PARENTHESIS);
            if (closeParen == null) {
                throw new CompileException(curr().getLocation(), "Expected close parenthesis \")\".");
            }
            primary = new ASTPrimary(loc, Arrays.asList(expression));
            primary.setOperation(OPEN_PARENTHESIS);
        }
        else if (isCurr(NEW)) {
            if (isNext(LESS_THAN)) {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression();
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            else if (isNext(IDENTIFIER)) {
                accept(NEW);
                ASTTypeToInstantiate tti = parseTypeToInstantiate();
                if (isCurr(OPEN_BRACKET) || isCurr(OPEN_CLOSE_BRACKET)) {
                    primary = new ASTPrimary(loc, Arrays.asList(parseArrayCreationExpression(tti)));
                }
                else if (isCurr(OPEN_PARENTHESIS)) {
                    primary = new ASTPrimary(loc, Arrays.asList(parseClassInstanceCreationExpression(tti)));
                }
                else {
                    throw new CompileException(curr().getLocation(), "Malformed array or class instance creation expression.");
                }
            }
            else {
                throw new CompileException(next().getLocation(), "Type arguments or type to instantiate expected after new.");
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected: literal, expression name, or array or class instance creation expression.");
        }

        // Qualified Class Instance Creation, Element Access, and Method Invocations may chain up.
        // E.g. new Foo()[i].method1()[j].method2().new Bar()
        return parsePrimary(primary);
    }

    /**
     * Parses an <code>ASTPrimary</code>, given an already parsed
     * <code>ASTExpressionName</code>.
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTExpressionName exprName) {
        ASTPrimary primary;
        Location loc = exprName.getLocation();

        // If exprName is a simple identifier, then it is the method name.
        List<ASTNode> children = exprName.getChildren();
        if (children.size() == 1 && isCurr(OPEN_PARENTHESIS)) {
            // id( -> method invocation
            ASTIdentifier methodName = (ASTIdentifier) children.get(0);
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            return parsePrimary(primary);
        }

        if (isCurr(DOT) && isNext(SELF)) {
            // TypeName.self
            ASTTypeName tn = exprName.convertToTypeName();
            accept(DOT);
            primary = new ASTPrimary(loc, Arrays.asList(tn, parseSelf()));
            primary.setOperation(DOT);
        }
        else if (isCurr(DOT) && isNext(LESS_THAN)) {
            // ExprName.<TypeArgs>methodName(args)
            // NOT ExprName.<TypeArgs>super(args) -- Constructor invocation.
            // TODO: Determine a way to parse Type Arguments, yet keep them in case
            // "super" is encountered, which terminates the Primary at the given
            // ExpressionName.  Consider storing unused type args in the Primary.
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(exprName)));
        }
        else if (isCurr(DOT) && isNext(SUPER) && !isPeek(OPEN_PARENTHESIS)) {
            // TypeName.super.methodInvocation()
            // TypeName.super.fieldAccess
            // NOT Expression.super() -- Constructor invocation.
            if (accept(DOT) == null) {
                throw new CompileException(curr().getLocation(), "Expected '.'.");
            }
            ASTSuper sooper = parseSuper();
            if (isCurr(DOUBLE_COLON)) {
                // TypeName.super::[TypeArguments]Identifier
                accept(DOUBLE_COLON);
                List<ASTNode> mrChildren = new ArrayList<>(4);
                mrChildren.add(exprName.convertToTypeName());
                mrChildren.add(sooper);
                if (isCurr(LESS_THAN)) {
                    mrChildren.add(getTypesParser().parseTypeArguments());
                }
                mrChildren.add(getNamesParser().parseIdentifier());
                ASTMethodReference methodReference = new ASTMethodReference(loc, mrChildren);
                methodReference.setOperation(DOUBLE_COLON);
                return new ASTPrimary(loc, Arrays.asList(methodReference));
            }
            else if (isCurr(DOT)) {
                if (accept(DOT) == null) {
                    throw new CompileException(curr().getLocation(), "Expected '.'.");
                }

                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    // TypeName.super.<TypeArgs>methodName(args)
                    primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(exprName, sooper)));
                }
                else {
                    // TypeName.super.<TypeArgs>fieldName
                    primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(exprName, sooper)));
                }
            }
            else {
                throw new CompileException(curr().getLocation(), "Expected method reference (::), method invocation, or field access (.).");
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // ExprNameExceptForMethodName.methodName(args)
            children = exprName.getChildren();
            ASTIdentifier methodName = (ASTIdentifier) children.get(1);
            ASTAmbiguousName ambiguous = (ASTAmbiguousName) children.get(0);
            ASTExpressionName actual = new ASTExpressionName(ambiguous.getLocation(), ambiguous.getChildren());
            actual.setOperation(ambiguous.getOperation());
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(actual, methodName)));
        }
        else {
            // ExpressionName
            primary = new ASTPrimary(loc, Arrays.asList(exprName));
        }
        return parsePrimary(primary);
    }

    /**
     * Parses an <code>ASTPrimary</code>, given an already parsed
     * <code>ASTPrimary</code>.  This accounts for circularity in the Primary
     * productions, where method invocations, element access, field access, and
     * qualified class instance creations can be chained.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTPrimary primary) {
        Location loc = primary.getLocation();

        // Non-recursive method reference.
        // Primary :: [TypeArguments] identifier
        if (isCurr(DOUBLE_COLON)) {
            accept(DOUBLE_COLON);
            List<ASTNode> children = new ArrayList<>(3);
            children.add(primary);
            if (isCurr(LESS_THAN)) {
                children.add(getTypesParser().parseTypeArguments());
            }
            children.add(getNamesParser().parseIdentifier());
            ASTMethodReference methodReference = new ASTMethodReference(loc, children);
            methodReference.setOperation(DOUBLE_COLON);
            return new ASTPrimary(loc, Arrays.asList(methodReference));
        }

        // Qualified Class Instance Creation, Element Access, Field Access, and
        // Method Invocations may chain up.
        // E.g. new Foo()[i].method1()[j].method2().new Bar()
        while (isCurr(OPEN_BRACKET) ||
                (isCurr(DOT) && isNext(NEW)) ||
                (isCurr(DOT) && isNext(LESS_THAN)) ||
                (isCurr(DOT) && isNext(IDENTIFIER))
                ) {
            if (isCurr(DOT) && isNext(NEW)) {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            if (isCurr(DOT) && (isNext(LESS_THAN) || isNext(IDENTIFIER))) {
                accept(DOT);
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    ASTMethodInvocation mi = parseMethodInvocation(primary);
                    primary = new ASTPrimary(loc, Arrays.asList(mi));
                }
                else {
                    ASTFieldAccess fa = parseFieldAccess(primary);
                    primary = new ASTPrimary(loc, Arrays.asList(fa));
                }
            }
            if (isCurr(OPEN_BRACKET)) {
                ASTElementAccess ea = parseElementAccess(loc, primary);
                primary = new ASTPrimary(loc, Arrays.asList(ea));
            }
        }
        return primary;
    }

    /**
     * Parses an <code>ASTMethodInvocation</code>, given an <code>ASTIdentifier</code>
     * that has already been parsed and its <code>Location</code>.
     * @param identifier An already parsed <code>ASTIdentifier</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTIdentifier identifier) {
        Location loc = identifier.getLocation();
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(identifier);
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(loc, children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * <p>Parses an <code>ASTMethodInvocation</code>, given an already parsed
     * <code>super</code>.  The DOT following "super" has been parsed also.</p>
     * <em>MethodInvocation: super . [TypeArguments] Identifier ( [ArgumentList] )</em>
     * @param sooper An already parsed <code>super</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTSuper sooper) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        children.add(sooper);
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArguments());
        }
        children.add(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(loc, children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * <p>Parses an <code>ASTMethodInvocation</code>, with <code>super</code>,
     * starting with an already parsed <code>ASTExpressionName</code>, which is
     * converted to an <code>ASTTypeName</code>, and an already parsed <code>ASTSuper</code>.
     * DOT has already been parsed after "super".</p>
     * <em>MethodInvocation: TypeName . super . [TypeArguments] Identifier ( [ArgumentList] )</em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTExpressionName exprName, ASTSuper sooper) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(5);
        children.add(exprName.convertToTypeName());
        children.add(sooper);
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArguments());
        }
        children.add(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(loc, children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * <p>Parses an <code>ASTMethodInvocation</code>, given an <code>ASTPrimary</code>
     * that has already been parsed.</p>
     * <em>MethodInvocation: Primary . [TypeArguments] Identifier ( [ArgumentList] )</em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTPrimary primary) {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(primary);
        if (isCurr(LESS_THAN)) {
            // Keep the type arguments in case of:
            // Primary . TypeArguments super (), a constructor invocation.

            // We can get here from the parseConstructorInvocation method in
            // the case of Primary . <    -- This could produce:
            // MethodInvocation -> Primary . TypeArguments Identifier ( [ArgumentList] )
            // OR
            // ConstructorInvocation -> Primary . TypeArguments super ( [ArgumentList] )

            // Currently there is no clean way of pushing the type arguments
            // back on to the parser when we get a super.  The easiest (not
            // best) way is to throw an (otherwise invisible) exception that
            // contains the primary and type arguments for a constructor invocation.

            // Get it working, then improve it later.
            ASTTypeArguments typeArgs = getTypesParser().parseTypeArguments();
            if (isCurr(SUPER)) {
                // Primary . TypeArguments super ( [ArgumentList] )
                List<ASTNode> alreadyParsed = new ArrayList<>(2);
                alreadyParsed.add(primary);
                alreadyParsed.add(typeArgs);
                throw new CompileException(curr().getLocation(), "Expected method name.", alreadyParsed);
            }
            children.add(typeArgs);
        }
        children.add(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(primary.getLocation(), children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * <p>Parses an <code>ASTMethodInvocation</code>, given an <code>ASTExpressionName</code>
     * that has already been parsed.</p>
     * <em>MethodInvocation: ExpressionName . [TypeArguments] Identifier ( [ArgumentList] )</em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTExpressionName exprName) {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(exprName);
        if (accept(DOT) == null) {
            throw new CompileException(curr().getLocation(), "Expected '.'.");
        }
        if (isCurr(LESS_THAN)) {
            // Keep the type arguments in case of:
            // ExpressionName . TypeArguments super (), a constructor invocation.

            // We can get here from the parseConstructorInvocation method in
            // the case of ExpressionName . <    -- This could produce:
            // MethodInvocation -> ExpressionName . TypeArguments Identifier ( [ArgumentList] )
            // OR
            // ConstructorInvocation -> ExpressionName . TypeArguments super ( [ArgumentList] )

            // Currently there is no clean way of pushing the type arguments
            // back on to the parser when we get a super.  The easiest (not
            // best) way is to throw an (otherwise invisible) exception that
            // contains the expression name and type arguments for a constructor invocation.

            // Get it working, then improve it later.
            ASTTypeArguments typeArgs = getTypesParser().parseTypeArguments();
            if (isCurr(SUPER)) {
                // ExpressionName . TypeArguments super ( [ArgumentList] )
                List<ASTNode> alreadyParsed = new ArrayList<>(2);
                alreadyParsed.add(exprName);
                alreadyParsed.add(typeArgs);
                throw new CompileException(curr().getLocation(), "Expected method name.", alreadyParsed);
            }
            children.add(typeArgs);
        }
        children.add(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(exprName.getLocation(), children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * <p>Parses an <code>ASTMethodInvocation</code>, given an <code>ASTExpressionName</code>
     * that has already been parsed and broken up into the given <code>ASTExpressionName</code>
     * and an <code>ASTIdentifier</code> serving as the method name.</p>
     * <em>MethodInvocation: ExpressionName . Identifier ( [ArgumentList] )</em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocation(ASTExpressionName exprName, ASTIdentifier methodName) {
        List<ASTNode> children = new ArrayList<>(3);
        children.add(exprName);
        children.add(methodName);
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }

        ASTMethodInvocation node = new ASTMethodInvocation(exprName.getLocation(), children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * Parses an <code>ASTMethodReference</code>, given an already parsed
     * <code>ASTDataType</code>.
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTMethodReference</code>.
     */
    public ASTMethodReference parseMethodReference(ASTDataType dataType) {
        Location loc = dataType.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (accept(DOUBLE_COLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected '::'.");
        }
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArguments());
        }
        if (isCurr(NEW)) {
            accept(NEW);
            children.add(0, dataType);
            ASTMethodReference node = new ASTMethodReference(loc, children);
            node.setOperation(DOUBLE_COLON);
            return node;
        }
        else if (isCurr(IDENTIFIER)) {
            children.add(getNamesParser().parseIdentifier());
            try {
                // ExpressionName :: [TypeArguments] Identifier
                ASTExpressionName exprName = dataType.convertToExpressionName();
                children.add(0, exprName);
            }
            catch (CompileException tryExpressionName) {
                // DataType :: [TypeArguments] Identifier
                children.add(0, dataType);
            }
            ASTMethodReference node = new ASTMethodReference(loc, children);
            node.setOperation(DOUBLE_COLON);
            return node;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected identifier or new.");
        }
    }

    /**
     * Parses an <code>ASTMethodReference</code>, given an already parsed
     * <code>ASTSuper</code>.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTMethodReference parseMethodReferenceSuper(ASTSuper sooper) {
        Location loc = sooper.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(sooper);
        if (accept(DOUBLE_COLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected '::'.");
        }
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArguments());
        }
        children.add(getNamesParser().parseIdentifier());
        ASTMethodReference node = new ASTMethodReference(loc, children);
        node.setOperation(DOUBLE_COLON);
        return node;
    }

    /**
     * Parses an <code>ASTClassInstanceCreationExpression</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (isCurr(NEW)) {
            children.add(parseUnqualifiedClassInstanceCreationExpression());
        }
        else {
            ASTPrimary primary = parsePrimary();
            return parseClassInstanceCreationExpression(primary);
        }
        return new ASTClassInstanceCreationExpression(loc, children);
    }

    /**
     * Parses an <code>ASTClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTPrimary</code>.  It is expected that the parser
     * is at ". new" in the Scanner.
     * @param alreadyParsed An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTPrimary alreadyParsed) {
        Location loc = alreadyParsed.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(alreadyParsed);
        if (isCurr(DOT) && isNext(NEW)) {
            accept(DOT);
            children.add(parseUnqualifiedClassInstanceCreationExpression());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected . new");
        }
        return new ASTClassInstanceCreationExpression(loc, children);
    }

    /**
     * Parses an <code>ASTClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "(" in
     * the Scanner.
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        return new ASTClassInstanceCreationExpression(alreadyParsed.getLocation(), Arrays.asList(
                parseUnqualifiedClassInstanceCreationExpression(alreadyParsed)
        ));
    }

    /**
     * Parses an <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression() {
        Location loc = curr().getLocation();
        if (accept(NEW) == null) {
            throw new CompileException(curr().getLocation(), "Expected new.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArguments());
        }
        children.add(parseTypeToInstantiate());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \")\".");
        }
        ASTUnqualifiedClassInstanceCreationExpression node = new ASTUnqualifiedClassInstanceCreationExpression(loc, children);
        node.setOperation(NEW);
        return node;
    }

    /**
     * Parses an <code>ASTUnqualifiedClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "(" in
     * the Scanner.
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(alreadyParsed);
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \")\".");
        }
        ASTUnqualifiedClassInstanceCreationExpression node = new ASTUnqualifiedClassInstanceCreationExpression(alreadyParsed.getLocation(), children);
        node.setOperation(NEW);
        return node;
    }

    /**
     * Parses an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList parseArgumentList() {
        if (isExpression(curr())) {
            return parseList(
                    BasicParser::isExpression,
                    "Expected an expression.",
                    COMMA,
                    this::parseGiveExpression,
                    ASTArgumentList::new);
        }
        else {
            return new ASTArgumentList(curr().getLocation(), Collections.emptyList());
        }
    }

    /**
     * Parses a <code>GiveExpression</code>.
     * @return A <code>GiveExpression</code>.
     */
    public ASTGiveExpression parseGiveExpression() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        ASTGiveExpression ge = new ASTGiveExpression(loc, children);
        if (isCurr(GIVE)) {
            accept(GIVE);
            ge.setOperation(GIVE);
        }
        children.add(parseExpression());
        return ge;
    }

    /**
     * Parses an <code>ASTArrayCreationExpression</code>.
     * @return An <code>ASTArrayCreationExpression</code>.
     */
    public ASTArrayCreationExpression parseArrayCreationExpression() {
        if (accept(NEW) == null) {
            throw new CompileException(curr().getLocation(), "Expected new.");
        }
        ASTTypeToInstantiate tti = parseTypeToInstantiate();
        return parseArrayCreationExpression(tti);
    }

    /**
     * Parses an <code>ASTArrayCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "[" or
     * "[[" in the Scanner.
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTArrayCreationExpression</code>.
     */
    public ASTArrayCreationExpression parseArrayCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(alreadyParsed);
        boolean dimExprsPresent = false;
        if (isCurr(OPEN_BRACKET)) {
            children.add(parseDimExprs());
            dimExprsPresent = true;
        }
        if (isCurr(OPEN_CLOSE_BRACKET)) {
            children.add(getTypesParser().parseDims());
        }
        if (isCurr(OPEN_BRACE)) {
            if (dimExprsPresent) {
                throw new CompileException(curr().getLocation(), "Array initializer not expected with dimension expressions.");
            }
            children.add(parseArrayInitializer());
        }
        ASTArrayCreationExpression node = new ASTArrayCreationExpression(loc, children);
        node.setOperation(NEW);
        return node;
    }

    /**
     * Parses an <code>ASTDimExprs</code>.
     * @return An <code>ASTDimExprs</code>.
     */
    public ASTDimExprs parseDimExprs() {
        return parseMultiple(
                t -> test(t, OPEN_BRACKET),
                "Expected \"[\".",
                this::parseDimExpr,
                ASTDimExprs::new
        );
    }

    /**
     * Parses an <code>ASTDimExpr</code>.
     * @return An <code>ASTDimExpr</code>.
     */
    public ASTDimExpr parseDimExpr() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"[\".");
        }
        ASTExpression expr = parseExpression();
        if (accept(CLOSE_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"]\".");
        }
        ASTDimExpr node = new ASTDimExpr(loc, Arrays.asList(expr));
        node.setOperation(OPEN_BRACKET);
        return node;
    }

    /**
     * Parses an <code>ASTArrayInitializer</code>.
     * @return An <code>ASTArrayInitializer</code>.
     */
    public ASTArrayInitializer parseArrayInitializer() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"{\".");
        }
        ASTArrayInitializer node;
        if (isPrimary(curr()) || isCurr(OPEN_BRACE)) {
            ASTVariableInitializerList vil = parseVariableInitializerList();
            node = new ASTArrayInitializer(loc, Arrays.asList(vil));
        }
        else {
            node = new ASTArrayInitializer(loc, Collections.emptyList());
        }
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"}\".");
        }
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTVariableInitializerList</code>.
     * @return An <code>ASTVariableInitializerList</code>.
     */
    public ASTVariableInitializerList parseVariableInitializerList() {
        return parseList(
                t -> isPrimary(t) || test(t, OPEN_BRACE),
                "Expected expression (no incr/decr) or array initializer.",
                COMMA,
                this::parseVariableInitializer,
                ASTVariableInitializerList::new
        );
    }

    /**
     * Parses an <code>ASTVariableInitializer</code>.
     * @return An <code>ASTVariableInitializer</code>.
     */
    public ASTVariableInitializer parseVariableInitializer() {
        Location loc = curr().getLocation();
        if (isPrimary(curr())) {
            ASTExpression expr = parseExpression();
            return new ASTVariableInitializer(loc, Arrays.asList(expr));
        }
        else if (isCurr(OPEN_BRACE)) {
            ASTArrayInitializer arrayInit = parseArrayInitializer();
            return new ASTVariableInitializer(loc, Arrays.asList(arrayInit));
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected expression (no incr/decr) or array initializer.");
        }
    }

    /**
     * Parses an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate parseTypeToInstantiate() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getNamesParser().parseTypeName());
        if (isCurr(LESS_THAN)) {
            children.add(getTypesParser().parseTypeArgumentsOrDiamond());
        }
        return new ASTTypeToInstantiate(loc, children);
    }

    /**
     * Parses an <code>ASTElementAccess</code>, given an <code>ASTPrimary</code>
     * that has already been parsed and its <code>Location</code>.
     * @param loc The <code>Location</code> of <code>primary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTElementAccess</code>.
     */
    public ASTElementAccess parseElementAccess(Location loc, ASTPrimary primary) {
        if (accept(OPEN_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected '['.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(primary);
        children.add(parseExpression());
        if (accept(CLOSE_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected ']'.");
        }

        ASTElementAccess ea = new ASTElementAccess(loc, children);
        while (isCurr(OPEN_BRACKET)) {
            accept(OPEN_BRACKET);
            children = new ArrayList<>(2);
            children.add(ea);
            children.add(parseExpression());
            if (accept(CLOSE_BRACKET) == null) {
                throw new CompileException(curr().getLocation(), "Expected ']'.");
            }
            ea = new ASTElementAccess(loc, children);
        }
        return ea;
    }

    /**
     * <p>Parses an <code>ASTFieldAccess</code>, given an already parsed
     * <code>ASTSuper</code>.  DOT has also already been parsed.</p>
     * <em>super . identifier</em>
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTSuper sooper) {
        Location loc = sooper.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(sooper);
        children.add(getNamesParser().parseIdentifier());
        ASTFieldAccess node = new ASTFieldAccess(loc, children);
        node.setOperation(DOT);
        return node;
    }

    /**
     * <p>Parses an <code>ASTFieldAccess</code>, given an already parsed
     * <code>ASTExpressionName</code> and an already parsed <code>ASTSuper</code>.
     * DOT has also already been parsed.</p>
     * <em>TypeName . super . identifier</em>
     * @param exprName An already parsed <code>ASTExpressionName</code>.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTExpressionName exprName, ASTSuper sooper) {
        Location loc = exprName.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(exprName.convertToTypeName());
        children.add(sooper);
        children.add(getNamesParser().parseIdentifier());
        ASTFieldAccess node = new ASTFieldAccess(loc, children);
        node.setOperation(DOT);
        return node;
    }

    /**
     * <p>Parses an <code>ASTFieldAccess</code>, given an already parsed
     * <code>ASTPrimary</code>.  DOT has also already been parsed.</p>
     * <em>Primary . Identifier</em>
     * @param primary An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccess(ASTPrimary primary) {
        Location loc = primary.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(primary);
        children.add(getNamesParser().parseIdentifier());
        ASTFieldAccess node = new ASTFieldAccess(loc, children);
        node.setOperation(DOT);
        return node;
    }

    /**
     * Parses an <code>ASTClassLiteral</code>, given an already parsed
     * <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTClassLiteral</code>.
     */
    public ASTClassLiteral parseClassLiteral(ASTDataType dt) {
        Location loc = dt.getLocation();

        if (accept(DOT) == null || accept(CLASS) == null) {
            throw new CompileException(curr().getLocation(), "Expected .class");
        }

        ASTClassLiteral node = new ASTClassLiteral(loc, Arrays.asList(dt));
        node.setOperation(CLASS);
        return node;
    }

    /**
     * Parses an <code>ASTSelf</code>.
     * @return An <code>ASTSelf</code>.
     */
    public ASTSelf parseSelf() {
        Token t;
        if ((t = accept(SELF)) != null) {
            return new ASTSelf(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected 'self'.");
        }
    }

    /**
     * Parses an <code>ASTSuper</code>.
     * @return An <code>ASTSuper</code>.
     */
    public ASTSuper parseSuper() {
        Token t;
        if ((t = accept(SUPER)) != null) {
            return new ASTSuper(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected 'super'.");
        }
    }
}
