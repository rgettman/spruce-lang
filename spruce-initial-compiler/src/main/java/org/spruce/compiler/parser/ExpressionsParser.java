package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTBinaryNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTDims;
import org.spruce.compiler.ast.types.ASTTypeArguments;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>ExpressionsParser</code> is a <code>BasicParser</code> that parses
 * expressions.
 */
public class ExpressionsParser extends BasicParser {
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LambdaExpression<br>
     * </em>
     * @return An <code>ASTNode</code> representing an expression.
     */
    public ASTNode parseExpression() {
        if (test(curr(), IDENTIFIER) && test(next(), ARROW)) {
            return parseLambdaExpression();
        }
        if (isPrimary(curr())) {
            return parseConditionalExpression();
        }
        else if (isCurr(PIPE) || isCurr(DOUBLE_PIPE)) {
            return parseLambdaExpression();
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected primary or lambda expression.  Got \"" + curr() + "\".");
        }
    }

    /**
     * Parses a <code>LambdaExpression</code>.
     * <em>
     * LambdaExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LambdaParameters -> LambdaBody<br>
     * </em>
     * @return An <code>ASTBinaryNode</code> representing the lambda expression.
     */
    public ASTBinaryNode parseLambdaExpression() {
        Location loc = curr().getLocation();
        ASTLambdaParameters lambdaParams = parseLambdaParameters();
        if (accept(ARROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"->\".");
        }
        return new ASTBinaryNode(loc, ARROW, lambdaParams, parseLambdaBody());
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
                throw new CompileException(curr().getLocation(), "Expected \"|\".");
            }
            yield result;
        }
        case IDENTIFIER -> new ASTLambdaParameters(loc, getNamesParser().parseIdentifier());
        default -> throw new CompileException(curr().getLocation(), "Expected lambda parameters.");
        };
    }

    /**
     * Parses a <code>LambdaParameterList</code>.
     * <em>
     * LambdaParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InferredParameterList<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameterList<br>
     * </em>
     * @return An <code>ASTListNode</code> representing either an inferred
     *     parameter list or a formal parameter list.
     */
    public ASTListNode parseLambdaParameterList() {
        return switch(curr().getType()) {
            case VAR, MUT, TAKE -> getClassesParser().parseFormalParameterList();
            case IDENTIFIER ->
                switch(next().getType()) {
                    case COMMA, PIPE -> parseInferredParameterList();
                    default -> getClassesParser().parseFormalParameterList();
                };
            case PIPE -> parseInferredParameterList();
            default -> throw new CompileException(curr().getLocation(), "Expected lambda parameter(s), got \"" + curr() + "\".");
        };
    }

    /**
     * Parses an <code>InferredParameterList</code>.
     * <em>
     * InferredParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}<br>
     * </em>
     * @return An <code>ASTListNode</code> with type <code>INFERRED_PARAMETERS</code>.
     */
    public ASTListNode parseInferredParameterList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                COMMA,
                getNamesParser()::parseIdentifier,
                INFERRED_PARAMETERS,
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
     * @return An <code>ASTNode</code> representing either an Expression or a Block.
     */
    public ASTNode parseLambdaBody() {
        if (test(curr(), OPEN_BRACE)) {
            return getStatementsParser().parseBlock();
        }
        else {
            return parseExpression();
        }
    }

    /**
     * Parses a <code>ConditionalExpression</code>; they are right-associative
     * with each other.
     * <em>
     * ConditionalExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression ? Expression : Expression<br>
     * </em>
     * @return An <code>ASTConditionalExpression</code>, or a lower production
     *     such as <code>ASTBinaryNode</code> or <code>ASTNode</code>.
     */
    public ASTNode parseConditionalExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            ASTNode logicalOrExpr = parseLogicalOrExpression();
            if (isCurr(QUESTION_MARK)) {
                accept(QUESTION_MARK);
                ASTNode exprIfTrue = parseExpression();
                if (isCurr(COLON)) {
                    accept(COLON);
                    ASTNode exprIfFalse = parseExpression();
                    return new ASTConditionalExpression(loc, logicalOrExpr, exprIfTrue, exprIfFalse);
                }
                else {
                    throw new CompileException(curr().getLocation(), "Expected ':'.");
                }
            }
            return logicalOrExpr;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseLogicalOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseLogicalXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseLogicalAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseRelationalExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            ASTNode result = parseCompareExpression();
            TokenType curr;
            while ( (curr = isAcceptedOperator(Arrays.asList(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, DOUBLE_EQUAL, NOT_EQUAL, ISA, IS, ISNT)) ) != null) {
                accept(curr);
                if (curr == ISA) {
                    result = new ASTBinaryNode(loc, curr, result, getTypesParser().parseDataType());
                }
                else {
                    result = new ASTBinaryNode(loc, curr, result, parseCompareExpression());
                }
            }
            return result;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Parses a <code>CompareExpression</code>; they are NOT associative
     * with each other.
     * <em>
     * CompareExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression &lt;=&gt; BitwiseOrExpression<br>
     * </em>
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseCompareExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            ASTNode result = parseBitwiseOrExpression();
            if (isCurr(COMPARISON)) {
                accept(COMPARISON);
                return new ASTBinaryNode(loc, COMPARISON, result, parseBitwiseOrExpression());
            }
            return result;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseBitwiseOrExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseBitwiseXorExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseBitwiseAndExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseShiftExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseAdditiveExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTNode</code> or an <code>ASTBinaryNode</code>.
     */
    public ASTNode parseMultiplicativeExpression() {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
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
     * @return An <code>ASTParentNode</code>.
     */
    public ASTParentNode parseCastExpression() {
        if (isPrimary(curr())) {
            Location loc = curr().getLocation();
            ASTParentNode result = parseUnaryExpression();
            while (isCurr(AS)) {
                accept(AS);
                result = new ASTBinaryNode(loc, AS, result, getTypesParser().parseIntersectionType());
            }
            return result;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
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
     * @return An <code>ASTUnaryNode</code>.
     */
    public ASTUnaryNode parseUnaryExpression() {
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
            return new ASTUnaryNode(loc, SWITCH, parseSwitchExpression());
        }
        else {
            return parsePrimary();
        }
    }

    /**
     * Parses a <code>SwitchExpression</code>.
     * <em>
     * SwitchStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;switch Expression SwitchExpressionBlock
     * </em>
     * @return An <code>ASTBinaryNode</code>.
     */
    public ASTBinaryNode parseSwitchExpression() {
        Location loc = curr().getLocation();
        if (accept(SWITCH) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"switch\".");
        }
        return new ASTBinaryNode(loc, SWITCH, parseConditionalExpression(), parseSwitchExpressionBlock());
    }

    /**
     * Parses a <code>SwitchExpressionBlock</code>.
     * <em>
     * SwitchExpressionBlock:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchExpressionRules }
     * </em>
     * @return An <code>ASTUnaryNode</code>.
     */
    public ASTUnaryNode parseSwitchExpressionBlock() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"{\".");
        }
        ASTListNode rules = parseSwitchExpressionRules();
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"}\".");
        }
        return new ASTUnaryNode(loc, rules);
    }

    /**
     * Parses a <code>SwitchExpressionRules</code>.
     * <em>
     * SwitchExpressionRules:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpressionRule {SwitchExpressionRule}<br>
     * </em>
     * @return An <code>ASTListNode</code>.
     */
    public ASTListNode parseSwitchExpressionRules() {
        return parseMultiple(
                t -> test(t, CASE, DEFAULT, MUT, VAR, IDENTIFIER) || isPrimary(curr()),
                "Expected a switch expression rule.",
                this::parseSwitchExpressionRule,
                SWITCH_EXPR_RULES
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
     * @return An <code>ASTBinaryNode</code>.
     */
    public ASTBinaryNode parseSwitchExpressionRule() {
        Location loc = curr().getLocation();
        ASTSwitchLabel switchLabel = parseSwitchLabel();
        if (accept(ARROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected arrow (->).");
        }
        ASTNode result = switch(curr().getType()) {
            case OPEN_BRACE -> getStatementsParser().parseBlock();
            case THROW -> getStatementsParser().parseThrowStatement();
            default -> {
                ASTNode expr = parseExpression();
                if (accept(SEMICOLON) == null) {
                    throw new CompileException(curr().getLocation(), "Expected semicolon.");
                }
                yield expr;
            }
        };
        return new ASTBinaryNode(loc, ARROW, switchLabel, result);
    }

    /**
     * Parses a <code>SwitchLabel</code>.
     * <em>
     * SwitchLabel:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;case SwitchConstants<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;default
     * &nbsp;&nbsp;&nbsp;&nbsp;Pattern [Guard]
     * </em>
     * @return An <code>ASTSwitchLabel</code>.
     */
    public ASTSwitchLabel parseSwitchLabel() {
        Location loc = curr().getLocation();
        return switch (curr().getType()) {
            case CASE -> {
                accept(CASE);
                yield new ASTSwitchLabel(loc, parseCaseConstants());
            }
            case DEFAULT -> {
                accept(DEFAULT);
                yield new ASTSwitchLabel(loc, DEFAULT);
            }
            default -> {
                ASTNode pattern = parsePattern();
                if (test(curr(), WHEN)) {
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
     * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression {, ConditionalExpression}
     * </em>
     * @return An <code>ASTListNode</code> with type <code>CASE_CONSTANTS</code>.
     */
    public ASTListNode parseCaseConstants() {
        return parseList(
                ExpressionsParser::isPrimary,
                "Expected conditional expression.",
                COMMA,
                this::parseConditionalExpression,
                CASE_CONSTANTS
        );
    }

    /**
     * Parses a <code>Pattern</code>.
     * <em>
     * Pattern:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypePattern<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RecordPattern
     * </em>
     * @return An <code>ASTPattern</code>.
     */
    public ASTNode parsePattern() {
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            return parseTypePattern();
        }
        else if (isCurr(IDENTIFIER)) {
            ASTDataType dataType = getTypesParser().parseDataType();
            if (isCurr(OPEN_PARENTHESIS)) {
                return parseRecordPattern(dataType);
            }
            else {
                return parseTypePattern(dataType);
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a record pattern or a type pattern.");
        }
    }

    /**
     * Parses a <code>Guard</code>.
     * <em>
     * Guard:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;when ConditionalExpression<br>
     * </em>
     * @return An <code>ASTUnaryNode</code>.
     */
    public ASTUnaryNode parseGuard() {
        Location loc = curr().getLocation();
        if (accept(WHEN) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"when\".");
        }
        accept(WHEN);
        return new ASTUnaryNode(loc, WHEN, parseConditionalExpression());
    }

    /**
     * Parses an <code>ASTPatternList</code>, <code>ASTPattern</code>s separated by a comma.
     * <em>
     * PatternList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Pattern {, Pattern}
     * </em>
     * @return An <code>ASTPatternList</code> with type <code>PATTERNS</code>.
     */
    public ASTListNode parsePatternList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected type pattern or record pattern",
                COMMA,
                this::parsePattern,
                PATTERNS
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
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            ASTListNode patternList = parsePatternList();
            if (accept(CLOSE_PARENTHESIS) == null) {
                throw new CompileException(curr().getLocation(), "Expected \")\".");
            }
            return new ASTRecordPattern(loc, dataType, patternList);
        }
        else {
            return new ASTRecordPattern(loc, dataType);
        }
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
        ASTListNode varModList = null;
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
        if (isLiteral(curr())) {
            // Literal
            ASTLiteral literal = getLiteralsParser().parseLiteral();
            primary = new ASTPrimary(loc, literal);
        }
        else if (isCurr(IDENTIFIER)) {
            if (isNext(OPEN_PARENTHESIS)) {
                // id(args)
                // MethodInvocation
                ASTIdentifier methodName = getNamesParser().parseIdentifier();
                primary = new ASTPrimary(loc, parseMethodInvocation(methodName));
            }
            else {
                ASTDataType dataType = getTypesParser().parseDataType();
                if (isCurr(DOUBLE_COLON)) {
                    // MethodReference
                    return new ASTPrimary(loc, parseMethodReference(dataType));
                }
                else if (isCurr(DOT) && isNext(CLASS)) {
                    // ClassLiteral
                    return new ASTPrimary(loc, parseClassLiteral(dataType));
                }
                ASTListNode expressionName = getTypesParser().convertToExpressionName(dataType);
                primary = parsePrimary(expressionName);
            }
        }
        else if (isCurr(SELF)) {
            // self
            ASTSelf keywordSelf = parseSelf();
            primary = new ASTPrimary(loc, keywordSelf);
        }
        else if (isCurr(SUPER)) {
            ASTSuper sooper = parseSuper();
            if (isCurr(DOUBLE_COLON)) {
                // Method references don't chain.
                // super ::
                // MethodReference
                return new ASTPrimary(loc, parseMethodReferenceSuper(sooper));
            }
            else {
                if (accept(DOT) == null) {
                    throw new CompileException(curr().getLocation(), "Expected '.'.");
                }
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    // MethodInvocation
                    primary = new ASTPrimary(loc, parseMethodInvocationSuper(sooper));
                }
                else {
                    // FieldAccess
                    primary = new ASTPrimary(loc, parseFieldAccessSuper(sooper));
                }
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // Expression
            accept(OPEN_PARENTHESIS);
            ASTNode expression = parseExpression();
            Token closeParen = accept(CLOSE_PARENTHESIS);
            if (closeParen == null) {
                throw new CompileException(curr().getLocation(), "Expected close parenthesis \")\".");
            }
            primary = new ASTPrimary(loc, expression, OPEN_PARENTHESIS);
        }
        else if (isCurr(NEW)) {
            if (isNext(LESS_THAN)) {
                // ClassInstanceCreationExpression
                ASTParentNode cice = parseClassInstanceCreationExpression();
                primary = new ASTPrimary(loc, cice);
            }
            else if (isNext(IDENTIFIER)) {
                accept(NEW);
                ASTTypeToInstantiate tti = parseTypeToInstantiate();
                if (isCurr(OPEN_BRACKET) || isCurr(OPEN_CLOSE_BRACKET)) {
                    // ArrayCreationExpression
                    primary = new ASTPrimary(loc, parseArrayCreationExpression(tti));
                }
                else if (isCurr(OPEN_PARENTHESIS)) {
                    // ClassInstanceCreationExpression
                    primary = new ASTPrimary(loc, parseClassInstanceCreationExpression(tti));
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
     * @param exprName An already parsed Expression Name as an <code>ASTListNode</code>
     *                 of type <code>EXPR_NAME_IDS</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary(ASTListNode exprName) {
        ASTPrimary primary;
        Location loc = exprName.getLocation();

        // If exprName is a simple identifier, then it is the method name.
        List<ASTNode> children = exprName.getChildren();
        if (children.size() == 1 && isCurr(OPEN_PARENTHESIS)) {
            // id( -> method invocation
            // MethodInvocation
            ASTIdentifier methodName = (ASTIdentifier) children.get(0);
            primary = new ASTPrimary(loc, parseMethodInvocation(methodName));
            return parsePrimary(primary);
        }

        if (isCurr(DOT) && isNext(SELF)) {
            // TypeName.self
            ASTListNode tn = getNamesParser().convertToTypeName(exprName);
            accept(DOT);
            ASTBinaryNode typeNameSelf = new ASTBinaryNode(tn.getLocation(), DOT, tn, parseSelf());
            primary = new ASTPrimary(loc, typeNameSelf);
        }
        else if (isCurr(DOT) && isNext(LESS_THAN)) {
            // ExprName.<TypeArgs>methodName(args)
            // NOT ExprName.<TypeArgs>super(args) -- Constructor invocation.
            // TODO: Determine a way to parse Type Arguments, yet keep them in case
            // "super" is encountered, which terminates the Primary at the given
            // ExpressionName.  Consider storing unused type args in the Primary.
            // MethodInvocation
            primary = new ASTPrimary(loc, parseMethodInvocationExprName(exprName));
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
                // MethodReference
                // TypeName.super::[TypeArguments]Identifier
                accept(DOUBLE_COLON);
                ASTListNode typeName = getTypesParser().convertToTypeName(exprName);
                ASTTypeArguments typeArgs = null;
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
                return new ASTPrimary(loc, methodReference);
            }
            else if (isCurr(DOT)) {
                if (accept(DOT) == null) {
                    throw new CompileException(curr().getLocation(), "Expected '.'.");
                }

                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    // MethodInvocation
                    // TypeName.super.<TypeArgs>methodName(args)
                    primary = new ASTPrimary(loc, parseMethodInvocationSuper(exprName, sooper));
                }
                else {
                    // TypeName.super.<TypeArgs>fieldName
                    // FieldAccess
                    primary = new ASTPrimary(loc, parseFieldAccessSuper(exprName, sooper));
                }
            }
            else {
                throw new CompileException(curr().getLocation(), "Expected method reference (::), method invocation, or field access (.).");
            }
        }
        else if (isCurr(OPEN_PARENTHESIS)) {
            // MethodInvocation
            // ExprNameExceptForMethodName.methodName(args)
            children = exprName.getChildren();
            ASTIdentifier methodName = (ASTIdentifier) children.getLast();
            children.removeLast();
            ASTListNode actualExprName = new ASTListNode(exprName.getLocation(), children, EXPR_NAME_IDS);
            primary = new ASTPrimary(loc, parseMethodInvocationExprNameIdentifier(actualExprName, methodName));
        }
        else {
            // ExpressionName
            primary = new ASTPrimary(loc, exprName);
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
            ASTTypeArguments typeArgs = null;
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
            return new ASTPrimary(loc, methodReference);
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
                ASTBinaryNode cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, cice);
            }
            if (isCurr(DOT) && (isNext(LESS_THAN) || isNext(IDENTIFIER))) {
                accept(DOT);
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))) {
                    // MethodInvocation
                    ASTMethodInvocation mi = parseMethodInvocation(primary);
                    primary = new ASTPrimary(loc, mi);
                }
                else {
                    // FieldAccess
                    ASTFieldAccess fa = parseFieldAccess(primary);
                    primary = new ASTPrimary(loc, fa);
                }
            }
            if (isCurr(OPEN_BRACKET)) {
                // ElementAccess
                ASTBinaryNode ea = parseElementAccess(loc, primary);
                primary = new ASTPrimary(loc, ea);
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
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(identifier.getLocation())
                .setIdentifier(identifier);
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocationSuper(ASTSuper sooper) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(sooper.getLocation())
                .setSooper(sooper);
        if (isCurr(LESS_THAN)) {
            builder.setTypeArgs(getTypesParser().parseTypeArguments());
        }
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * <p>Parses a <code>MethodInvocation</code>, with <code>super</code>,
     * starting with an already parsed <code>ASTExpressionName</code>, which is
     * converted to an <code>ASTTypeName</code>, and an already parsed <code>ASTSuper</code>.
     * DOT has already been parsed after "super".</p>
     * <em>
     * MethodInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . super . [TypeArguments] Identifier ( ArgumentList )</strong>
     * </em>
     * @param exprName An already parsed <code>ASTListNode</code> representing an Expression Name.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationSuper(ASTListNode exprName, ASTSuper sooper) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setTypeName(getNamesParser().convertToTypeName(exprName))
                .setSooper(sooper);
        if (isCurr(LESS_THAN)) {
            builder.setTypeArgs(getTypesParser().parseTypeArguments());
        }
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
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
            builder.setTypeArgs(typeArgs);
        }
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
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
     * @param exprName An already parsed <code>ASTListNode</code> representing an
     *                 ExpressionName of type <code>EXPR_NAME_IDS</code>.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationExprName(ASTListNode exprName) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setExprName(exprName);
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
            builder.setTypeArgs(typeArgs);
        }
        builder.setIdentifier(getNamesParser().parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
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
     * @param exprName An already parsed <code>ASTListNode</code> representing an ExpressionName.
     * @return An <code>ASTMethodInvocation</code>.
     */
    public ASTMethodInvocation parseMethodInvocationExprNameIdentifier(ASTListNode exprName, ASTIdentifier methodName) {
        ASTMethodInvocation.Builder builder = new ASTMethodInvocation.Builder()
                .setLocation(exprName.getLocation())
                .setExprName(exprName)
                .setIdentifier(methodName);
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
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
        if (accept(DOUBLE_COLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected '::'.");
        }
        ASTTypeArguments typeArgs = null;
        if (isCurr(LESS_THAN)) {
            // [TypeArguments]
            typeArgs = getTypesParser().parseTypeArguments();
        }
        if (isCurr(NEW)) {
            // DataType :: [TypeArguments] new
            accept(NEW);
            return new ASTMethodReference.Builder()
                    .setLocation(loc)
                    .setDataType(dataType)
                    .setTypeArguments(typeArgs)
                    .build();
        }
        else if (isCurr(IDENTIFIER)) {
            ASTIdentifier identifier = getNamesParser().parseIdentifier();
            ASTMethodReference.Builder builder = new ASTMethodReference.Builder()
                    .setLocation(loc);
            try {
                // ExpressionName :: [TypeArguments] Identifier
                ASTListNode exprName = getTypesParser().convertToExpressionName(dataType);
                builder.setExprName(exprName);
            }
            catch (CompileException tryExpressionName) {
                // DataType :: [TypeArguments] Identifier
                builder.setDataType(dataType);
            }
            return builder.setTypeArguments(typeArgs)
                    .setIdentifier(identifier)
                    .build();
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected identifier or new.");
        }
    }

    /**
     * Parses a <code>MethodReference</code>, given an already parsed
     * <code>ASTSuper</code>.
     * <em>
     * MethodReference:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super :: [TypeArguments] Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] new<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary :: [TypeArguments] Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super :: [TypeArguments] Identifier
     * </em>
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTMethodReference parseMethodReferenceSuper(ASTSuper sooper) {
        Location loc = sooper.getLocation();
        if (accept(DOUBLE_COLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected '::'.");
        }
        ASTTypeArguments typeArgs = null;
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
     * @return An <code>ASTBinaryNode</code> or an <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTParentNode parseClassInstanceCreationExpression() {
        if (isCurr(NEW)) {
            return parseUnqualifiedClassInstanceCreationExpression();
        }
        else {
            ASTPrimary primary = parsePrimary();
            return parseClassInstanceCreationExpression(primary);
        }
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
     * @return An <code>ASTBinaryNode</code>.
     */
    public ASTBinaryNode parseClassInstanceCreationExpression(ASTPrimary alreadyParsed) {
        Location loc = alreadyParsed.getLocation();
        if (isCurr(DOT) && isNext(NEW)) {
            accept(DOT);
            ASTUnqualifiedClassInstanceCreationExpression ucice = parseUnqualifiedClassInstanceCreationExpression();
            return new ASTBinaryNode(loc, NEW, alreadyParsed, ucice);
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected . new");
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
     * @return An <code>ASTParentNode</code>.
     */
    public ASTParentNode parseClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed) {
        return parseUnqualifiedClassInstanceCreationExpression(alreadyParsed);
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
            throw new CompileException(curr().getLocation(), "Expected new.");
        }
        ASTTypeArguments typeArgs = null;
        if (isCurr(LESS_THAN)) {
            typeArgs = getTypesParser().parseTypeArguments();
        }
        ASTTypeToInstantiate tti = parseTypeToInstantiate();
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        ASTListNode argumentList = parseArgumentList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \")\".");
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
            throw new CompileException(curr().getLocation(), "Expected \"(\".");
        }
        ASTListNode argumentList = parseArgumentList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected \")\".");
        }
        return new ASTUnqualifiedClassInstanceCreationExpression(alreadyParsed.getLocation(), alreadyParsed, argumentList);
    }

    /**
     * Parses an <code>ArgumentList</code>.
     * <em>
     * ArgumentList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression {, Expression}
     * </em>
     * @return An <code>ASTListNode</code> with type <code>ARGUMENTS</code>.
     */
    public ASTListNode parseArgumentList() {
        return parseList(
                BasicParser::isExpression,
                "Expected an expression.",
                COMMA,
                this::parseGiveExpression,
                ARGUMENTS,
                false);
    }

    /**
     * Parses a <code>GiveExpression</code>.
     * <em>
     * GiveExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;give Expression<br>
     * </em>
     * @return An <code>ASTUnaryNode</code>.
     */
    public ASTUnaryNode parseGiveExpression() {
        Location loc = curr().getLocation();
        boolean giveExpr = false;
        if (isCurr(GIVE)) {
            accept(GIVE);
            giveExpr = true;
        }
        return new ASTUnaryNode(loc, giveExpr ? GIVE : null, parseExpression());
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
        ASTListNode dimExprs = null;
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
                throw new CompileException(curr().getLocation(), "Array initializer not expected with dimension expressions.");
            }
            ASTUnaryNode arrayInitializer = parseArrayInitializer();
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
     * @return An <code>ASTListNode</code> representing dimension expressions.
     */
    public ASTListNode parseDimExprs() {
        return parseMultiple(
                t -> test(t, OPEN_BRACKET),
                "Expected \"[\".",
                this::parseDimExpr,
                DIM_EXPRS
        );
    }

    /**
     * Parses a <code>DimExpr</code>.
     * <em>
     * DimExpr:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[ ConditionalExpression ]
     * </em>
     * @return An <code>ASTUnaryNode</code> representing the dim expression.
     */
    public ASTUnaryNode parseDimExpr() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"[\".");
        }
        ASTNode expr = parseConditionalExpression();
        if (accept(CLOSE_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"]\".");
        }
        return new ASTUnaryNode(loc, OPEN_BRACKET, expr);
    }

    /**
     * Parses an <code>ArrayInitializer</code>.
     * <em>
     * ArrayInitializer:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ VariableInitializerList }<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ }
     * </em>
     * @return An <code>ASTUnaryNode</code> representing the array initializer.
     */
    public ASTUnaryNode parseArrayInitializer() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"{\".");
        }
        ASTListNode vil = parseVariableInitializerList();
        ASTUnaryNode node = new ASTUnaryNode(loc, OPEN_BRACE, vil);
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"}\".");
        }
        return node;
    }

    /**
     * Parses a <code>VariableInitializerList</code>.
     * <em>
     * VariableInitializerList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializer {, VariableInitializer}
     * </em>
     * @return An <code>ASTListNode</code> with type <code>VARIABLE_INITIALIZERS</code>.
     */
    public ASTListNode parseVariableInitializerList() {
        return parseList(
                t -> isPrimary(t) || test(t, OPEN_BRACE),
                "Expected an expression or an array initializer.",
                COMMA,
                this::parseVariableInitializer,
                VARIABLE_INITIALIZERS,
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
     * @return An <code>ASTNode</code> representing a variable initializer.
     */
    public ASTNode parseVariableInitializer() {
        if (isPrimary(curr())) {
            return parseExpression();
        }
        else if (isCurr(OPEN_BRACE)) {
            return parseArrayInitializer();
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected an expression or an array initializer.");
        }
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
        ASTListNode typeName = getNamesParser().parseTypeName();
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
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary [ ConditionalExpression ]<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess [ ConditionalExpression ]<br>
     * </em>
     * @param loc The <code>Location</code> of <code>primary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTBinaryNode</code> representing the element access.
     */
    public ASTBinaryNode parseElementAccess(Location loc, ASTPrimary primary) {
        if (accept(OPEN_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected '['.");
        }
        ASTBinaryNode result = new ASTBinaryNode(loc, OPEN_BRACKET, primary, parseConditionalExpression());
        if (accept(CLOSE_BRACKET) == null) {
            throw new CompileException(curr().getLocation(), "Expected ']'.");
        }

        while (isCurr(OPEN_BRACKET)) {
            accept(OPEN_BRACKET);
            result = new ASTBinaryNode(loc, OPEN_BRACKET, result, parseConditionalExpression());
            if (accept(CLOSE_BRACKET) == null) {
                throw new CompileException(curr().getLocation(), "Expected ']'.");
            }
        }
        return result;
    }

    /**
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTSuper</code>.  DOT has also already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>super . Identifier</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
     * </em>
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTSuper sooper) {
        Location loc = sooper.getLocation();
        return new ASTFieldAccess(loc, sooper, getNamesParser().parseIdentifier());
    }

    /**
     * <p>Parses a <code>FieldAccess</code>, given an already parsed
     * <code>ASTExpressionName</code> and an already parsed <code>ASTSuper</code>.
     * DOT has also already been parsed.</p>
     * <em>
     * FieldAccess:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeName . super . Identifier</strong>
     * </em>
     * @param exprName An already parsed <code>ASTListNode</code> representing an Expression Name.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTFieldAccess</code>.
     */
    public ASTFieldAccess parseFieldAccessSuper(ASTListNode exprName, ASTSuper sooper) {
        Location loc = exprName.getLocation();
        return new ASTFieldAccess(loc, getNamesParser().convertToTypeName(exprName), sooper, getNamesParser().parseIdentifier());
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
     * @return An <code>ASTUnaryNode</code> representing the class literal.
     */
    public ASTUnaryNode parseClassLiteral(ASTDataType dt) {
        Location loc = dt.getLocation();

        if (accept(DOT) == null || accept(CLASS) == null) {
            throw new CompileException(curr().getLocation(), "Expected .class");
        }
        return new ASTUnaryNode(loc, CLASS, dt);
    }

    /**
     * Parses the keyword <code>self</code>.
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
     * Parses the keyword <code>super</code>.
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
