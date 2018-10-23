package org.spruce.compiler.scanner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.spruce.compiler.exception.CompileException;

/**
 * Reads input from a Reader representing a compilation unit.
 * Scans tokens from the text read from the Reader.
 */
public class Scanner
{
    private boolean amInTypeContext;
    private List<String> myLines;

    private String myFilename;
    private int myLineNbr;
    private int myCharPos;

    private int myTokenLineNbr;
    private int myTokenCharPos;

    private Token myCurrToken;
    private Token myNextToken;
    private Token myPeekToken;

    /**
     * Constructs a <code>Scanner</code> based on a <code>String</code>.
     * @param contents The contents of the code to scan.
     */
    public Scanner(String contents)
    {
        Objects.requireNonNull(contents);

        myFilename = "<no file>";
        myLines = Arrays.asList(contents.split("\\R", -1));
        init();
    }

    /**
     * Construcs a <code>Scanner</code> that will scan the contents of the
     * file specified by the given filename, in the default character set.
     * @param path The path of the file to read.
     * @throws IOException If there is a problem reading the file.
     */
    public Scanner(Path path) throws IOException
    {
        this(path, Charset.defaultCharset());
    }

    /**
     * Constructs a <code>Scanner</code> that will scan the contents of the
     * file specified by the given filename, in the given character set.
     * @param path The path of the file to read.
     * @param charset The <code>Charset</code> of the file.
     * @throws IOException If there is a problem reading the file.
     */
    public Scanner(Path path, Charset charset) throws IOException
    {
        Objects.requireNonNull(path);
        Objects.requireNonNull(charset);

        myFilename = path.toString();
        myLines = Files.readAllLines(path);
        init();
    }

    /**
     * Sets the line number and char pos to 0, with the current token being
     * unknown.
     */
    private void init()
    {
        myLineNbr = 0;
        myCharPos = 0;
        amInTypeContext = false;
    }

    /**
     * Returns whether a type context is active.
     * @return Whether a type context is active.
     */
    public boolean isInTypeContext()
    {
        return amInTypeContext;
    }

    /**
     * Sets whether a type context is active.
     * @param inTypeContext Whether a type context is active.
     */
    public void setInTypeContext(boolean inTypeContext)
    {
        amInTypeContext = inTypeContext;
    }

    /**
     * Returns the current <code>Token</code>, or <code>null</code> if there
     * isn't one yet.
     * @return The current <code>Token</code>, or <code>null</code> if there
     * isn't one yet.
     */
    public Token getCurrToken()
    {
        return myCurrToken;
    }

    /**
     * Returns the next <code>Token</code>, or <code>null</code> if there
     * isn't one yet.  If there is a current <code>Token</code>, determines the
     * next <code>Token</code> and returns it.
     * @return The next <code>Token</code>, or <code>null</code> if there
     * isn't one yet.
     */
    public Token getNextToken()
    {
        return myNextToken;
    }

    /**
     * Returns the peek <code>Token</code>, or <code>null</code> if there
     * isn't one yet.  If there is a peek <code>Token</code>, determines the
     * next <code>Token</code> after that and returns it.
     * @return The peek <code>Token</code>, or <code>null</code> if there
     * isn't one yet.
     */
    public Token getPeekToken()
    {
        return myPeekToken;
    }

    /**
     * Returns a new current token type/value.
     * @param t The token type.
     * @param value The string value.
     * @return A new <code>Token</code>.
     */
    private Token createToken(TokenType t, String value)
    {
        Location loc = new Location(myFilename, myTokenLineNbr, myTokenCharPos, myLines.get(myTokenLineNbr));
        return new Token(loc, t, value);
    }

