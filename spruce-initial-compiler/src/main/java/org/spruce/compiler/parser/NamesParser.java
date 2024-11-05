package org.spruce.compiler.parser;

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
     * Parses a <code>NamespaceName</code>.
     * <em>
     * NamespaceName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceName . Identifier
     * </em>
     * @return An <code>ASTNamespaceName</code>.
     */
    public ASTNamespaceName parseNamespaceName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                ASTNamespaceName::new
        );
    }

    /**
     * Parses a <code>TypeName</code>.
     * <em>
     * TypeName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier
     * </em>
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName parseTypeName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                ASTTypeName::new
        );
    }

    /**
     * Parses a <code>NamespaceOrTypeName</code>.
     * <em>
     * NamespaceOrTypeName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier<br>
     * </em>
     * @return An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTNamespaceOrTypeName parseNamespaceOrTypeName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                ASTNamespaceOrTypeName::new
        );
    }

    /**
     * Parses an <code>ExpressionName</code>.
     * <em>
     * ExpressionName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
     * </em>
     * @return An <code>ASTExpressionName</code>.
     */
    public ASTExpressionName parseExpressionName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                ASTExpressionName::new
        );
    }

    /**
     * Parses an <code>AmbiguousName</code>.
     * <em>
     * AmbiguousName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
     * </em>
     * @return An <code>ASTAmbiguousName</code>.
     */
    public ASTAmbiguousName parseAmbiguousName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                ASTAmbiguousName::new
        );
    }

    /**
     * Parses an <code>IdentifierList</code>.
     * <em>
     * IdentifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}
     * </em>
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
     * Parses an <code>Identifier</code>.
     * <em>
     * Identifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IdentifierStart [IdentifierPart]*<br>
     * <br>
     * IdentifierStart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;JavaLetter<br>
     * <br>
     * IdentifierPart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;JavaLetterOrDigit
     * </em>
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier parseIdentifier() {
        Token t;
        if ((t = accept(IDENTIFIER)) != null) {
            return new ASTIdentifier(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected an identifier.");
        }
    }

    /**
     * Converts an Expression Name to a TypeName.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTTypeName</code> with the same structure as the given Expression Name.
     * @see #convertToNamespaceOrTypeName
     */
    public ASTTypeName convertToTypeName(ASTExpressionName exprName) {
        return new ASTTypeName(exprName.getLocation(), exprName.getTypedChildren());
    }

    /**
     * Copies a list node representing a type name to a new list node
     * representing a namespace or type name.
     * @param typeName An <code>ASTTypeName</code>.
     * @return An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTNamespaceOrTypeName convertToNamespaceOrTypeName(ASTTypeName typeName) {
        return new ASTNamespaceOrTypeName(typeName.getLocation(), typeName.getTypedChildren());
    }
}
