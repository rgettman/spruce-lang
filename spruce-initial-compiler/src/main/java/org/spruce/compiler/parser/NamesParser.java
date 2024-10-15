package org.spruce.compiler.parser;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
     * @return An <code>ASTListNode</code> with type <code>NAMESPACE_IDS</code>.
     */
    public ASTListNode parseNamespaceName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                NAMESPACE_IDS
        );
    }

    /**
     * Parses a <code>TypeName</code>.
     * <em>
     * TypeName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier
     * </em>
     * @return An <code>ASTListNode</code> with type <code>TYPENAME_IDS</code>.
     */
    public ASTListNode parseTypeName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                TYPENAME_IDS
        );
    }

    /**
     * Parses a <code>NamespaceOrTypeName</code>.
     * <em>
     * NamespaceOrTypeName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier<br>
     * </em>
     * @return An <code>ASTListNode</code> with type <code>NAMESPACE_OR_TYPENAME_IDS</code>.
     */
    public ASTListNode parseNamespaceOrTypeName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                NAMESPACE_OR_TYPENAME_IDS
        );
    }

    /**
     * Parses an <code>ExpressionName</code>.
     * <em>
     * ExpressionName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
     * </em>
     * @return An <code>ASTListNode</code> with type <code>EXPR_NAME_IDS</code>.
     */
    public ASTListNode parseExpressionName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                EXPR_NAME_IDS
        );
    }

    /**
     * Parses an <code>AmbiguousName</code>.
     * <em>
     * AmbiguousName:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
     * </em>
     * @return An <code>ASListNode</code> with type <code>AMBIGUOUS_NAME_IDS</code>.
     */
    public ASTListNode parseAmbiguousName() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                DOT,
                this::parseIdentifier,
                AMBIGUOUS_NAME_IDS
        );
    }

    /**
     * Parses an <code>IdentifierList</code>.
     * <em>
     * IdentifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}
     * </em>
     * @return An <code>ASTListNode</code> with type <code>IDENTIFIERS</code>.
     */
    public ASTListNode parseIdentifierList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier",
                COMMA,
                this::parseIdentifier,
                IDENTIFIERS
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
            throw new CompileException(curr().getLocation(), "Expected an identifier.");
        }
    }

    /**
     * Converts an Expression Name to a TypeName.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTListNode</code> representing a Type Name, with type
     *     <code>TYPENAME_IDS</code>, with the same structure as the given Expression Name.
     * @see NamesParser#convertToNamespaceOrTypeName
     */
    public ASTListNode convertToTypeName(ASTListNode exprName) {
        List<ASTNode> children = exprName.getChildren();
        if (!children.isEmpty() && children.get(0) instanceof ASTAmbiguousName ambName)
        {
            ASTNamespaceOrTypeName portName = ambName.convertToNamespaceOrTypeName();
            children.set(0, portName);
        }
        ASTListNode typeName = new ASTListNode(exprName.getLocation(), children, TYPENAME_IDS);
        return typeName;
    }

    /**
     * Copies a list node representing a type name to a new list node
     * representing a namespace or type name.
     * @param typeName An <code>ASTListNode</code> representing a type name.
     * @return An <code>ASTListNode</code> with type <code>NAMESPACE_OR_TYPENAME_IDS</code>
     */
    public ASTListNode convertToNamespaceOrTypeName(ASTListNode typeName) {
        return new ASTListNode(typeName.getLocation(), typeName.getChildren(), NAMESPACE_OR_TYPENAME_IDS);
    }
}