    /**
     * Advances to the next token.  Skips whitespace and comments.
     * @return Whether there is another token before EOF to be read.
     */
    public boolean next()
    {
        if (myCurrToken == null)
        {
            myCurrToken = advanceSkippingWhitespaceComments();
            myNextToken = advanceSkippingWhitespaceComments();
            myPeekToken = advanceSkippingWhitespaceComments();
        }
        else if (myPeekToken.getType() != TokenType.EOF)
        {
            myCurrToken = myNextToken;
            myNextToken = myPeekToken;
            myPeekToken = advanceSkippingWhitespaceComments();
        }
        else if (myNextToken.getType() != TokenType.EOF)
        {
            myCurrToken = myNextToken;
            myNextToken = myPeekToken;
        }
        else if (myCurrToken.getType() != TokenType.EOF)
        {
            myCurrToken = myNextToken;
        }

        return myCurrToken.getType() != TokenType.EOF;
    }

    /**
     * Helper method to scan for the next token, skipping whitespace and comments.
     * @return The next non-whitespace, non-comment token.
     */
    private Token advanceSkippingWhitespaceComments()
    {
        Token t;
        do
        {
            t = advance();
        }
        while (t.getType() == TokenType.WHITESPACE || t.getType() == TokenType.COMMENT);
        return t;
    }

    /**
     * Advances to the next token.
     * @return The next token.
     */
    private Token advance()
    {
        // Note down start of token position.
        myTokenLineNbr = myLineNbr;
        myTokenCharPos = myCharPos;

        Token t;

        char first = peek();
        if (first == (char) -1)
        {
            t = createToken(TokenType.EOF, null);
        }
        else if (Character.isWhitespace(first))
        {
            readWhitespace();
            t = createToken(TokenType.WHITESPACE, null);
        }
        // numeric literal: int/long/float/double/BigInteger/BigDecimal
        else if (Character.isDigit(first))
        {
            t = readNumericLiteral();
        }
        else if (Character.isJavaIdentifierStart(first))
        {
            t = readIdentifierOrKeyword();
        }
        else
        {
            switch(first)
            {
            case '"':
                t = readStringLiteral();
                break;
            case '\'':
                t = readCharacterLiteral();
                break;
            case '@':
                t = createToken(TokenType.AT_SIGN, String.valueOf(read()));
                break;
            case '{':
                t = createToken(TokenType.OPEN_BRACE, String.valueOf(read()));
                break;
            case '}':
                t = createToken(TokenType.CLOSE_BRACE, String.valueOf(read()));
                break;
            case '[':
                t = readStartingWithOpenBracket();
                break;
            case ']':
                t = createToken(TokenType.CLOSE_BRACKET, String.valueOf(read()));
                break;
            case '(':
                t = createToken(TokenType.OPEN_PARENTHESIS, String.valueOf(read()));
                break;
            case ')':
                t = createToken(TokenType.CLOSE_PARENTHESIS, String.valueOf(read()));
                break;
            case ',':
                t = createToken(TokenType.COMMA, String.valueOf(read()));
                break;
            case ':':
                t = readStartingWithColon();
                break;
            case ';':
                t = createToken(TokenType.SEMICOLON, String.valueOf(read()));
                break;
            case '?':
                t = createToken(TokenType.QUESTION_MARK, String.valueOf(read()));
                break;
            case '=':
                t = readStartingWithEquals();
                break;
            case '.':
                t = readStartingWithDot();
                break;
            case '!':
                t = readStartingWithExclamation();
                break;
            case '<':
                t = readStartingWithLessThan();
                break;
            case '>':
                t = readStartingWithGreaterThan();
                break;
            case '+':
                t = readStartingWithPlus();
                break;
            case '-':
                t = readStartingWithMinus();
                break;
            case '*':
                t = readStartingWithStar();
                break;
            case '/':
                t = readStartingWithSlash();
                break;
            case '%':
                t = readStartingWithPercent();
                break;
            case '&':
                t = readStartingWithAmpersand();
                break;
            case '|':
                t = readStartingWithPipe();
                break;
            case '^':
                t = readStartingWithCaret();
                break;
            case '~':
                t = createToken(TokenType.BITWISE_COMPLEMENT, String.valueOf(read()));
                break;
            default:
                t = createToken(TokenType.UNKNOWN, String.valueOf(read()));
            }
        }
        return t;
    }

