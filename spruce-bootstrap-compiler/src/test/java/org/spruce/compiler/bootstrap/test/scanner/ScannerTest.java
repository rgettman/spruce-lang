package org.spruce.compiler.bootstrap.test.scanner;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

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
            System.out.println(loc.getFileLinePos() + ": " + token);
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
                assertFalse(token.getCompilerMessage().isPresent());
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
     * Helper method to ensure that the given token is an error token.
     * The <code>CompilerMessage</code> is expected to exist on the <code>Token</code>,
     * and its level is supposed to be <code>ERROR</code>.
     * @param test The <code>Token</code> to test.
     */
    private void ensureErrorToken(Token test) {
        assertTrue(test.getCompilerMessage().isPresent());
        assertEquals(CompilerMessage.Level.ERROR, test.getCompilerMessage().get().getLevel());
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
        String line = "class Test {}";
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(CLASS, "class"), new Token(IDENTIFIER, "Test"),
                new Token(OPEN_BRACE, "{"), new Token(CLOSE_BRACE, "}")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>constructor</code>.
     */
    @Test
    public void testConstructorAndOtherModifiers() {
        String line = "constructor(Integer nbr, constant List<String> words) { super(); } ";  // Ends with a tab
        Scanner scanner = new Scanner(line);

        List<Token> expectedTokens = Arrays.asList(
                new Token(CONSTRUCTOR, "constructor"),
                new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "Integer"),
                new Token(IDENTIFIER, "nbr"), new Token(COMMA, ","),
                new Token(CONSTANT, "constant"), new Token(IDENTIFIER, "List"),
                new Token(LESS_THAN, "<"), new Token(IDENTIFIER, "String"),
                new Token(GREATER_THAN, ">"), new Token(IDENTIFIER, "words"),
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
        String line = """
            override void testMethod() {
                boolean t = true;
                boolean f = false;
                byte b = 1;
                short s = 2;
                int i = 3;
                long l = 4;
                float f = 5;
                double d = 6 as double;
                char c = '7';
                String str = "none";
                return self;
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(OVERRIDE, "override"),
                new Token(VOID, "void"), new Token(IDENTIFIER, "testMethod"),
                new Token(OPEN_PARENTHESIS, "("), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"),

                new Token(IDENTIFIER, "boolean"),
                new Token(IDENTIFIER, "t"), new Token(EQUAL, "="),
                new Token(TRUE, "true"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "boolean"),
                new Token(IDENTIFIER, "f"), new Token(EQUAL, "="),
                new Token(FALSE, "false"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "byte"),
                new Token(IDENTIFIER, "b"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "1"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "short"),
                new Token(IDENTIFIER, "s"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "2"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "int"),
                new Token(IDENTIFIER, "i"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "3"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "long"),
                new Token(IDENTIFIER, "l"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "4"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "float"),
                new Token(IDENTIFIER, "f"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "5"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "double"),
                new Token(IDENTIFIER, "d"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "6"), new Token(AS, "as"),
                new Token(IDENTIFIER, "double"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "char"),
                new Token(IDENTIFIER, "c"), new Token(EQUAL, "="),
                new Token(CHARACTER_LITERAL, "7"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "String"),
                new Token(IDENTIFIER, "str"), new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "none"), new Token(SEMICOLON, ";"),

                new Token(RETURN, "return"),
                new Token(SELF, "self"), new Token(SEMICOLON, ";")
        );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing string literals, including escape sequences.
     * Carriage return in a text block gets converted to a newline.
     */
    @Test
    public void testStringLiterals() {
        String text = "String escapeTest = \"Test #1: \\b\\t\\n\\f\\r\\s\\0\\\"\\'\\\\\";\n";
        text += """
            String literalTest = \"""\s   
                Test #2: \\t\\b\\n    \\f!\\r    \\s\\0\\"\\'\\\\
                \""";
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "String"), new Token(IDENTIFIER, "escapeTest"),
                new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "Test #1: \b\t\n\f\r \0\"'\\"), new Token(SEMICOLON, ";"),

                new Token(IDENTIFIER, "String"), new Token(IDENTIFIER, "literalTest"),
                new Token(EQUAL, "="),
                new Token(STRING_LITERAL, "Test #2: \t\b\n\f!\n \0\"'\\\n"), new Token(SEMICOLON, ";")
        );
        Scanner scanner = new Scanner(text);
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
        scanner.next();
        ensureErrorToken(scanner.getCurrToken());
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
        scanner.next();
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Tests text block-only escape sequences.
     */
    @Test
    public void testTextBlockEscapes() {
        String code = """
                \"""
                Lorem ipsum dolor sit amet,\\s\\
                consectetur adipiscing elit,
                \"""
                """;
        Scanner scanner = new Scanner(code);
        List<Token> expectedTokens = Arrays.asList(
                new Token(STRING_LITERAL,
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n")
        );
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>if</code>, <code>else</code>, and <code>throw</code>.
     */
    @Test
    public void testIfElse() {
        String line = """
            if (b < s) { i = i + 1; }
            else if (b > s) { i = i + 2; }
            else { i = i + 3; }
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(IF, "if"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "b"), new Token(LESS_THAN, "<"),
                new Token(IDENTIFIER, "s"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "i"),
                new Token(EQUAL, "="), new Token(IDENTIFIER, "i"),
                new Token(PLUS, "+"), new Token(INT_LITERAL, "1"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(ELSE, "else"),
                new Token(IF, "if"),new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "b"), new Token(GREATER_THAN, ">"),
                new Token(IDENTIFIER, "s"), new Token(CLOSE_PARENTHESIS, ")"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "i"),
                new Token(EQUAL, "="), new Token(IDENTIFIER, "i"),
                new Token(PLUS, "+"), new Token(INT_LITERAL, "2"),
                new Token(SEMICOLON, ";"), new Token(CLOSE_BRACE, "}"),

                new Token(ELSE, "else"),
                new Token(OPEN_BRACE, "{"), new Token(IDENTIFIER, "i"),
                new Token(EQUAL, "="), new Token(IDENTIFIER, "i"),
                new Token(PLUS, "+"), new Token(INT_LITERAL, "3"),
                new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
                );
        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>do</code>, <code>while</code>, and <code>continue</code>.
     */
    @Test
    public void testWhileContinue() {
        String line = """
            while (a >= b) {
                b = b + 10;
                continue;
            }
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(WHILE, "while"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "a"),
                new Token(GREATER_THAN_OR_EQUAL, ">="), new Token(IDENTIFIER, "b"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),

                new Token(IDENTIFIER, "b"), new Token(EQUAL, "="),
                new Token(IDENTIFIER, "b"), new Token(PLUS, "+"),
                new Token(INT_LITERAL, "10"), new Token(SEMICOLON, ";"),

                new Token(CONTINUE, "continue"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }


    /**
     * Tests recognizing <code>for</code> and <code>break</code>.
     */
    @Test
    public void testFor() {
        String line = "for (i = 1; i <= 10; i = i + 1) {";
            line += "\n    break;";
            line += "\n}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(FOR, "for"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "i"), new Token(EQUAL, "="),
                new Token(INT_LITERAL, "1"), new Token(SEMICOLON, ";"),
                new Token(IDENTIFIER, "i"), new Token(LESS_THAN_OR_EQUAL, "<="),
                new Token(INT_LITERAL, "10"), new Token(SEMICOLON, ";"),
                new Token(IDENTIFIER, "i"), new Token(EQUAL, "="),
                new Token(IDENTIFIER, "i"), new Token(PLUS, "+"),
                new Token(INT_LITERAL, "1"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),

                new Token(BREAK, "break"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
                );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>for</code> and <code>in</code>.
     */
    @Test
    public void testForIn() {
        String line = "for (Int i in integers) {";
        line += "\n    break;";
        line += "\n}";

        List<Token> expectedTokens = Arrays.asList(
                new Token(FOR, "for"), new Token(OPEN_PARENTHESIS, "("),
                new Token(IDENTIFIER, "Int"),
                new Token(IDENTIFIER, "i"), new Token(IN, "in"),
                new Token(IDENTIFIER, "integers"),
                new Token(CLOSE_PARENTHESIS, ")"), new Token(OPEN_BRACE, "{"),

                new Token(BREAK, "break"), new Token(SEMICOLON, ";"),
                new Token(CLOSE_BRACE, "}")
        );

        Scanner scanner = new Scanner(line);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests recognizing <code>isa</code>.
     */
    @Test
    public void testIsa() {
        String line = "a isa b";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "a"), new Token(ISA, "isa"),
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
        String text = """
            foo // through end of line comment
            bar /* multi-";
            line-";
            comment */ baz
            """;

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "foo"),  new Token(IDENTIFIER, "bar"),
                new Token(IDENTIFIER, "baz")
        );

        Scanner scanner = new Scanner(text);
        compareToExpected(expectedTokens, scanner);
    }

    /**
     * Tests remaining operators
     */
    @Test
    public void testMiscOperators() {
        String line = "a = == != && || ! + - b";

        List<Token> expectedTokens = Arrays.asList(
                new Token(IDENTIFIER, "a"),
                new Token(EQUAL, "="), new Token(DOUBLE_EQUAL, "=="),
                new Token(EXCLAMATION_EQUAL, "!="),
                new Token(DOUBLE_AMPERSAND, "&&"), new Token(DOUBLE_PIPE, "||"),
                new Token(EXCLAMATION, "!"),
                new Token(PLUS, "+"), new Token(MINUS, "-"),
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
     * Ensure that we catch an un-ended traditional/multiline comment.
     */
    @Test
    public void testErrorNoEndTradComment() {
        String line = """
                /* Not ended!
                Even after a newline!
                """;
        Scanner scanner = new Scanner(line);
        scanner.next();
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch an empty character literal.
     */
    @Test
    public void testErrorEmptyCharLiteral() {
        String line = "char err = '';";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch a character literal that is too long.
     */
    @Test
    public void testErrorCharLiteralTooLong() {
        String line = "char err = 'ab';";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch a string literal not ended before the end of the
     * line.
     */
    @Test
    public void testErrorStringLiteralEndOfLine() {
        String line = "String err = \"Not ended!\nreturn;";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch a string literal not ended before the end of the
     * file.
     */
    @Test
    public void testErrorStringLiteralEndOfFile() {
        String line = "String err = \"Not ended!";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch a raw string literal not ended before the end of the
     * file.
     */
    @Test
    public void testErrorRawStringLiteralEndOfFile() {
        String line = "String err = \"\"\"Not ended!\nEven after a newline!";

        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch an illegal escape character.
     */
    @Test
    public void testErrorIllegalEscapeSequence() {
        String line = "String err = \"\\a\";";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch exponent indicator without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithoutDigits() {
        String line = "double d = 3.14e";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch exponent indicator with plus without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithPlusWithoutDigits() {
        String line = "double d = 3.14e+";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure that we catch exponent indicator with plus without digits.
     */
    @Test
    public void testErrorFloatLiteralExpWithMinusWithoutDigits() {
        String line = "double d = 3.14e-";
        Scanner scanner = new Scanner(line);
        for (int i = 0; i < 4; i++) {
            scanner.next();
        }
        ensureErrorToken(scanner.getCurrToken());
    }

    /**
     * Ensure unrecognized character is an error.
     */
    @Test
    public void testErrorUnrecognizedCharacter() {
        String line = "#";
        Scanner scanner = new Scanner(line);
        scanner.next();
        ensureErrorToken(scanner.getCurrToken());
    }
}
