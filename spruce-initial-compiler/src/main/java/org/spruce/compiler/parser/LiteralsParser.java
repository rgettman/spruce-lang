package org.spruce.compiler.parser;

import org.spruce.compiler.ast.literals.ASTBooleanLiteral;
import org.spruce.compiler.ast.literals.ASTCharacterLiteral;
import org.spruce.compiler.ast.literals.ASTFloatingPointLiteral;
import org.spruce.compiler.ast.literals.ASTIntegerLiteral;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.literals.ASTNullLiteral;
import org.spruce.compiler.ast.literals.ASTStringLiteral;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>LiteralsParser</code> is a <code>BasicParser</code> that also parses
 * literals.
 */
public class LiteralsParser extends BasicParser
{
    /**
     * Constructs a <code>LiteralsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     */
    public LiteralsParser(Scanner scanner)
    {
        super(scanner);
    }

    /**
     * Parses an <code>ASTLiteral</code>.
     * @return An <code>ASTLiteral</code>.
     */
    public ASTLiteral parseLiteral()
    {
        Token curr = curr();
        if (isCurr(INT_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseIntegerLiteral());
        }
        else if (isCurr(FLOATING_POINT_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseFloatingPointLiteral());
        }
        else if (isCurr(STRING_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseStringLiteral());
        }
        else if (isCurr(CHARACTER_LITERAL))
        {
            return new ASTLiteral(curr.getLocation(), parseCharacterLiteral());
        }
        else if (isCurr(TRUE))
        {
            return new ASTLiteral(curr.getLocation(), parseBooleanLiteral());
        }
        else if (isCurr(FALSE))
        {
            return new ASTLiteral(curr.getLocation(), parseBooleanLiteral());
        }
        else if (isCurr(NULL))
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

    /**
     * Determines whether the given token is a literal.
     *
     * @param t A <code>Token</code>.
     * @return Whether the give token is a literal.
     */
    protected static boolean isLiteral(Token t)
    {
        switch (t.getType())
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
}
