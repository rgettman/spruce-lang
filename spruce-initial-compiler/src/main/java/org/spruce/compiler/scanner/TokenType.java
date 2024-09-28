package org.spruce.compiler.scanner;

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

    // Annotation and directive indicators.

    /**
     * The token <code>@</code>.
     */
    AT_SIGN("@"),
    /**
     * The token <code>#</code>.
     */
    HASHTAG("#"),

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
     * A floating point literal from <code>-Double.MAX_VALUE</code> through <code>Double.MAX_VALUE</code>,
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
    ARROW("->"),
    /**
     * The token <code>..</code>.
     */
    TWO_DOTS(".."),
    /**
     * The token <code>...</code>.
     */
    THREE_DOTS("..."),

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
    AMPERSAND("&"),
    /**
     * The token <code>&amp;=</code>
     */
    AMPERSAND_EQUALS("&="),
    /**
     * The token <code>|</code>
     */
    PIPE("|"),
    /**
     * The token <code>|=</code>
     */
    PIPE_EQUALS("|="),
    /**
     * The token <code>^</code>
     */
    CARET("^"),
    /**
     * The token <code>^=</code>
     */
    CARET_EQUALS("^="),
    /**
     * The token <code>~</code>
     */
    TILDE("~"),
    /**
     * The token <code>&amp;&amp;</code>
     */
    DOUBLE_AMPERSAND("&&"),
    /**
     * The token <code>||</code>
     */
    DOUBLE_PIPE("||"),
    /**
     * The token <code>&amp;:</code>
     */
    AMPERSAND_COLON("&:"),
    /**
     * The token <code>^:</code>
     */
    CARET_COLON("^:"),
    /**
     * The token <code>|:</code>
     */
    PIPE_COLON("|:"),
    /**
     * The token <code>!</code>
     */
    EXCLAMATION("!"),
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

    /**
     * Not implemented/instantiable.
     */
    ABSTRACT,
    /**
     * Special class declaration as algebraic data type.
     */
    ADT,
    /**
     * Special class declaration; instances applied to classes, constructors,
     * fields, methods with "@".
     */
    ANNOTATION,
    /**
     * Cast operator
     */
    AS,
    /**
     * Assertions
     */
    ASSERT,
    /**
     * Variable takes the type of the expression assigned to it.
     */
    AUTO,
    /**
     * Get out of current loop and don't start any more iterations.
     */
    BREAK,
    /**
     * Case labels within switch statements.
     */
    CASE,
    /**
     * Catch an exception.
     */
    CATCH,
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
     * Critical section.
     */
    CRITICAL,
    /**
     * Default cases, default methods, default parameter values?
     */
    DEFAULT,
    /**
     * Special class declaration; instances applied to classes, constructors,
     * fields, methods with "#".  Directives tell the compiler to do something:
     * generate boilerplate code, check for failing the compiler, etc.
     */
    DIRECTIVE,
    /**
     * Do/while statement.
     */
    DO,
    /**
     * else, else if.
     */
    ELSE,
    /**
     * Special class declaration with constant types.
     */
    ENUM,
    /**
     * Class extends superclass; upper bound generics wildcard.
     */
    EXTENDS,
    /**
     * Explicit fallthrough in cases in switch statements.
     */
    FALLTHROUGH,
    /**
     * The literal <code>false</code>.
     */
    FALSE,
    /**
     * Can't extend class or can't override method.
     */
    FINAL,
    /**
     * Always executed.
     */
    FINALLY,
    /**
     * For statements, traditional and "enhanced".
     */
    FOR,
    /**
     * Give ownership of a variable to something else.
     */
    GIVE,
    /**
     * if, else, else if.
     */
    IF,
    /**
     * Class implements interface.
     */
    IMPLEMENTS,
    /**
     * All functionality abstract except for default methods; constants.
     */
    INTERFACE,
    /**
     * Can be accessed only by any same-package code.
     */
    INTERNAL,
    /**
     * Determines if an object reference refers to the same object as another
     * object reference (reference == in Java).
     */
    IS,
    /**
     * Determines if an object referred to by a reference is an instance of a
     * class, interface, or enum.
     */
    ISA,
    /**
     * Determines if an object reference does not refer to the same object as another
     * object reference (reference != in Java).
     */
    ISNT,
    /**
     * Match statement.
     */
    MATCH,
    /**
     * Contents are mutable.
     */
    MUT,
    /**
     * Declare membership in a namespace.
     */
    NAMESPACE,
    /**
     * Implemented in native code.
     */
    NATIVE,
    /**
     * Create a new object, yielding an object reference.
     */
    NEW,
    /**
     * With this modifier, the method MUST override a superclass method.
     */
    OVERRIDE,
    /**
     * Allowed subclasses of a sealed class.
     */
    PERMITS,
    /**
     * Can be accessed only by any same-class code.
     */
    PRIVATE,
    /**
     * Can be accessed only by any subclasses or other same-package code.
     */
    PROTECTED,
    /**
     * Can be accessed by any code.
     */
    PUBLIC,
    /**
     * Data type class with special handling.
     */
    RECORD,
    /**
     * End a method or constructor, possibly returning a value.
     */
    RETURN,
    /**
     * Sealed classes have controlled, limited inheritance.
     */
    SEALED,
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
     * Switch statement.
     */
    SWITCH,
    /**
     * Take ownership of a variable from something else.
     */
    TAKE,
    /**
     * Throw an exception.
     */
    THROW,
    /**
     * The literal <code>true</code>.
     */
    TRUE,
    /**
     * Try a block of code that may throw an exception.
     */
    TRY,
    /**
     * Allow simple names for identifiers not in same package or in spruce.lang.
     * Also used for "use statements" inside switch expressions.
     */
    USE,
    /**
     * Variable; can be reassigned.
     */
    VAR,
    /**
     * Method doesn't return anything.
     */
    VOID,
    /**
     * Writes provide "happens-before".
     */
    VOLATILE,
    /**
     * When clause for guards on switch labels.
     */
    WHEN,
    /**
     * While or do/while statement.
     */
    WHILE,
    /**
     * Yield statement.
     */
    YIELD,

    // types
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
    DOUBLE;

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
