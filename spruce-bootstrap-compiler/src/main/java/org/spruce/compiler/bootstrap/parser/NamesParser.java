package org.spruce.compiler.bootstrap.parser;

import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifierList;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceName;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>NamesParser</code> is a <code>BasicParser</code> that parses names.
 */
public class NamesParser extends BasicParser {
    /**
     * Constructs a <code>NamesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public NamesParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
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
            error(curr().getLocation(), "Expected an identifier.");
            return new ASTIdentifier(curr().getLocation(), "Bad Identifier");
        }
    }
}
