package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.expressions.ASTAdditiveExpression;
import org.spruce.compiler.ast.expressions.ASTArgumentList;
import org.spruce.compiler.ast.expressions.ASTArrayCreationExpression;
import org.spruce.compiler.ast.expressions.ASTArrayInitializer;
import org.spruce.compiler.ast.expressions.ASTAssignment;
import org.spruce.compiler.ast.expressions.ASTAssignmentExpression;
import org.spruce.compiler.ast.expressions.ASTBitwiseAndExpression;
import org.spruce.compiler.ast.expressions.ASTBitwiseOrExpression;
import org.spruce.compiler.ast.expressions.ASTBitwiseXorExpression;
import org.spruce.compiler.ast.expressions.ASTCastExpression;
import org.spruce.compiler.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTClassLiteral;
import org.spruce.compiler.ast.expressions.ASTCompareExpression;
import org.spruce.compiler.ast.expressions.ASTConditionalExpression;
import org.spruce.compiler.ast.expressions.ASTDimExpr;
import org.spruce.compiler.ast.expressions.ASTDimExprs;
import org.spruce.compiler.ast.expressions.ASTElementAccess;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.ast.expressions.ASTExpressionNoIncrDecr;
import org.spruce.compiler.ast.expressions.ASTFieldAccess;
import org.spruce.compiler.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.ast.expressions.ASTLogicalAndExpression;
import org.spruce.compiler.ast.expressions.ASTLogicalOrExpression;
import org.spruce.compiler.ast.expressions.ASTLogicalXorExpression;
import org.spruce.compiler.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.ast.expressions.ASTMethodReference;
import org.spruce.compiler.ast.expressions.ASTMultiplicativeExpression;
import org.spruce.compiler.ast.expressions.ASTPostfixExpression;
import org.spruce.compiler.ast.expressions.ASTPrefixExpression;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.expressions.ASTRelationalExpression;
import org.spruce.compiler.ast.expressions.ASTShiftExpression;
import org.spruce.compiler.ast.expressions.ASTSuper;
import org.spruce.compiler.ast.expressions.ASTThis;
import org.spruce.compiler.ast.expressions.ASTTypeToInstantiate;
import org.spruce.compiler.ast.expressions.ASTUnaryExpression;
import org.spruce.compiler.ast.expressions.ASTUnqualifiedClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTVariableInitializer;
import org.spruce.compiler.ast.expressions.ASTVariableInitializerList;
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
 * A <code>ExpressionsParser</code> is a <code>TypesParser</code> that also parses
 * expressions.
 */
public class ExpressionsParser extends TypesParser
{
    /**
     * Constructs a <code>ExpressionsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     */
    public ExpressionsParser(Scanner scanner)
    {
        super(scanner);
    }

    /**
     * Parses an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression parseExpression()
    {
        Location loc = curr().getLocation();
        if (isCurr(INCREMENT) || isCurr(DECREMENT))
        {
            ASTPrefixExpression prefixExpr = parsePrefixExpression();
            return new ASTExpression(loc, Arrays.asList(prefixExpr));
        }
        else if (isPrimary(curr()))
        {
            ASTExpressionNoIncrDecr exprNoIncrDecr = parseExpressionNoIncrDecr();
            if (isCurr(INCREMENT) || isCurr(DECREMENT))
            {
                return new ASTExpression(loc, Arrays.asList(parsePostfixExpression(loc, exprNoIncrDecr.getLeftHandSide())));
            }
            else
            {
                return new ASTExpression(loc, Arrays.asList(exprNoIncrDecr));
            }
        }
        else
        {
            throw new CompileException("Expected primary, identifier, ++, or --");
        }
    }

    /**
     * Parses an <code>ASTExpressionNoIncrDecr</code>.
     * @return An <code>ASTExpressionNoIncrDecr</code>.
     */
    public ASTExpressionNoIncrDecr parseExpressionNoIncrDecr()
    {
        Location loc = curr().getLocation();
        ASTAssignmentExpression assignExpr = parseAssignmentExpression();
        return new ASTExpressionNoIncrDecr(loc, Arrays.asList(assignExpr));
    }

