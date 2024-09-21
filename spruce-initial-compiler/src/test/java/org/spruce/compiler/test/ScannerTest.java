package org.spruce.compiler.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import static org.spruce.compiler.scanner.TokenType.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests related to the <code>Scanner</code>.
 */
public class ScannerTest {
    @Test
    public void test() throws IOException {
        Scanner scanner = new Scanner(Paths.get("src-spruce/Tokens.spruce"));
        while(scanner.next()) {
            Token token = scanner.getCurrToken();
            Location loc = token.getLocation();
            //System.out.println(token + " at " + loc);
            System.out.println(loc.getFileAndLineNbr() + ": " + token);
            System.out.println(loc.getLine());
            System.out.println(loc.getPosIndicator());
        }
    }

    /**
     * Helper method to compare a list of expected tokens against tokens
     * generated from the given <code>Scanner</code>.
     * @param expectedTokens A <code>List</code> of expected <code>Tokens</code>.
     * @param scanner A <code>Scanner</code> that produces <code>Tokens</code>.
     */
    private void compareToExpected(List<Token> expectedTokens, Scanner scanner) {
        int i = 0;
        while(scanner.next()) {
            try {
                Token token = scanner.getCurrToken();
                assertEquals(expectedTokens.get(i), token, "Mismatch on token " + i);
                i++;
            }
            catch (ArrayIndexOutOfBoundsException aioobe) {
                fail("Found more tokens than expected (" + expectedTokens.size() + ")!");
            }
        }
        if (expectedTokens.size() > i) {
            fail("Expected more tokens (" + expectedTokens.size() + ") than found (" + i + ")!");
        }
    }

