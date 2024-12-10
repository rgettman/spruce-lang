package org.spruce.compiler.parser;

import java.util.Arrays;

import org.spruce.compiler.ast.names.*;
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
                Arrays.asList(SEMICOLON, USE,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, SEALED, SHARED,  // Type modifiers
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
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
                Arrays.asList(SEMICOLON, USE, OPEN_BRACE, OPEN_PARENTHESIS, STAR, SELF, SUPER, LESS_THAN,
                        OPEN_BRACKET, OPEN_CLOSE_BRACKET,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, SEALED, SHARED,  // Type modifiers
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTTypeName::new
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
                Arrays.asList(SEMICOLON, USE, CLOSE_BRACE,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, SEALED, SHARED,  // Type modifiers
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
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
            error(curr().getLocation(), "Expected an identifier.");
            return new ASTIdentifier(curr().getLocation(), "Bad Identifier");
        }
    }
}