    /**
     * Parses an <code>ASTAssignmentExpression</code>; they are right-
     * associative with each other.
     * @return An <code>ASTAssignmentExpression</code>.
     */
    public ASTAssignmentExpression parseAssignmentExpression()
    {
        if (isPrimary(curr()))
        {
            Token curr = curr();
            Location loc = curr.getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            ASTConditionalExpression condExpr = parseConditionalExpression();

            TokenType currToken = curr().getType();
            switch(currToken)
            {
            case ASSIGNMENT:
            case PLUS_EQUALS:
            case MINUS_EQUALS:
            case STAR_EQUALS:
            case SLASH_EQUALS:
            case PERCENT_EQUALS:
            case SHIFT_LEFT_EQUALS:
            case SHIFT_RIGHT_EQUALS:
            case UNSIGNED_SHIFT_RIGHT_EQUALS:
            case AND_EQUALS:
            case OR_EQUALS:
            case XOR_EQUALS:
                ASTAssignment assignment = parseAssignment(loc,
                        condExpr.getLeftHandSide());
                return new ASTAssignmentExpression(loc, Arrays.asList(assignment));
            default:
                children.add(condExpr);
                return new ASTAssignmentExpression(loc, children);
            }
        }
        else
        {
            throw new CompileException("Expected a variable name or element access.");
        }
    }

    /**
     * Parses an <code>ASTPrefixExpression</code>, given an <code>ASTLeftHandSide</code>
     * that has already been parsed and its <code>Location</code>.
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTPrefixExpression</code>.
     */
    public ASTAssignment parseAssignment(Location loc, ASTLeftHandSide lhs)
    {
        TokenType currToken = curr().getType();
        switch(currToken)
        {
        case ASSIGNMENT:
        case PLUS_EQUALS:
        case MINUS_EQUALS:
        case STAR_EQUALS:
        case SLASH_EQUALS:
        case PERCENT_EQUALS:
        case SHIFT_LEFT_EQUALS:
        case SHIFT_RIGHT_EQUALS:
        case UNSIGNED_SHIFT_RIGHT_EQUALS:
        case AND_EQUALS:
        case OR_EQUALS:
        case XOR_EQUALS:
            List<ASTNode> children = new ArrayList<>(2);
            children.add(lhs);
            accept(currToken);
            children.add(parseAssignmentExpression());
            ASTAssignment node = new ASTAssignment(loc, children);
            node.setOperation(currToken);
            return node;
        default:
            throw new CompileException("Expected assignment operator.");
        }
    }

    /**
     * Parses an <code>ASTPrefixExpression</code>.
     * @return An <code>ASTPrefixExpression</code>.
     */
    public ASTPrefixExpression parsePrefixExpression()
    {
        if (isCurr(INCREMENT))
        {
            Location loc = curr().getLocation();
            accept(INCREMENT);
            ASTLeftHandSide lhs = parseLeftHandSide();
            return new ASTPrefixExpression(loc, lhs, INCREMENT);
        }
        else if (isCurr(DECREMENT))
        {
            Location loc = curr().getLocation();
            accept(DECREMENT);
            ASTLeftHandSide lhs = parseLeftHandSide();
            return new ASTPrefixExpression(loc, lhs, DECREMENT);
        }
        else
        {
            throw new CompileException("Operator ++ or -- expected.");
        }
    }

    /**
     * Parses an <code>ASTPostfixExpression</code>.
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTPostfixExpression</code>.
     */
    public ASTPostfixExpression parsePostfixExpression(Location loc, ASTLeftHandSide lhs)
    {
        if (isCurr(INCREMENT))
        {
            accept(INCREMENT);
            return new ASTPostfixExpression(loc, lhs, INCREMENT);
        }
        else if (isCurr(DECREMENT))
        {
            accept(DECREMENT);
            return new ASTPostfixExpression(loc, lhs, DECREMENT);
        }
        else
        {
            throw new CompileException("Operator ++ or -- expected.");
        }
    }

