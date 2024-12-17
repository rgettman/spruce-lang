package org.spruce.compiler.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTDims;
import org.spruce.compiler.ast.types.ASTTypeArgumentList;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.ast.expressions.ASTPrimary.Type.*;
import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>ExpressionsParser</code> is a <code>BasicParser</code> that parses
 * expressions.
 */
public class ExpressionsParser extends BasicParser {
    // If we get a bad Primary, don't consume these when attempting to parse
    // beyond it.
    public static final List<TokenType> PRIMARY_STOPPERS = Arrays.asList(
            CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE,
            COLON, COMMA, SEMICOLON, ARROW,
            INCREMENT, DECREMENT,
            LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL, NOT_EQUAL, COMPARISON,
            AMPERSAND, AMPERSAND_COLON, DOUBLE_AMPERSAND, PIPE, PIPE_COLON, DOUBLE_PIPE, CARET, TILDE, EXCLAMATION,
            SHIFT_LEFT, SHIFT_RIGHT, INCREMENT, DECREMENT, PLUS, MINUS, STAR, SLASH, PERCENT, AS, ISA, IS, ISNT,
            EOF
    );

    // If we get a bad Expression, don't consume these when attempting to parse
    // beyond it.  Primary stoppers without the binary operators.  Switch
    // expression parts and statement starters.
    public static final List<TokenType> EXPRESSION_STOPPERS = Arrays.asList(
            CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE,
            COLON, COMMA, SEMICOLON, ARROW,
            INCREMENT, DECREMENT,
            CASE, DEFAULT,
            ASSERT, BREAK, CONTINUE, CRITICAL, DO, FALLTHROUGH, FOR, IF, RETURN, THROW, TRY, USE, WHILE, YIELD,
            EOF
    );

    /**
     * Constructs an <code>ExpressionsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public ExpressionsParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>Expression</code>.
     * <em>
     * Expression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LambdaExpression<br>
     * </em>
     * @return An <code>ASTExpression</code> that could be an <code>ASTLambdaExpression</code>
     *     or an <code>ASTValueExpression</code>.
     */
    public ASTExpression parseExpression() {
        if (test(curr(), IDENTIFIER) && test(next(), ARROW)) {
            return parseLambdaExpression();
        }
        if (isValueExpression(curr())) {
            return parseValueExpression();
        }
        else if (isCurr(PIPE) || isCurr(DOUBLE_PIPE)) {
            return parseLambdaExpression();
        }
        else {
            error(curr().getLocation(), "Expected primary or lambda expression.  Got \"" + curr() + "\".");
            return parseBadPrimary(EXPRESSION_STOPPERS);
        }
    }

    /**
     * Parses a <code>LambdaExpression</code>.
     * <em>
     * LambdaExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LambdaParameters -> LambdaBody<br>
     * </em>
     * @return An <code>ASTLambdaExpression</code>.
     */
    public ASTLambdaExpression parseLambdaExpression() {
        Location loc = curr().getLocation();
        ASTLambdaParameters lambdaParams = parseLambdaParameters();
        if (accept(ARROW) == null) {
            error(curr().getLocation(), "Expected arrow (->).");
        }
        return new ASTLambdaExpression(loc, lambdaParams, parseLambdaBody());
    }

    /**
     * Parses a <code>LambdaParameters</code>.
     * <em>
     * LambdaParameters:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;||<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;| [LambdaParameterList] |<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * </em>
     * @return An <code>ASTLambdaParameters</code>.
     */
    public ASTLambdaParameters parseLambdaParameters() {
        Location loc = curr().getLocation();
        return switch (curr().getType()) {
        case DOUBLE_PIPE -> {
            accept(DOUBLE_PIPE);
            yield new ASTLambdaParameters(loc);
        }
        case PIPE -> {
            accept(PIPE);
            ASTLambdaParameters result;
            if (!test(curr(), PIPE)) {
                result = new ASTLambdaParameters(loc, parseLambdaParameterList());
            }
            else {
                result = new ASTLambdaParameters(loc);
            }
            if (accept(PIPE) == null) {
                error(curr().getLocation(), "Expected '|'.");
            }
            yield result;
        }
        case IDENTIFIER -> new ASTLambdaParameters(loc, getNamesParser().parseIdentifier());
        default -> {
            error(loc, "Expected lambda parameters.");
            yield new ASTLambdaParameters(loc);
        }
        };
    }

    /**
     * Parses a <code>LambdaParameterList</code>.
     * <em>
     * LambdaParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InferredParameterList<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameterList<br>
     * </em>
     * @return An <code>ASTLambdaParameterList</code> that could be either a
     *     <code>ASTInferredParameterList</code> or an <code>ASTFormalParameterList</code>.
     */
    public ASTLambdaParameterList parseLambdaParameterList() {
        return switch(curr().getType()) {
            case IDENTIFIER ->
                switch(next().getType()) {
                    case COMMA, PIPE -> parseInferredParameterList();
                    default -> getClassesParser().parseFormalParameterList();
                };
            case PIPE -> parseInferredParameterList();
            default -> getClassesParser().parseFormalParameterList();
        };
    }

