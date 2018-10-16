package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * The parser for the Spruce language.
 */
public class Parser
{
    private Scanner myScanner;

    /**
     * Constructs a <code>Parser</code> using a <code>Scanner</code>.
     * @param scanner A <code>Scanner</code>.
     */
    public Parser(Scanner scanner)
    {
        myScanner = scanner;
        advance();
    }

    /**
     * If the current token's type is the given type, then advance to the next
     * token, returning the original token.  If it doesn't match, don't advance,
     * and return <code>null</code>.
     * @param tokenType The expected token type.
     * @return The token that matches, or <code>null</code> on mismatch.
     */
    private Token accept(TokenType tokenType)
    {
        if (test(curr(), tokenType))
        {
            Token t = myScanner.getCurrToken();
            advance();
            return t;
        }
        return null;
    }

    /**
     * Returns the current <code>Token</code> from the <code>Scanner</code>.
     * @return The current <code>Token</code> from the <code>Scanner</code>.
     */
    private Token curr()
    {
        return myScanner.getCurrToken();
    }

    /**
     * Returns the next <code>Token</code> from the <code>Scanner</code>.
     * @return The next <code>Token</code> from the <code>Scanner</code>.
     */
    private Token peek()
    {
        return myScanner.peekNextToken();
    }

    /**
     * Simple test if the given token's type matches the given type.
     * @param t The <code>Token</code>.
     * @param tokenType The expected token type.
     * @return Whether the given token's type matches the given type.
     */
    private static boolean test(Token t, TokenType tokenType)
    {
        return t.getType() == tokenType;
    }

    /**
     * Advance the <code>Scanner</code> to the next token.
     */
    private void advance()
    {
        myScanner.next();
    }

    /**
     * Determines whether the given token is a literal.
     * @param t A <code>Token</code>.
     * @return Whether the give token is a literal.
     */
    private static boolean isLiteral(Token t)
    {
        switch(t.getType())
        {
        case TRUE:
        case FALSE:
        case NULL:
        case INT_LITERAL:
        case FLOATING_POINT_LITERAL:
        case STRING_LITERAL:
        case CHARACTER_LITERAL:
            return true;
        default:
            return false;
        }
    }

    /**
     * Determines whether the given token can start an expression.
     * @param t A <code>Token</code>.
     * @return Whether the given token can start an expression.
     */
    private static boolean isExpression(Token t)
    {
        return (test(t, INCREMENT) || test(t, DECREMENT) || isPrimary(t));
    }

    /**
     * Determines whether the given token can start a type argument.
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a type argument.
     */
    private static boolean isTypeArgument(Token t)
    {
        return (test(t, QUESTION_MARK) || test(t, IDENTIFIER));
    }

