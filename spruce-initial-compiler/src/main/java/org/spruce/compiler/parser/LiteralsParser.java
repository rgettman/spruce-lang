package org.spruce.compiler.parser;

import java.util.function.BiFunction;

import org.spruce.compiler.ast.literals.ASTBooleanLiteral;
import org.spruce.compiler.ast.literals.ASTCharacterLiteral;
import org.spruce.compiler.ast.literals.ASTFloatingPointLiteral;
import org.spruce.compiler.ast.literals.ASTIntegerLiteral;
import org.spruce.compiler.ast.literals.ASTLiteral;
import org.spruce.compiler.ast.literals.ASTStringLiteral;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

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
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public LiteralsParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * Parses a <code>Literal</code>.
     * <em>
     * Literal:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IntegerLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FloatingPointLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CharacterLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StringLiteral<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BooleanLiteral
     * </em>
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
            throw internalError("a literal.");
        }
    }

    /**
     * Parses an <code>IntegerLiteral</code>.
     * <em>
     * IntegerLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Digits<br>
     * <br>
     * Digits:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;0-9
     * </em>
     * @return An <code>ASTIntegerLiteral</code>.
     */
    public ASTIntegerLiteral parseIntegerLiteral() {
        return parseSpecificLiteral(INT_LITERAL, ASTIntegerLiteral::new);
    }

    /**
     * Parses a <code>FloatingPointLiteral</code>.
     * <em>
     * FloatingPointLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Digits . [Digits] [ExponentPart]<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[Digits] . Digits [ExponentPart]<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Digits ExponentPart<br>
     * <br>
     * Digits:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;0-9<br>
     * <br>
     * ExponentPart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;e|E[+|-][Digits]\
     * </em>
     * @return An <code>ASTFloatingPointLiteral</code>.
     */
    public ASTFloatingPointLiteral parseFloatingPointLiteral() {
        return parseSpecificLiteral(FLOATING_POINT_LITERAL, ASTFloatingPointLiteral::new);
    }

    /**
     * Parses a <code>StringLiteral</code>.
     * <em>
     * StringLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;" StringCharacter "<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;""" InputCharacter """<br>
     * <br>
     * StringCharacter:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InputCharacter ( but not " or \ )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;EscapeCharacter
     * </em>
     * @return An <code>ASTStringLiteral</code>.
     */
    public ASTStringLiteral parseStringLiteral() {
        return parseSpecificLiteral(STRING_LITERAL, ASTStringLiteral::new);
    }

    /**
     * Parses a <code>CharacterLiteral</code>.
     * <em>
     * CharacterLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;' inputCharacter '<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;' escapeCharacter '<br>
     * </em>
     * @return An <code>ASTCharacterLiteral</code>.
     */
    public ASTCharacterLiteral parseCharacterLiteral() {
        return parseSpecificLiteral(CHARACTER_LITERAL, ASTCharacterLiteral::new);
    }

    /**
     * Parses a <code>BooleanLiteral</code>.
     * <em>
     * BooleanLiteral:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;true<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;false
     * </em>
     * @return An <code>ASTBooleanLiteral</code>.
     */
    public ASTBooleanLiteral parseBooleanLiteral() {
        Token t;
        if (isCurr(TRUE)) {
            t = accept(TRUE);
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else if (isCurr(FALSE)) {
            t = accept(FALSE);
            return new ASTBooleanLiteral(t.getLocation(), t.getValue());
        }
        else {
            throw internalError("true or false");
        }
    }

    /**
     * Helper method to parse a specific kind of literal.  It is expected that
     * at this point in the parsing, the caller has already tested for the
     * presence of the expected token.  If not, the caller should NOT call this
     * method; instead, generate a compiler message or parse an alternate
     * production.
     * @param accepted The expected <code>TokenType</code>, e.g. <code>INT_LITERAL</code>.
     * @param nodeSupplier A <code>BiFunction</code> that can create an
     *                     <code>ASTLiteral</code> of the desired type, given a
     *                     <code>Location</code> and a value, usually a method
     *                     reference to a constructor.
     * @return An instance of a specific type of <code>ASTLiteral</code>.
     * @param <T> The specific type of <code>ASTLiteral</code> to create.
     * @throws IllegalStateException If the expected token was not found.
     */
    private <T extends ASTLiteral> T parseSpecificLiteral(TokenType accepted,
              BiFunction<? super Location, ? super String, ? extends T> nodeSupplier) {
        Token t;
        if ((t = accept(accepted)) != null) {
            return nodeSupplier.apply(t.getLocation(), t.getValue());
        }
        else {
            throw internalError(accepted.getRepresentation());
        }
    }
}
