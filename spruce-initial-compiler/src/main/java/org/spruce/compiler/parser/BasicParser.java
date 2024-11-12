package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTBinaryExpression;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>BasicParser</code> provides basic parser functionality -- access to
 * a <code>Scanner</code>, token test/advance methods, and general methods for
 * lists, multiples, and binary/left-associative.  Subclasses represent parsers
 * of various categories of productions and can obtain references to each other
 * for parsing productions outside their category, using the Parser class.
 */
public class BasicParser {
    private final Scanner myScanner;
    private final Parser myParser;

    /**
     * Constructs a <code>BasicParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public BasicParser(Scanner scanner, Parser parser) {
        myScanner = scanner;
        myParser = parser;
    }

    /**
     * Returns the <code>LiteralsParser</code>.
     * @return The <code>LiteralsParser</code>.
     */
    public LiteralsParser getLiteralsParser() {
        return myParser.getLiteralsParser();
    }

    /**
     * Returns the <code>NamesParser</code>.
     * @return The <code>NamesParser</code>.
     */
    public NamesParser getNamesParser() {
        return myParser.getNamesParser();
    }

    /**
     * Returns the <code>TypesParser</code>.
     * @return The <code>TypesParser</code>.
     */
    public TypesParser getTypesParser() {
        return myParser.getTypesParser();
    }

    /**
     * Returns the <code>ExpressionsParser</code>.
     * @return The <code>ExpressionsParser</code>.
     */
    public ExpressionsParser getExpressionsParser() {
        return myParser.getExpressionsParser();
    }

    /**
     * Returns the <code>StatementsParser</code>.
     * @return The <code>StatementsParser</code>.
     */
    public StatementsParser getStatementsParser() {
        return myParser.getStatementsParser();
    }

    /**
     * Returns the <code>ClassesParser</code>.
     * @return The <code>ClassesParser</code>.
     */
    public ClassesParser getClassesParser() {
        return myParser.getClassesParser();
    }