    /**
     * <p>Determines whether the given token can start a Primary.</p>
     * <ul>
     *     <li>-</li>
     *     <li>~</li>
     *     <li>!</li>
     *     <li>identifier</li>
     *     <li><code>this</code></li>
     *     <li><code>super</code></li>
     *     <li>(</li>
     * </ul>
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a Primary.
     */
    private static boolean isPrimary(Token t)
    {
        if (isLiteral(t))
        {
            return true;
        }
        switch(t.getType())
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

    //**************************************
    // STATEMENTS
    //**************************************

    /**
     * Parses an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock parseBlock()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        ASTBlock node = new ASTBlock(loc, children);
        node.setOperation(OPEN_BRACE);
        if (!test(curr(), CLOSE_BRACE))
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>();
        ASTBlockStatements node = new ASTBlockStatements(loc, children);
        while (!test(curr(), CLOSE_BRACE))
        {
            children.add(parseBlockStatement());
        }
        return node;
    }

    /**
     * Parses an <code>ASTBlockStatement</code>.
     * @return An <code>ASTBlockStatement</code>.
     */
    public ASTBlockStatement parseBlockStatement()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case AUTO:
            return new ASTBlockStatement(loc, Arrays.asList(parseLocalVariableDeclarationStatement()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (test(curr(), IDENTIFIER))
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST)) != null)
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
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST)) == null)
        {
            throw new CompileException("Expected final or const.");
        }
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>();
        children.add(parseVariableModifier());
        ASTVariableModifierList node = new ASTVariableModifierList(loc, children);
        while (isAcceptedOperator(Arrays.asList(FINAL, CONST)) != null)
        {
            children.add(parseVariableModifier());
        }
        return node;
    }

    /**
     * Parses an <code>ASTVariableModifier</code>.
     * @return An <code>ASTVariableModifier</code>.
     */
    public ASTVariableModifier parseVariableModifier()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        TokenType operation = isAcceptedOperator(Arrays.asList(FINAL, CONST));
        if (operation == null)
        {
            throw new CompileException("Expected final or const.");
        }
        accept(operation);
        ASTVariableModifier node = new ASTVariableModifier(loc, Collections.emptyList());
        node.setOperation(operation);
        return node;
    }

    /**
     * Parses an <code>ASTVariableDeclarator</code>; they are left-associative
     * with each other.
     * @return An <code>ASTVariableDeclarator</code>.
     */
    public ASTVariableDeclaratorList parseVariableDeclaratorList()
    {
        return parseMultiple(
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseIdentifier());
        ASTVariableDeclarator node = new ASTVariableDeclarator(loc, children);
        if (test(curr(), ASSIGNMENT))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), AUTO))
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
        Location loc = myScanner.getCurrToken().getLocation();
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
     * Parses an <code>ASTTryStatement</code>.
     * @return An <code>ASTTryStatement</code>.
     */
    public ASTTryStatement parseTryStatement()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(TRY) == null)
        {
            throw new CompileException("Expected try.");
        }
        if (test(curr(), OPEN_PARENTHESIS))
        {
            children.add(parseResourceSpecification());
        }
        children.add(parseBlock());
        if (test(curr(), CATCH))
        {
            children.add(parseCatches());
        }
        if (test(curr(), FINALLY))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        ASTResourceSpecification node = new ASTResourceSpecification(loc, Arrays.asList(parseResourceList()));
        if (test(curr(), SEMICOLON))
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
        return parseMultiple(
                t -> isPrimary(t) || isAcceptedOperator(Arrays.asList(FINAL, CONST, AUTO)) != null,
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
        Location loc = myScanner.getCurrToken().getLocation();
        ASTPrimary primary;
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case AUTO:
            return new ASTResource(loc, Arrays.asList(parseResourceDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (test(curr(), IDENTIFIER))
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        ASTResourceDeclaration node = new ASTResourceDeclaration(loc, children);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST)) != null)
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>();
        ASTCatches node = new ASTCatches(loc, children);
        while (test(curr(), CATCH))
        {
            children.add(parseCatchClause());
        }
        return node;
    }

    /**
     * Parses an <code>ASTCatchClause</code>.
     * @return An <code>ASTCatchClause</code>.
     */
    public ASTCatchClause parseCatchClause()
    {
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(FINAL, CONST)) != null)
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
        return parseMultiple(
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        ASTForStatement node;
        if (accept(FOR) == null)
        {
            System.out.println("Expected for.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            System.out.println("Expected '('.");
        }
        if (test(curr(), SEMICOLON))
        {
            ASTBasicForStatement basicForStmt = parseBasicForStatement(loc);
            node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
        }
        else
        {
            ASTInit init = parseInit();
            if (test(curr(), SEMICOLON))
            {
                ASTBasicForStatement basicForStmt = parseBasicForStatement(loc, init);
                node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
            }
            else if (test(curr(), COLON))
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
        if (!test(curr(), SEMICOLON))
        {
            children.add(parseExpressionNoIncrDecr());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected second semicolon.");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(IF) == null)
        {
            throw new CompileException("Expected if.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (test(curr(), OPEN_BRACE))
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
        if (test(curr(), ELSE))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(WHILE) == null)
        {
            throw new CompileException("Expected while.");
        }
        List<ASTNode> children = new ArrayList<>(3);
        if (test(curr(), OPEN_BRACE))
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(RETURN) == null)
        {
            throw new CompileException("Expected return.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!test(curr(), SEMICOLON))
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(ASSERT) == null)
        {
            throw new CompileException("Expected assert.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseExpression());
        if (test(curr(), COLON))
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
        Location loc = myScanner.getCurrToken().getLocation();

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
        Location loc = myScanner.getCurrToken().getLocation();
        switch(curr().getType())
        {
        case FINAL:
        case CONST:
        case AUTO:
            return new ASTInit(loc, Arrays.asList(parseLocalVariableDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = parseDataType();
            // DataType varName ...
            if (test(curr(), IDENTIFIER))
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
        return parseMultiple(
                t -> test(t, INCREMENT) || test(t, DECREMENT) || isPrimary(t),
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
        if (test(curr(), COMMA))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), INCREMENT) || test(curr(), DECREMENT))
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
        if (test(curr(), INCREMENT) || test(curr(), DECREMENT))
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

    //**************************************
    // TYPES, VALUES, AND VARIABLES
    //**************************************

    /**
     * Parses an <code>ASTIntersectionType</code>; they are left-
     * associative with each other.
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType parseIntersectionType()
    {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(BITWISE_AND),
                this::parseDataType,
                ASTIntersectionType::new
        );
    }

    /**
     * Parses an <code>ASTTypeArguments</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.
     * @return An <code>ASTTypeArguments</code>.
     */
    public ASTTypeArguments parseTypeArguments()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        myScanner.setInTypeContext(true);
        if (accept(LESS_THAN) != null)
        {
            ASTTypeArgumentList typeArgList = parseTypeArgumentList();
            if (accept(GREATER_THAN) == null)
            {
                throw new CompileException("Expected \">\".");
            }
            myScanner.setInTypeContext(false);
            return new ASTTypeArguments(loc, Arrays.asList(typeArgList));
        }
        else
        {
            throw new CompileException("Expected \"<\".");
        }
    }

    // TODO: TypeParameter, TypeParameters, TypeParameterList

    /**
     * Parses an <code>ASTTypeArgumentList</code>.
     * @return An <code>ASTTypeArgumentList</code>.
     */
    public ASTTypeArgumentList parseTypeArgumentList()
    {
        return parseMultiple(
                Parser::isTypeArgument,
                "Expected a type argument.",
                COMMA,
                this::parseTypeArgument,
                ASTTypeArgumentList::new);
    }

    /**
     * Parses an <code>ASTTypeArgument</code>.
     * @return An <code>ASTTypeArgument</code>.
     */
    public ASTTypeArgument parseTypeArgument()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), QUESTION_MARK))
        {
            ASTWildcard wildcard = parseWildcard();
            return new ASTTypeArgument(loc, Arrays.asList(wildcard));
        }
        else if (test(curr(), IDENTIFIER))
        {
            ASTDataType dt = parseDataType();
            return new ASTTypeArgument(loc, Arrays.asList(dt));
        }
        else
        {
            throw new CompileException("Expected wildcard or data type.");
        }
    }

    /**
     * Parses an <code>ASTWildcard</code>.
     * @return An <code>ASTWildcard</code>.
     */
    public ASTWildcard parseWildcard()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(QUESTION_MARK) == null)
        {
            throw new CompileException("Wildcard expected.");
        }
        ASTWildcard node = new ASTWildcard(loc, new ArrayList<>(1));
        if (test(curr(), SUBTYPE) || test(curr(), SUPERTYPE))
        {
            ASTWildcardBounds wb = parseWildcardBounds();
            node.getChildren().add(wb);
        }
        return node;
    }

    /**
     * Parses an <code>ASTWildcardBounds</code>.
     * @return An <code>ASTWildcardBounds</code>.
     */
    public ASTWildcardBounds parseWildcardBounds()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        TokenType curr;
        if (test(curr(), SUBTYPE))
        {
            accept(SUBTYPE);
            curr = SUBTYPE;
        }
        else if (test(curr(), SUPERTYPE))
        {
            accept(SUPERTYPE);
            curr = SUPERTYPE;
        }
        else
        {
            throw new CompileException("Expected \"<:\" or \":>\".");
        }
        ASTWildcardBounds node = new ASTWildcardBounds(loc, Arrays.asList(parseDataType()));
        node.setOperation(curr);
        return node;
    }

    //**************************************
    // EXPRESSIONS
    //**************************************

    /**
     * Parses an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression parseExpression()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), INCREMENT) || test(curr(), DECREMENT))
        {
            ASTPrefixExpression prefixExpr = parsePrefixExpression();
            return new ASTExpression(loc, Arrays.asList(prefixExpr));
        }
        else if (isPrimary(curr()))
        {
            ASTExpressionNoIncrDecr exprNoIncrDecr = parseExpressionNoIncrDecr();
            if (test(curr(), INCREMENT) || test(curr(), DECREMENT))
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
        Location loc = myScanner.getCurrToken().getLocation();
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
            Token curr = myScanner.getCurrToken();
            Location loc = curr.getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            ASTConditionalExpression condExpr = parseConditionalExpression();

            TokenType currToken = myScanner.getCurrToken().getType();
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
        TokenType currToken = myScanner.getCurrToken().getType();
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
        if (test(curr(), INCREMENT))
        {
            Location loc = myScanner.getCurrToken().getLocation();
            accept(INCREMENT);
            ASTLeftHandSide lhs = parseLeftHandSide();
            return new ASTPrefixExpression(loc, lhs, INCREMENT);
        }
        else if (test(curr(), DECREMENT))
        {
            Location loc = myScanner.getCurrToken().getLocation();
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
        if (test(curr(), INCREMENT))
        {
            accept(INCREMENT);
            return new ASTPostfixExpression(loc, lhs, INCREMENT);
        }
        else if (test(curr(), DECREMENT))
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
            Location loc = myScanner.getCurrToken().getLocation();
            ASTPrimary primary = parsePrimary();
            if (test(curr(), OPEN_BRACKET))
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
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(3);
            children.add(parseLogicalOrExpression());
            ASTConditionalExpression node = new ASTConditionalExpression(loc, children);

            if (test(curr(), QUESTION_MARK))
            {
                accept(QUESTION_MARK);
                children.add(parseLogicalOrExpression()); // parseExpressionNoIncrDecr()
                node.setOperation(QUESTION_MARK);

                if (test(curr(), COLON))
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseCompareExpression());
            ASTRelationalExpression node = new ASTRelationalExpression(loc, children);

            TokenType curr;
            while ( (curr = isAcceptedOperator(Arrays.asList(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, EQUAL, NOT_EQUAL, INSTANCEOF, IS)) ) != null)
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
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseBitwiseOrExpression());
            ASTCompareExpression node = new ASTCompareExpression(loc, children);

            if (test(curr(), COMPARISON))
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
                Parser::isPrimary,
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
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseUnaryExpression());
            ASTCastExpression node = new ASTCastExpression(loc, children);

            while (test(curr(), AS))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), LOGICAL_COMPLEMENT))
        {
            accept(LOGICAL_COMPLEMENT);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), LOGICAL_COMPLEMENT);
        }
        else if (test(curr(), BITWISE_COMPLEMENT))
        {
            accept(BITWISE_COMPLEMENT);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), BITWISE_COMPLEMENT);
        }
        else if (test(curr(), MINUS))
        {
            accept(MINUS);
            return new ASTUnaryExpression(loc, parseUnaryExpression(), MINUS);
        }
        else
        {
            return new ASTUnaryExpression(loc, parsePrimary());
        }
    }

    //**************************************
    // NAMES AND ACCESS
    //**************************************

    /**
     * Parses an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList parseArgumentList()
    {
        if (isExpression(curr()))
        {
            return parseMultiple(
                    Parser::isExpression,
                    "Expected an expression.",
                    COMMA,
                    this::parseExpression,
                    ASTArgumentList::new);
        }
        else
        {
            return new ASTArgumentList(myScanner.getCurrToken().getLocation(), Arrays.asList());
        }
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
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        children.add(sooper);
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(5);
        children.add(exprName.convertToTypeName());
        children.add(sooper);
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
     * <em>MethodInvocation: ExpressionName . Identifier ( [ArgumentList] )</em>
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
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        while(test(curr(), OPEN_BRACKET))
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
     * Parses an <code>ASTPrimary</code>.
     * @return An <code>ASTPrimary</code>.
     */
    public ASTPrimary parsePrimary()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        ASTPrimary primary;
        if (isLiteral(curr()))
        {
            ASTLiteral literal = parseLiteral();
            primary = new ASTPrimary(loc, Arrays.asList(literal));
        }
        else if (test(curr(), IDENTIFIER))
        {
            if (test(peek(), OPEN_PARENTHESIS))
            {
                // id(args)
                ASTIdentifier methodName = parseIdentifier();
                primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            }
            else
            {
                ASTExpressionName expressionName = parseExpressionName();
                primary = parsePrimary(expressionName);
            }
        }
        else if (test(curr(), THIS))
        {
            ASTThis keywordThis = parseThis();
            primary = new ASTPrimary(loc, Arrays.asList(keywordThis));
        }
        else if (test(curr(), SUPER))
        {
            ASTSuper sooper = parseSuper();
            if (accept(DOT) == null)
            {
                throw new CompileException("Expected '.'.");
            }
            if (test(curr(), LESS_THAN) || (test(curr(), IDENTIFIER) && test(peek(), OPEN_PARENTHESIS)))
            {
                primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(sooper)));
            }
            else
            {
                // Field access
                primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(sooper)));
            }
        }
        else if (test(curr(), OPEN_PARENTHESIS))
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
        else if (test(curr(), NEW))
        {
            if (test(peek(), LESS_THAN))
            {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression();
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            else if (test(peek(), IDENTIFIER))
            {
                accept(NEW);
                ASTTypeToInstantiate tti = parseTypeToInstantiate();
                if (test(curr(), OPEN_BRACKET) || test(curr(), OPEN_CLOSE_BRACKET))
                {
                    primary = new ASTPrimary(loc, Arrays.asList(parseArrayCreationExpression(tti)));
                }
                else if (test(curr(), OPEN_PARENTHESIS))
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
        if (children.size() == 1 && test(curr(), OPEN_PARENTHESIS))
        {
            // id( -> method invocation
            ASTIdentifier methodName = (ASTIdentifier) children.get(0);
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(methodName)));
            return parsePrimary(primary);
        }

        if ((test(curr(), DOT) && test(peek(), CLASS)) || test(curr(), OPEN_CLOSE_BRACKET))
        {
            // exprName.class OR exprName[]
            // Get the class literal and get out.
            ASTTypeName tn = exprName.convertToTypeName();
            return new ASTPrimary(loc, Arrays.asList(parseClassLiteral(tn)));
        }
        else if (test(curr(), DOT) && test(peek(), THIS))
        {
            // TypeName.this
            ASTTypeName tn = exprName.convertToTypeName();
            accept(DOT);
            primary = new ASTPrimary(loc, Arrays.asList(tn, parseThis()));
            primary.setOperation(DOT);
        }
        else if (test(curr(), DOT) && test(peek(), LESS_THAN))
        {
            // ExprName.<TypeArgs>methodName(args)
            primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocation(exprName)));
        }
        else if (test(curr(), DOT) && test(peek(), SUPER))
        {
            // TypeName.super.<TypeArgs>methodName(args)
            if (accept(DOT) == null)
            {
                throw new CompileException("Expected '.'.");
            }
            ASTSuper sooper = parseSuper();
            if (accept(DOT) == null)
            {
                throw new CompileException("Expected '.'.");
            }
            if (test(curr(), LESS_THAN) || (test(curr(), IDENTIFIER) && test(peek(), OPEN_PARENTHESIS)))
            {
                primary = new ASTPrimary(loc, Arrays.asList(parseMethodInvocationSuper(exprName, sooper)));
            }
            else
            {
                primary = new ASTPrimary(loc, Arrays.asList(parseFieldAccessSuper(exprName, sooper)));
            }
        }
        else if (test(curr(), OPEN_PARENTHESIS))
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
        // Qualified Class Instance Creation, Element Access, Field Access, and
        // Method Invocations may chain up.
        // E.g. new Foo()[i].method1()[j].method2().new Bar()
        while (test(curr(), OPEN_BRACKET) ||
                (test(curr(), DOT) && test(peek(), NEW)) ||
                (test(curr(), DOT) && test(peek(), LESS_THAN)) ||
                (test(curr(), DOT) && test(peek(), IDENTIFIER))
                )
        {
            if (test(curr(), DOT) && test(peek(), NEW))
            {
                ASTClassInstanceCreationExpression cice = parseClassInstanceCreationExpression(primary);
                primary = new ASTPrimary(loc, Arrays.asList(cice));
            }
            if (test(curr(), DOT) && (test(peek(), LESS_THAN) || test(peek(), IDENTIFIER)))
            {
                accept(DOT);
                if (test(curr(), LESS_THAN) || (test(curr(), IDENTIFIER) && test(peek(), OPEN_PARENTHESIS)))
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
            if (test(curr(), OPEN_BRACKET))
            {
                ASTElementAccess ea = parseElementAccess(loc, primary);
                primary = new ASTPrimary(loc, Arrays.asList(ea));
            }
        }
        return primary;
    }

    /**
     * Parses an <code>ASTClassInstanceCreationExpression</code>.
     * @return An <code>ASTClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression parseClassInstanceCreationExpression()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (test(curr(), NEW))
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
        if (test(curr(), DOT) && test(peek(), NEW))
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(NEW) == null)
        {
            throw new CompileException("Expected new.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        children.add(parseTypeToInstantiate());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected \"(\".");
        }
        if (!test(curr(), CLOSE_PARENTHESIS))
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
        if (!test(curr(), CLOSE_PARENTHESIS))
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
     * Parses an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate parseTypeToInstantiate()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseTypeName());
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArgumentsOrDiamond());
        }
        return new ASTTypeToInstantiate(loc, children);
    }

    /**
     * Parses an <code>ASTTypeArgumentsOrDiamond</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.
     * @return An <code>ASTTypeArgumentsOrDiamond</code>.
     */
    public ASTTypeArgumentsOrDiamond parseTypeArgumentsOrDiamond()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        myScanner.setInTypeContext(true);
        ASTTypeArgumentsOrDiamond node;
        if (test(curr(), LESS_THAN) && test(peek(), GREATER_THAN))
        {
            accept(LESS_THAN);
            accept(GREATER_THAN);
            node = new ASTTypeArgumentsOrDiamond(loc, Collections.emptyList());
            node.setOperation(LESS_THAN);
        }
        else
        {
            ASTTypeArguments ta = parseTypeArguments();
            node = new ASTTypeArgumentsOrDiamond(loc, Collections.singletonList(ta));
        }
        return node;
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
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(alreadyParsed);
        boolean dimExprsPresent = false;
        if (test(curr(), OPEN_BRACKET))
        {
            children.add(parseDimExprs());
            dimExprsPresent = true;
        }
        if (test(curr(), OPEN_CLOSE_BRACKET))
        {
            children.add(parseDims());
        }
        if (children.size() == 0)
        {
            throw new CompileException("Expected \"[\".");
        }
        if (test(curr(), OPEN_BRACE))
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
        if (test(curr(), OPEN_BRACKET))
        {
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(parseDimExpr());
            ASTDimExprs node = new ASTDimExprs(loc, children);

            while (test(curr(), OPEN_BRACKET))
            {
                //children = new ArrayList<>(2);
                //children.add(node);
                children.add(parseDimExpr());
                //node = new ASTDimExprs(loc, children);
            }
            return node;
        }
        else
        {
            throw new CompileException("Expected \"[\".");
        }
    }

    /**
     * Parses an <code>ASTDimExpr</code>.
     * @return An <code>ASTDimExpr</code>.
     */
    public ASTDimExpr parseDimExpr()
    {
        Location loc = myScanner.getCurrToken().getLocation();
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected \"{\".");
        }
        ASTArrayInitializer node;
        if (isPrimary(curr()) || test(curr(), OPEN_BRACE))
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
        return parseMultiple(
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
        Location loc = myScanner.getCurrToken().getLocation();
        if (isPrimary(curr()))
        {
            ASTExpressionNoIncrDecr exprNoIncrDecr = parseExpressionNoIncrDecr();
            return new ASTVariableInitializer(loc, Arrays.asList(exprNoIncrDecr));
        }
        else if (test(curr(), OPEN_BRACE))
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
     * Parses an <code>ASTClassLiteral</code>, given an already parsed
     * <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTClassLiteral</code>.
     */
    public ASTClassLiteral parseClassLiteral(ASTTypeName tn)
    {
        Location loc = tn.getLocation();

        ASTDims dims = null;
        List<ASTNode> children = null;
        while (test(curr(), OPEN_CLOSE_BRACKET))
        {
            Location dimsLoc = myScanner.getCurrToken().getLocation();
            accept(OPEN_CLOSE_BRACKET);
            if (dims == null)
            {
                children = new ArrayList<>(1);
                dims = new ASTDims(dimsLoc, children);
                dims.setOperation(OPEN_CLOSE_BRACKET);
            }
            else
            {
                ASTDims child = new ASTDims(dimsLoc, new ArrayList<>(1));
                child.setOperation(OPEN_CLOSE_BRACKET);
                children.add(child);
                children = child.getChildren();
            }
        }

        if (accept(DOT) == null || accept(CLASS) == null)
        {
            throw new CompileException("Expected .class");
        }

        children = new ArrayList<>(2);
        children.add(tn);
        if (dims != null)
        {
            children.add(dims);
        }
        ASTClassLiteral node = new ASTClassLiteral(loc, children);
        node.setOperation(CLASS);
        return node;
    }

    /**
     * Parses an <code>ASTClassLiteral</code>.
     * @return An <code>ASTClassLiteral</code>.
     */
    public ASTClassLiteral parseClassLiteral()
    {
        ASTTypeName tn = parseTypeName();
        return parseClassLiteral(tn);
    }

    /**
     * Helper method to avoid duplicating code for parsing binary expressions
     * that are left associative.
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param acceptedTokens A <code>List</code> of accepted <code>TokenTypes</code>
     *      that can serve as operators.
     * @param childParser Parses and returns the child node (operand).
     * @param nodeSupplier Creates the desired node, given a <code>Location</code>
     *      and a <code>List</code> of child nodes.
     * @param <T> The type of node to parse and create.
     * @return A node of the desired type with all children parsed in a left-
     *      associative structure.
     */
    private <T extends ASTParentNode> T parseBinaryExpressionLeftAssociative(Predicate<Token> isOnInitialToken, String initialErrorMessage,
                                                                             List<TokenType> acceptedTokens,
                                                                             Supplier<? extends ASTNode> childParser,
                                                                             BiFunction<Location, List<ASTNode>, T> nodeSupplier)
    {
        if (isOnInitialToken.test(curr()))
        {
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>(2);
            children.add(childParser.get());
            T node = nodeSupplier.apply(loc, children);

            TokenType curr;
            while ( (curr = isAcceptedOperator(acceptedTokens) ) != null && isOnInitialToken.test(peek()))
            {
                accept(curr);
                children = new ArrayList<>(2);
                children.add(node);
                children.add(childParser.get());
                node = nodeSupplier.apply(loc, children);
                node.setOperation(curr);
            }
            return node;
        }
        else
        {
            throw new CompileException(initialErrorMessage);
        }
    }

    /**
     * Helper method to avoid duplicating code for parsing list-like expressions.
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param acceptedToken The accepted <code>TokenTypes</code>
     *      that can serve as the separator.
     * @param childParser Parses and returns the child node (list item).
     * @param nodeSupplier Creates the desired node, given a <code>Location</code>
     *      and a <code>List</code> of child nodes.
     * @param <T> The type of node to parse and create.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    private <T extends ASTParentNode> T parseMultiple(Predicate<Token> isOnInitialToken, String initialErrorMessage,
                                                      TokenType acceptedToken,
                                                      Supplier<? extends ASTNode> childParser,
                                                      BiFunction<Location, List<ASTNode>, T> nodeSupplier)
    {
        if (isOnInitialToken.test(curr()))
        {
            Location loc = myScanner.getCurrToken().getLocation();
            List<ASTNode> children = new ArrayList<>();
            children.add(childParser.get());
            T node = nodeSupplier.apply(loc, children);
            node.setOperation(acceptedToken);

            while (test(curr(), acceptedToken) && isOnInitialToken.test(peek()))
            {
                accept(acceptedToken);
                children.add(childParser.get());
            }
            return node;
        }
        else
        {
            throw new CompileException(initialErrorMessage);
        }
    }

    /**
     * Helper method to determine if the next token, expected to be an operator,
     * is one of the expected token types or expected keywords.
     * @param acceptedTokens A <code>List</code> of acceptable <code>TokenType</code>s.
     * @return The <code>TokenType</code> that matches, or <code>null</code> if none match.
     */
    private TokenType isAcceptedOperator(List<TokenType> acceptedTokens)
    {
        TokenType type = myScanner.getCurrToken().getType();
        if (acceptedTokens.indexOf(type) >= 0)
        {
            return type;
        }
        return null;
    }

    //**************************************
    // NAMES
    //**************************************

    /**
     * Parses an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType parseDataType()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        ASTDataTypeNoArray dtna = parseDataTypeNoArray();
        if (test(curr(), OPEN_CLOSE_BRACKET))
        {
            ASTDims dims = parseDims();
            ASTArrayType arrayType = new ASTArrayType(loc, Arrays.asList(dtna, dims));
            return new ASTDataType(loc, Arrays.asList(arrayType));
        }
        else
        {
            return new ASTDataType(loc, Arrays.asList(dtna));
        }
    }

    /**
     * Parses an <code>ASTArrayType</code>.
     * @return An <code>ASTArrayType</code>.
     */
    public ASTArrayType parseArrayType()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        if (test(curr(), IDENTIFIER))
        {
            ASTDataTypeNoArray dtna = parseDataTypeNoArray();
            if (test(curr(), OPEN_CLOSE_BRACKET))
            {
                ASTDims dims = parseDims();
                return new ASTArrayType(loc, Arrays.asList(dtna, dims));
            }
            else
            {
                throw new CompileException("Expected [].");
            }
        }
        else
        {
            throw new CompileException("Identifier expected.");
        }
    }

    /**
     * Parses an <code>ASTDims</code>.
     * @return An <code>ASTDims</code>.
     */
    public ASTDims parseDims()
    {
        if (!test(curr(), OPEN_CLOSE_BRACKET))
        {
            throw new CompileException("Expected [].");
        }
        ASTDims node = null;
        List<ASTNode> children = null;
        while (test(curr(), OPEN_CLOSE_BRACKET))
        {
            Location loc = myScanner.getCurrToken().getLocation();
            accept(OPEN_CLOSE_BRACKET);
            if (node == null)
            {
                children = new ArrayList<>(1);
                node = new ASTDims(loc, children);
                node.setOperation(OPEN_CLOSE_BRACKET);
            }
            else
            {
                ASTDims dims = new ASTDims(loc, new ArrayList<>(1));
                dims.setOperation(OPEN_CLOSE_BRACKET);
                children.add(dims);
                children = dims.getChildren();
            }
        }
        return node;
    }

    /**
     * Parses an <code>ASTDataTypeNoArray</code>; they are left-associative
     * with each other.
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray parseDataTypeNoArray()
    {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                Collections.singletonList(DOT),
                this::parseSimpleType,
                ASTDataTypeNoArray::new
        );
    }

    /**
     * Parses an <code>ASTSimpleType</code>.
     * @return An <code>ASTSimpleType</code>.
     */
    public ASTSimpleType parseSimpleType()
    {
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseIdentifier());
        if (test(curr(), LESS_THAN))
        {
            children.add(parseTypeArguments());
        }
        return new ASTSimpleType(loc, children);
    }

    /**
     * Parses an <code>ASTTypeName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName parseTypeName()
    {
        ASTPackageOrTypeName ptName = parsePackageOrTypeName();
        ASTTypeName node = new ASTTypeName(ptName.getLocation(), ptName.getChildren());
        node.setOperation(ptName.getOperation());
        return node;
    }

    /**
     * Parses an <code>ASTPackageOrTypeName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTPackageOrTypeName</code>.
     */
    public ASTPackageOrTypeName parsePackageOrTypeName()
    {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(DOT),
                this::parseIdentifier,
                ASTPackageOrTypeName::new
        );
    }

    /**
     * Parses an <code>ASTExpressionName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTExpressionName</code>.
     */
    public ASTExpressionName parseExpressionName()
    {
        ASTAmbiguousName ambName = parseAmbiguousName();
        ASTExpressionName node = new ASTExpressionName(ambName.getLocation(), ambName.getChildren());
        node.setOperation(ambName.getOperation());
        return node;
    }

    /**
     * Parses an <code>ASTAmbiguousName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTAmbiguousName</code>.
     */
    public ASTAmbiguousName parseAmbiguousName()
    {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(DOT),
                this::parseIdentifier,
                ASTAmbiguousName::new
        );
    }

    /**
     * Parses an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier parseIdentifier()
    {
        Token t;
        if ((t = accept(IDENTIFIER)) != null)
        {
            return new ASTIdentifier(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected an identifier.");
        }
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

    //**************************************
    // LITERALS
    //**************************************

    /**
     * Parses an <code>ASTLiteral</code>.
     * @return An <code>ASTLiteral</code>.
     */
    public ASTLiteral parseLiteral()
    {
        Token curr = myScanner.getCurrToken();
        if (test(curr(), INT_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseIntegerLiteral());
        }
        else if (test(curr(), FLOATING_POINT_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseFloatingPointLiteral());
        }
        else if (test(curr(), STRING_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseStringLiteral());
        }
        else if (test(curr(), CHARACTER_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseCharacterLiteral());
        }
        else if (test(curr(), TRUE))
        {
            return new ASTLiteral(curr.getLocation(), parseBooleanLiteral());
        }
        else if (test(curr(), FALSE))
        {
            return new ASTLiteral(curr.getLocation(), parseBooleanLiteral());
        }
        else if (test(curr(), NULL))
        {
            return new ASTLiteral(curr.getLocation(), parseNullLiteral());
        }
        else
        {
            throw new CompileException("Expected a literal.");
        }
    }

    /**
     * Parses an <code>ASTIntegerLiteral</code>.
     * @return An <code>ASTIntegerLiteral</code>.
     */
    public ASTIntegerLiteral parseIntegerLiteral()
    {
        Token t;
        if ((t = accept(INT_LITERAL)) != null)
        {
            return new ASTIntegerLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected an integer.");
        }
    }

    /**
     * Parses an <code>ASTFloatingPointLiteral</code>.
     * @return An <code>ASTFloatingPointLiteral</code>.
     */
    public ASTFloatingPointLiteral parseFloatingPointLiteral()
    {
        Token t;
        if ((t = accept(FLOATING_POINT_LITERAL)) != null)
        {
            return new ASTFloatingPointLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected a floating point number.");
        }
    }

    /**
     * Parses an <code>ASTStringLiteral</code>.
     * @return An <code>ASTStringLiteral</code>.
     */
    public ASTStringLiteral parseStringLiteral()
    {
        Token t;
        if ((t = accept(STRING_LITERAL)) != null)
        {
            return new ASTStringLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected a string.");
        }
    }

    /**
     * Parses an <code>ASTCharacterLiteral</code>.
     * @return An <code>ASTCharacterLiteral</code>.
     */
    public ASTCharacterLiteral parseCharacterLiteral()
    {
        Token t;
        if ((t = accept(CHARACTER_LITERAL)) != null)
        {
            return new ASTCharacterLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected a character.");
        }
    }

    /**
     * Parses an <code>ASTBooleanLiteral</code>.
     * @return An <code>ASTBooleanLiteral</code>.
     */
    public ASTBooleanLiteral parseBooleanLiteral()
    {
        Token t;
        if ((t = accept(TRUE)) != null)
        {
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else if ((t = accept(FALSE)) != null)
        {
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected true or false.");
        }
    }

    /**
     * Parses an <code>ASTNullLiteral</code>.
     * @return An <code>ASTNullLiteral</code>.
     */
    public ASTNullLiteral parseNullLiteral()
    {
        Token t;
        if ((t = accept(NULL)) != null)
        {
            return new ASTNullLiteral(t.getLocation(), t.getValue());
        }
        else
        {
            throw new CompileException("Expected null.");
        }
    }
}
