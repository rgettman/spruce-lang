package org.spruce.compiler.scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * The types of tokens.  This includes separators, literals, operators,
 * keywords, identifiers, end-of-file, and "unknown".
 */
public enum TokenType
{
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
    /**
     * The token <code>@</code>.
     */
    AT_SIGN("@"),

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
    CHARACTER_LITERAL("'char'"),
    /**
     * A token of the form <code>"string"</code> or <code>"""string"""</code>.
     */
    STRING_LITERAL("\"str\""),
    /**
     * An integer literal from <code>Integer.MIN_VALUE</code> through <code>Integer.MAX_VALUE</code>,
     * e.g. -1000, 0, 1, 100
     */
    INT_LITERAL("int"),
    /**
     * An floating point literal from <code>-Double.MAX_VALUE</code> through <code>Double.MAX_VALUE</code>,
     * e.g. -1.256, 1E100.
     * Decimal point, digits on either side of it or both, with optional exponent <code>[eE][+-]?[digits]+</code>,
     * or digits with exponent.
     */
    FLOATING_POINT_LITERAL("fl.pt"),

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
     * The token <code>[</code>.
     */
    OPEN_BRACKET("["),
    /**
     * The token <code>]</code>.
     */
    CLOSE_BRACKET("]"),
    /**
     * The token <code>[]</code>.
     */
    OPEN_CLOSE_BRACKET("[]"),
    /**
     * The token <code>(</code>.
     */
    OPEN_PARENTHESIS("("),
    /**
     * The token <code>)</code>.
     */
    CLOSE_PARENTHESIS(")"),
    /**
     * The token <code>:</code>.
     */
    COLON(":"),
    /**
     * The token <code>::</code>.
     */
    DOUBLE_COLON("::"),
    /**
     * The token <code>;</code>.
     */
    SEMICOLON(";"),
    /**
     * The token <code>,</code>.
     */
    COMMA(","),
    /**
     * The token <code>?</code>.
     */
    QUESTION_MARK("?"),
    /**
     * The token <code>-&gt;</code>.
     */
    LAMBDA_MAPS_TO("->"),
    /**
     * The token <code>...</code>.
     */
    ELLIPSIS("..."),

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
     * The token <code>!=</code>.
     */
    NOT_EQUAL("!="),
    /**
     * The token <code>&gt;</code>.
     */
    GREATER_THAN(">"),
    /**
     * The token <code>&gt;=</code>.
     */
    GREATER_THAN_OR_EQUAL(">="),
    /**
     * The token <code>&lt;=&gt;</code>.
     */
    COMPARISON("<=>"),
    // Logical/bitwise
    /**
     * The token <code>&amp;</code>
     */
    BITWISE_AND("&"),
    /**
     * The token <code>&amp;=</code>
     */
    AND_EQUALS("&="),
    /**
     * The token <code>|</code>
     */
    BITWISE_OR("|"),
    /**
     * The token <code>|=</code>
     */
    OR_EQUALS("|="),
    /**
     * The token <code>^</code>
     */
    BITWISE_XOR("^"),
    /**
     * The token <code>^=</code>
     */
    XOR_EQUALS("^="),
    /**
     * The token <code>~</code>
     */
    BITWISE_COMPLEMENT("~"),
    /**
     * The token <code>&amp;&amp;</code>
     */
    CONDITIONAL_AND("&&"),
    /**
     * The token <code>||</code>
     */
    CONDITIONAL_OR("||"),
    /**
     * The token <code>&amp;:</code>
     */
    LOGICAL_AND("&:"),
    /**
     * The token <code>^:</code>
     */
    LOGICAL_XOR("^:"),
    /**
     * The token <code>|:</code>
     */
    LOGICAL_OR("|:"),
    /**
     * The token <code>!</code>
     */
    LOGICAL_COMPLEMENT("!"),
    /**
     * The token <code>&lt;&lt;</code>
     */
    SHIFT_LEFT("<<"),
    /**
     * The token <code>&gt;&gt;=</code>
     */
    SHIFT_LEFT_EQUALS("<<="),
    /**
     * The token <code>&gt;&gt;</code>
     */
    SHIFT_RIGHT(">>"),
    /**
     * The token <code>&gt;&gt;&gt;=</code>
     */
    SHIFT_RIGHT_EQUALS(">>="),
    /**
     * The token <code>&gt;&gt;&gt;</code>
     */
    UNSIGNED_SHIFT_RIGHT(">>>"),
    /**
     * The token <code>&gt;&gt;&gt;=</code>
     */
    UNSIGNED_SHIFT_RIGHT_EQUALS(">>>="),
    // increment/decrement
    /**
     * The token <code>++</code>
     */
    INCREMENT("++"),
    /**
     * The token <code>--</code>
     */
    DECREMENT("--"),
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
     * The token <code>+=</code>.
     */
    PLUS_EQUALS("+="),
    /**
     * The token <code>-</code>.
     */
    MINUS("-"),
    /**
     * The token <code>-=</code>.
     */
    MINUS_EQUALS("-="),
    /**
     * The token <code>*</code>.
     */
    STAR("*"),
    /**
     * The token <code>*=</code>.
     */
    STAR_EQUALS("*="),
    /**
     * The token <code>/</code>.
     */
    SLASH("/"),
    /**
     * The token <code>/=</code>.
     */
    SLASH_EQUALS("/="),
    /**
     * The token <code>%</code>.
     */
    PERCENT("%"),
    /**
     * The token <code>%=</code>.
     */
    PERCENT_EQUALS("%="),
    // Assignment
    /**
     * The token <code>:=</code>.
     */
    ASSIGNMENT(":="),
    // Generics subtype/supertype.
    /**
     * The token <code>&lt;:</code>.
     */
    SUBTYPE("<:"),
    /**
     * The token <code>:&gt;</code>.
     */
    SUPERTYPE(":>"),

    // KEYWORDS

    // class-related
    /**
     * Allow simple names for identifiers not in same package or in spruce.lang.
     */
    RECOGNIZE,
    /**
     * Declare membership in a namespace.
     */
    NAMESPACE,
    /**
     * Declare a class.
     */
    CLASS,
    /**
     * All functionality abstract except for default methods; constants.
     */
    INTERFACE,
    /**
     * Special class declaration with constant types.
     */
    ENUM,
    /**
     * Special class declaration; instances applied to classes, constructors,
     * fields, methods with "@".
     */
    ANNOTATION,
    /**
     * Class extends superclass; upper bound generics wildcard.
     */
    EXTENDS,
    /**
     * Define constructor; <strong>not</strong> the same name as the class.
     */
    CONSTRUCTOR,
    /**
     * Refer to superclass method/instance; lower bound generics wildcard.
     */
    SUPER,
    /**
     * Class implements interface.
     */
    IMPLEMENTS,
    /**
     * Create a new object, yielding an object reference.
     */
    NEW,
    /**
     * End a method or constructor, possibly returning a value.
     */
    RETURN,
    /**
     * Implicitly typed variable.  Still strongly typed, like Java 10.
     */
    VAR,
    /**
     * The "this" object reference for constructors and non-shared methods.
     */
    THIS,
    /**
     * Determines if an object referred to by a reference is an instance of a
     * class, interface, or enum.
     */
    INSTANCEOF,
    /**
     * Determines if an object reference refers to the same object as another
     * object reference (reference == in Java).
     */
    IS,
    /**
     * Cast operator
     */
    AS,
    // access modifiers
    /**
     * Can be accessed only by any same-class code.
     */
    PRIVATE,
    /**
     * Can be accessed only by any same-package code.
     */
    INTERNAL,
    /**
     * Can be accessed only by any subclasses or other same-package code.
     */
    PROTECTED,
    /**
     * Can be accessed by any code.
     */
    PUBLIC,
    // other modifiers
    /**
     * Not implemented/instantiable.
     */
    ABSTRACT,
    /**
     * Class, not, instance-specific.  This is "static" in Java.
     */
    SHARED,
    /**
     * Strict IEEE math; no x86 80-bit mode.
     */
    STRICTFP,
    /**
     * Implemented in native code.
     */
    NATIVE,
    /**
     * Obtain lock before executing.
     */
    SYNCHRONIZED,
    /**
     * Not part of serialized state.
     */
    TRANSIENT,
    /**
     * Writes provide "happens-before".
     */
    VOLATILE,
    /**
     * With this modifier, the method MUST override a superclass method.
     */
    OVERRIDE,
    // restrictions
    /**
     * Reference may not be changed.
     */
    FINAL,
    /**
     * Contents are immutable.
     */
    CONST,
    // LOGIC
    /**
     * if, else, else if.
     */
    IF,
    /**
     * else, else if.
     */
    ELSE,
    /**
     * For statements, traditional and "enhanced".
     */
    FOR,
    /**
     * Do/while statement.
     */
    DO,
    /**
     * While or do/while statement.
     */
    WHILE,
    /**
     * Switch statement.
     */
    SWITCH,
    /**
     * Case labels within switch statements.
     */
    CASE,
    /**
     * Default cases, default methods, default parameter values?
     */
    DEFAULT,
    /**
     * Explicit fall through for switch case labels.
     */
    FALLTHROUGH,
    /**
     * Get out of current loop and don't start any more iterations.
     */
    BREAK,
    /**
     * Get out of current loop and start the next iteration.
     */
    CONTINUE,
    /**
     * Assertions
     */
    ASSERT,
    // types
    /**
     * Method doesn't return anything.
     */
    VOID,
    /**
     * <code>true</code> or <code>false</code>.
     */
    BOOLEAN,
    /**
     * 8-bit two's complement.
     */
    BYTE,
    /**
     * 16-bit unsigned.
     */
    CHAR,
    /**
     * 16-bit signed.
     */
    SHORT,
    /**
     * 32-bit two's complement.
     */
    INT,
    /**
     * 64-bit two's complement.
     */
    LONG,
    /**
     * 32-bit IEEE floating-point.
     */
    FLOAT,
    /**
     * 64-bit IEEE floating-point.
     */
    DOUBLE,
    // exception handling
    /**
     * Throw an exception.
     */
    THROW,
    /**
     * Declare exceptions thrown.
     */
    THROWS,
    /**
     * Try a block of code that may throw an exception.
     */
    TRY,
    /**
     * Catch an exception.
     */
    CATCH,
    /**
     * Always executed.
     */
    FINALLY,
    // values
    /**
     * The literal <code>true</code>.
     */
    TRUE,
    /**
     * The literal <code>false</code>.
     */
    FALSE,
    /**
     * The literal <code>null</code>.
     */
    NULL;

    private static final Map<String, TokenType> LOOKUP;

    static
    {
        LOOKUP = new HashMap<>();
        for (TokenType t : values())
        {
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
    public static TokenType forRepresentation(String representation)
    {
        return LOOKUP.get(representation);
    }

    private String myRepresentation;

    TokenType(String representation)
    {
        myRepresentation = representation;
    }

    TokenType()
    {
        myRepresentation = toString().toLowerCase();
    }

    /**
     * Returns the representation string for the <code>TokenType</code>.
     * @return The representation string for the <code>TokenType</code>.
     */
    public String getRepresentation()
    {
        return myRepresentation;
    }
}
