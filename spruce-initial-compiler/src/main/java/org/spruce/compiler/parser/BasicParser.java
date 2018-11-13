package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

/**
 * A <code>BasicParser</code> provides basic parser functionality -- access to
 * a <code>Scanner</code>, token test/advance methods, and general methods for
 * lists, multiples, and binary/left-associative.
 */
public class BasicParser
{
    private Scanner myScanner;

    /**
     * Constructs a <code>BasicParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     */
    public BasicParser(Scanner scanner)
    {
        myScanner = scanner;
        advance();
    }

    /**
     * If the current token's type is the given type, then advance to the next
     * token, returning the original token.  If it doesn't match, don't advance,
     * and return <code>null</code>.
     *
     * @param tokenType The expected token type.
     * @return The token that matches, or <code>null</code> on mismatch.
     */
    protected Token accept(TokenType tokenType)
    {
        if (isCurr(tokenType))
        {
            Token t = myScanner.getCurrToken();
            advance();
            return t;
        }
        return null;
    }

    /**
     * Returns the current <code>Token</code> from the <code>Scanner</code>.
     *
     * @return The current <code>Token</code> from the <code>Scanner</code>.
     */
    protected Token curr()
    {
        return myScanner.getCurrToken();
    }

    /**
     * Returns the next <code>Token</code> from the <code>Scanner</code>.
     *
     * @return The next <code>Token</code> from the <code>Scanner</code>.
     */
    protected Token next()
    {
        return myScanner.getNextToken();
    }

    /**
     * Returns the peek <code>Token</code> (after "next") from the <code>Scanner</code>.
     *
     * @return The peek <code>Token</code> (after "next") from the <code>Scanner</code>.
     */
    protected Token peek()
    {
        return myScanner.getPeekToken();
    }

    /**
     * Simple test if the given token's type matches the given type.
     *
     * @param t         The <code>Token</code>.
     * @param tokenType The expected token type.
     * @return Whether the given token's type matches the given type.
     */
    protected static boolean test(Token t, TokenType tokenType)
    {
        return t.getType() == tokenType;
    }

    /**
     * Simple test if the given token's type exists in a list of token types.
     *
     * @param t          The <code>Token</code>.
     * @param tokenTypes The list of expected token types.
     * @return Whether the given token's type exists in a list of token types.
     */
    protected static boolean test(Token t, TokenType... tokenTypes)
    {
        return Arrays.asList(tokenTypes).contains(t.getType());
    }

    /**
     * Tests if the current token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the current token's type matches the given type.
     */
    protected boolean isCurr(TokenType tokenType)
    {
        return curr().getType() == tokenType;
    }

    /**
     * Tests if the next token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the next token's type matches the given type.
     */
    protected boolean isNext(TokenType tokenType)
    {
        return next().getType() == tokenType;
    }

    /**
     * Tests if the peek token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the peek token's type matches the given type.
     */
    protected boolean isPeek(TokenType tokenType)
    {
        return peek().getType() == tokenType;
    }

    /**
     * Advance the <code>Scanner</code> to the next token.
     */
    protected void advance()
    {
        myScanner.next();
    }

    /**
     * Helper method to determine if the next token, expected to be an operator,
     * is one of the expected token types or expected keywords.
     * @param acceptedTokens A <code>List</code> of acceptable <code>TokenType</code>s.
     * @return The <code>TokenType</code> that matches, or <code>null</code> if none match.
     */
    protected TokenType isAcceptedOperator(List<TokenType> acceptedTokens)
    {
        TokenType type = myScanner.getCurrToken().getType();
        if (acceptedTokens.indexOf(type) >= 0)
        {
            return type;
        }
        return null;
    }

    /**
     * Sets whether the <code>Scanner</code> is in a type context.  This
     * controls whether multiple '&gt;' symbols are interpreted as multiple
     * end-angle-brackets, instead of a shift-right or unsigned-shift-right
     * operator.
     * @param isInTypeContext Whether the <code>Scanner</code> is in a type
     *     context.
     */
    protected void setInTypeContext(boolean isInTypeContext)
    {
        myScanner.setInTypeContext(isInTypeContext);
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
    protected <T extends ASTParentNode> T parseBinaryExpressionLeftAssociative(Predicate<Token> isOnInitialToken, String initialErrorMessage,
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
            while ( (curr = isAcceptedOperator(acceptedTokens) ) != null && isOnInitialToken.test(next()))
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
     * <em>List:  Element {sep Element}</em>
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
    protected <T extends ASTParentNode> T parseList(Predicate<Token> isOnInitialToken, String initialErrorMessage,
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

            while (isCurr(acceptedToken) && isOnInitialToken.test(next()))
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
     * Helper method to avoid duplicating code for parsing multiple expressions
     * in a repeating production.
     * <em>Repeating:  Element {Element}</em>
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param childParser Parses and returns the child node (repeating item).
     * @param nodeSupplier Creates the desired node, given a <code>Location</code>
     *      and a <code>List</code> of child nodes.
     * @param <T> The type of node to parse and create.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <T extends ASTParentNode> T parseMultiple(Predicate<Token> isOnInitialToken, String initialErrorMessage,
                                                      Supplier<? extends ASTNode> childParser,
                                                      BiFunction<Location, List<ASTNode>, T> nodeSupplier)
    {
        if (!isOnInitialToken.test(curr()))
        {
            throw new CompileException(initialErrorMessage);
        }
        Location loc = myScanner.getCurrToken().getLocation();
        List<ASTNode> children = new ArrayList<>();
        children.add(childParser.get());
        T node = nodeSupplier.apply(loc, children);
        while (isOnInitialToken.test(curr()))
        {
            children.add(childParser.get());
        }
        return node;
    }

    /**
     * Helper method to avoid duplicating code for parsing "one of" productions.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param acceptedTokens A <code>List</code> of accepted <code>TokenTypes</code>
     *      that can serve as operators.
     * @param nodeSupplier Creates the desired node, given a <code>Location</code>
     *      and a <code>List</code> of child nodes.
     * @param <T> The type of node to parse and create.
     * @return A node of the desired type with no children.
     */
    protected <T extends ASTParentNode> T parseOneOf(List<TokenType> acceptedTokens, String initialErrorMessage, BiFunction<Location, List<ASTNode>, T> nodeSupplier)
    {
        Location loc = curr().getLocation();
        TokenType operation = isAcceptedOperator(acceptedTokens);
        if (operation == null)
        {
            throw new CompileException(initialErrorMessage);
        }
        accept(operation);
        T node = nodeSupplier.apply(loc, Collections.emptyList());
        node.setOperation(operation);
        return node;
    }
}