    /**
     * Scans whitespace until we encounter a non-whitespace character.
     */
    private void readWhitespace()
    {
        while (Character.isWhitespace(peek()))
        {
            read();
        }
    }

    /**
     * Reads an identifier or keyword.
     * @return The <code>Token</code> associated with the identifier or keyword.
     */
    private Token readIdentifierOrKeyword()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(read());
        while (Character.isJavaIdentifierPart(peek()))
        {
            buf.append(read());
        }
        String result = buf.toString();
        TokenType keyword = TokenType.forRepresentation(result);
        if (keyword != null)
        {
            return createToken(keyword, result);
        }
        else
        {
            return createToken(TokenType.IDENTIFIER, result);
        }
    }

    /**
     * Reads until the end of a traditional comment, "&#42;/".
     * @return The comment text.
     * @throws CompileException If the end of the file was reached before the
     *     end of the traditional comment.
     */
    private String readCommentUntilEndComment()
    {
        StringBuilder buf = new StringBuilder();
        char c;
        boolean endCommentReached = false;
        while ( (c = read()) != (char) -1)
        {
            if (c == '*')
            {
                if (peek() == '/')
                {
                    read();
                    endCommentReached = true;
                    break;
                }
            }
            buf.append(c);
        }
        if (!endCommentReached)
        {
            throw new CompileException("End of file reached before end of traditional comment!");
        }
        return buf.toString();
    }

    /**
     * Reads until the end of the line or file.
     * @return The comment text.
     * @throws CompileException If the end of the file was reached before the
     *     end of the traditional comment.
     */
    private String readCommentUntilEndOfLine()
    {
        StringBuilder buf = new StringBuilder();
        char c;
        while (true)
        {
            c = peek();
            // '\n' by itself
            if (c == '\n')
            {
                read();
                break;
            }
            else if (c == (char) -1)
            {
                break;
            }
            read();
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Reads a character literal, which must be exactly one character enclosed
     * in single quotes.  Escape characters are respected.
     * @return The <code>Token</code> associated with the character literal.
     */
    private Token readCharacterLiteral()
    {
        read();
        if (peek() == '\'')
        {
            throw new CompileException("Illegal empty character literal.");
        }

        Token t;

        switch(peek())
        {
        case '\\':
            read();
            t = createToken(TokenType.CHARACTER_LITERAL, String.valueOf(applyEscape()));
            break;
        default:
            t = createToken(TokenType.CHARACTER_LITERAL, String.valueOf(read()));
            break;
        }
        if (read() != '\'')
        {
            throw new CompileException("Illegal unclosed character literal.");
        }
        return t;
    }

    /**
     * Reads a string literal until an unescaped double-quote character.  If
     * two more additional double-quote characters are read, making three
     * consecutive, then the string is read without escapes and possibly with
     * newlines.
     * @return The <code>Token</code> associated with the string literal.
     * @throws CompileException If end-of-line or end-of-file occurs before the
     *     next double-quote character.
     */
    private Token readStringLiteral()
    {
        read();
        if (peek() == '"')
        {
            read();
            if (peek() == '"')
            {
                read();
                return readUnescapedMultilineStringLiteral();
            }
        }
        StringBuilder buf = new StringBuilder();
        while (peek() != '"')
        {
            // Escapes
            switch(peek())
            {
            case '\\':
                read();
                buf.append(applyEscape());
                break;
            case '\n':
            case '\r':
                throw new CompileException("String not terminated before end of line.");
            case (char) -1:
                throw new CompileException("String not terminated before end of file.");
            default:
                buf.append(read());
                break;
            }
        }
        // Advance past closing double-quote.
        read();
        return createToken(TokenType.STRING_LITERAL, buf.toString());
    }

    /**
     * We have already read the backslash character.  Now read the next
     * character and determine what escape character it is, if it is a valid
     * escape character.  Apply the escape and return the char.
     * @return The character that the escape sequence represents.
     */
    private char applyEscape()
    {
        switch (read())
        {
        case 'b':
            return '\b';
        case 'f':
            return '\f';
        case 'n':
            return '\n';
        case 'r':
            return '\r';
        case 't':
            return '\t';
        case '"':
            return '"';
        case '\'':
            return '\'';
        case '\\':
            return '\\';
        default:
            throw new CompileException("Illegal escape sequence: \\" + peek());
        }
    }

    /**
     * Reads an unescaped string literal where newlines are allowed.  The
     * literal is ended by 3 consecutive double-quote characters in the source.
     * More than 3 consecutive means that additional double-quote characters
     * are appended to the literal.
     * @return The <code>Token</code> associated with the string literal.
     */
    private Token readUnescapedMultilineStringLiteral()
    {
        StringBuilder buf = new StringBuilder();
        boolean terminated = false;
        while (!terminated)
        {
            switch(peek())
            {
            case '"':
                read();
                if (peek() == '"')
                {
                    read();
                    if (peek() == '"')
                    {
                        read();
                        terminated = true;
                        break;
                    }
                    else
                    {
                        buf.append("\"\"");
                    }
                }
                else
                {
                    buf.append("\"");
                }
                break;
            case (char) -1:
                throw new CompileException("String not terminated before end of file.");
            default:
                buf.append(read());
                break;
            }
        }
        // Support double-quote characters at the end of the triple-double-
        // quoted string literal.  If n >= 3, and we have n double-quotes at
        // the end of the string, then append (n - 3) double-quotes to the
        // literal.
        // Here we've read 3 double-quote characters already.
        while (peek() == '"')
        {
            buf.append(read());
        }

        return createToken(TokenType.STRING_LITERAL, buf.toString());
    }

    /**
     * Reads an integer literal.  This will also read long literals, float
     * literals, double literals, <code>BigInteger</code> literals, and
     * <code>BigDecimal</code> literals.
     * @return The <code>Token</code> associated with the numeric literal.
     */
    private Token readNumericLiteral()
    {
        StringBuilder buf = new StringBuilder();
        while (Character.isDigit(peek()))
        {
            buf.append(read());
        }
        char c = peek();
        if (c == 'e' || c == 'E' || c == '.')
        {
            return readFloatingPointLiteral(buf, c == '.');
        }
        else
        {
            return createToken(TokenType.INT_LITERAL, buf.toString());
        }
    }

    /**
     * Reads a floating point literal.  When this method is called, either of
     * the following have already happened.
     * <ul>
     *     <li>Digits have been read, and one of <code>e E .</code> is next.</li>
     *     <li>A decimal point has been read, and a digit is next.</li>
     * </ul>
     * @param soFar What has been read so far into the token.
     * @param dotIsNext Whether a <code>.</code> is next to be read.
     * @return The <code>Token</code> associated with the floating point literal.
     */
    private Token readFloatingPointLiteral(StringBuilder soFar, boolean dotIsNext)
    {
        if (dotIsNext)
        {
            soFar.append(read());
        }
        while (Character.isDigit(peek()))
        {
            soFar.append(read());
        }
        char expPart = peek();
        // Exponent part.
        if (expPart == 'e' || expPart == 'E')
        {
            soFar.append(read());
            char next = peek();
            if (next == '+' || next == '-')
            {
                soFar.append(read());
            }
            if (!Character.isDigit(peek()))
            {
                throw new CompileException("Invalid floating point literal; missing exponent");
            }
            while (Character.isDigit(peek()))
            {
                soFar.append(read());
            }
        }
        return createToken(TokenType.FLOATING_POINT_LITERAL, soFar.toString());
    }

    /**
     * Scans "[" and "[]".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithOpenBracket()
    {
        read();
        if (peek() == ']')
        {
            read();
            return createToken(TokenType.OPEN_CLOSE_BRACKET, "[]");
        }
        else
        {
            return createToken(TokenType.OPEN_BRACKET, "[");
        }
    }

    /**
     * Scans "::", ":=", ":&gt;", and ":".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithColon()
    {
        read();
        switch (peek())
        {
        case ':':
            read();
            return createToken(TokenType.DOUBLE_COLON, "::");
        case '=':
            read();
            return createToken(TokenType.ASSIGNMENT, ":=");
        case '>':
            read();
            return createToken(TokenType.SUPERTYPE, ":>");
        default:
            return createToken(TokenType.COLON, ":");
        }
    }

    /**
     * Scans "=".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithEquals()
    {
        read();
        return createToken(TokenType.EQUAL, "=");
    }

    /**
     * Scans "...", ".", and floating point literals that start with ".".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithDot()
    {
        read();
        if (Character.isDigit(peek()))
        {
            return readFloatingPointLiteral(new StringBuilder("."), true);
        }
        switch (peek())
        {
        case '.':
            read();
            if (peek() == '.')
            {
                read();
                return createToken(TokenType.ELLIPSIS, "...");
            }
            else
            {
                putBack();
            }
            // FALLTHROUGH!
        default:
            return createToken(TokenType.DOT, ".");
        }
    }

    /**
     * Scans "!=" and "!".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithExclamation()
    {
        read();
        switch (peek())
        {
        case '=':
            read();
            return createToken(TokenType.NOT_EQUAL, "!=");
        default:
            return createToken(TokenType.LOGICAL_COMPLEMENT, "!");
        }
    }

    /**
     * Scans "&lt;&lt;", "&lt;&lt;=", "&lt;=", "&lt;=&gt;", "&lt;:", and "&lt;".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithLessThan()
    {
        read();
        switch (peek())
        {
        case '=':
            read();
            if (peek() == '>')
            {
                read();
                return createToken(TokenType.COMPARISON, "<=>");
            }
            else
            {
                return createToken(TokenType.LESS_THAN_OR_EQUAL, "<=");
            }
        case '<':
            read();
            if (peek() == '=')
            {
                read();
                return createToken(TokenType.SHIFT_LEFT_EQUALS, "<<=");
            }
            else
            {
                return createToken(TokenType.SHIFT_LEFT, "<<");
            }
        case ':':
            read();
            return createToken(TokenType.SUBTYPE, "<:");
        default:
            return createToken(TokenType.LESS_THAN, "<");
        }
    }

    /**
     * Scans "&gt;&gt;", "&gt;&gt;=", "&gt;=", "&gt;&gt;&gt;", "&gt;&gt;&gt;=", and "&gt;".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithGreaterThan()
    {
        read();
        switch (peek())
        {
        case '=':
            read();
            return createToken(TokenType.GREATER_THAN_OR_EQUAL, ">=");
        case '>':
            if (!amInTypeContext)
            {
                read();
                switch (peek())
                {
                case '>':
                    read();
                    if (peek() == '=')
                    {
                        read();
                        return createToken(TokenType.UNSIGNED_SHIFT_RIGHT_EQUALS, ">>>=");
                    } else
                    {
                        return createToken(TokenType.UNSIGNED_SHIFT_RIGHT, ">>>");
                    }
                case '=':
                    read();
                    return createToken(TokenType.SHIFT_RIGHT_EQUALS, ">>=");
                default:
                    return createToken(TokenType.SHIFT_RIGHT, ">>");
                }
            }
            /* FALLTHROUGH */
        default:
            return createToken(TokenType.GREATER_THAN, ">");
        }
    }

    /**
     * Scans "++", "+=", and "+".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithPlus()
    {
        read();
        switch(peek())
        {
        case '+':
            read();
            return createToken(TokenType.INCREMENT, "++");
        case '=':
            read();
            return createToken(TokenType.PLUS_EQUALS, "+=");
        default:
            return createToken(TokenType.PLUS, "+");
        }
    }

    /**
     * Scans "--", "-=", "->", and "-".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithMinus()
    {
        read();
        switch(peek())
        {
        case '-':
            read();
            return createToken(TokenType.DECREMENT, "--");
        case '=':
            read();
            return createToken(TokenType.MINUS_EQUALS, "-=");
        case '>':
            read();
            return createToken(TokenType.LAMBDA_MAPS_TO, "->");
        default:
            return createToken(TokenType.MINUS, "-");
        }
    }

    /**
     * Scans "*=" and "*".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithStar()
    {
        read();
        if (peek() == '=')
        {
            read();
            return createToken(TokenType.STAR_EQUALS, "*=");
        }
        else
        {
            return createToken(TokenType.STAR, "*");
        }
    }

    /**
     * Scans "/=" and "/".  Also scans "traditional" comment start/comment/end
     * comment, e.g. <code>/&#42; comment &#42;/</code>.  Also scans "end of
     * line" comment, e.g. <code>// comment &lt;end-of-line&gt;</code>
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithSlash()
    {
        read();
        switch(peek())
        {
        case '=':
            read();
            return createToken(TokenType.SLASH_EQUALS, "/=");
        case '*':
            read();
            return createToken(TokenType.COMMENT, readCommentUntilEndComment());
        case '/':
            read();
            return createToken(TokenType.COMMENT, readCommentUntilEndOfLine());
        default:
            return createToken(TokenType.SLASH, "/");
        }
    }

    /**
     * Scans "%=" and "%".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithPercent()
    {
        read();
        switch(peek())
        {
        case '=':
            read();
            return createToken(TokenType.PERCENT_EQUALS, "%=");
        default:
            return createToken(TokenType.PERCENT, "%");
        }
    }

    /**
     * Scans "&=", "&&", "&:", and "&".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithAmpersand()
    {
        read();
        switch(peek())
        {
        case '=':
            read();
            return createToken(TokenType.AND_EQUALS, "&=");
        case '&':
            read();
            return createToken(TokenType.CONDITIONAL_AND, "&&");
        case ':':
            read();
            return createToken(TokenType.LOGICAL_AND, "&:");
        default:
            return createToken(TokenType.BITWISE_AND, "&");
        }
    }

    /**
     * Scans "|=", "||", "|:", and "|".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithPipe()
    {
        read();
        switch(peek())
        {
        case '=':
            read();
            return createToken(TokenType.OR_EQUALS, "|=");
        case '|':
            read();
            return createToken(TokenType.CONDITIONAL_OR, "||");
        case ':':
            read();
            return createToken(TokenType.LOGICAL_OR, "|:");
        default:
            return createToken(TokenType.BITWISE_OR, "|");
        }
    }

    /**
     * Scans "^=" and "^".
     * @return The appropriate <code>Token</code>.
     */
    private Token readStartingWithCaret()
    {
        read();
        switch(peek())
        {
        case '=':
            read();
            return createToken(TokenType.XOR_EQUALS, "^=");
        case ':':
            read();
            return createToken(TokenType.LOGICAL_XOR, "^:");
        default:
            return createToken(TokenType.BITWISE_XOR, "^");
        }
    }

    /**
     * Reads the next character, advancing to it.
     * @return The next character, or <code>(char) -1</code> if EOF.
     */
    private char read()
    {
        char c = peek();
        myCharPos++;
        if (c == '\n')
        {
            myCharPos = 0;
            myLineNbr++;
        }
        return c;
    }

    /**
     * Peeks at the next character, not advancing to it.
     * @return The next character, or <code>(char) -1</code> if EOF.
     */
    private char peek()
    {
        String currLine = myLines.get(myLineNbr);
        if (myCharPos >= currLine.length())
        {
            if (myLineNbr == myLines.size() - 1)
            {
                return (char) -1; // EOF
            }
            else
            {
                return '\n';
            }
        }
        return currLine.charAt(myCharPos);
    }

    /**
     * Effectively "puts back" the most recent character read.  It just backs
     * up the counter.
     */
    private void putBack()
    {
        if (myCharPos == 0)
        {
            throw new IllegalStateException("Internal error: Attempted to put back before beginning of line!");
        }
        myCharPos--;
    }
}
