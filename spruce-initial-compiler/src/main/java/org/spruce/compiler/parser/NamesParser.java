package org.spruce.compiler.parser;

import java.util.Collections;

import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>NamesParser</code> is a <code>BasicParser</code> that parses names.
 */
public class NamesParser extends BasicParser {
    /**
     * Constructs a <code>NamesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public NamesParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTNamespaceName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTNamespaceName</code>.
     */
    public ASTNamespaceName parseNamespaceName() {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(DOT),
                this::parseIdentifier,
                ASTNamespaceName::new
        );
    }

    /**
     * Parses an <code>ASTTypeName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName parseTypeName() {
        ASTNamespaceOrTypeName ptName = parseNamespaceOrTypeName();
        ASTTypeName node = new ASTTypeName(ptName.getLocation(), ptName.getChildren());
        node.setOperation(ptName.getOperation());
        return node;
    }

    /**
     * Parses an <code>ASTNamespaceOrTypeName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTNamespaceOrTypeName parseNamespaceOrTypeName() {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(DOT),
                this::parseIdentifier,
                ASTNamespaceOrTypeName::new
        );
    }

    /**
     * Parses an <code>ASTExpressionName</code>; they are left-associative
     * with each other.
     * @return An <code>ASTExpressionName</code>.
     */
    public ASTExpressionName parseExpressionName() {
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
    public ASTAmbiguousName parseAmbiguousName() {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                Collections.singletonList(DOT),
                this::parseIdentifier,
                ASTAmbiguousName::new
        );
    }

    /**
     * Parses an <code>ASTIdentifierList</code>.
     * @return An <code>ASTIdentifierList</code>.
     */
    public ASTIdentifierList parseIdentifierList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier",
                COMMA,
                this::parseIdentifier,
                ASTIdentifierList::new
        );
    }

    /**
     * Parses an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier parseIdentifier() {
        Token t;
        if ((t = accept(IDENTIFIER)) != null) {
            return new ASTIdentifier(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException("Expected an identifier.");
        }
    }
}