    /**
     * Tests type context, so that "&gt;&gt;" parses as two separate "&gt;"
     * tokens instead of one "&gt;&gt;" token.
     */
    @Test
    public void testTypeContext() {
        String line = "Map<Class<?>, List<Integer>>";
        Scanner scanner = new Scanner(line);
        scanner.setInTypeContext(true);

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "Map"), new Token(LESS_THAN, "<"),
                new Token(IDENTIFIER, "Class"), new Token(LESS_THAN, "<"),
                new Token(QUESTION_MARK, "?"), new Token(GREATER_THAN, ">"),
                new Token(COMMA, ","), new Token(IDENTIFIER, "List"),
                new Token(LESS_THAN, "<"), new Token(IDENTIFIER, "Integer"),
                new Token(GREATER_THAN, ">"), new Token(GREATER_THAN, ">")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>use</code>.
     */
    @Test
    public void testUse() {
        String line = "use spruce.test;";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(USE, "use"), new Token(IDENTIFIER, "spruce"),
                new Token(DOT, "."), new Token(IDENTIFIER, "test"),
                new Token(SEMICOLON, ";")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>namespace</code>.
     */
    @Test
    public void testNamespace() {
        String line = "namespace spruce.test;";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(NAMESPACE, "namespace"), new Token(IDENTIFIER, "spruce"),
                new Token(DOT, "."), new Token(IDENTIFIER, "test"), new Token(SEMICOLON, ";")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>class</code>.
     */
    @Test
    public void testClassAccessModifiersBraces() {
        String line = "public private internal protected sealed class Test implements Runnable permits Test1, Test2 {}";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(PUBLIC, "public"), new Token(PRIVATE, "private"),
                new Token(INTERNAL, "internal"), new Token(PROTECTED, "protected"),
                new Token(SEALED, "sealed"),
                new Token(CLASS, "class"), new Token(IDENTIFIER, "Test"),
                new Token(IMPLEMENTS, "implements"), new Token(IDENTIFIER, "Runnable"),
                new Token(PERMITS, "permits"), new Token(IDENTIFIER, "Test1"),
                new Token(COMMA, ","), new Token(IDENTIFIER, "Test2"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>interface</code>.
     */
    @Test
    public void testInterface() {
        String line = "interface ITest extends IExample {}";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(INTERFACE, "interface"), new Token(IDENTIFIER, "ITest"),
                new Token(EXTENDS, "extends"), new Token(IDENTIFIER, "IExample"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>enum</code>.
     */
    @Test
    public void testEnum() {
        String line = "enum TrafficLights { RED, YELLOW, GREEN }";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(ENUM, "enum"), new Token(IDENTIFIER, "TrafficLights"),
                new Token(OPEN_BRACE, "{"),
                new Token(IDENTIFIER, "RED"), new Token(COMMA, ","),
                new Token(IDENTIFIER, "YELLOW"), new Token(COMMA, ","),
                new Token(IDENTIFIER, "GREEN"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>annotation</code>.
     */
    @Test
    public void testAnnotation() {
        String line = "annotation SuppressWarnings { String[] value(); }";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(ANNOTATION, "annotation"),
                new Token(IDENTIFIER, "SuppressWarnings"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "String"),
                new Token(OPEN_CLOSE_BRACKET, "[]"),
                new Token(IDENTIFIER, "value"),
                new Token(OPEN_PARENTHESIS, "("), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>directive</code>.
     */
    @Test
    public void testDirective() {
        String line = "directive Derive {}";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(DIRECTIVE, "directive"), new Token(IDENTIFIER, "Derive"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>critical</code>.
     */
    @Test
    public void testCritical() {
        String line = "critical (lock) { } ";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(CRITICAL, "critical"),
                new Token(OPEN_PARENTHESIS, "("), new Token(IDENTIFIER, "lock"),
                new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>give</code> and <code>take</code>.
     */
    @Test
    public void testGiveAndTake() {
        String code = """
                void test(take Status st) {
                    test2(give st);
                }
                """;
        Scanner scanner = new Scanner(code);

        List<Token> expectedTokens = Arrays.asList(
                new Token(VOID, "void"), new Token(IDENTIFIER, "test"),
                new Token(OPEN_PARENTHESIS, "("), new Token(TAKE, "take"),
                new Token(IDENTIFIER, "Status"), new Token(IDENTIFIER, "st"),
                new Token(CLOSE_PARENTHESIS, ")"),

                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "test2"),
                new Token(OPEN_PARENTHESIS, "("), new Token(GIVE, "give"),
                new Token(IDENTIFIER, "st"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>match</code>.
     */
    @Test
    public void testMatch() {
        String code = """
                match result {
                    Some(value) -> { process(value); }
                    None -> { log("None!"); }
                }
                """;
        Scanner scanner = new Scanner(code);

        List<Token> expectedTokens = Arrays.asList(
                new Token(MATCH, "match"), new Token(IDENTIFIER, "result"),
                new Token(OPEN_BRACE, "{"),

                new Token(IDENTIFIER, "Some"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "value"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(ARROW, "->"), new Token(OPEN_BRACE, "{"),
                new Token(IDENTIFIER, "process"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "value"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(IDENTIFIER, "None"),
                new Token(ARROW, "->"), new Token(OPEN_BRACE, "{"),
                new Token(IDENTIFIER, "log"), new Token(OPEN_PARENTHESIS, "("),
                new Token(STRING_LITERAL, "None!"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Test recognizing <code>record</code>.
     */
    @Test
    public void testRecord() {
        String line = "record Time { int hour; int minute; }";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(RECORD, "record"), new Token(IDENTIFIER, "Time"),
                new Token(OPEN_BRACE, "{"),

                new Token(INT, "int"), new Token(IDENTIFIER, "hour"),
                new Token(SEMICOLON, ";"),

                new Token(INT, "int"), new Token(IDENTIFIER, "minute"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing the range operator <code>..</code> within a range.
     */
    @Test
    public void testRange() {
        String line = "[1..length]";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(OPEN_BRACKET, "["), new Token(INT_LITERAL, "1"),
                new Token(TWO_DOTS, ".."), new Token(IDENTIFIER, "length"),
                new Token(CLOSE_BRACKET, "]")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>constructor</code>.
     */
    @Test
    public void testConstructorAndOtherModifiers() {
        String line = "constructor(var mut int[] nbrs, constant List<String> words) { super(); } ";  // Ends with a tab
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(CONSTRUCTOR, "constructor"),
                new Token(OPEN_PARENTHESIS, "("),
                new Token(VAR, "var"), new Token(MUT, "mut"),
                new Token(INT, "int"),
                new Token(OPEN_CLOSE_BRACKET, "[]"),
                new Token(IDENTIFIER, "nbrs"), new Token(COMMA, ","),
                new Token(CONSTANT, "constant"), new Token(IDENTIFIER, "List"),
                new Token(LESS_THAN, "<"), new Token(IDENTIFIER, "String"), new Token(GREATER_THAN, ">"),
                new Token(IDENTIFIER, "words"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),
                new Token(SUPER, "super"),
                new Token(OPEN_PARENTHESIS, "("), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>constructor</code>, a bunch of modifiers,
     * <code>throws</code>, and <code>this</code>.
     */
    @Test
    public void testMethodAndPrimitiveTypes() {
        String line = "abstract shared native final volatile override void testMethod() {";
            line += "\n    critical (obj) {}";
            line += "\n    int[] iarray = new int[] {1, 2, 3};";
            line += "\n    byte b = 1;";
            line += "\n    short s = 2;";
            line += "\n    int i = 3;";
            line += "\n    long l = 4;";
            line += "\n    float f = 5;";
            line += "\n    double d = 6 as double;";
            line += "\n    char c = '7';";
            line += "\n    boolean z = false;";
            line += "\n    mut z_2 = true;";
            line += "\n    String str = \"none\";";
            line += "\n    return self;";
            line += "\n    yield 8;";
            line += "\n}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(ABSTRACT, "abstract"), new Token(SHARED, "shared"),
                new Token(NATIVE, "native"), new Token(FINAL, "final"),

                new Token(VOLATILE, "volatile"), new Token(OVERRIDE, "override"),
                new Token(VOID, "void"), new Token(IDENTIFIER, "testMethod"),
                new Token(OPEN_PARENTHESIS, "("), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"),

                new Token(CRITICAL, "critical"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "obj"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}"),

                new Token(INT, "int"), new Token(OPEN_CLOSE_BRACKET, "[]"),
                new Token(IDENTIFIER, "iarray"), new Token(EQUAL, "="),
                new Token(NEW, "new"), new Token(INT, "int"), new Token(OPEN_CLOSE_BRACKET, "[]"),
                new Token(OPEN_BRACE, "{"),
                new Token(INT_LITERAL, "1"), new Token(COMMA, ","),
                new Token(INT_LITERAL, "2"), new Token(COMMA, ","),
                new Token(INT_LITERAL, "3"), new Token(CLOSE_BRACE, "}"), new Token(SEMICOLON, ";"),

                new Token(BYTE, "byte"),
                new Token(IDENTIFIER, "b"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "1"), new Token(SEMICOLON, ";"),

                new Token(SHORT, "short"),
                new Token(IDENTIFIER, "s"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "2"), new Token(SEMICOLON, ";"),

                new Token(INT, "int"),
                new Token(IDENTIFIER, "i"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "3"), new Token(SEMICOLON, ";"),

                new Token(LONG, "long"),
                new Token(IDENTIFIER, "l"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "4"), new Token(SEMICOLON, ";"),

                new Token(FLOAT, "float"),
                new Token(IDENTIFIER, "f"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "5"), new Token(SEMICOLON, ";"),

                new Token(DOUBLE, "double"),
                new Token(IDENTIFIER, "d"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "6"), new Token(AS, "as"),
                new Token(DOUBLE, "double"), new Token(SEMICOLON, ";"),

                new Token(CHAR, "char"),
                new Token(IDENTIFIER, "c"), new Token(EQUAL, "="),
                new Token(CHARACTER_LITERAL, "7"), new Token(SEMICOLON, ";"),

                new Token(BOOLEAN, "boolean"),
                new Token(IDENTIFIER, "z"), new Token(EQUAL, "="),
                new Token(FALSE, "false"), new Token(SEMICOLON, ";"),

                new Token(MUT, "mut"),
                new Token(IDENTIFIER, "z_2"), new Token(EQUAL, "="),
                new Token(TRUE, "true"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "String"),
                new Token(IDENTIFIER, "str"), new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "none"), new Token(SEMICOLON, ";"),

                new Token(RETURN, "return"),
                new Token(SELF, "self"), new Token(SEMICOLON, ";"),

                new Token(YIELD, "yield"),
                new Token(INT_LITERAL, "8"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
        );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests open/close brackets by themselves.
     */
    @Test
    public void testOpenBracketCloseBracket() {
        String line = "new int[3][];";

        List<Token> expectedTokens = Arrays.asList(
                new Token(NEW, "new"), new Token(INT, "int"),
                new Token(OPEN_BRACKET, "["), new Token(INT_LITERAL, "3"), new Token(CLOSE_BRACKET, "]"),
                new Token(OPEN_CLOSE_BRACKET, "[]"), new Token(SEMICOLON, ";")
        );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing string literals.
     */
    @Test
    public void testStringLiterals() {
        String line = "String escapeTest = \"Test #1: \\b\\t\\n\\f\\r\\\"\\'\\\\\";\n";
        line += """
            String literalTest = \"""\s   
                Test #2: \\b\\t\\n\\f\\r\\"\\'\\
                \""";
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "String"), new Token(IDENTIFIER, "escapeTest"),
                new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "Test #1: \b\t\n\f\r\"'\\"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "String"), new Token(IDENTIFIER, "literalTest"),
                new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "Test #2: \\b\\t\\n\\f\\r\\\"\\'\\\n"), new Token(SEMICOLON, ";")

        );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Ensure a compiler exception if after """, there is non-whitespace
     * before the first line terminator.
     */
    @Test
    public void testBadTextBlock() {
        String code = """
                \""" bad
                actualText
                \"""
                """;
        Scanner scanner = new Scanner(code);
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Test a bad text block that doesn't end.
     */
    @Test
    public void testTextBlockNoEnd() {
        String code = """
                \"""
                noEndInSight
                """;
        Scanner scanner = new Scanner(code);
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Tests recognizing <code>if</code>, <code>else</code>, and <code>throw</code>.
     */
    @Test
    public void testIfElse() {
        String line = "if (b < s) { i++; }";
            line += "\nelse if (b > s) { i--; }";
            line += "\nelse { throw e; }";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IF, "if"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "b"), new Token(LESS_THAN, "<"),
                new Token(IDENTIFIER, "s"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "i"),
                new Token(INCREMENT, "++"), new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(ELSE, "else"),
                new Token(IF, "if"),new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "b"), new Token(GREATER_THAN, ">"),
                new Token(IDENTIFIER, "s"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "i"),
                new Token(DECREMENT, "--"), new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(ELSE, "else"),
                new Token(OPEN_BRACE, "{"), new Token(THROW, "throw"),
                new Token(IDENTIFIER, "e"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
                );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>do</code>, <code>while</code>, and <code>continue</code>.
     */
    @Test
    public void testDoWhileContinue() {
        String line = "do { continue; } while (a >= b);";

        List<Token> expectedTokens = Arrays.asList(
                new Token(DO, "do"), new Token(OPEN_BRACE, "{"),
                new Token(CONTINUE, "continue"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}"), new Token(WHILE, "while"),
                new Token(OPEN_PARENTHESIS, "("), new Token(IDENTIFIER, "a"),
                new Token(GREATER_THAN_OR_EQUAL, ">="), new Token(IDENTIFIER, "b"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(SEMICOLON, ";")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }


    /**
     * Tests recognizing <code>for</code> and <code>break</code>.
     */
    @Test
    public void testFor() {
        String line = "for (i = 1; i <= 10; i++) {";
            line += "\n    break;";
            line += "\n}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(FOR, "for"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "i"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "1"), new Token(SEMICOLON, ";"),
                new Token(IDENTIFIER, "i"), new Token(LESS_THAN_OR_EQUAL, "<="),
                new Token(INT_LITERAL, "10"), new Token(SEMICOLON, ";"),
                new Token(IDENTIFIER, "i"), new Token(INCREMENT, "++"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),

                new Token(BREAK, "break"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>switch</code>, <code>case</code>, <code>fallthrough</code>,
     * and <code>default</code>.
     */
    @Test
    public void testSwitch() {
        String line = "switch(a) {";
        line += "\ncase 1:";
        line += "\ncase 2 when true:";
        line += "\n    fallthrough;";
        line += "\ndefault:";
        line += "\n    out.println(a);";
        line += "\n}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(SWITCH, "switch"), new Token(OPEN_PARENTHESIS, "("), new Token(IDENTIFIER, "a"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),

                new Token(CASE, "case"),
                new Token(INT_LITERAL, "1"), new Token(COLON, ":"),

                new Token(CASE, "case"),
                new Token(INT_LITERAL, "2"), new Token(WHEN, "when"),
                new Token(TRUE, "true"), new Token(COLON, ":"),

                new Token(FALLTHROUGH, "fallthrough"), new Token(SEMICOLON, ";"),

                new Token(DEFAULT, "default"), new Token(COLON, ":"),

                new Token(IDENTIFIER, "out"), new Token(DOT, "."),
                new Token(IDENTIFIER, "println"), new Token(OPEN_PARENTHESIS, "("), new Token(IDENTIFIER, "a"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(SEMICOLON, ";"),

                new Token(CLOSE_BRACE, "}")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>try</code>, <code>catch</code>, and <code>finally</code>.
     */
    @Test
    public void testTryCatchFinally() {
        String line = "try {}";
            line += "\ncatch (RuntimeException e) {}";
            line += "\nfinally {}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(TRY, "try"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}"),

                new Token(CATCH, "catch"),
                new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "RuntimeException"), new Token(IDENTIFIER, "e"),
                new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}"),

                new Token(FINALLY, "finally"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing at-sign, <code>instanceof</code>, <code>is</code>, and <code>isnt</code>.
     */
    @Test
    public void testIsaOfIsAtSign() {
        String line = "#Derive @NotNull a is b a isa b a isnt b";

        List<Token> expectedTokens = Arrays.asList(
                new Token(HASHTAG, "#"), new Token(IDENTIFIER, "Derive"),
                new Token(AT_SIGN, "@"), new Token(IDENTIFIER, "NotNull"),
                new Token(IDENTIFIER, "a"), new Token(IS, "is"),
                new Token(IDENTIFIER, "b"),
                new Token(IDENTIFIER, "a"), new Token(ISA, "isa"),
                new Token(IDENTIFIER, "b"),
                new Token(IDENTIFIER, "a"), new Token(ISNT, "isnt"),
                new Token(IDENTIFIER, "b")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing comments.
     */
    @Test
    public void testComments() {
        String line = "foo // through end of line comment";
            line += "\nbar /* multi-";
            line += "\nline-";
            line += "\ncomment */ baz";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "foo"),  new Token(IDENTIFIER, "bar"),
                new Token(IDENTIFIER, "baz")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests remaining operators, question mark, ellipsis
     */
    @Test
    public void testMiscOperators() {
        String line = "a = == != <=> & &= | |= ^ ^= ~ && || &: ^: |: ! << <<= >> >>= + += - -= * *= / /= % %= <: :> ... ? b";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "a"),
                new Token(EQUAL, "="), new Token(DOUBLE_EQUAL, "=="),
                new Token(NOT_EQUAL, "!="), new Token(COMPARISON, "<=>"),
                new Token(AMPERSAND, "&"), new Token(AMPERSAND_EQUALS, "&="),
                new Token(PIPE, "|"),  new Token(PIPE_EQUALS, "|="),
                new Token(CARET, "^"), new Token(CARET_EQUALS, "^="),
                new Token(TILDE, "~"),
                new Token(DOUBLE_AMPERSAND, "&&"), new Token(DOUBLE_PIPE, "||"),
                new Token(AMPERSAND_COLON, "&:"), new Token(CARET_COLON, "^:"),
                new Token(PIPE_COLON, "|:"), new Token(EXCLAMATION, "!"),
                new Token(SHIFT_LEFT, "<<"), new Token(SHIFT_LEFT_EQUALS, "<<="),
                new Token(SHIFT_RIGHT, ">>"), new Token(SHIFT_RIGHT_EQUALS, ">>="),
                new Token(PLUS, "+"), new Token(PLUS_EQUALS, "+="),
                new Token(MINUS, "-"), new Token(MINUS_EQUALS, "-="),
                new Token(STAR, "*"), new Token(STAR_EQUALS, "*="),
                new Token(SLASH, "/"), new Token(SLASH_EQUALS, "/="),
                new Token(PERCENT, "%"), new Token(PERCENT_EQUALS, "%="),
                new Token(SUBTYPE, "<:"), new Token(SUPERTYPE, ":>"),
                new Token(THREE_DOTS, "..."),
                new Token(QUESTION_MARK, "?"),
                new Token(IDENTIFIER, "b")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests floating point literals.
     */
    @Test
    public void testFloatingPointLiterals() {
        String line = "3. 3.14 3.e+2 3.14e-2 .14 .14E2 3e+4";

        List<Token> expectedTokens = Arrays.asList(
                new Token(FLOATING_POINT_LITERAL, "3."), new Token(FLOATING_POINT_LITERAL, "3.14"),
                new Token(FLOATING_POINT_LITERAL, "3.e+2"), new Token(FLOATING_POINT_LITERAL, "3.14e-2"),
                new Token(FLOATING_POINT_LITERAL, ".14"), new Token(FLOATING_POINT_LITERAL, ".14E2"),
                new Token(FLOATING_POINT_LITERAL, "3e+4")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests map literals.
     */
    @Test
    public void testMapsTo() {
        String line = "var map = { \"one\" : 1, \"two\" : 2 };";

        List<Token> expectedTokens = Arrays.asList(
                new Token(VAR, "var"), new Token(IDENTIFIER, "map"),
                new Token(EQUAL, "="), new Token(OPEN_BRACE, "{"), new Token(STRING_LITERAL, "one"),
                new Token(COLON, ":"), new Token(INT_LITERAL, "1"), new Token(COMMA, ","),
                new Token(STRING_LITERAL, "two"), new Token(COLON, ":"), new Token(INT_LITERAL, "2"),
                new Token(CLOSE_BRACE, "}"), new Token(SEMICOLON, ";")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests lambda "->" method reference "::".
     */
    @Test
    public void testLambdaMethodReference() {
        String line = "a -> a + 1 Object::toString";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "a"), new Token(ARROW, "->"), new Token(IDENTIFIER, "a"),
                new Token(PLUS, "+"), new Token(INT_LITERAL, "1"), new Token(IDENTIFIER, "Object"),
                new Token(DOUBLE_COLON, "::"), new Token(IDENTIFIER, "toString")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests assertions.
     */
    @Test
    public void testAssert() {
        String line = "assert 0 < 1;";

        List<Token> expectedTokens = Arrays.asList(
                new Token(ASSERT, "assert"), new Token(INT_LITERAL, "0"),
                new Token(LESS_THAN, "<"), new Token(INT_LITERAL, "1"), new Token(SEMICOLON, ";")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Ensure that we catch an un-ended traditional/multiline comment.
     */
    @Test
    public void testErrorNoEndTradComment() {
        String line = "/* Not ended!\nEven after a newline!";
        Scanner scanner = new Scanner(line);
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch an empty character literal.
     */
    @Test
    public void testErrorEmptyCharLiteral() {
        String line = "char err = '';";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch a character literal that is too long.
     */
    @Test
    public void testErrorCharLiteralTooLong() {
        String line = "char err = 'ab';";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch a string literal not ended before the end of the
     * line.
     */
    @Test
    public void testErrorStringLiteralEndOfLine() {
        String line = "String err = \"Not ended!\nreturn;";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch a string literal not ended before the end of the
     * file.
     */
    @Test
    public void testErrorStringLiteralEndOfFile() {
        String line = "String err = \"Not ended!";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch a raw string literal not ended before the end of the
     * file.
     */
    @Test
    public void testErrorRawStringLiteralEndOfFile() {
        String line = "String err = \"\"\"Not ended!\nEven after a newline!";

        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch an illegal escape character.
     */
    @Test
    public void testErrorIllegalEscapeSequence() {
        String line = "String err = \"\\a\";";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch exponent indicator without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithoutDigits() {
        String line = "double d = 3.14e";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch exponent indicator with plus without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithPlusWithoutDigits() {
        String line = "double d = 3.14e+";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }

    /**
     * Ensure that we catch exponent indicator with plus without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithMinusWithoutDigits() {
        String line = "double d = 3.14e-";
        Scanner scanner = new Scanner(line);
        scanner.next();
        assertThrows(CompileException.class, scanner::next);
    }
}

