package org.spruce.compiler.parser;

import org.spruce.compiler.ast.literals.ASTBooleanLiteral;
import org.spruce.compiler.ast.literals.ASTCharacterLiteral;
import org.spruce.compiler.ast.literals.ASTFloatingPointLiteral;
import org.spruce.compiler.ast.literals.ASTIntegerLiteral;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.literals.ASTStringLiteral;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>LiteralsParser</code> is a <code>BasicParser</code> that parses
 * literals.
 */
public class LiteralsParser extends BasicParser {
    /**
     * Constructs a <code>LiteralsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public LiteralsParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTLiteral</code>.
     * @return An <code>ASTLiteral</code>.
     */
    public ASTLiteral parseLiteral() {
        if (isCurr(INT_LITERAL)) {
            return parseIntegerLiteral();
        }
        else if (isCurr(FLOATING_POINT_LITERAL)) {
            return parseFloatingPointLiteral();
        }
        else if (isCurr(STRING_LITERAL)) {
            return parseStringLiteral();
        }
        else if (isCurr(CHARACTER_LITERAL)) {
            return parseCharacterLiteral();
        }
        else if (isCurr(TRUE) || isCurr(FALSE)) {
            return parseBooleanLiteral();
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a literal.");
        }
    }

    /**
     * Parses an <code>ASTIntegerLiteral</code>.
     * @return An <code>ASTIntegerLiteral</code>.
     */
    public ASTIntegerLiteral parseIntegerLiteral() {
        Token t;
        if ((t = accept(INT_LITERAL)) != null) {
            return new ASTIntegerLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected an integer.");
        }
    }

    /**
     * Parses an <code>ASTFloatingPointLiteral</code>.
     * @return An <code>ASTFloatingPointLiteral</code>.
     */
    public ASTFloatingPointLiteral parseFloatingPointLiteral() {
        Token t;
        if ((t = accept(FLOATING_POINT_LITERAL)) != null) {
            return new ASTFloatingPointLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a floating point number.");
        }
    }

    /**
     * Parses an <code>ASTStringLiteral</code>.
     * @return An <code>ASTStringLiteral</code>.
     */
    public ASTStringLiteral parseStringLiteral() {
        Token t;
        if ((t = accept(STRING_LITERAL)) != null) {
            return new ASTStringLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a string.");
        }
    }

    /**
     * Parses an <code>ASTCharacterLiteral</code>.
     * @return An <code>ASTCharacterLiteral</code>.
     */
    public ASTCharacterLiteral parseCharacterLiteral() {
        Token t;
        if ((t = accept(CHARACTER_LITERAL)) != null) {
            return new ASTCharacterLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected a character.");
        }
    }

    /**
     * Parses an <code>ASTBooleanLiteral</code>.
     * @return An <code>ASTBooleanLiteral</code>.
     */
    public ASTBooleanLiteral parseBooleanLiteral() {
        Token t;
        if ((t = accept(TRUE)) != null) {
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else if ((t = accept(FALSE)) != null) {
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected true or false.");
        }
    }
}
