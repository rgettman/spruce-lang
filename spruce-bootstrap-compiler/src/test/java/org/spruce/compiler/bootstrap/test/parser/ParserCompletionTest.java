package org.spruce.compiler.bootstrap.test.parser;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.parser.BasicParser;
import org.spruce.compiler.bootstrap.parser.ClassesParser;
import org.spruce.compiler.bootstrap.parser.ExpressionsParser;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.parser.StatementsParser;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;
import org.spruce.compiler.bootstrap.scanner.TokenType;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Makes sure that the parser completes, even with random junk.  Can't have an
 * infinite loop in the parser.
 */
public class ParserCompletionTest {
    /**
     * A <code>TokenCountingScanner</code> is a <code>Scanner</code> that
     * keeps track of how many tokens it has scanned.
     */
    private static class TokenCountingScanner extends Scanner {
        private final AtomicInteger myTokenNbr;

        /**
         * Constructs a <code>TokenCountingScanner</code> that will count the
         * number of tokens it has generated.
         * @param contents The contents of the code to scan.
         */
        public TokenCountingScanner(String contents) {
            super(contents);
            myTokenNbr = new AtomicInteger(0);
        }

        /**
         * Generate a random token instead of scanning the input for the next
         * token.  Does not generate <code>UNKNOWN</code>, <code>WHITESPACE</code>,
         * or <code>COMMENT</code>.  The only time <code>EOF</code> will be
         * generated is for the last token.
         * @return A random <code>Token</code>.
         */
        @Override
        protected Token advance() {
            Token advance = super.advance();
            myTokenNbr.incrementAndGet();
            return advance;
        }

        /**
         * Returns how many tokens have been generated.
         * @return How many tokens have been generated.
         */
        public int getNumTokensGenerated() {
            return myTokenNbr.get();
        }
    }

    /**
     * Try to parse a stream of random tokens.  Sure, there will be lots of
     * errors, but the parser must complete, not going into an infinite loop.
     */
    @Test
    public void testRandomTokens() {
        String code = generateRandomSourceCode(200);
        TopLevelParser parser = new Parser(new TokenCountingScanner(code), new BaseMessageProducer()).getTopLevelParser();
        parseCodeWithTimeLimitAndCountTokens(code, parser, parser::parseOrdinaryCompilationUnit);
    }

    /**
     * Try to parse a stream of random tokens inside a class body.  Sure, there
     * will be lots of errors, but the parser must complete, not going into an
     * infinite loop.
     */
    @Test
    public void testRandomTokensInClass() {
        String code = "class RandomTokensInClass {\n" + generateRandomSourceCode(20) + "\n}";
        TopLevelParser parser = new Parser(new TokenCountingScanner(code), new BaseMessageProducer()).getTopLevelParser();
        parseCodeWithTimeLimitAndCountTokens(code, parser, parser::parseTypeDeclaration);
    }

    /**
     * Try to parse a stream of random tokens inside a formal parameter list.  Sure, there
     * will be lots of errors, but the parser must complete, not going into an
     * infinite loop.
     */
    @Test
    public void testRandomTokensInFormalParameters() {
        String code = "class RandomTokensInClass {\n" + generateRandomSourceCode(20) + "\n}";
        ClassesParser parser = new Parser(new TokenCountingScanner(code), new BaseMessageProducer()).getClassesParser();
        parseCodeWithTimeLimitAndCountTokens(code, parser, parser::parseFormalParameterList);
    }

    /**
     * Try to parse a stream of random tokens inside a block.  Sure, there
     * will be lots of errors, but the parser must complete, not going into an
     * infinite loop.
     */
    @Test
    public void testRandomTokensInBlock() {
        String code = "{\n" + generateRandomSourceCode(20) + "\n}";
        StatementsParser parser = new Parser(new TokenCountingScanner(code), new BaseMessageProducer()).getStatementsParser();
        parseCodeWithTimeLimitAndCountTokens(code, parser, parser::parseBlock);
    }

    /**
     * Try to parse a stream of random tokens inside an arguments list.  Sure, there
     * will be lots of errors, but the parser must complete, not going into an
     * infinite loop.
     */
    @Test
    public void testRandomTokensInArguments() {
        String code = generateRandomSourceCode(20);
        ExpressionsParser parser = new Parser(new TokenCountingScanner(code), new BaseMessageProducer()).getExpressionsParser();
        parseCodeWithTimeLimitAndCountTokens(code, parser, parser::parseArgumentList);
    }

    private <P extends BasicParser, R extends Node> void parseCodeWithTimeLimitAndCountTokens(String code, P parser, Callable<R> parseFunction) {
        System.out.println("Code: \n" + code);
        TokenCountingScanner scanner = new TokenCountingScanner(code);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<R> future = executor.submit(parseFunction);
            try {
                future.get(5, TimeUnit.SECONDS);
            }
            catch (TimeoutException | InterruptedException | ExecutionException e) {
                fail("Exception of type " + e.getClass().getName() + " caught.  " +
                        scanner.getNumTokensGenerated() + " tokens generated.", e);
            }
        }
        List<CompilerMessage> msgs = parser.getCompilerMessages();
        System.out.println("There were " + msgs.size() + " compiler messages:");
        for (CompilerMessage msg : msgs) {
            System.out.println(msg);
        }
    }

    private String generateRandomSourceCode(int numTokens) {
        StringBuilder buf = new StringBuilder();
        Random random = new Random();
        List<TokenType> tokenTypes = new ArrayList<>(EnumSet.complementOf(
                EnumSet.of(TokenType.UNKNOWN, TokenType.EOF, TokenType.WHITESPACE, TokenType.COMMENT)));

        for (int i = 0; i < numTokens - 1; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            Token randomToken = createRandomToken(random, tokenTypes);
            buf.append(randomToken.getValue());
        }

        return buf.toString();
    }

    private Token createRandomToken(Random random, List<TokenType> tokenTypes) {
        TokenType type = tokenTypes.get(random.nextInt(tokenTypes.size()));
        String value = switch(type) {
            case CHARACTER_LITERAL -> "'c'";
            case STRING_LITERAL -> "\"string literal\"";
            case INT_LITERAL -> "1";
            case FLOATING_POINT_LITERAL -> "1.0";
            default -> type.getRepresentation();
        };
        return new Token(type, value);
    }

    // Below here, put failing test cases so they can be reproduced and resolved.
    // Parser bugs that can be narrowed down to specific test cases should
    // move to a new test case for the appropriate parser.

}
