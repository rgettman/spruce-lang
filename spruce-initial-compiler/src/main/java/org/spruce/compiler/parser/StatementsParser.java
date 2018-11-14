package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTFieldAccess;
import org.spruce.compiler.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.ast.expressions.ASTPrefixExpression;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>StatementsParser</code> is a <code>ExpressionsParser</code> that also parses
 * statements.
 */
public class StatementsParser extends ExpressionsParser
{
    /**
     * Constructs a <code>ExpressionsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     */
    public StatementsParser(Scanner scanner)
    {
        super(scanner);
    }

    /**
     * Parses an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock parseBlock()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        ASTBlock node = new ASTBlock(loc, children);
        node.setOperation(OPEN_BRACE);
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseBlockStatements());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        return node;
    }

    /**
     * Parses an <code>ASTBlockStatements</code>.
     * @return An <code>ASTBlockStatements</code>.
     */
    public ASTBlockStatements parseBlockStatements()
    {
        return parseMultiple(
                t -> !test(t, CLOSE_BRACE) && !test(t, DEFAULT) && !test(t, CASE),
                "Expected statement or local variable declaration.",
                this::parseBlockStatement,
                ASTBlockStatements::new
        );
    }

    /**
     * Parses an <code>ASTBlockStatement</code>.
     * @return An <code>ASTBlockStatement</code>.
     */
    public ASTBlockStatement parseBlockStatement()
    {
        Location loc = curr().getLocation();
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case CONSTANT:
        case AUTO:
            return new ASTBlockStatement(loc, Arrays.asList(parseLocalVariableDeclarationStatement()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER))
            {
                return new ASTBlockStatement(loc, Arrays.asList(parseLocalVariableDeclarationStatement(dt)));
            }
            else
            {
                // Convert to Expression Name.
                ASTExpressionName exprName = dt.convertToExpressionName();
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = parsePrimary(exprName);
                return new ASTBlockStatement(loc, Arrays.asList(parseStatement(primary)));
            }
        default:
            return new ASTBlockStatement(loc, Arrays.asList(parseStatement()));
        }
    }

    /**
     * Parses an <code>ASTLocalVariableDeclarationStatement</code>.
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement()
    {
        Location loc = curr().getLocation();
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration();
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTLocalVariableDeclarationStatement node = new ASTLocalVariableDeclarationStatement(loc, Arrays.asList(localVarDecl));
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableDeclarationStatement</code>, given an
     * already parsed <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement(ASTDataType dt)
    {
        Location loc = dt.getLocation();
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration(dt);
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTLocalVariableDeclarationStatement node = new ASTLocalVariableDeclarationStatement(loc, Arrays.asList(localVarDecl));
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableDeclaration</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST, CONSTANT)) != null)
        {
            children.add(parseVariableModifierList());
        }
        children.add(parseLocalVariableType());
        children.add(parseVariableDeclaratorList());
        return new ASTLocalVariableDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTLocalVariableDeclaration</code>, given an already
     * parsed <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration(ASTDataType dt)
    {
        Location loc = dt.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(new ASTLocalVariableType(loc, Collections.singletonList(dt)));
        children.add(parseVariableDeclaratorList());
        return new ASTLocalVariableDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTVariableModifierList</code>.
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList parseVariableModifierList()
    {
        return parseMultiple(
                t -> test(t, FINAL, CONST, CONSTANT),
                "Expected final, const, or constant.",
                this::parseVariableModifier,
                ASTVariableModifierList::new
        );
    }

    /**
     * Parses an <code>ASTVariableModifier</code>.
     * @return An <code>ASTVariableModifier</code>.
     */
    public ASTVariableModifier parseVariableModifier()
    {
        return parseOneOf(
                Arrays.asList(FINAL, CONST, CONSTANT),
                "Expected final, const, or constant.",
                ASTVariableModifier::new
        );
    }

    /**
     * Parses an <code>ASTVariableDeclarator</code>; they are left-associative
     * with each other.
     * @return An <code>ASTVariableDeclarator</code>.
     */
    public ASTVariableDeclaratorList parseVariableDeclaratorList()
    {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier",
                COMMA,
                this::parseVariableDeclarator,
                ASTVariableDeclaratorList::new
        );
    }

    /**
     * Parses an <code>ASTVariableDeclarator</code>.
     * @return An <code>ASTVariableDeclarator</code>.
     */
    public ASTVariableDeclarator parseVariableDeclarator()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseIdentifier());
        ASTVariableDeclarator node = new ASTVariableDeclarator(loc, children);
        if (isCurr(ASSIGNMENT))
        {
            accept(ASSIGNMENT);
            children.add(parseVariableInitializer());
            node.setOperation(ASSIGNMENT);
        }
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableType</code>.
     * @return An <code>ASTLocalVariableType</code>.
     */
    public ASTLocalVariableType parseLocalVariableType()
    {
        Location loc = curr().getLocation();
        if (isCurr(AUTO))
        {
            accept(AUTO);
            ASTLocalVariableType node = new ASTLocalVariableType(loc, Collections.emptyList());
            node.setOperation(AUTO);
            return node;
        }
        else
        {
            ASTDataType dt = parseDataType();
            return new ASTLocalVariableType(loc, Collections.singletonList(dt));
        }
    }

    /**
     * Parses an <code>ASTStatement</code>.
     * @return An <code>ASTStatement</code>.
     */
    public ASTStatement parseStatement()
    {
        Location loc = curr().getLocation();
        switch(curr().getType())
        {
        case OPEN_BRACE:
            ASTBlock block = parseBlock();
            return new ASTStatement(loc, Arrays.asList(block));
        case RETURN:
            ASTReturnStatement retnStmt = parseReturnStatement();
            return new ASTStatement(loc, Arrays.asList(retnStmt));
        case THROW:
            ASTThrowStatement throwStmt = parseThrowStatement();
            return new ASTStatement(loc, Arrays.asList(throwStmt));
        case BREAK:
            ASTBreakStatement breakStmt = parseBreakStatement();
            return new ASTStatement(loc, Arrays.asList(breakStmt));
        case CONTINUE:
            ASTContinueStatement contStmt = parseContinueStatement();
            return new ASTStatement(loc, Arrays.asList(contStmt));
        case FALLTHROUGH:
            ASTFallthroughStatement ftStmt = parseFallthroughStatement();
            return new ASTStatement(loc, Arrays.asList(ftStmt));
        case ASSERT:
            ASTAssertStatement assertStmt = parseAssertStatement();
            return new ASTStatement(loc, Arrays.asList(assertStmt));
        case IF:
            ASTIfStatement ifStmt = parseIfStatement();
            return new ASTStatement(loc, Arrays.asList(ifStmt));
        case WHILE:
            ASTWhileStatement whileStmt = parseWhileStatement();
            return new ASTStatement(loc, Arrays.asList(whileStmt));
        case FOR:
            ASTForStatement forStmt = parseForStatement();
            return new ASTStatement(loc, Arrays.asList(forStmt));
        case DO:
            ASTDoStatement doStmt = parseDoStatement();
            return new ASTStatement(loc, Arrays.asList(doStmt));
        case SYNCHRONIZED:
            ASTSynchronizedStatement syncStmt = parseSynchronizedStatement();
            return new ASTStatement(loc, Arrays.asList(syncStmt));
        case TRY:
            ASTTryStatement tryStmt = parseTryStatement();
            return new ASTStatement(loc, Arrays.asList(tryStmt));
        case SWITCH:
            ASTSwitchStatement switchStmt = parseSwitchStatement();
            return new ASTStatement(loc, Arrays.asList(switchStmt));
        default:
            ASTExpressionStatement exprStmt = parseExpressionStatement();
            return new ASTStatement(loc, Arrays.asList(exprStmt));
        }
    }

    /**
     * Parses an <code>ASTStatement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatement</code>.
     */
    public ASTStatement parseStatement(ASTPrimary primary)
    {
        return new ASTStatement(primary.getLocation(), Arrays.asList(parseExpressionStatement(primary)));
    }

    /**
     * Parses an <code>ASTSwitchStatement</code>.
     * @return An <code>ASTSwitchStatement</code>.
     */
    public ASTSwitchStatement parseSwitchStatement()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (accept(SWITCH) == null)
        {
            throw new CompileException("Expected switch.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        children.add(parseExpression());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseSwitchBlock());
        ASTSwitchStatement node = new ASTSwitchStatement(loc, children);
        node.setOperation(SWITCH);
        return node;
    }

    /**
     * Parses an <code>ASTSwitchBlock</code>.
     * @return An <code>ASTSwitchBlock</code>.
     */
    public ASTSwitchBlock parseSwitchBlock()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseSwitchCases());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        ASTSwitchBlock node = new ASTSwitchBlock(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTSwitchCases</code>.
     * @return An <code>ASTSwitchCases</code>.
     */
    public ASTSwitchCases parseSwitchCases()
    {
        return parseMultiple(
                t -> test(t, DEFAULT, CASE),
                "Expected case label or default.",
                this::parseSwitchCase,
                ASTSwitchCases::new
        );
    }

    /**
     * Parses an <code>ASTSwitchCase</code>.
     * @return An <code>ASTSwitchCase</code>.
     */
    public ASTSwitchCase parseSwitchCase()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseSwitchLabel());
        if (isAcceptedOperator(Arrays.asList(CLOSE_BRACE, CASE, DEFAULT)) == null)
        {
            children.add(parseBlockStatements());
        }
        return new ASTSwitchCase(loc, children);
    }

    /**
     * Parses an <code>ASTSwitchLabel</code>.
     * @return An <code>ASTSwitchLabel</code>.
     */
    public ASTSwitchLabel parseSwitchLabel()
    {
        Location loc = curr().getLocation();
        if (isCurr(CASE))
        {
            accept(CASE);
            ASTSwitchLabel node = new ASTSwitchLabel(loc, Arrays.asList(parseSwitchValues()));
            if (accept(COLON) == null)
            {
                throw new CompileException("Expected colon.");
            }
            node.setOperation(CASE);
            return node;
        }
        else if (isCurr(DEFAULT))
        {
            accept(DEFAULT);
            ASTSwitchLabel node = new ASTSwitchLabel(loc, Collections.emptyList());
            if (accept(COLON) == null)
            {
                throw new CompileException("Expected colon.");
            }
            node.setOperation(DEFAULT);
            return node;
        }
        else
        {
            throw new CompileException("Expected case or default.");
        }
    }

    /**
     * Parses an <code>ASTSwitchValues</code>.
     * @return An <code>ASTSwitchValues</code>.
     */
    public ASTSwitchValues parseSwitchValues()
    {
        return parseList(
                StatementsParser::isExpression,
                "Expected an expression.",
                COMMA,
                this::parseSwitchValue,
                ASTSwitchValues::new);
    }

    /**
     * Parses an <code>ASTSwitchValue</code>.
     * @return An <code>ASTSwitchValue</code>.
     */
    public ASTSwitchValue parseSwitchValue()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        if (isCurr(IDENTIFIER) && (isNext(COLON) || isNext(COMMA)))
        {
            children.add(parseIdentifier());
        }
        else
        {
            children.add(parseExpressionNoIncrDecr());
        }
        return new ASTSwitchValue(loc, children);
    }

    /**
     * Parses an <code>ASTTryStatement</code>.
     * @return An <code>ASTTryStatement</code>.
     */
    public ASTTryStatement parseTryStatement()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(TRY) == null)
        {
            throw new CompileException("Expected try.");
        }
        if (isCurr(OPEN_PARENTHESIS))
        {
            children.add(parseResourceSpecification());
        }
        children.add(parseBlock());
        if (isCurr(CATCH))
        {
            children.add(parseCatches());
        }
        if (isCurr(FINALLY))
        {
            children.add(parseFinally());
        }
        if (children.size() <= 1)
        {
            throw new CompileException("Expected 'catch' and/or 'finally' block.");
        }
        ASTTryStatement node = new ASTTryStatement(loc, children);
        node.setOperation(TRY);
        return node;
    }

    /**
     * Parses an <code>ASTResourceSpecification</code>.
     * @return An <code>ASTResourceSpecification</code>.
     */
    public ASTResourceSpecification parseResourceSpecification()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        ASTResourceSpecification node = new ASTResourceSpecification(loc, Arrays.asList(parseResourceList()));
        if (isCurr(SEMICOLON))
        {
            accept(SEMICOLON);
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        return node;
    }

    /**
     * Parses an <code>ASTResourceList</code>.
     * @return An <code>ASTResourceList</code>.
     */
    public ASTResourceList parseResourceList()
    {
        return parseList(
                t -> isPrimary(t) || isAcceptedOperator(Arrays.asList(FINAL, CONST, CONSTANT, AUTO)) != null,
                "Expected an expression.",
                SEMICOLON,
                this::parseResource,
                ASTResourceList::new);
    }

    /**
     * Parses an <code>ASTResource</code>.
     * @return An <code>ASTResource</code>.
     */
    public ASTResource parseResource()
    {
        Location loc = curr().getLocation();
        ASTPrimary primary;
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case CONSTANT:
        case AUTO:
            return new ASTResource(loc, Arrays.asList(parseResourceDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER))
            {
                return new ASTResource(loc, Arrays.asList(parseResourceDeclaration(dt)));
            }
            else
            {
                // Convert to Expression Name.
                ASTExpressionName exprName = dt.convertToExpressionName();
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                primary = parsePrimary(exprName);
            }
            break;
        default:
            // May be an expression name or a field access.
            primary = parsePrimary();
        }

        // Must be an expression name or a field access.
        List<ASTNode> children = primary.getChildren();
        if (children.size() == 1)
        {
            ASTNode child = children.get(0);
            if (child instanceof ASTExpressionName || child instanceof ASTFieldAccess)
            {
                return new ASTResource(loc, Arrays.asList(child));
            }
        }
        throw new CompileException("Expected resource declaration or variable.");
    }

    /**
     * Parses an <code>ASTResourceDeclaration</code>.
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        ASTResourceDeclaration node = new ASTResourceDeclaration(loc, children);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST, CONSTANT)) != null)
        {
            children.add(parseVariableModifierList());
        }
        children.add(parseLocalVariableType());
        children.add(parseIdentifier());
        if (accept(ASSIGNMENT) == null)
        {
            throw new CompileException("Expected ':='.");
        }
        children.add(parseExpressionNoIncrDecr());
        node.setOperation(ASSIGNMENT);
        return node;
    }

    /**
     * Parses an <code>ASTResourceDeclaration</code>, given an already parsed
     * <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration(ASTDataType dt)
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        ASTResourceDeclaration node = new ASTResourceDeclaration(loc, children);
        children.add(new ASTLocalVariableType(loc, Collections.singletonList(dt)));
        children.add(parseIdentifier());
        if (accept(ASSIGNMENT) == null)
        {
            throw new CompileException("Expected ':='.");
        }
        children.add(parseExpressionNoIncrDecr());
        node.setOperation(ASSIGNMENT);
        return node;
    }

    /**
     * Parses an <code>ASTCatches</code>.
     * @return An <code>ASTCatches</code>.
     */
    public ASTCatches parseCatches()
    {
        return parseMultiple(
                t -> test(t, CATCH),
                "Expected catch clause.",
                this::parseCatchClause,
                ASTCatches::new
        );
    }

    /**
     * Parses an <code>ASTCatchClause</code>.
     * @return An <code>ASTCatchClause</code>.
     */
    public ASTCatchClause parseCatchClause()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (accept(CATCH) == null)
        {
            throw new CompileException("Expected catch.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('");
        }
        children.add(parseCatchFormalParameter());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'");
        }
        children.add(parseBlock());
        ASTCatchClause node = new ASTCatchClause(loc, children);
        node.setOperation(CATCH);
        return node;
    }

    /**
     * Parses an <code>ASTCatchFormalParameter</code>.
     * @return An <code>ASTCatchFormalParameter</code>.
     */
    public ASTCatchFormalParameter parseCatchFormalParameter()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST, CONSTANT)) != null)
        {
            children.add(parseVariableModifierList());
        }
        children.add(parseCatchType());
        children.add(parseIdentifier());
        return new ASTCatchFormalParameter(loc, children);
    }

    /**
     * Parses an <code>ASTCatchType</code>.
     * @return An <code>ASTCatchType</code>.
     */
    public ASTCatchType parseCatchType()
    {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected data type.",
                BITWISE_OR,
                this::parseDataType,
                ASTCatchType::new
        );
    }

    /**
     * Parses an <code>ASTFinally</code>.
     * @return An <code>ASTFinally</code>.
     */
    public ASTFinally parseFinally()
    {
        Location loc = curr().getLocation();
        if (accept(FINALLY) == null)
        {
            throw new CompileException("Expected finally.");
        }
        ASTBlock block = parseBlock();
        ASTFinally node = new ASTFinally(loc, Arrays.asList(block));
        node.setOperation(FINALLY);
        return node;
    }

    /**
     * Parses an <code>ASTForStatement</code>.
     * @return An <code>ASTForStatement</code>.
     */
    public ASTForStatement parseForStatement()
    {
        Location loc = curr().getLocation();
        ASTForStatement node;
        if (accept(FOR) == null)
        {
            System.out.println("Expected for.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            System.out.println("Expected '('.");
        }
        if (isCurr(SEMICOLON))
        {
            ASTBasicForStatement basicForStmt = parseBasicForStatement(loc);
            node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
        }
        else
        {
            ASTInit init = parseInit();
            if (isCurr(SEMICOLON))
            {
                ASTBasicForStatement basicForStmt = parseBasicForStatement(loc, init);
                node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
            }
            else if (isCurr(COLON))
            {
                ASTEnhancedForStatement enhForStmt = parseEnhancedForStatement(loc, init);
                node = new ASTForStatement(loc, Arrays.asList(enhForStmt));
            }
            else
            {
                throw new CompileException("Expected semicolon or colon.");
            }
        }
        node.setOperation(FOR);
        return node;
    }

    /**
     * Parses an <code>ASTEnhancedForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the colon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTEnhancedForStatement</code>.
     */
    public ASTEnhancedForStatement parseEnhancedForStatement(Location locFor, ASTInit init)
    {
        List<ASTNode> children = new ArrayList<>(3);

        List<ASTNode> initChildren = init.getChildren();
        ASTNode child = initChildren.get(0);
        if (child instanceof ASTLocalVariableDeclaration)
        {
            ASTLocalVariableDeclaration varDecl = (ASTLocalVariableDeclaration) child;
            List<ASTNode> varDeclChildren = varDecl.getChildren();
            ASTVariableDeclaratorList variables = (ASTVariableDeclaratorList) varDeclChildren.get(varDeclChildren.size() - 1);
            List<ASTNode> variablesChildren = variables.getChildren();
            if (variablesChildren.size() != 1)
            {
                throw new CompileException("Only one variable can be declared in an enhanced for loop.");
            }
            children.add(varDecl);
        }
        else
        {
            throw new CompileException("Enhanced for loop requires a variable declaration before the colon.");
        }

        if (accept(COLON) == null)
        {
            throw new CompileException("Expected colon.");
        }
        children.add(parseExpressionNoIncrDecr());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseStatement());
        ASTEnhancedForStatement node = new ASTEnhancedForStatement(locFor, children);
        node.setOperation(COLON);
        return node;
    }

    /**
     * Parses an <code>ASTBasicForStatement</code>, given that "for (" has
     * already been parsed, and no <code>ASTInit</code> was found before the
     * first semicolon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        if (!isCurr(SEMICOLON))
        {
            children.add(parseExpressionNoIncrDecr());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected second semicolon.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseStatementExpressionList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseStatement());
        ASTBasicForStatement node = new ASTBasicForStatement(locFor, children);
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTBasicForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the first semicolon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor, ASTInit init)
    {
        ASTBasicForStatement node = parseBasicForStatement(locFor);
        node.getChildren().add(0, init);
        return node;
    }

    /**
     * Parses an <code>ASTIfStatement</code>.
     * @return An <code>ASTIfStatement</code>.
     */
    public ASTIfStatement parseIfStatement()
    {
        Location loc = curr().getLocation();
        if (accept(IF) == null)
        {
            throw new CompileException("Expected if.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (isCurr(OPEN_BRACE))
        {
            accept(OPEN_BRACE);
            children.add(parseInit());
            if (accept(CLOSE_BRACE) == null)
            {
                throw new CompileException("Expected '}'.");
            }
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        children.add(parseExpressionNoIncrDecr());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseStatement());
        // Greedy else.
        if (isCurr(ELSE))
        {
            accept(ELSE);
            children.add(parseStatement());
        }
        ASTIfStatement node = new ASTIfStatement(loc, children);
        node.setOperation(IF);
        return node;
    }

    /**
     * Parses an <code>ASTWhileStatement</code>.
     * @return An <code>ASTWhileStatement</code>.
     */
    public ASTWhileStatement parseWhileStatement()
    {
        Location loc = curr().getLocation();
        if (accept(WHILE) == null)
        {
            throw new CompileException("Expected while.");
        }
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(OPEN_BRACE))
        {
            accept(OPEN_BRACE);
            children.add(parseInit());
            if (accept(CLOSE_BRACE) == null)
            {
                throw new CompileException("Expected '}'.");
            }
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        children.add(parseExpressionNoIncrDecr());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseStatement());
        ASTWhileStatement node = new ASTWhileStatement(loc, children);
        node.setOperation(WHILE);
        return node;
    }

    /**
     * Parses an <code>ASTDoStatement</code>.
     * @return An <code>ASTDoStatement</code>.
     */
    public ASTDoStatement parseDoStatement()
    {
        Location loc = curr().getLocation();
        if (accept(DO) == null)
        {
            throw new CompileException("Expected do.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseStatement());
        if (accept(WHILE) == null)
        {
            throw new CompileException("Expected while.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        children.add(parseExpressionNoIncrDecr());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        ASTDoStatement node = new ASTDoStatement(loc, children);
        node.setOperation(DO);
        return node;
    }

    /**
     * Parses an <code>ASTSynchronizedStatement</code>.
     * @return An <code>ASTSynchronizedStatement</code>.
     */
    public ASTSynchronizedStatement parseSynchronizedStatement()
    {
        Location loc = curr().getLocation();
        if (accept(SYNCHRONIZED) == null)
        {
            throw new CompileException("Expected synchronized.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        children.add(parseExpressionNoIncrDecr());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        children.add(parseBlock());
        ASTSynchronizedStatement node = new ASTSynchronizedStatement(loc, children);
        node.setOperation(SYNCHRONIZED);
        return node;
    }

    /**
     * Parses an <code>ASTThrowStatement</code>.
     * @return An <code>ASTThrowStatement</code>.
     */
    public ASTThrowStatement parseThrowStatement()
    {
        Location loc = curr().getLocation();
        if (accept(THROW) == null)
        {
            throw new CompileException("Expected throw.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        children.add(parseExpression());
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTThrowStatement node = new ASTThrowStatement(loc, children);
        node.setOperation(THROW);
        return node;
    }

    /**
     * Parses an <code>ASTReturnStatement</code>.
     * @return An <code>ASTReturnStatement</code>.
     */
    public ASTReturnStatement parseReturnStatement()
    {
        Location loc = curr().getLocation();
        if (accept(RETURN) == null)
        {
            throw new CompileException("Expected return.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(SEMICOLON))
        {
            children.add(parseExpression());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTReturnStatement node = new ASTReturnStatement(loc, children);
        node.setOperation(RETURN);
        return node;
    }

    /**
     * Parses an <code>ASTBreakStatement</code>.
     * @return An <code>ASTBreakStatement</code>.
     */
    public ASTBreakStatement parseBreakStatement()
    {
        Location loc = curr().getLocation();
        if (accept(BREAK) == null)
        {
            throw new CompileException("Expected break.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTBreakStatement node = new ASTBreakStatement(loc, Collections.emptyList());
        node.setOperation(BREAK);
        return node;
    }

    /**
     * Parses an <code>ASTContinueStatement</code>.
     * @return An <code>ASTContinueStatement</code>.
     */
    public ASTContinueStatement parseContinueStatement()
    {
        Location loc = curr().getLocation();
        if (accept(CONTINUE) == null)
        {
            throw new CompileException("Expected continue.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTContinueStatement node = new ASTContinueStatement(loc, Collections.emptyList());
        node.setOperation(CONTINUE);
        return node;
    }

    /**
     * Parses an <code>ASTFallthroughStatement</code>.
     * @return An <code>ASTFallthroughStatement</code>.
     */
    public ASTFallthroughStatement parseFallthroughStatement()
    {
        Location loc = curr().getLocation();
        if (accept(FALLTHROUGH) == null)
        {
            throw new CompileException("Expected fallthrough.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTFallthroughStatement node = new ASTFallthroughStatement(loc, Collections.emptyList());
        node.setOperation(FALLTHROUGH);
        return node;
    }

    /**
     * Parses an <code>ASTAssertStatement</code>.
     * @return An <code>ASTAssertStatement</code>.
     */
    public ASTAssertStatement parseAssertStatement()
    {
        Location loc = curr().getLocation();
        if (accept(ASSERT) == null)
        {
            throw new CompileException("Expected assert.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseExpression());
        if (isCurr(COLON))
        {
            accept(COLON);
            children.add(parseExpression());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTAssertStatement node = new ASTAssertStatement(loc, children);
        node.setOperation(ASSERT);
        return node;
    }

    /**
     * Parses an <code>ASTExpressionStatement</code>.
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement()
    {
        Location loc = curr().getLocation();

        ASTStatementExpression stmtExpr = parseStatementExpression();
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Semicolon expected.");
        }
        ASTExpressionStatement exprStmt = new ASTExpressionStatement(loc, Arrays.asList(stmtExpr));
        exprStmt.setOperation(SEMICOLON);
        return exprStmt;
    }

    /**
     * Parses an <code>ASTExpressionStatement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement(ASTPrimary primary)
    {
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Semicolon expected.");
        }
        ASTExpressionStatement exprStmt = new ASTExpressionStatement(primary.getLocation(), Arrays.asList(stmtExpr));
        exprStmt.setOperation(SEMICOLON);
        return exprStmt;
    }

    /**
     * Parses an <code>ASTInit</code>.
     * @return An <code>ASTInit</code>.
     */
    public ASTInit parseInit()
    {
        Location loc = curr().getLocation();
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case CONSTANT:
        case AUTO:
            return new ASTInit(loc, Arrays.asList(parseLocalVariableDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER))
            {
                return new ASTInit(loc, Arrays.asList(parseLocalVariableDeclaration(dt)));
            }
            else
            {
                // Convert to Expression Name.
                ASTExpressionName exprName = dt.convertToExpressionName();
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = parsePrimary(exprName);
                return new ASTInit(loc, Arrays.asList(parseStatementExpressionList(primary)));
            }
        default:
            return new ASTInit(loc, Arrays.asList(parseStatementExpressionList()));
        }
    }

    /**
     * Parses an <code>ASTStatementExpressionList</code>.
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList()
    {
        return parseList(
                t -> test(t, INCREMENT, DECREMENT) || isPrimary(t),
                "Expected a statement expression.",
                COMMA,
                this::parseStatementExpression,
                ASTStatementExpressionList::new);
    }

    /**
     * Parses an <code>ASTStatementExpressionList</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList(ASTPrimary primary)
    {
        Location loc = primary.getLocation();
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        ASTStatementExpressionList node;
        if (isCurr(COMMA))
        {
            accept(COMMA);
            ASTStatementExpressionList rest = parseStatementExpressionList();
            List<ASTNode> children = rest.getChildren();
            children.add(0, stmtExpr);
            node = new ASTStatementExpressionList(loc, rest.getChildren());
        }
        else
        {
            List<ASTNode> children = Arrays.asList(stmtExpr);
            node = new ASTStatementExpressionList(loc, children);
        }
        node.setOperation(COMMA);
        return node;
    }

    /**
     * Parses an <code>ASTStatementExpression</code>.
     * @return An <code>ASTStatementExpression</code>.
     */
    public ASTStatementExpression parseStatementExpression()
    {
        Location loc = curr().getLocation();
        if (isCurr(INCREMENT) || isCurr(DECREMENT))
        {
            ASTPrefixExpression prefixExpression = parsePrefixExpression();
            return new ASTStatementExpression(loc, Arrays.asList(prefixExpression));
        }
        if (isPrimary(curr()))
        {
            ASTPrimary primary = parsePrimary();
            return parseStatementExpression(primary);
        }
        else
        {
            throw new CompileException("Expected assignment, post/pre increment/decrement, or method invocation.");
        }
    }

    /**
     * Parses an <code>ASTStatementExpression</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpression</code>.
     */
    public ASTStatementExpression parseStatementExpression(ASTPrimary primary)
    {
        Location loc = primary.getLocation();
        if (isCurr(INCREMENT) || isCurr(DECREMENT))
        {
            return new ASTStatementExpression(loc, Arrays.asList(parsePostfixExpression(loc, primary.getLeftHandSide())));
        }
        else
        {
            // Primary may already be a method invocation or class instance creation expression.
            // If so, retrieve and use it.
            ASTNode child = primary.getChildren().get(0);
            if (child instanceof ASTMethodInvocation || child instanceof ASTClassInstanceCreationExpression)
            {
                return new ASTStatementExpression(loc, Arrays.asList(child));
            }
            else
            {
                // Assume assignment.
                return new ASTStatementExpression(loc, Arrays.asList(parseAssignment(loc, primary.getLeftHandSide())));
            }
        }
    }
}