    /**
     * If the current token's type is the given type, then advance to the next
     * token, returning the original token.  If it doesn't match, don't advance,
     * and return <code>null</code>.
     *
     * @param tokenType The expected token type.
     * @return The token that matches, or <code>null</code> on mismatch.
     */
    protected Token accept(TokenType tokenType) {
        if (isCurr(tokenType)) {
            Token t = curr();
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
    protected Token curr() {
        return myScanner.getCurrToken();
    }

    /**
     * Returns the next <code>Token</code> from the <code>Scanner</code>.
     *
     * @return The next <code>Token</code> from the <code>Scanner</code>.
     */
    protected Token next() {
        return myScanner.getNextToken();
    }

    /**
     * Returns the peek <code>Token</code> (after "next") from the <code>Scanner</code>.
     *
     * @return The peek <code>Token</code> (after "next") from the <code>Scanner</code>.
     */
    protected Token peek() {
        return myScanner.getPeekToken();
    }

    /**
     * Simple test if the given token's type matches the given type.
     *
     * @param t         The <code>Token</code>.
     * @param tokenType The expected token type.
     * @return Whether the given token's type matches the given type.
     */
    protected static boolean test(Token t, TokenType tokenType) {
        return t.getType() == tokenType;
    }

    /**
     * Simple test if the given token's type exists in a list of token types.
     *
     * @param t          The <code>Token</code>.
     * @param tokenTypes The list of expected token types.
     * @return Whether the given token's type exists in a list of token types.
     */
    protected static boolean test(Token t, TokenType... tokenTypes) {
        return Arrays.asList(tokenTypes).contains(t.getType());
    }

    /**
     * Tests if the current token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the current token's type matches the given type.
     */
    protected boolean isCurr(TokenType tokenType) {
        return curr().getType() == tokenType;
    }

    /**
     * Tests if the next token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the next token's type matches the given type.
     */
    protected boolean isNext(TokenType tokenType) {
        return next().getType() == tokenType;
    }

    /**
     * Tests if the peek token's type matches the given type.
     *
     * @param tokenType The expected token type.
     * @return Whether the peek token's type matches the given type.
     */
    protected boolean isPeek(TokenType tokenType) {
        return peek().getType() == tokenType;
    }

    /**
     * Advance the <code>Scanner</code> to the next token.
     */
    protected void advance() {
        myScanner.next();
    }

    /**
     * Helper method to determine if the next token, expected to be an operator,
     * is one of the expected token types or expected keywords.
     * @param acceptedTokens A <code>List</code> of acceptable <code>TokenType</code>s.
     * @return The <code>TokenType</code> that matches, or <code>null</code> if none match.
     */
    protected TokenType isAcceptedOperator(List<TokenType> acceptedTokens) {
        TokenType type = curr().getType();
        if (acceptedTokens.contains(type)) {
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
    protected void setInTypeContext(boolean isInTypeContext) {
        myScanner.setInTypeContext(isInTypeContext);
    }

    /**
     * Helper method to avoid duplicating code for parsing binary expressions
     * that are left associative.
     *
     * @param isOnInitialToken Determines whether a given token is a valid
     *                         token on which to start parsing the desired node.
     * @param acceptedTokens   A <code>List</code> of accepted <code>TokenTypes</code>
     *                         that can serve as operators.
     * @param childParser      Parses and returns the child node (operand).
     * @return Either an <code>ASTNode</code> of the child type or an
     *     <code>ASTBinaryExpression</code> containing left-associative children.
     */
    protected ASTValueExpression parseBinaryExpressionLeftAssociative(Predicate<Token> isOnInitialToken,
                                                                      List<TokenType> acceptedTokens, Supplier<? extends ASTValueExpression> childParser) {
        if (isOnInitialToken.test(curr())) {
            Location loc = curr().getLocation();
            ASTValueExpression result = childParser.get();
            TokenType curr;
            while ( (curr = isAcceptedOperator(acceptedTokens) ) != null && isOnInitialToken.test(next())) {
                accept(curr);
                result = new ASTBinaryExpression(loc, result, childParser.get(), curr);
            }
            return result;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal or expression name.");
        }
    }

    /**
     * Helper method to avoid duplicating code for parsing list-like expressions.
     * Requires at least one element.
     * <em>List:  Element {sep Element}</em>
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param acceptedToken The accepted <code>TokenTypes</code>
     *      that can serve as the separator.
     * @param childParser Parses and returns the child node (list item).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of child nodes that constructs
     *                     and returns the desired list node type.
     * @param <C> The type of the child node.
     * @param <L> The type of the list node, parent to the children of type C.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <C extends Node, L extends ASTListNode<C>> L parseList(Predicate<Token> isOnInitialToken,
              String initialErrorMessage, TokenType acceptedToken, Supplier<C> childParser,
              BiFunction<Location, List<C>, L> nodeSupplier) {
        return parseList(isOnInitialToken, initialErrorMessage, acceptedToken, childParser, nodeSupplier, true);
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
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of child nodes that constructs
     *                     and returns the desired list node type.
     * @param requireAtLeastOne Requires at least one element if true, allows
     *                          an empty list if false.
     * @param <C> The type of the child node.
     * @param <L> The type of the list node, parent to the children of type C.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <C extends Node, L extends ASTListNode<C>> L parseList(Predicate<Token> isOnInitialToken,
               String initialErrorMessage, TokenType acceptedToken, Supplier<C> childParser,
               BiFunction<Location, List<C>, L> nodeSupplier, boolean requireAtLeastOne) {
        if (isOnInitialToken.test(curr())) {
            Location loc = curr().getLocation();
            List<C> children = new ArrayList<>();
            children.add(childParser.get());
            while (isCurr(acceptedToken) && isOnInitialToken.test(next())) {
                accept(acceptedToken);
                children.add(childParser.get());
            }
            return nodeSupplier.apply(loc, children);
        }
        else if (requireAtLeastOne) {
            throw new CompileException(curr().getLocation(), initialErrorMessage);
        }
        else {
            return nodeSupplier.apply(curr().getLocation(), new ArrayList<>());
        }
    }

    /**
     * Helper method to avoid duplicating code for parsing multiple expressions
     * into a list from a repeating production.
     * <em>Repeating:  Element {Element}</em>
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param childParser Parses and returns the child node (repeating item).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of child nodes that constructs
     *                     and returns the desired list node type.
     * @param <C> The type of the child node.
     * @param <L> The type of the list node, parent to the children of type C.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <C extends Node, L extends ASTListNode<C>> L parseMultiple(Predicate<Token> isOnInitialToken,
              String initialErrorMessage, Supplier<C> childParser, BiFunction<Location, List<C>, L> nodeSupplier) {
        return parseMultiple(isOnInitialToken, initialErrorMessage, childParser, nodeSupplier, true);
    }

    /**
     * Helper method to avoid duplicating code for parsing multiple expressions
     * into a list from a repeating production.
     * <em>Repeating:  Element {Element}</em>
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param childParser Parses and returns the child node (repeating item).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of child nodes that constructs
     *                     and returns the desired list node type.
     * @param requireAtLeastOne Requires at least one element if true, allows
     *                          an empty list if false.
     * @param <C> The type of the child node.
     * @param <L> The type of the list node, parent to the children of type C.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <C extends Node, L extends ASTListNode<C>> L parseMultiple(Predicate<Token> isOnInitialToken,
              String initialErrorMessage, Supplier<C> childParser,
              BiFunction<Location, List<C>, L> nodeSupplier, boolean requireAtLeastOne) {
        return parseMultiple(isOnInitialToken, initialErrorMessage, childParser, nodeSupplier,
                requireAtLeastOne, Collections.emptyList());
    }

    /**
     * Helper method to avoid duplicating code for parsing multiple expressions
     * into a list from a repeating production.
     * <em>Repeating:  Element {Element}</em>
     * @param isOnInitialToken Determines whether a given token is a valid
     *      token on which to start parsing the desired node.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param childParser Parses and returns the child node (repeating item).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of child nodes that constructs
     *                     and returns the desired list node type.
     * @param requireAtLeastOne Requires at least one element if true, allows
     *                          an empty list if false.
     * @param okNotInclude A <code>List</code> of <code>TokenType</code>s that,
     *                     if one is encountered, ends the parsing without including
     *                     that keyword in the returned list.
     * @param <C> The type of the child node.
     * @param <L> The type of the list node, parent to the children of type C.
     * @return A node of the desired type with all children parsed in order,
     *      leftmost first.
     */
    protected <C extends Node, L extends ASTListNode<C>> L parseMultiple(Predicate<Token> isOnInitialToken,
              String initialErrorMessage, Supplier<C> childParser, BiFunction<Location, List<C>, L> nodeSupplier,
              boolean requireAtLeastOne, List<TokenType> okNotInclude) {
        Location loc = curr().getLocation();
        if (isOnInitialToken.test(curr()) && !okNotInclude.contains(curr().getType())) {
            List<C> children = new ArrayList<>();
            children.add(childParser.get());
            while (isOnInitialToken.test(curr()) && !okNotInclude.contains(curr().getType())) {
                children.add(childParser.get());
            }
            return nodeSupplier.apply(loc, children);
        }
        else if (requireAtLeastOne) {
            throw new CompileException(curr().getLocation(), initialErrorMessage);
        }
        else {
            return nodeSupplier.apply(curr().getLocation(), new ArrayList<>());
        }
    }

    /**
     * Helper method to avoid duplicating code for parsing "modifier" productions.
     * @param initialErrorMessage If the initial token is not a valid token,
     *      the <code>CompilerException</code> thrown has this message.
     * @param acceptedTokens A <code>List</code> of accepted <code>TokenTypes</code>
     *      that can serve as operators.
     * @param nodeSupplier Creates the desired node, given a <code>Location</code>
     *      and a <code>TokenType</code> representing the found operation.
     * @param <T> The type of node to parse and create.
     * @return A node of the desired type with the desired operation.
     */
    protected <T extends ASTNode> T parseModifier(List<TokenType> acceptedTokens, String initialErrorMessage, BiFunction<Location, TokenType, T> nodeSupplier) {
        Location loc = curr().getLocation();
        TokenType operation = isAcceptedOperator(acceptedTokens);
        if (operation == null) {
            throw new CompileException(curr().getLocation(), initialErrorMessage);
        }
        accept(operation);
        return nodeSupplier.apply(loc, operation);
    }

    /**
     * Determines whether the given token is a literal.
     *
     * @param t A <code>Token</code>.
     * @return Whether the give token is a literal.
     */
    protected static boolean isLiteral(Token t) {
        return switch (t.getType()) {
            case TRUE, FALSE, INT_LITERAL, FLOATING_POINT_LITERAL, STRING_LITERAL, CHARACTER_LITERAL -> true;
            default -> false;
        };
    }

    /**
     * Determines whether the given token can start an expression.
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start an expression.
     */
    protected static boolean isExpression(Token t) {
        return (test(t, INCREMENT, DECREMENT) || isPrimary(t));
    }

    /**
     * <p>Determines whether the given token can start a Primary.</p>
     * <ul>
     * <li>-</li>
     * <li>~</li>
     * <li>!</li>
     * <li>identifier</li>
     * <li><code>self</code></li>
     * <li><code>new</code></li>
     * <li><code>super</code></li>
     * <li>(</li>
     * </ul>
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a Primary.
     */
    protected static boolean isPrimary(Token t) {
        if (isLiteral(t)) {
            return true;
        }
        return switch (t.getType()) {
            case MINUS, TILDE, EXCLAMATION, IDENTIFIER, SELF, OPEN_PARENTHESIS, NEW, SUPER -> true;
            default -> false;
        };
    }
}
