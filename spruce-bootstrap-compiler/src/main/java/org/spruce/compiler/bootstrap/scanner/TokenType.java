package org.spruce.compiler.bootstrap.scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * The types of tokens.  This includes separators, literals, operators,
 * keywords, identifiers, end-of-file, and "unknown".
 */
public enum TokenType {
    // SPECIAL

    /**
     * Unknown.
     */
    UNKNOWN("unknown"),
    /**
     * End of file.
     */
    EOF("$"),
    /**
     * Includes space, tab, newline, carriage return, form feed.
     */
    WHITESPACE(" "),

    // COMMENTS

    /**
     * A token that is the comment itself.
     */
    COMMENT("/*comment*/"),

    // IDENTIFIERS

    /**
     * Names defined by the programmer: names of classes, variables, methods, etc.
     */
    IDENTIFIER("id"),

    // LITERALS

    /**
     * A token of the form <code>'char'</code>.
     */
    CHARACTER_LITERAL("character literal"),
    /**
     * A token of the form <code>"string"</code> or <code>"""string"""</code>.
     */
    STRING_LITERAL("string literal"),
    /**
     * An integer literal from <code>Integer.MIN_VALUE</code> through <code>Integer.MAX_VALUE</code>,
     * e.g. -1000, 0, 1, 100
     */
    INT_LITERAL("integer literal"),
    /**
     * A floating point literal from <code>-Double.MAX_VALUE</code> through <code>Double.MAX_VALUE</code>,
     * e.g. -1.256, 1E100.
     * Decimal point, digits on either side of it or both, with optional exponent <code>[eE][+-]?[digits]+</code>,
     * or digits with exponent.
     */
    FLOATING_POINT_LITERAL("floating point literal"),

    // STRUCTURAL

    /**
     * The token <code>{</code>.
     */
    OPEN_BRACE("{"),
    /**
     * The token <code>}</code>.
     */
    CLOSE_BRACE("}"),
    /**
     * The token <code>(</code>.
     */
    OPEN_PARENTHESIS("("),
    /**
     * The token <code>)</code>.
     */
    CLOSE_PARENTHESIS(")"),
    /**
     * The token <code>;</code>.
     */
    SEMICOLON(";"),
    /**
     * The token <code>,</code>.
     */
    COMMA(","),

    // OPERATORS

    // Relational
    /**
     * The token <code>&lt;</code>.
     */
    LESS_THAN("<"),
    /**
     * The token <code>&lt;=</code>.
     */
    LESS_THAN_OR_EQUAL("<="),
    /**
     * The token <code>=</code>.
     */
    EQUAL("="),
    /**
     * The token <code>==</code>.
     */
    DOUBLE_EQUAL("=="),
    /**
     * The token <code>!=</code>.
     */
    EXCLAMATION_EQUAL("!="),
    /**
     * The token <code>&gt;</code>.
     */
    GREATER_THAN(">"),
    /**
     * The token <code>&gt;=</code>.
     */
    GREATER_THAN_OR_EQUAL(">="),

    // Logical/bitwise
    /**
     * The token <code>&amp;&amp;</code>
     */
    DOUBLE_AMPERSAND("&&"),
    /**
     * The token <code>||</code>
     */
    DOUBLE_PIPE("||"),
    /**
     * The token <code>!</code>
     */
    EXCLAMATION("!"),

    // Access
    /**
     * The token <code>.</code>.
     */
    DOT("."),
    // Mathematical
    /**
     * The token <code>+</code>.
     */
    PLUS("+"),
    /**
     * The token <code>-</code>.
     */
    MINUS("-"),

    // KEYWORDS
    /**
     * Not implemented/instantiable.
     */
    ABSTRACT,
    /**
     * Cast operator
     */
    AS,
    /**
     * Get out of current loop and don't start any more iterations.
     */
    BREAK,
    /**
     * Declare a class.
     */
    CLASS,
    /**
     * Shared, not reassignable, and immutable.
     */
    CONSTANT,
    /**
     * Define constructor; <strong>not</strong> the same name as the class.
     */
    CONSTRUCTOR,
    /**
     * Get out of current loop and start the next iteration.
     */
    CONTINUE,
    /**
     * else, else if.
     */
    ELSE,
    /**
     * Class extends superclass; upper bound generics wildcard.
     */
    EXTENDS,
    /**
     * The literal <code>false</code>.
     */
    FALSE,
    /**
     * For statements, traditional and "enhanced".
     */
    FOR,
    /**
     * if, else, else if.
     */
    IF,
    /**
     * Class implements interface.
     */
    IMPLEMENTS,
    /**
     * For use in enhanced for loop to indicate what to loop over.
     */
    IN,
    /**
     * All functionality abstract except for default methods; constants.
     */
    INTERFACE,
    /**
     * Determines if an object referred to by a reference is an instance of a
     * class, interface, or enum.
     */
    ISA,
    /**
     * Declare membership in a namespace.
     */
    NAMESPACE,
    /**
     * Create a new object, yielding an object reference.
     */
    NEW,
    /**
     * With this modifier, the method MUST override a superclass method.
     */
    OVERRIDE,
    /**
     * End a method or constructor, possibly returning a value.
     */
    RETURN,
    /**
     * The "self" object reference for constructors and non-shared methods.
     */
    SELF,
    /**
     * Class, not, instance-specific.  This is "static" in Java.
     */
    SHARED,
    /**
     * Refer to superclass method/instance; lower bound generics wildcard.
     */
    SUPER,
    /**
     * The literal <code>true</code>.
     */
    TRUE,
    /**
     * Allow simple names for identifiers not in same package or in spruce.lang.
     * Also used for "use statements" inside switch expressions.
     */
    USE,
    /**
     * Method doesn't return anything.
     */
    VOID,
    /**
     * While or do/while statement.
     */
    WHILE;


    private static final Map<String, TokenType> LOOKUP;

    static {
        LOOKUP = new HashMap<>();
        for (TokenType t : values()) {
            LOOKUP.put(t.getRepresentation(), t);
        }
    }

    /**
     * Finds the <code>TokenType</code> associated with the given representation, or
     * <code>null</code> if it isn't a token type.  Token types are known by their
     * representation.  E.g. to find
     * <code>SUPERTYPE</code>, pass <code>":&gt;"</code>.
     * @param representation The representation associated with a keyword.
     * @return The associated <code>TokenType</code>, or <code>null</code> if not
     *     found.
     */
    public static TokenType forRepresentation(String representation) {
        return LOOKUP.get(representation);
    }

    private final String myRepresentation;

    TokenType(String representation) {
        myRepresentation = representation;
    }

    TokenType() {
        myRepresentation = toString().toLowerCase();
    }

    /**
     * Returns the representation string for the <code>TokenType</code>.
     * @return The representation string for the <code>TokenType</code>.
     */
    public String getRepresentation() {
        return myRepresentation;
    }
}
