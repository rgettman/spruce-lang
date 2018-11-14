package org.spruce.compiler.parser;

import java.util.Collections;

import org.spruce.compiler.ast.names.ASTAmbiguousName;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTPackageOrTypeName;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>NamesParser</code> is a <code>BasicParser</code> that parses names.
 */
public class NamesParser extends BasicParser
{
    /**
     * Constructs a <code>NamesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public NamesParser(Scanner scanner, Parser parser)
    {
        super(scanner, parser);
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
}