    /**
     * Parses an <code>InferredParameterList</code>.
     * <em>
     * InferredParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}<br>
     * </em>
     * @return An <code>ASTInferredParameterList</code>.
     */
    public ASTInferredParameterList parseInferredParameterList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                COMMA,
                getNamesParser()::parseIdentifier,
                Arrays.asList(PIPE, CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE, OPEN_BRACE, SEMICOLON, ARROW, EOF),
                ASTInferredParameterList::new,
                false
        );
    }

    /**
     * Parses a <code>LambdaBody</code>.
     * <em>
     * LambdaBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
     * </em>
     * @return An <code>ASTLambdaBody</code> which could be an <code>ASTExpression</code>
     *     or an <code>ASTBlock</code>.
     */
    public ASTLambdaBody parseLambdaBody() {
        if (test(curr(), OPEN_BRACE)) {
            return getStatementsParser().parseBlock();
        }
        else {
            return parseExpression();
        }
    }

    /**
     * Parses a <code>ValueExpression</code>.
     * <em>
     * ValueExpression:
     * &nbsp;&nbsp;&nbsp;&nbsp;IfExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;BinaryExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;IsaExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpression
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary
     * </em>
     * <em>
     * IfExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;if LogicalOrExpression use Expression else Expression<br>
     * </em>
     * @return An implementation of <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseValueExpression() {
        if (isValueExpression(curr())) {
            if (isCurr(IF)) {
                Location loc = curr().getLocation();
                accept(IF);
                ASTValueExpression logicalOrExpr = parseLogicalOrExpression();
                if (accept(USE) == null) {
                    error(curr().getLocation(), "Expected 'use'.");
                }
                ASTExpression exprIfTrue = parseExpression();
                if (accept(ELSE) == null) {
                    error(curr().getLocation(), "Expected 'else'.");
                }
                ASTExpression exprIfFalse = parseExpression();
                    return new ASTIfExpression(loc, logicalOrExpr, exprIfTrue, exprIfFalse);
            }
            else {
                return parseLogicalOrExpression();
            }
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
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalXorExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression || LogicalXorExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression |: LogicalXorExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseLogicalOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(PIPE_COLON, DOUBLE_PIPE),
                this::parseLogicalXorExpression
        );
    }

    /**
     * Parses a <code>LogicalXorExpression</code>; they are left-
     * associative with each other.
     * <em>
     * LogicalXorExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalXorExpression ^: LogicalAndExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseLogicalXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(CARET_COLON),
                this::parseLogicalAndExpression
        );
    }

    /**
     * Parses a <code>LogicalAndExpression</code>; they are left-
     * associative with each other.
     * <em>
     * LogicalAndExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression &amp;&amp; RelationalExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression &amp;: RelationalExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseLogicalAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(AMPERSAND_COLON, DOUBLE_AMPERSAND),
                this::parseRelationalExpression
        );
    }

    /**
     * Parses a <code>RelationalExpression</code>; they are left-
     * associative with each other.
     * <em>
     * RelationalExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt; CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt;= CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt; CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt;= CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression == CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression != CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression is CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression isnt CompareExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression isa DataType
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseRelationalExpression() {
        Location loc = curr().getLocation();
        ASTValueExpression result = parseCompareExpression();
        TokenType curr;
        while ( (curr = isAcceptedOperator(Arrays.asList(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL, NOT_EQUAL, ISA, IS, ISNT)) ) != null) {
            accept(curr);
            if (curr == ISA) {
                result = new ASTIsaExpression(loc, result, getTypesParser().parseDataType());
            }
            else {
                result = new ASTBinaryExpression(loc, result, parseCompareExpression(), curr);
            }
        }
        return result;
    }

    /**
     * Parses a <code>CompareExpression</code>; they are NOT associative
     * with each other.
     * <em>
     * CompareExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression &lt;=&gt; BitwiseOrExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseCompareExpression() {
        if (isValueExpression(curr())) {
            Location loc = curr().getLocation();
            ASTValueExpression result = parseBitwiseOrExpression();
            if (isCurr(COMPARISON)) {
                accept(COMPARISON);
                return new ASTBinaryExpression(loc, result, parseBitwiseOrExpression(), COMPARISON);
            }
            return result;
        }
        else {
            error(curr().getLocation(), "Expected a literal or expression name.");
            return parseBadPrimary(EXPRESSION_STOPPERS);
        }
    }

    /**
     * Parses a <code>BitwiseOrExpression</code>; they are left-
     * associative with each other.
     * <em>
     * BitwiseOrExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseXorExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression | BitwiseXorExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseBitwiseOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(PIPE),
                this::parseBitwiseXorExpression
        );
    }

    /**
     * Parses a <code>BitwiseXorExpression</code>; they are left-
     * associative with each other.
     * <em>
     * BitwiseXorExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseAndExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseXorExpression ^ BitwiseAndExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseBitwiseXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(CARET),
                this::parseBitwiseAndExpression
        );
    }

    /**
     * Parses a <code>BitwiseAndExpression</code>; they are left-
     * associative with each other.
     * <em>
     * BitwiseAndExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseAndExpression &amp; ShiftExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseBitwiseAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(AMPERSAND),
                this::parseShiftExpression
        );
    }

    /**
     * Parses a <code>ShiftExpression</code>; they are left-
     * associative with each other.
     * <em>
     * ShiftExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &lt;&lt; AdditiveExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &gt;&gt; AdditiveExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseShiftExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(SHIFT_LEFT, SHIFT_RIGHT),
                this::parseAdditiveExpression
        );
    }

    /**
     * Parses a <code>AdditiveExpression</code>; they are left-
     * associative with each other.
     * <em>
     * AdditiveExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression + MultiplicativeExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression - MultiplicativeExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseAdditiveExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(PLUS, MINUS),
                this::parseMultiplicativeExpression
        );
    }

    /**
     * Parses a <code>MultiplicativeExpression</code>; they are left-
     * associative with each other.
     * <em>
     * MultiplicativeExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression * CastExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression / CastExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression % CastExpression
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseMultiplicativeExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isValueExpression,
                Arrays.asList(STAR, SLASH, PERCENT),
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
     * &nbsp;&nbsp;&nbsp;&nbsp;- UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;~ UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;! UnaryExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpression<br>
     * </em>
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression parseUnaryExpression() {
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
            return parseSwitchExpression();
        }
        else {
            return parsePrimary();
        }
    }

    /**
     * Parses a <code>SwitchExpression</code>.
     * <em>
     * SwitchStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;switch ValueExpression SwitchExpressionBlock
     * </em>
     * @return An <code>ASTSwitchExpression</code>.
     */
    public ASTSwitchExpression parseSwitchExpression() {
        Location loc = curr().getLocation();
        if (accept(SWITCH) == null) {
            throw internalError(SWITCH);
        }
        return new ASTSwitchExpression(loc, parseValueExpression(), parseSwitchExpressionBlock());
    }

    /**
     * Parses a <code>SwitchExpressionBlock</code>.
     * <em>
     * SwitchExpressionBlock:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchExpressionRules }
     * </em>
     * @return An <code>ASTSwitchExpressionRules</code>.
     */
    public ASTSwitchExpressionRules parseSwitchExpressionBlock() {
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTSwitchExpressionRules rules = parseSwitchExpressionRules();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return rules;
    }

    /**
     * Parses a <code>SwitchExpressionRules</code>.
     * <em>
     * SwitchExpressionRules:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpressionRule {SwitchExpressionRule}<br>
     * </em>
     * @return An <code>ASTSwitchExpressionRules</code>.
     */
    public ASTSwitchExpressionRules parseSwitchExpressionRules() {
        return parseMultiple(
                t -> test(t, Arrays.asList(CASE, DEFAULT, MUT, VAR, IDENTIFIER)),
                "Expected a switch expression rule.",
                this::parseSwitchExpressionRule,
                PRIMARY_STOPPERS,
                ASTSwitchExpressionRules::new
        );
    }

    /**
     * Parses a <code>SwitchExpressionRule</code>.
     * <em>
     * SwitchExpressionRule:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Expression ;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
     * </em>
     * @return An <code>ASTSwitchExpressionRule</code>.
     */
    public ASTSwitchExpressionRule parseSwitchExpressionRule() {
        Location loc = curr().getLocation();
        ASTSwitchLabel switchLabel = parseSwitchLabel();
        if (accept(ARROW) == null) {
            error(curr().getLocation(), "Expected arrow (->).");
        }
        switch(curr().getType()) {
            case OPEN_BRACE -> {
                return new ASTSwitchExpressionRule(loc, switchLabel, getStatementsParser().parseBlock());
            }
            case THROW -> {
                return new ASTSwitchExpressionRule(loc, switchLabel, getStatementsParser().parseThrowStatement());
            }
            default -> {
                ASTExpression expr = parseExpression();
                if (accept(SEMICOLON) == null) {
                    error(curr().getLocation(), "Expected semicolon.");
                }
                return new ASTSwitchExpressionRule(loc, switchLabel, expr);
            }
        }
    }

    /**
     * Parses a <code>SwitchLabel</code>.
     * <em>
     * SwitchLabel:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;case CaseConstants<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;default
     * &nbsp;&nbsp;&nbsp;&nbsp;Pattern [Guard]
     * </em>
     * @return An <code>ASTSwitchLabel</code>.
     */
    public ASTSwitchLabel parseSwitchLabel() {
        Location loc = curr().getLocation();
        return switch (curr().getType()) {
            case CASE -> {
                ASTKeywordNode caseKeyword = parseModifier(
                        Arrays.asList(CASE),
                        "'case'"
                );
                yield new ASTSwitchLabel(loc, caseKeyword, parseCaseConstants());
            }
            case DEFAULT -> {
                ASTKeywordNode defaultKeyword = parseModifier(
                        Arrays.asList(DEFAULT),
                        "'default'"
                );
                yield new ASTSwitchLabel(loc, defaultKeyword);
            }
            default -> {
                ASTPattern pattern = parsePattern();
                if (isCurr(WHEN)) {
                    yield new ASTSwitchLabel(loc, pattern, parseGuard());
                }
                yield new ASTSwitchLabel(loc, pattern);
            }
        };
    }

    /**
     * Parses a <code>CaseConstants</code>.
     * <em>
     * CaseConstants:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression {, ValueExpression}
     * </em>
     * @return An <code>ASTCaseConstants</code>.
     */
    public ASTCaseConstants parseCaseConstants() {
        return parseList(
                ExpressionsParser::isValueExpression,
                "Expected value expression.",
                COMMA,
                this::parseValueExpression,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE, OPEN_BRACE, SEMICOLON, ARROW,
                        THROW, EOF),
                ASTCaseConstants::new
        );
    }

    /**
     * Parses a <code>Pattern</code>.
     * <em>
     * Pattern:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypePattern<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RecordPattern
     * </em>
     * @return An <code>ASTPattern</code> which could be an <code>ASTTypePattern</code>
     *     or an <code>ASTRecordPattern</code>.
     */
    public ASTPattern parsePattern() {
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            return parseTypePattern();
        }
        ASTDataType dataType = getTypesParser().parseDataType();
        if (isCurr(OPEN_PARENTHESIS)) {
            return parseRecordPattern(dataType);
        }
        else {
            return parseTypePattern(dataType);
        }
    }

    /**
     * Parses a <code>Guard</code>.
     * <em>
     * Guard:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;when ValueExpression<br>
     * </em>
     * @return An <code>ASTGuard</code>.
     */
    public ASTGuard parseGuard() {
        Location loc = curr().getLocation();
        if (accept(WHEN) == null) {
            throw internalError(WHEN);
        }
        return new ASTGuard(loc, parseValueExpression());
    }

    /**
     * Parses an <code>ASTListNode</code>, <code>ASTPattern</code>s,
     * separated by a comma, that are either <code>ASTTypePattern</code>s or
     * <code>ASTRecordPattern</code>s.
     * <em>
     * PatternList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Pattern {, Pattern}
     * </em>
     * @return An <code>ASTPatternList</code>.
     */
    public ASTPatternList parsePatternList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected type pattern or record pattern",
                COMMA,
                this::parsePattern,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE, SEMICOLON, ARROW,
                        WHEN, THROW, EOF),
                ASTPatternList::new,
                false
        );
    }

    /**
     * Parses a <code>RecordPattern</code> with the already parsed
     * <code>DataType</code>.
     * <em>
     * RecordPattern:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType ( [PatternList] )<br>
     * </em>
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTRecordPattern</code>.
     */
    public ASTRecordPattern parseRecordPattern(ASTDataType dataType) {
        Location loc = curr().getLocation();
        if (accept(OPEN_PARENTHESIS) == null) {
            throw internalError(OPEN_PARENTHESIS);
        }
        ASTPatternList patternList = parsePatternList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTRecordPattern(loc, dataType, patternList);
    }

    /**
     * Parses a <code>TypePattern</code>.
     * <em>
     * TypePattern:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
     * </em>
     * @return An <code>ASTTypePattern</code>.
     */
    public ASTTypePattern parseTypePattern() {
        Location loc = curr().getLocation();
        ASTVariableModifierList varModList = null;
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            varModList = getStatementsParser().parseVariableModifierList();
        }
        ASTDataType dataType = getTypesParser().parseDataType();
        ASTIdentifier identifier = getNamesParser().parseIdentifier();
        if (varModList != null) {
            return new ASTTypePattern(loc, varModList, dataType, identifier);
        }
        return new ASTTypePattern(loc, dataType, identifier);
    }

    /**
     * Parses a <code>TypePattern</code> with the already parsed
     * <code>ASTDataType</code>.
     * <em>
     * TypePattern:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
     * </em>
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTTypePattern</code>.
     */
    public ASTTypePattern parseTypePattern(ASTDataType dataType) {
        Location loc = curr().getLocation();
        ASTIdentifier identifier = getNamesParser().parseIdentifier();
        return new ASTTypePattern(loc, dataType, identifier);
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
        return new ASTPrimary(loc, badChild, BAD);
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br> // Array, List, Map access with [i]
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ArrayCreationExpression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ClassInstanceCreationExpression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodReference</strong>
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
                if (isCurr(DOUBLE_COLON)) {
                    // MethodReference
                    return new ASTPrimary(loc, parseMethodReference(dataType), METHOD_REFERENCE);
                }
                else if (isCurr(DOT) && isNext(CLASS)) {
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
            primary = new ASTPrimary(loc, keywordSelf, ASTPrimary.Type.SELF);
        }
        else if (isCurr(SUPER)) {
            ASTKeywordNode sooper = parseSuper();
            if (isCurr(DOUBLE_COLON)) {
                // Method references don't chain.
                // super ::
                // MethodReference
                return new ASTPrimary(loc, parseMethodReferenceSuper(sooper), METHOD_REFERENCE);
            }
            else {
                if (accept(DOT) == null) {
                    error(curr().getLocation(), "Expected '.'.");
                }
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    // MethodInvocation
                    primary = new ASTPrimary(loc, parseMethodInvocationSuper(sooper), METHOD_INVOCATION);
                }
                else {
                    // FieldAccess
                    primary = new ASTPrimary(loc, parseFieldAccessSuper(sooper), FIELD_ACCESS);
                }
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
            if (isNext(LESS_THAN)) {
                // ClassInstanceCreationExpression
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression();
                primary = new ASTPrimary(loc, cice, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR);
            }
            else {
                accept(NEW);
                // Assume an identifier is next.
                ASTTypeToInstantiate tti = parseTypeToInstantiate();
                if (isCurr(OPEN_BRACKET) || isCurr(OPEN_CLOSE_BRACKET)) {
                    // ArrayCreationExpression
                    primary = new ASTPrimary(loc, parseArrayCreationExpression(tti), ARRAY_CREATION_EXPR);
                }
                else {
                    // Assume '(' is next.
                    // ClassInstanceCreationExpression
                    primary = new ASTPrimary(loc, parseClassInstanceCreationExpression(tti),
                            ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR);
                }
            }
        }
        else {
            throw internalError("Primary");
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br> // Array, List, Map access with [i]
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodReference</strong>
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
        else if (isCurr(DOT) && isNext(LESS_THAN)) {
            // ExprName.<TypeArgs>methodName(args)
            // NOT ExprName.<TypeArgs>super(args) -- Constructor invocation.
            // TODO: Determine a way to parse Type Arguments, yet keep them in case
            // "super" is encountered, which terminates the Primary at the given
            // ExpressionName.  Consider storing unused type args in the Primary.
            // MethodInvocation
            primary = new ASTPrimary(loc, parseMethodInvocationExprName(exprName), METHOD_INVOCATION);
        }
        else if (isCurr(DOT) && isNext(SUPER) && !isPeek(OPEN_PARENTHESIS)) {
            // TypeName.super.methodInvocation()
            // TypeName.super.fieldAccess
            // NOT Expression.super() -- Constructor invocation.
            accept(DOT);
            ASTKeywordNode sooper = parseSuper();
            if (isCurr(DOUBLE_COLON)) {
                // MethodReference
                // TypeName.super::[TypeArguments]Identifier
                accept(DOUBLE_COLON);
                ASTTypeName typeName = exprName.convertToTypeName();
                ASTTypeArgumentList typeArgs = null;
                if (isCurr(LESS_THAN)) {
                    typeArgs = getTypesParser().parseTypeArguments();
                }
                ASTIdentifier identifier = getNamesParser().parseIdentifier();
                ASTMethodReference methodReference = new ASTMethodReference.Builder()
                        .setLocation(loc)
                        .setTypeName(typeName)
                        .setSuper(sooper)
                        .setTypeArguments(typeArgs)
                        .setIdentifier(identifier)
                        .build();
                return new ASTPrimary(loc, methodReference, METHOD_REFERENCE);
            }
            else if (isCurr(DOT)) {
                accept(DOT);
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
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
                error(loc, "Expected method reference (::), method invocation, or field access (.).");
                primary = parseBadPrimary(PRIMARY_STOPPERS);
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // MethodInvocation
            // ExprNameExceptForMethodName.methodName(args)
            children = exprName.getTypedChildren();
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
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ElementAccess</strong><br> // Array, List, Map access with [i]
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodInvocation</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayCreationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ClassInstanceCreationExpression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>FieldAccess</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>MethodReference</strong>
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTPrimary primary) {
        Location loc = primary.getLocation();

        // Non-recursive method reference.
        // Primary :: [TypeArguments] identifier
        if (isCurr(DOUBLE_COLON)) {
            // MethodReference
            accept(DOUBLE_COLON);
            ASTTypeArgumentList typeArgs = null;
            if (isCurr(LESS_THAN)) {
                typeArgs = getTypesParser().parseTypeArguments();
            }
            ASTIdentifier identifier = getNamesParser().parseIdentifier();
            ASTMethodReference methodReference = new ASTMethodReference.Builder()
                    .setLocation(loc)
                    .setTypeArguments(typeArgs)
                    .setPrimary(primary)
                    .setIdentifier(identifier)
                    .build();
            return new ASTPrimary(loc, methodReference, METHOD_REFERENCE);
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
                // ClassInstanceCreationExpression
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, cice, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR);
            }
            if (isCurr(DOT) && (isNext(LESS_THAN) || isNext(IDENTIFIER))) {
                accept(DOT);
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
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
            if (isCurr(OPEN_BRACKET)) {
                // ElementAccess
                ASTElementAccess ea = parseElementAccess(primary);
                primary = new ASTPrimary(loc, ea, ASTPrimary.Type.ELEMENT_ACCESS);
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . [TypeArguments] Identifier ( ArgumentList )
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
     * <p>Parses a <code>MethodInvocation</code>, given an already parsed
     * <code>super</code>.  The DOT following "super" has been parsed also.</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super . [TypeArguments] Identifier ( ArgumentList )</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . [TypeArguments] Identifier ( ArgumentList )
     * </em>
     * @param sooper An already parsed <code>super</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTKeywordNode sooper) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(sooper.getLocation())
                .setSooper(sooper);
        if (isCurr(LESS_THAN)) {
            builder.setTypeArgs(getTypesParser().parseTypeArguments());
        }
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . super . [TypeArguments] Identifier ( ArgumentList )</strong>
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
        if (isCurr(LESS_THAN)) {
            builder.setTypeArgs(getTypesParser().parseTypeArguments());
        }
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
        if (isCurr(LESS_THAN)) {
            ASTTypeArgumentList typeArgs = getTypesParser().parseTypeArguments();
            // Keep the type arguments in case of:
            // Primary . TypeArguments super (), a constructor invocation.

            // We can get here from the parseConstructorInvocation method in
            // the case of Primary . <    -- This could produce:
            // MethodInvocation -> Primary . TypeArguments Identifier ( [ArgumentList] )
            // OR
            // ConstructorInvocation -> Primary . TypeArguments super ( [ArgumentList] )

            // Currently there is no clean way of pushing the type arguments
            // back on to the parser when we get a super.  If the decision is
            // made to allow constructor invocations in a constructor body,
            // perhaps decide to create an ASTInvocation that could be a
            // ConstructorInvocation or a MethodInvocation.

//            if (isCurr(SUPER)) {
//                // Primary . TypeArguments super ( [ArgumentList] )
//            }
            builder.setTypeArgs(typeArgs);
        }
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
     * that has already been parsed.</p>
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
    public ASTMethodInvocation parseMethodInvocationExprName(ASTExpressionName exprName) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setExprName(exprName);
        if (accept(DOT) == null) {
            error(curr().getLocation(), "Expected '.'.");
        }
        if (isCurr(LESS_THAN)) {
            ASTTypeArgumentList typeArgs = getTypesParser().parseTypeArguments();
            // Keep the type arguments in case of:
            // ExpressionName . TypeArguments super (), a constructor invocation.

            // We can get here from the parseConstructorInvocation method in
            // the case of ExpressionName . <    -- This could produce:
            // MethodInvocation -> ExpressionName . TypeArguments Identifier ( [ArgumentList] )
            // OR
            // ConstructorInvocation -> ExpressionName . TypeArguments super ( [ArgumentList] )

            // Currently there is no clean way of pushing the type arguments
            // back on to the parser when we get a super.  If the decision is
            // made to allow constructor invocations in a constructor body,
            // perhaps decide to create an ASTInvocation that could be a
            // ConstructorInvocation or a MethodInvocation.

//            if (isCurr(SUPER)) {
//                // ExpressionName . TypeArguments super ( [ArgumentList] )
//            }
            builder.setTypeArgs(typeArgs);
        }
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
     * Parses a <code>MethodReference</code>, given an already parsed
     * <code>ASTDataType</code>.
     * <em>
     * MethodReference:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ExpressionName :: [TypeArguments] Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>DataType :: [TypeArguments] Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>DataType :: [TypeArguments] new</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super :: [TypeArguments] Identifier
     * </em>
     * @param dataType An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTMethodReference</code>.
     */
    public ASTMethodReference parseMethodReference(ASTDataType dataType) {
        Location loc = dataType.getLocation();
        ASTMethodReference.Builder builder = new ASTMethodReference.Builder()
                .setLocation(loc);
        if (accept(DOUBLE_COLON) == null) {
            throw internalError(DOUBLE_COLON);
        }
        ASTTypeArgumentList typeArgs = null;
        if (isCurr(LESS_THAN)) {
            // [TypeArguments]
            typeArgs = getTypesParser().parseTypeArguments();
        }
        if (isCurr(NEW)) {
            // DataType :: [TypeArguments] new
            accept(NEW);
            return builder
                    .setDataType(dataType)
                    .setTypeArguments(typeArgs)
                    .build();
        }
        else if (isCurr(IDENTIFIER)) {
            ASTIdentifier identifier = getNamesParser().parseIdentifier();
            // ExpressionName :: [TypeArguments] Identifier
            if (dataType.canConvertToExpressionName()) {
                ASTExpressionName exprName = dataType.convertToExpressionName();
                builder.setExprName(exprName);
            }
            else {
                // DataType :: [TypeArguments] Identifier
                builder.setDataType(dataType);
            }
            return builder.setTypeArguments(typeArgs)
                    .setIdentifier(identifier)
                    .build();
        }
        else {
            error(curr().getLocation(), "Expected identifier or new.");
            return builder
                    .setDataType(dataType)
                    .setIdentifier(new ASTIdentifier(curr().getLocation(), "bad identifier"))
                    .build();
        }
    }

    /**
     * Parses a <code>MethodReference</code>, given an already parsed
     * <code>ASTKeywordNode</code> of type <code>super</code>.
     * <em>
     * MethodReference:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super :: [TypeArguments] Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] new<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super :: [TypeArguments] Identifier
     * </em>
     * @param sooper An already parsed <code>ASTKeywordNode</code> of keyword <code>super</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTMethodReference parseMethodReferenceSuper(ASTKeywordNode sooper) {
        Location loc = sooper.getLocation();
        if (accept(DOUBLE_COLON) == null) {
            throw internalError(DOUBLE_COLON);
        }
        ASTTypeArgumentList typeArgs = null;
        if (isCurr(LESS_THAN)) {
            typeArgs = getTypesParser().parseTypeArguments();
        }
        ASTIdentifier identifier = getNamesParser().parseIdentifier();
        return new ASTMethodReference.Builder()
                .setLocation(loc)
                .setSuper(sooper)
                .setTypeArguments(typeArgs)
                .setIdentifier(identifier)
                .build();
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
     * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] ) ClassBody
     * </em>
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression() {
        Location loc = curr().getLocation();
        if (accept(NEW) == null) {
            throw internalError(NEW);
        }
        ASTTypeArgumentList typeArgs = null;
        if (isCurr(LESS_THAN)) {
            typeArgs = getTypesParser().parseTypeArguments();
        }
        ASTTypeToInstantiate tti = parseTypeToInstantiate();
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTArgumentList argumentList = parseArgumentList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        if (typeArgs == null) {
            return new ASTUnqualifiedClassInstanceCreationExpression(loc, tti, argumentList);
        }
        return new ASTUnqualifiedClassInstanceCreationExpression(loc, typeArgs, tti, argumentList);
    }

    /**
     * Parses an <code>UnqualifiedClassInstanceCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "(" in
     * the Scanner.
     * <em>
     * UnqualifiedClassInstanceCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] ) ClassBody
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
                this::parseGiveExpression,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE, SEMICOLON, ARROW, EOF),
                ASTArgumentList::new,
                false);
    }

    /**
     * Parses a <code>GiveExpression</code>.
     * <em>
     * GiveExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;give Expression<br>
     * </em>
     * @return An <code>ASTGiveExpression</code>.
     */
    public ASTGiveExpression parseGiveExpression() {
        Location loc = curr().getLocation();
        if (isCurr(GIVE)) {
            ASTKeywordNode giveKeyword = parseModifier(
                    Arrays.asList(GIVE),
                    "'give'"
            );
            return new ASTGiveExpression(loc, giveKeyword, parseExpression());
        }
        return new ASTGiveExpression(loc, parseExpression());
    }

    /**
     * Parses an <code>ArrayCreationExpression</code>.
     * <em>
     * ArrayCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs Dims<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims ArrayInitializer
     * </em>
     * @return An <code>ASTArrayCreationExpression</code>.
     */
    public ASTArrayCreationExpression parseArrayCreationExpression() {
        if (accept(NEW) == null) {
            error(curr().getLocation(), "Expected new.");
        }
        ASTTypeToInstantiate tti = parseTypeToInstantiate();
        return parseArrayCreationExpression(tti);
    }

    /**
     * Parses an <code>ASTArrayCreationExpression</code>, using an
     * already parsed <code>ASTTypeToInstantiate</code>.  It is expected that
     * the parser has already parsed "new TypeToInstantiate" and is at "[" or
     * "[[" in the Scanner.
     * <em>
     * ArrayCreationExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs Dims<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims ArrayInitializer
     * </em>
     * @param alreadyParsed An already parsed <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTArrayCreationExpression</code>.
     */
    public ASTArrayCreationExpression parseArrayCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        Location loc = curr().getLocation();
        boolean dimExprsPresent = false;
        ASTDimExprs dimExprs = null;
        ASTDims dims = null;
        if (isCurr(OPEN_BRACKET)) {
            dimExprs = parseDimExprs();
            dimExprsPresent = true;
        }
        if (isCurr(OPEN_CLOSE_BRACKET)) {
            dims = getTypesParser().parseDims();
        }
        if (isCurr(OPEN_BRACE)) {
            if (dimExprsPresent) {
                error(curr().getLocation(), "Array initializer not expected with dimension expressions.");
            }
            ASTArrayInitializer arrayInitializer = parseArrayInitializer();
            return new ASTArrayCreationExpression(loc, alreadyParsed, dims, arrayInitializer);
        }
        if (dims == null) {
            return new ASTArrayCreationExpression(loc, alreadyParsed, dimExprs);
        }
        return new ASTArrayCreationExpression(loc, alreadyParsed, dimExprs, dims);
    }

    /**
     * Parses a <code>DimExprs</code>.
     * <em>
     * DimExprs:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DimExpr {DimExpr}<br>
     * </em>
     * @return An <code>ASTDimExprs</code>.
     */
    public ASTDimExprs parseDimExprs() {
        return parseMultiple(
                t -> test(t, OPEN_BRACKET) && !isNext(CLOSE_BRACKET),
                this::parseDimExpr,
                ASTDimExprs::new
        );
    }

    /**
     * Parses a <code>DimExpr</code>.
     * <em>
     * DimExpr:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[ ValueExpression ]
     * </em>
     * @return An <code>ASTDimExpr</code>.
     */
    public ASTDimExpr parseDimExpr() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACKET) == null) {
            error(curr().getLocation(), "Expected '['.");
        }
        ASTValueExpression expr = parseValueExpression();
        if (accept(CLOSE_BRACKET) == null) {
            error(curr().getLocation(), "Expected ']'.");
        }
        return new ASTDimExpr(loc, expr);
    }

    /**
     * Parses an <code>ArrayInitializer</code>.
     * <em>
     * ArrayInitializer:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ VariableInitializerList }<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ }
     * </em>
     * @return An <code>ASTArrayInitializer</code>.
     */
    public ASTArrayInitializer parseArrayInitializer() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTVariableInitializerList varInitList = parseVariableInitializerList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return new ASTArrayInitializer(loc, varInitList);
    }

    /**
     * Parses a <code>VariableInitializerList</code>.
     * <em>
     * VariableInitializerList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializer {, VariableInitializer}
     * </em>
     * @return An <code>ASTVariableInitializerList</code>.
     */
    public ASTVariableInitializerList parseVariableInitializerList() {
        return parseList(
                t -> isExpression(t) || test(t, OPEN_BRACE),
                "Expected an expression or an array initializer.",
                COMMA,
                this::parseVariableInitializer,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACKET, CLOSE_BRACE, SEMICOLON, ARROW, EOF),
                ASTVariableInitializerList::new,
                false
        );
    }

    /**
     * Parses a <code>VariableInitializer</code>.
     * <em>
     * VariableInitializer:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayInitializer
     * </em>
     * @return An <code>ASTVariableInitializer</code> which could be an
     *     <code>ASTExpression</code> or <code>ASTArrayInitializer</code>.
     */
    public ASTVariableInitializer parseVariableInitializer() {
        if (isCurr(OPEN_BRACE)) {
            return parseArrayInitializer();
        }
        return parseExpression();
    }

    /**
     * Parses a <code>TypeToInstantiate</code>.
     * <em>
     * TypeToInstantiate:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName TypeArgumentsOrDiamond
     * </em>
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate parseTypeToInstantiate() {
        Location loc = curr().getLocation();
        ASTTypeName typeName = getNamesParser().parseTypeName();
        if (isCurr(LESS_THAN)) {
            return new ASTTypeToInstantiate(loc, typeName, getTypesParser().parseTypeArgumentsOrDiamond());
        }
        return new ASTTypeToInstantiate(loc, typeName);
    }

    /**
     * Parses an <code>ElementAccess</code>, given an <code>ASTPrimary</code>
     * that has already been parsed and its <code>Location</code>.  Element Access
     * expressions are left-associative.
     * <em>
     * ElementAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary [ ValueExpression ]<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess [ ValueExpression ]<br>
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTElementAccess</code>.
     */
    public ASTElementAccess parseElementAccess(ASTPrimary primary) {
        Location loc = primary.getLocation();
        if (accept(OPEN_BRACKET) == null) {
            throw internalError(OPEN_BRACKET);
        }
        ASTElementAccess result = new ASTElementAccess(loc, primary, parseValueExpression());
        if (accept(CLOSE_BRACKET) == null) {
            error(curr().getLocation(), "Expected ']'.");
        }

        while (isCurr(OPEN_BRACKET)) {
            accept(OPEN_BRACKET);
            result = new ASTElementAccess(loc, result, parseValueExpression());
            if (accept(CLOSE_BRACKET) == null) {
                error(curr().getLocation(), "Expected ']'.");
            }
        }
        return result;
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
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTPrimary</code>.  DOT has also already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>Primary . Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
     * </em>
     * @param primary An already parsed <code>ASTExpressionName</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccess(ASTPrimary primary) {
        Location loc = primary.getLocation();
        return new ASTFieldAccess(loc, primary, getNamesParser().parseIdentifier());
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
