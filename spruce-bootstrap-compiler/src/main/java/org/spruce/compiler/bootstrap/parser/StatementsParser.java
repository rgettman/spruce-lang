package org.spruce.compiler.bootstrap.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.TokenType;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>StatementsParser</code> is a <code>BasicParser</code> that parses
 * statements.
 */
public class StatementsParser extends BasicParser {
    /**
     * Constructs a <code>StatementsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner     A <code>Scanner</code>.
     * @param parser      A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public StatementsParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * Parses a <code>Block</code>.
     * <em>
     * Block:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ [BlockStatements] }
     * </em>
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock parseBlock() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTBlockStatements blockStmts = parseBlockStatements();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return new ASTBlock(loc, blockStmts);
    }

    /**
     * Parses a <code>BlockStatements</code>.
     * <em>
     * BlockStatements:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BlockStatement {BlockStatement}
     * </em>
     * @return An <code>ASTBlockStatements</code>.
     */
    public ASTBlockStatements parseBlockStatements() {
        return parseMultiple(
                t -> !test(t, CLOSE_BRACE) && !test(t, EOF),
                "Expected statement or local variable declaration.",
                this::parseBlockStatement,
                ASTBlockStatements::new,
                false
        );
    }

    /**
     * Parses a <code>BlockStatement</code>.
     * <em>
     * BlockStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclarationStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorInvocation<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Statement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration
     * </em>
     * @return An <code>ASTBlockStatement</code> representing a Local Variable
     *     Declaration Statement or a Statement.
     */
    public ASTBlockStatement parseBlockStatement() {
        if (curr().getType() == IDENTIFIER) {
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return parseLocalVariableDeclarationStatement(dt);
            } else {
                // Convert to Expression Name.
                ASTExpressionName exprName;
                if (dt.canConvertToExpressionName()) {
                    exprName = dt.convertToExpressionName();
                } else {
                    error(dt.getLocation(), "Expected an Expression Name.");
                    exprName = ASTExpressionName.badExpressionName(dt.getLocation());
                }
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = getExpressionsParser().parsePrimary(exprName);
                return parseStatement(primary);
            }
        }
        else if (isAcceptedOperator(Arrays.asList(SELF, SUPER)) != null &&
                 isNext(OPEN_PARENTHESIS)) {
            return parseConstructorInvocation();
        }
        else {
            return parseStatement();
        }
    }

    /**
     * Parses a <code>ConstructorInvocation</code>.
     * <em>
     * ConstructorInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;self ( ArgumentList ) ;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;super ( ArgumentList ) ;
     * </em>
     * @return An <code>ASTConstructorInvocation</code>.
     */
    public ASTConstructorInvocation parseConstructorInvocation() {
        Location loc = curr().getLocation();
        ASTConstructorInvocation.Builder builder = new ASTConstructorInvocation.Builder()
                .setLocation(loc)
                .setConstructorKeyword(parseModifier(Arrays.asList(SELF, SUPER),
                "'self' or 'super'"));
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(getExpressionsParser().parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return builder.build();
    }

    /**
     * Parses a <code>LocalVariableDeclarationStatement</code>, given an
     * already parsed <code>ASTDataType</code>.
     * <em>
     * LocalVariableDeclarationStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration ;
     * </em>
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement(ASTDataType dt) {
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration(dt);
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Missing semicolon.");
        }
        return new ASTLocalVariableDeclarationStatement(localVarDecl.getLocation(), localVarDecl);
    }

    /**
     * Parses a <code>LocalVariableDeclaration</code>.
     * <em>
     * LocalVariableDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[VariableModifierList] LocalVariableType VariableDeclaratorList<br>
     * </em>
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration() {
        Location loc = curr().getLocation();
        ASTLocalVariableType localVarType = parseLocalVariableType();
        ASTVariableDeclaratorList varDeclList = parseVariableDeclaratorList();
        return new ASTLocalVariableDeclaration(loc, localVarType, varDeclList);
    }

    /**
     * Parses a <code>LocalVariableDeclaration</code>, given an already
     * parsed <code>ASTDataType</code>.
     * <em>
     * LocalVariableDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[VariableModifierList] LocalVariableType VariableDeclaratorList<br>
     * </em>
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration(ASTDataType dt) {
        Location loc = dt.getLocation();
        ASTLocalVariableType localVarType = new ASTLocalVariableType(loc, dt);
        ASTVariableDeclaratorList varDeclList = parseVariableDeclaratorList();
        return new ASTLocalVariableDeclaration(loc, localVarType, varDeclList);
    }

    /**
     * Parses a <code>VariableDeclaratorList</code>.
     * <em>
     * VariableDeclaratorList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableDeclarator {, VariableDeclarator}
     * </em>
     * @return An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTVariableDeclaratorList parseVariableDeclaratorList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier",
                COMMA,
                this::parseVariableDeclarator,
                ASTVariableDeclaratorList::new
        );
    }

    /**
     * Parses a <code>VariableDeclarator</code>.
     * <em>
     * VariableDeclarator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = VariableInitializer
     * </em>
     * @return An <code>ASTVariableDeclarator</code>.
     */
    public ASTVariableDeclarator parseVariableDeclarator() {
        Location loc = curr().getLocation();
        ASTIdentifier varName = getNamesParser().parseIdentifier();
        if (isCurr(EQUAL)) {
            accept(EQUAL);
            return new ASTVariableDeclarator(loc, varName, getExpressionsParser().parseExpression());
        }
        return new ASTVariableDeclarator(loc, varName);
    }

    /**
     * Parses a <code>LocalVariableType</code>.
     * <em>
     * LocalVariableType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;auto
     * </em>
     * @return An <code>ASTLocalVariableType</code>.
     */
    public ASTLocalVariableType parseLocalVariableType() {
        Location loc = curr().getLocation();
        ASTDataType dt = getTypesParser().parseDataType();
        return new ASTLocalVariableType(loc, dt);
    }

    /**
     * Parses a <code>Statement</code>.
     * <em>
     * Statement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement
     * </em>
     * @return An <code>ASTStatement</code> representing the Statement.
     */
    public ASTStatement parseStatement() {
        return switch (curr().getType()) {
            case OPEN_BRACE -> parseBlock();
            case RETURN -> parseReturnStatement();
            case BREAK -> parseBreakStatement();
            case CONTINUE -> parseContinueStatement();
            case IF -> parseIfStatement();
            case WHILE -> parseWhileStatement();
            case FOR -> parseForStatement();
            default -> parseExpressionStatement();
        };
    }

    /**
     * Parses a <code>Statement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * <em>
     * Statement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>ExpressionStatement</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatement</code> representing an ExpressionStatement.
     */
    public ASTStatement parseStatement(ASTPrimary primary) {
        return parseExpressionStatement(primary);
    }

    /**
     * Parses a <code>ForStatement</code>.
     * <em>
     * ForStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BasicForStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;EnhancedForStatement
     * </em>
     * @return An <code>ASTForStatement</code> that is either an
     *     <code>ASTBasicForStatement</code> or an <code>ASTEnhancedForStatement</code>.
     */
    public ASTForStatement parseForStatement() {
        Location loc = curr().getLocation();
        if (accept(FOR) == null) {
            throw internalError(FOR);
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        if (isCurr(SEMICOLON)) {
            return parseBasicForStatement(loc);
        }
        else {
            ASTInit init = parseInit();
            if (isCurr(IN)) {
                return parseEnhancedForStatement(loc, init);
            }
            else {
                return parseBasicForStatement(loc, init);
            }
        }
    }

    /**
     * <p>Parses an <code>EnhancedForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the colon.</p>
     * <em>
     * EnhancedForStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration in ValueExpression ) Block<br>
     * </em>
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTEnhancedForStatement</code>.
     */
    public ASTEnhancedForStatement parseEnhancedForStatement(Location locFor, ASTInit init) {
        if (accept(IN) == null) {
            throw internalError(IN);
        }
        ASTLocalVariableDeclaration localVariableDecl;
        if (init instanceof ASTLocalVariableDeclaration localVarDecl) {
            localVariableDecl = localVarDecl;
        }
        else {
            error(init.getLocation(), "Enhanced for loop requires a variable declaration before the colon.");
            localVariableDecl = ASTLocalVariableDeclaration.badLocalVariableDeclaration(init.getLocation());
        }
        ASTValueExpression valueExpr = getExpressionsParser().parseValueExpression();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        ASTBlock block = parseBlock();
        return new ASTEnhancedForStatement(locFor, localVariableDecl, valueExpr, block);
    }

    /**
     * Parses a <code>BasicForStatement</code>, given that "for (" has
     * already been parsed, and no <code>ASTInit</code> was found before the
     * first semicolon.
     * <em>
     * BasicForStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;for ( [Init] ; [ValueExpression] ; [StatementExpressionList] ) Block<br>
     * </em>
     * @param locFor The location of the "for" keyword, already parsed.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor) {
        ASTBasicForStatement.Builder builder = new ASTBasicForStatement.Builder()
                .setLocation(locFor);
        return parseBasicForStatement(builder);
    }

    /**
     * Parses a <code>BasicForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the first semicolon.
     * <em>
     * BasicForStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;for ( [Init] ; [ValueExpression] ; [StatementExpressionList] ) Block<br>
     * </em>
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor, ASTInit init) {
        ASTBasicForStatement.Builder builder = new ASTBasicForStatement.Builder()
                .setLocation(locFor)
                .setInit(init);
        return parseBasicForStatement(builder);
    }

    /**
     * Helper method to parse the "remaining" parts of the Basic For Statement.
     * Called by the two cases of whether there is an Init.  It is assumed that
     * the first semicolon is the current token.
     * @param builder An <code>ASTBasicForStatement.Builder</code>.
     * @return An <code>ASTBasicForStatement</code>.
     */
    private ASTBasicForStatement parseBasicForStatement(ASTBasicForStatement.Builder builder) {
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (!isCurr(SEMICOLON) && !isCurr(CLOSE_PARENTHESIS)) {
            builder.setValueExpr(getExpressionsParser().parseValueExpression());
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected second ';'.");
        }
        builder.setStmtExprList(parseStatementExpressionList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        builder.setBlock(parseBlock());
        return builder.build();
    }

    /**
     * Parses an <code>IfStatement</code>.
     * <em>
     * IfStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block else Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block else IfStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block<br>
     * </em>
     * @return An <code>ASTIfStatement</code>.
     */
    public ASTIfStatement parseIfStatement() {
        Location loc = curr().getLocation();
        if (accept(IF) == null) {
            throw internalError(IF);
        }
        ASTIfStatement.Builder builder = new ASTIfStatement.Builder()
                .setLocation(loc);
        if (isCurr(OPEN_BRACE)) {
            accept(OPEN_BRACE);
            builder.setInit(parseInit());
            if (accept(CLOSE_BRACE) == null) {
                error(curr().getLocation(), "Expected '}'.");
            }
        }
        builder.setCondExpr(getExpressionsParser().parseValueExpression())
                .setIfBlock(parseBlock());
        // Greedy else.
        if (isCurr(ELSE)) {
            accept(ELSE);
            if (isCurr(IF)) {
                builder.setElseIf(parseIfStatement());
            }
            else {
                builder.setElseBlock(parseBlock());
            }
        }
        return builder.build();
    }

    /**
     * Parses a <code>WhileStatement</code>.
     * <em>
     * WhileStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;while { Init } ValueExpression Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;while ValueExpression Block
     * </em>
     * @return An <code>ASTWhileStatement</code>.
     */
    public ASTWhileStatement parseWhileStatement() {
        Location loc = curr().getLocation();
        boolean initExists = false;
        if (accept(WHILE) == null) {
            throw internalError(WHILE);
        }
        ASTInit init = null;
        if (isCurr(OPEN_BRACE)) {
            accept(OPEN_BRACE);
            init = parseInit();
            if (accept(CLOSE_BRACE) == null) {
                error(curr().getLocation(), "Expected '}'.");
            }
            initExists = true;
        }
        ASTValueExpression valueExpr = getExpressionsParser().parseValueExpression();
        ASTBlock block = parseBlock();
        if (initExists) {
            return new ASTWhileStatement(loc, init, valueExpr, block);
        }
        else {
            return new ASTWhileStatement(loc, valueExpr, block);
        }
    }

    /**
     * Parses a <code>ReturnStatement</code>.
     * <em>
     * ReturnStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;return ;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;return Expression ;
     * </em>
     * @return An <code>ASTReturnStatement</code>.
     */
    public ASTReturnStatement parseReturnStatement() {
        Location loc = curr().getLocation();
        if (accept(RETURN) == null) {
            throw internalError(RETURN);
        }
        ASTReturnStatement returnStmt;
        if (!isCurr(SEMICOLON) && !isCurr(CLOSE_BRACE)) {
            returnStmt = new ASTReturnStatement(loc, getExpressionsParser().parseExpression());
        }
        else {
            returnStmt = new ASTReturnStatement(loc);
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return returnStmt;
    }

    /**
     * Parses a <code>BreakStatement</code>.
     * <em>
     * BreakStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;break ;<br>
     * </em>
     * @return An <code>ASTBreakStatement</code>.
     */
    public ASTBreakStatement parseBreakStatement() {
        return parseKeywordStatement(BREAK, ASTBreakStatement::new);
    }

    /**
     * Parses a <code>ContinueStatement</code>.
     * <em>
     * ContinueStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;continue ;<br>
     * </em>
     * @return An <code>ASTContinueStatement</code>.
     */
    public ASTContinueStatement parseContinueStatement() {
        return parseKeywordStatement(CONTINUE, ASTContinueStatement::new);
    }

    /**
     * Helper method to parse similar "keyword" statements that consist of
     * the keyword and a semicolon.  Expects:
     * <em>keyword ;</em>
     * @param keyword A <code>TokenType</code> representing the keyword to parse first.
     * @param stmtConstructor A <code>Function</code> representing a constructor
     *                        of the statement type to create.
     * @param <T> The type of statement to create.
     * @return An <code>ASTStatement</code> of type <code>T</code>.
     */
    private <T extends ASTStatement> T parseKeywordStatement(TokenType keyword, BiFunction<Location, ASTKeywordNode, T> stmtConstructor) {
        Location loc = curr().getLocation();
        if (!isCurr(keyword)) {
            throw internalError(keyword);
        }
        ASTKeywordNode keywordNode = parseModifier(
                Arrays.asList(keyword),
                keyword.getRepresentation()
        );
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return stmtConstructor.apply(loc, keywordNode);
    }

    /**
     * Parses an <code>ExpressionStatement</code>.
     * <em>
     * ExpressionStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression ;
     * </em>
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement() {
        Location loc = curr().getLocation();

        ASTStatementExpression stmtExpr = parseStatementExpression();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return new ASTExpressionStatement(loc, stmtExpr);
    }

    /**
     * Parses an <code>ExpressionStatement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * <em>
     * ExpressionStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression ;
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement(ASTPrimary primary) {
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return new ASTExpressionStatement(primary.getLocation(), stmtExpr);
    }

    /**
     * Parses an <code>Init</code>.
     * <em>
     * Init:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpressionList
     * </em>
     * @return An <code>ASTInit</code>.
     */
    public ASTInit parseInit() {
        switch(curr().getType()) {
        case CONSTANT:
            return parseLocalVariableDeclaration();
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return parseLocalVariableDeclaration(dt);
            }
            else {
                // Convert to Expression Name.
                ASTExpressionName exprName = dt.convertToExpressionName();
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = getExpressionsParser().parsePrimary(exprName);
                return parseStatementExpressionList(primary);
            }
        default:
            return parseStatementExpressionList();
        }
    }

    /**
     * Parses a <code>StatementExpression</code>.
     * <em>
     * StatementExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
     * </em>
     * @return An <code>ASTStatementExpression</code> representing either an Assignment,
     *     a Method Invocation Expression, or a Class Instance Creation Expression.
     */
    public ASTStatementExpression parseStatementExpression() {
        ASTPrimary primary = getExpressionsParser().parsePrimary();
        return parseStatementExpression(primary);
    }

    /**
     * Parses a <code>StatementExpressionList</code>.
     * <em>
     * StatementExpressionList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression {, StatementExpression}
     * </em>
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList() {
        return parseList(
                BasicParser::isPrimary,
                "Expected a statement expression.",
                COMMA,
                this::parseStatementExpression,
                ASTStatementExpressionList::new,
                false
        );
    }

    /**
     * Parses a <code>StatementExpressionList</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * <em>
     * StatementExpressionList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression {, StatementExpression}
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList(ASTPrimary primary) {
        Location loc = primary.getLocation();
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        ASTStatementExpressionList node;
        if (isCurr(COMMA)) {
            accept(COMMA);
            ASTStatementExpressionList rest = parseStatementExpressionList();
            List<ASTStatementExpression> children = rest.getTypedChildren();
            children.add(0, stmtExpr);
            node = rest;
        }
        else {
            List<ASTStatementExpression> children = Arrays.asList(stmtExpr);
            node = new ASTStatementExpressionList(loc, children);
        }
        return node;
    }

    /**
     * Parses a <code>StatementExpression</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * <em>
     * StatementExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpression</code> representing either an Assignment,
     *     a Method Invocation Expression, or a Class Instance Creation Expression.
     */
    public ASTStatementExpression parseStatementExpression(ASTPrimary primary) {
        Location loc = primary.getLocation();

        // Primary may already be a method invocation or class instance creation expression.
        // If so, retrieve and use it.
        Node child = primary.getChild();
        if (child instanceof ASTMethodInvocation ||
                child instanceof ASTClassInstanceCreationExpression) {
            return (ASTStatementExpression) child;
        }
        else {
            // Assume assignment.
            if (!primary.isLeftHandSide()) {
                error(primary.getLocation(), "Expected a LeftHandSide.");
                return parseAssignment(loc, ASTExpressionName.badExpressionName(primary.getLocation()));
            }
            return parseAssignment(loc, primary.getLeftHandSide());
        }
    }

    /**
     * Parses an <code>Assignment</code>, given an <code>ASTLeftHandSide</code>
     * that has already been parsed and its <code>Location</code>.
     * <em>
     * Assignment:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide AssignmentOperator Expression
     * </em>
     * <em>
     * AssignmentOperator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;=
     * </em>
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTAssignment</code>.
     */
    public ASTAssignment parseAssignment(Location loc, ASTLeftHandSide lhs) {
        TokenType currToken = curr().getType();
        if (currToken == TokenType.EQUAL) {
            accept(currToken);
        } else {
            error(curr().getLocation(), "Expected assignment operator.");
            accept(currToken);
        }
        ASTExpression expr = getExpressionsParser().parseExpression();
        return new ASTAssignment(loc, lhs, expr, currToken);
    }
}