    /**
     * Parses an <code>ASTLeftHandSide</code>.
     * @return An <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide parseLeftHandSide()
    {
        if (isPrimary(curr()))
        {
            Location loc = curr().getLocation();
            ASTPrimary primary = parsePrimary();
            if (isCurr(OPEN_BRACKET))
            {
                return new ASTLeftHandSide(loc, Arrays.asList(parseElementAccess(loc, primary)));
            }
            else
            {
                return primary.getLeftHandSide();
            }
        }
        else
        {
            throw new CompileException("Element access or identifier expected.");
        }
    }

    /**
     * Parses an <code>ASTConditionalExpression</code>; they are right-
     * associative with each other.
     * @return An <code>ASTConditionalExpression</code>.
     */
    public ASTConditionalExpression parseConditionalExpression()
    {
        if (isPrimary(curr()))
        {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(3);
            children.add(parseLogicalOrExpression());
            ASTConditionalExpression node = new ASTConditionalExpression(loc, children);

            if (isCurr(QUESTION_MARK))
            {
                accept(QUESTION_MARK);
                children.add(parseLogicalOrExpression()); // parseExpressionNoIncrDecr()
                node.setOperation(QUESTION_MARK);

                if (isCurr(COLON))
                {
                    accept(COLON);
                    children.add(parseConditionalExpression());
                }
                else
                {
                    throw new CompileException("Expected colon.");
                }
            }
            return node;
        }
        else
        {
            throw new CompileException("Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTLogicalOrExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalOrExpression</code>.
     */
    public ASTLogicalOrExpression parseLogicalOrExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(LOGICAL_OR, CONDITIONAL_OR),
                this::parseLogicalXorExpression,
                ASTLogicalOrExpression::new
        );
    }

    /**
     * Parses an <code>ASTLogicalXorExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalXorExpression</code>.
     */
    public ASTLogicalXorExpression parseLogicalXorExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Collections.singletonList(LOGICAL_XOR),
                this::parseLogicalAndExpression,
                ASTLogicalXorExpression::new
        );
    }

    /**
     * Parses an <code>ASTLogicalAndExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTLogicalAndExpression</code>.
     */
    public ASTLogicalAndExpression parseLogicalAndExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(LOGICAL_AND, CONDITIONAL_AND),
                this::parseRelationalExpression,
                ASTLogicalAndExpression::new
        );
    }

    /**
     * Parses an <code>ASTRelationalExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTRelationalExpression</code>.
     */
    public ASTRelationalExpression parseRelationalExpression()
    {
        if (isPrimary(curr()))
        {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseCompareExpression());
            ASTRelationalExpression node = new ASTRelationalExpression(loc, children);

            TokenType curr;
            while ( (curr = isAcceptedOperator(Arrays.asList(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, EQUAL, NOT_EQUAL, INSTANCEOF, IS, ISNT)) ) != null)
            {
                accept(curr);
                children = new ArrayList<>(2);
                children.add(node);
                if (curr == INSTANCEOF)
                {
                    children.add(parseDataType());
                }
                else
                {
                    children.add(parseCompareExpression());
                }
                node = new ASTRelationalExpression(loc, children);
                node.setOperation(curr);
            }
            return node;
        }
        else
        {
            throw new CompileException("Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTCompareExpression</code>; they are NOT associative
     * with each other.
     * @return An <code>ASTCompareExpression</code>.
     */
    public ASTCompareExpression parseCompareExpression()
    {
        if (isPrimary(curr()))
        {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseBitwiseOrExpression());
            ASTCompareExpression node = new ASTCompareExpression(loc, children);

            if (isCurr(COMPARISON))
            {
                accept(COMPARISON);
                children.add(parseBitwiseOrExpression());
                node.setOperation(COMPARISON);
            }
            return node;
        }
        else
        {
            throw new CompileException("Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTBitwiseOrExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseOrExpression</code>.
     */
    public ASTBitwiseOrExpression parseBitwiseOrExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Collections.singletonList(BITWISE_OR),
                this::parseBitwiseXorExpression,
                ASTBitwiseOrExpression::new
        );
    }

    /**
     * Parses an <code>ASTBitwiseXorExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseXorExpression</code>.
     */
    public ASTBitwiseXorExpression parseBitwiseXorExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Collections.singletonList(BITWISE_XOR),
                this::parseBitwiseAndExpression,
                ASTBitwiseXorExpression::new
        );
    }

    /**
     * Parses an <code>ASTBitwiseAndExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTBitwiseAndExpression</code>.
     */
    public ASTBitwiseAndExpression parseBitwiseAndExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Collections.singletonList(BITWISE_AND),
                this::parseShiftExpression,
                ASTBitwiseAndExpression::new
        );
    }

    /**
     * Parses an <code>ASTShiftExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTShiftExpression</code>.
     */
    public ASTShiftExpression parseShiftExpression()
    {
        return parseBinaryExpressionLeftAssociative(
                ExpressionsParser::isPrimary,
                "Expected a literal or expression name.",
                Arrays.asList(SHIFT_LEFT, SHIFT_RIGHT, UNSIGNED_SHIFT_RIGHT),
                this::parseAdditiveExpression,
                ASTShiftExpression::new
        );
    }

    /**
     * Parses an <code>ASTAdditiveExpression</code>; they are left-
     * associative with each other.
     * @return An <code>ASTAdditiveExpression</code>.
     */
    public ASTAdditiveExpression parseAdditiveExpression()
    {
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
    public ASTMultiplicativeExpression parseMultiplicativeExpression()
    {
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
    public ASTCastExpression parseCastExpression()
    {
        if (isPrimary(curr()))
        {
            Location loc = curr().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseUnaryExpression());
            ASTCastExpression node = new ASTCastExpression(loc, children);

            while (isCurr(AS))
            {
                accept(AS);
                if (children.size() == 1)
                {
                    children.add(parseIntersectionType());
                    node.setOperation(AS);
                }
                else
                {
                    List<ASTNode> siblings = new ArrayList<>(2);
                    siblings.add(node);
                    siblings.add(parseIntersectionType());
                    node = new ASTCastExpression(loc, siblings);
                    node.setOperation(AS);
                }
            }
            return node;
        }
        else
        {
            throw new CompileException("Expected a literal or expression name.");
        }
    }

    /**
     * Parses an <code>ASTUnaryExpression</code>.
     * @return An <code>ASTUnaryExpression</code>.
     */
    public ASTUnaryExpression parseUnaryExpression()
    {
        Location loc = curr().getLocation();
        if (isCurr(LOGICAL_COMPLEMENT))
        {
            accept(LOGICAL_COMPLEMENT);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), LOGICAL_COMPLEMENT);
        }
        else if (isCurr(BITWISE_COMPLEMENT))
        {
            accept(BITWISE_COMPLEMENT);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), BITWISE_COMPLEMENT);
        }
        else if (isCurr(MINUS))
        {
            accept(MINUS);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), MINUS);
        }
        else
        {
            return new ASTUnaryExpression(loc, parsePrimary());
        }
    }



    /**
     * Parses an <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary()
    {
        Location loc = curr().getLocation();
        ASTPrimary primary;
        if (isLiteral(curr()))
        {
            ASTLiteral literal = parseLiteral();
            primary = new ASTPrimary(loc, Arrays.asList(literal));
        }
        else if (isCurr(IDENTIFIER))
        {
            if (isNext(OPEN_PARENTHESIS))
            {
                // id(args)
                ASTIdentifier methodName = parseIdentifier();
                primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            }
            else
            {
                ASTDataType dataType = parseDataType();
                if (isCurr(DOUBLE_COLON))
                {
                    return new ASTPrimary(loc, Arrays.asList(parseMethodReference(dataType)));
                }
                else if (isCurr(DOT) && isNext(CLASS))
                {
                    // Get the class literal and get out.
                    return new ASTPrimary(loc, Arrays.asList(parseClassLiteral(dataType)));
                }
                ASTExpressionName expressionName = dataType.convertToExpressionName();
                primary = parsePrimary(expressionName);
            }
        }
        else if (isCurr(THIS))
        {
            ASTThis keywordThis = parseThis();
            primary = new ASTPrimary(loc, Arrays.asList(keywordThis));
        }
        else if (isCurr(SUPER))
        {
            ASTSuper sooper = parseSuper();
            if (isCurr(DOUBLE_COLON))
            {
                // Method references don't chain.
                return new ASTPrimary(loc, Arrays.asList(parseMethodReferenceSuper(sooper)));
            }
            else
            {
                if (accept(DOT) == null)
                {
                    throw new CompileException("Expected '.'.");
                }
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)))
                {
                    primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(sooper)));
                }
                else
                {
                    // Field access
                    primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(sooper)));
                }
            }
        }
        else if (isCurr(OPEN_PARENTHESIS))
        {
            accept(OPEN_PARENTHESIS);
            ASTExpressionNoIncrDecr expression = parseExpressionNoIncrDecr();
            Token closeParen = accept(CLOSE_PARENTHESIS);
            if (closeParen == null)
            {
                throw new CompileException("Expected close parenthesis \")\".");
            }
            primary = new ASTPrimary(loc, Arrays.asList(expression));
            primary.setOperation(OPEN_PARENTHESIS);
        }
        else if (isCurr(NEW))
        {
            if (isNext(LESS_THAN))
            {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression();
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            else if (isNext(IDENTIFIER))
            {
                accept(NEW);
                ASTTypeToInstantiate tti = parseTypeToInstantiate();
                if (isCurr(OPEN_BRACKET) || isCurr(OPEN_CLOSE_BRACKET))
                {
                    primary = new ASTPrimary(loc, Arrays.asList(parseArrayCreationExpression(tti)));
                }
                else if (isCurr(OPEN_PARENTHESIS))
                {
                    primary = new ASTPrimary(loc, Arrays.asList(parseClassInstanceCreationExpression(tti)));
                }
                else
                {
                    throw new CompileException("Malformed array or class instance creation expression.");
                }
            }
            else
            {
                throw new CompileException("Type arguments or type to instantiate expected after new.");
            }
        }
        else
        {
            throw new CompileException("Expected: literal, expression name, or array or class instance creation expression.");
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
    public ASTPrimary parsePrimary(ASTExpressionName exprName)
    {
        ASTPrimary primary;
        Location loc = exprName.getLocation();

        // If exprName is a simple identifier, then it is the method name.
        List<ASTNode> children = exprName.getChildren();
        if (children.size() == 1 && isCurr(OPEN_PARENTHESIS))
        {
            // id( -> method invocation
            ASTIdentifier methodName = (ASTIdentifier) children.get(0);
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            return parsePrimary(primary);
        }

        if (isCurr(DOT) && isNext(THIS))
        {
            // TypeName.this
            ASTTypeName tn = exprName.convertToTypeName();
            accept(DOT);
            primary = new ASTPrimary(loc, Arrays.asList(tn, parseThis()));
            primary.setOperation(DOT);
        }
        else if (isCurr(DOT) && isNext(LESS_THAN))
        {
            // ExprName.<TypeArgs>methodName(args)
            // NOT ExprName.<TypeArgs>super(args) -- Constructor invocation.
            // TODO: Determine a way to parse Type Arguments, yet keep them in case
            // "super" is encountered, which terminates the Primary at the given
            // ExpressionName.  Consider storing unused type args in the Primary.
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(exprName)));
        }
        else if (isCurr(DOT) && isNext(SUPER) && !isPeek(OPEN_PARENTHESIS))
        {
            // TypeName.super.methodInvocation()
            // TypeName.super.fieldAccess
            // NOT Expression.super() -- Constructor invocation.
            if (accept(DOT) == null)
            {
                throw new CompileException("Expected '.'.");
            }
            ASTSuper sooper = parseSuper();
            if (isCurr(DOUBLE_COLON))
            {
                // TypeName.super::[TypeArguments]Identifier
                accept(DOUBLE_COLON);
                List<ASTNode> mrChildren = new ArrayList<>(4);
                mrChildren.add(exprName.convertToTypeName());
                mrChildren.add(sooper);
                if (isCurr(LESS_THAN))
                {
                    mrChildren.add(parseTypeArguments());
                }
                mrChildren.add(parseIdentifier());
                ASTMethodReference methodReference = new ASTMethodReference(loc, mrChildren);
                methodReference.setOperation(DOUBLE_COLON);
                return new ASTPrimary(loc, Arrays.asList(methodReference));
            }
            else if (isCurr(DOT))
            {

                if (accept(DOT) == null)
                {
                    throw new CompileException("Expected '.'.");
                }
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)))
                {
                    // TypeName.super.<TypeArgs>methodName(args)
                    primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(exprName, sooper)));
                }
                else
                {
                    // TypeName.super.<TypeArgs>fieldName
                    primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(exprName, sooper)));
                }
            }
            else
            {
                throw new CompileException("Expected method reference (::), method invocation, or field access (.).");
            }
        }
        else if (isCurr(OPEN_PARENTHESIS))
        {
            // ExprNameExceptForMethodName.methodName(args)
            children = exprName.getChildren();
            ASTIdentifier methodName = (ASTIdentifier) children.get(1);
            ASTAmbiguousName ambiguous = (ASTAmbiguousName) children.get(0);
            ASTExpressionName actual = new ASTExpressionName(ambiguous.getLocation(), ambiguous.getChildren());
            actual.setOperation(ambiguous.getOperation());
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(actual, methodName)));
        }
        else
        {
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
    public ASTPrimary parsePrimary(ASTPrimary primary)
    {
        Location loc = primary.getLocation();

        // Non-recursive method reference.
        // Primary :: [TypeArguments] identifier
        if (isCurr(DOUBLE_COLON))
        {
            accept(DOUBLE_COLON);
            List<ASTNode> children = new ArrayList<>(3);
            children.add(primary);
            if (isCurr(LESS_THAN))
            {
                children.add(parseTypeArguments());
            }
            children.add(parseIdentifier());
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
                )
        {
            if (isCurr(DOT) && isNext(NEW))
            {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            if (isCurr(DOT) && (isNext(LESS_THAN) || isNext(IDENTIFIER)))
            {
                accept(DOT);
                if (isCurr(LESS_THAN) || (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)))
                {
                    ASTMethodInvocation mi = parseMethodInvocation(primary);
                    primary = new ASTPrimary(loc, Arrays.asList(mi));
                }
                else
                {
                    ASTFieldAccess fa = parseFieldAccess(primary);
                    primary = new ASTPrimary(loc, Arrays.asList(fa));
                }
            }
            if (isCurr(OPEN_BRACKET))
            {
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
    public ASTMethodInvocation parseMethodInvocation(ASTIdentifier identifier)
    {
        Location loc = identifier.getLocation();
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(identifier);
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocationSuper(ASTSuper sooper)
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        children.add(sooper);
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocationSuper(ASTExpressionName exprName, ASTSuper sooper)
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(5);
        children.add(exprName.convertToTypeName());
        children.add(sooper);
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocation(ASTPrimary primary)
    {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(primary);
        if (isCurr(LESS_THAN))
        {
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
            ASTTypeArguments typeArgs = parseTypeArguments();
            if (isCurr(SUPER))
            {
                // Primary . TypeArguments super ( [ArgumentList] )
                List<ASTNode> alreadyParsed = new ArrayList<>(2);
                alreadyParsed.add(primary);
                alreadyParsed.add(typeArgs);
                throw new CompileException("Expected method name.", alreadyParsed);
            }
            children.add(typeArgs);
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocation(ASTExpressionName exprName)
    {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(exprName);
        if (accept(DOT) == null)
        {
            throw new CompileException("Expected '.'.");
        }
        if (isCurr(LESS_THAN))
        {
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
            ASTTypeArguments typeArgs = parseTypeArguments();
            if (isCurr(SUPER))
            {
                // ExpressionName . TypeArguments super ( [ArgumentList] )
                List<ASTNode> alreadyParsed = new ArrayList<>(2);
                alreadyParsed.add(exprName);
                alreadyParsed.add(typeArgs);
                throw new CompileException("Expected method name.", alreadyParsed);
            }
            children.add(typeArgs);
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodInvocation parseMethodInvocation(ASTExpressionName exprName, ASTIdentifier methodName)
    {
        List<ASTNode> children = new ArrayList<>(3);
        children.add(exprName);
        children.add(methodName);
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
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
    public ASTMethodReference parseMethodReference(ASTDataType dataType)
    {
        Location loc = dataType.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (accept(DOUBLE_COLON) == null)
        {
            throw new CompileException("Expected '::'.");
        }
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        if (isCurr(NEW))
        {
            accept(NEW);
            children.add(0, dataType);
            ASTMethodReference node = new ASTMethodReference(loc, children);
            node.setOperation(DOUBLE_COLON);
            return node;
        }
        else if (isCurr(IDENTIFIER))
        {
            children.add(parseIdentifier());
            try
            {
                // ExpressionName :: [TypeArguments] Identifier
                ASTExpressionName exprName = dataType.convertToExpressionName();
                children.add(0, exprName);
            }
            catch (CompileException tryExpressionName)
            {
                // DataType :: [TypeArguments] Identifier
                children.add(0, dataType);
            }
            ASTMethodReference node = new ASTMethodReference(loc, children);
            node.setOperation(DOUBLE_COLON);
            return node;
        }
        else
        {
            throw new CompileException("Expected identifier or new.");
        }

    }

    /**
     * Parses an <code>ASTMethodReference</code>, given an already parsed
     * <code>ASTSuper</code>.
     * @param sooper An already parsed <code>ASTSuper</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTMethodReference parseMethodReferenceSuper(ASTSuper sooper)
    {
        Location loc = sooper.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(sooper);
        if (accept(DOUBLE_COLON) == null)
        {
            throw new CompileException("Expected '::'.");
        }
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        ASTMethodReference node = new ASTMethodReference(loc, children);
        node.setOperation(DOUBLE_COLON);
        return node;
    }

    /**
     * Parses an <code>ASTClassInstanceCreationExpression</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (isCurr(NEW))
        {
            children.add(parseUnqualifiedClassInstanceCreationExpression());
        }
        else
        {
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
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTPrimary alreadyParsed)
    {
        Location loc = alreadyParsed.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(alreadyParsed);
        if (isCurr(DOT) && isNext(NEW))
        {
            accept(DOT);
            children.add(parseUnqualifiedClassInstanceCreationExpression());
        }
        else
        {
            throw new CompileException("Expected . new");
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
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed)
    {
        return new ASTClassInstanceCreationExpression(alreadyParsed.getLocation(), Arrays.asList(
                parseUnqualifiedClassInstanceCreationExpression(alreadyParsed)
        ));
    }

    /**
     * Parses an <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression()
    {
        Location loc = curr().getLocation();
        if (accept(NEW) == null)
        {
            throw new CompileException("Expected new.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseTypeToInstantiate());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected \")\".");
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
    public ASTUnqualifiedClassInstanceCreationExpression parseUnqualifiedClassInstanceCreationExpression(ASTTypeToInstantiate alreadyParsed)
    {
        List<ASTNode> children = new ArrayList<>(4);
        children.add(alreadyParsed);
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected \"(\".");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected \")\".");
        }
        ASTUnqualifiedClassInstanceCreationExpression node = new ASTUnqualifiedClassInstanceCreationExpression(alreadyParsed.getLocation(), children);
        node.setOperation(NEW);
        return node;
    }

    /**
     * Parses an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList parseArgumentList()
    {
        if (isExpression(curr()))
        {
            return parseList(
                    ExpressionsParser::isExpression,
                    "Expected an expression.",
                    COMMA,
                    this::parseExpression,
                    ASTArgumentList::new);
        }
        else
        {
            return new ASTArgumentList(curr().getLocation(), Arrays.asList());
        }
    }

    /**
     * Parses an <code>ASTArrayCreationExpression</code>.
     * @return An <code>ASTArrayCreationExpression</code>.
     */
    public ASTArrayCreationExpression parseArrayCreationExpression()
    {
        if (accept(NEW) == null)
        {
            throw new CompileException("Expected new.");
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
    public ASTArrayCreationExpression parseArrayCreationExpression(ASTTypeToInstantiate alreadyParsed)
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(alreadyParsed);
        boolean dimExprsPresent = false;
        if (isCurr(OPEN_BRACKET))
        {
            children.add(parseDimExprs());
            dimExprsPresent = true;
        }
        if (isCurr(OPEN_CLOSE_BRACKET))
        {
            children.add(parseDims());
        }
        if (children.size() == 0)
        {
            throw new CompileException("Expected \"[\".");
        }
        if (isCurr(OPEN_BRACE))
        {
            if (dimExprsPresent)
            {
                throw new CompileException("Array initializer not expected with dimension expressions.");
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
    public ASTDimExprs parseDimExprs()
    {
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
    public ASTDimExpr parseDimExpr()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACKET) == null)
        {
            throw new CompileException("Expected \"[\".");
        }
        ASTExpression expr = parseExpression();
        if (accept(CLOSE_BRACKET) == null)
        {
            throw new CompileException("Expected \"]\".");
        }
        ASTDimExpr node = new ASTDimExpr(loc, Arrays.asList(expr));
        node.setOperation(OPEN_BRACKET);
        return node;
    }

    /**
     * Parses an <code>ASTArrayInitializer</code>.
     * @return An <code>ASTArrayInitializer</code>.
     */
    public ASTArrayInitializer parseArrayInitializer()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected \"{\".");
        }
        ASTArrayInitializer node;
        if (isPrimary(curr()) || isCurr(OPEN_BRACE))
        {
            ASTVariableInitializerList vil = parseVariableInitializerList();
            node = new ASTArrayInitializer(loc, Collections.singletonList(vil));
        }
        else
        {
            node = new ASTArrayInitializer(loc, Collections.emptyList());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected \"}\".");
        }
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTVariableInitializerList</code>.
     * @return An <code>ASTVariableInitializerList</code>.
     */
    public ASTVariableInitializerList parseVariableInitializerList()
    {
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
    public ASTVariableInitializer parseVariableInitializer()
    {
        Location loc = curr().getLocation();
        if (isPrimary(curr()))
        {
            ASTExpressionNoIncrDecr exprNoIncrDecr = parseExpressionNoIncrDecr();
            return new ASTVariableInitializer(loc, Arrays.asList(exprNoIncrDecr));
        }
        else if (isCurr(OPEN_BRACE))
        {
            ASTArrayInitializer arrayInit = parseArrayInitializer();
            return new ASTVariableInitializer(loc, Arrays.asList(arrayInit));
        }
        else
        {
            throw new CompileException("Expected expression (no incr/decr) or array initializer.");
        }
    }

    /**
     * Parses an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate parseTypeToInstantiate()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseTypeName());
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArgumentsOrDiamond());
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
    public ASTElementAccess parseElementAccess(Location loc, ASTPrimary primary)
    {
        if (accept(OPEN_BRACKET) == null)
        {
            throw new CompileException("Expected '['.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(primary);
        children.add(parseExpression());
        if (accept(CLOSE_BRACKET) == null)
        {
            throw new CompileException("Expected ']'.");
        }

        ASTElementAccess ea = new ASTElementAccess(loc, children);
        while(isCurr(OPEN_BRACKET))
        {
            accept(OPEN_BRACKET);
            children = new ArrayList<>(2);
            children.add(ea);
            children.add(parseExpression());
            if (accept(CLOSE_BRACKET) == null)
            {
                throw new CompileException("Expected ']'.");
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
    public ASTFieldAccess parseFieldAccessSuper(ASTSuper sooper)
    {
        Location loc = sooper.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(sooper);
        children.add(parseIdentifier());
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
    public ASTFieldAccess parseFieldAccessSuper(ASTExpressionName exprName, ASTSuper sooper)
    {
        Location loc = exprName.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(exprName.convertToTypeName());
        children.add(sooper);
        children.add(parseIdentifier());
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
    public ASTFieldAccess parseFieldAccess(ASTPrimary primary)
    {
        Location loc = primary.getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(primary);
        children.add(parseIdentifier());
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
    public ASTClassLiteral parseClassLiteral(ASTDataType dt)
    {
        Location loc = dt.getLocation();

        if (accept(DOT) == null || accept(CLASS) == null)
        {
            throw new CompileException("Expected .class");
        }

        ASTClassLiteral node = new ASTClassLiteral(loc, Arrays.asList(dt));
        node.setOperation(CLASS);
        return node;
    }

    /**
     * Parses an <code>ASTThis</code>.
     * @return An <code>ASTThis</code>.
     */
    public ASTThis parseThis()
    {
        Token t;
        if ((t = accept(THIS)) != null)
        {
            return new ASTThis(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected 'this'.");
        }
    }

    /**
     * Parses an <code>ASTSuper</code>.
     * @return An <code>ASTSuper</code>.
     */
    public ASTSuper parseSuper()
    {
        Token t;
        if ((t = accept(SUPER)) != null)
        {
            return new ASTSuper(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected 'super'.");
        }
    }

    /**
     * Determines whether the given token can start an expression.
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start an expression.
     */
    protected static boolean isExpression(Token t)
    {
        return (test(t, INCREMENT, DECREMENT) || isPrimary(t));
    }

    /**
     * <p>Determines whether the given token can start a Primary.</p>
     * <ul>
     * <li>-</li>
     * <li>~</li>
     * <li>!</li>
     * <li>identifier</li>
     * <li><code>this</code></li>
     * <li><code>super</code></li>
     * <li>(</li>
     * </ul>
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a Primary.
     */
    protected static boolean isPrimary(Token t)
    {
        if (isLiteral(t))
        {
            return true;
        }
        switch (t.getType())
        {
        case MINUS:
        case BITWISE_COMPLEMENT:
        case LOGICAL_COMPLEMENT:
        case IDENTIFIER:
        case THIS:
        case OPEN_PARENTHESIS:
        case NEW:
        case SUPER:
            return true;
        default:
            return false;
        }
    }
}
