package org.spruce.compiler.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTElementAccess;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.ast.expressions.ASTFieldAccess;
import org.spruce.compiler.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>StatementsParser</code> is a <code>BasicParser</code> that parses
 * statements.
 */
public class StatementsParser extends BasicParser {
    /**
     * Constructs a <code>StatementsParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
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
                t -> !test(t, CLOSE_BRACE) && !test(t, DEFAULT) && !test(t, CASE) && !test(t, EOF),
                "Expected statement or local variable declaration.",
                this::parseBlockStatement,
                Arrays.asList(CLOSE_BRACE, SEMICOLON, ELSE, CATCH, FINALLY,
                        CASE, DEFAULT,  // Switch statement stuff
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF
                ),
                ASTBlockStatements::new,
                false
        );
    }

    /**
     * Parses a <code>BlockStatement</code>.
     * <em>
     * BlockStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclarationStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Statement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
     * </em>
     * @return An <code>ASTBlockStatement</code> representing a Local Variable
     *     Declaration Statement or a Statement.
     */
    public ASTBlockStatement parseBlockStatement() {
        switch(curr().getType()) {
        case MUT:
        case VAR:
            return parseLocalVariableDeclarationStatement();
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return parseLocalVariableDeclarationStatement(dt);
            }
            else {
                // Convert to Expression Name.
                ASTExpressionName exprName;
                if (dt.canConvertToExpressionName()) {
                    exprName = dt.convertToExpressionName();
                }
                else {
                    error(dt.getLocation(), "Expected an Expression Name.");
                    exprName = ASTExpressionName.badExpressionName(dt.getLocation());
                }
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = getExpressionsParser().parsePrimary(exprName);
                return parseStatement(primary);
            }
        default:
            return parseStatement();
        }
    }

    /**
     * Parses a <code>LocalVariableDeclarationStatement</code>.
     * <em>
     * LocalVariableDeclarationStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration ;
     * </em>
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement() {
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Missing semicolon.");
        }
        return new ASTLocalVariableDeclarationStatement(localVarDecl.getLocation(), localVarDecl);
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
        ASTVariableModifierList varModList = parseVariableModifierList();
        ASTLocalVariableType localVarType = parseLocalVariableType();
        ASTVariableDeclaratorList varDeclList = parseVariableDeclaratorList();
        return new ASTLocalVariableDeclaration(loc, varModList, localVarType, varDeclList);
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
        ASTVariableModifierList varModList = new ASTVariableModifierList(dt.getLocation(), Collections.emptyList());
        ASTLocalVariableType localVarType = new ASTLocalVariableType(loc, dt);
        ASTVariableDeclaratorList varDeclList = parseVariableDeclaratorList();
        return new ASTLocalVariableDeclaration(loc, varModList, localVarType, varDeclList);
    }

    /**
     * Parses a <code>VariableModifierList</code>.
     * <em>
     * VariableModifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifier {VariableModifier}
     * </em>
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList parseVariableModifierList() {
        return parseMultiple(
                t -> test(t, Arrays.asList(MUT, VAR)),
                this::parseVariableModifier,
                ASTVariableModifierList::new
        );
    }

    /**
     * Parses a <code>VariableModifier</code>.
     * <em>
     * VariableModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;var<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;mut
     * </em>
     * @return An <code>ASTKeywordNode</code> of the appropriate keyword.
     */
    public ASTKeywordNode parseVariableModifier() {
        return parseModifier(
                Arrays.asList(MUT, VAR),
                "'mut' or 'var'.'"
        );
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
                Arrays.asList(CLOSE_BRACE, OPEN_BRACE, SEMICOLON, EOF),
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
        if (isCurr(AUTO)) {
            ASTKeywordNode autoKeyword = parseModifier(
                    Arrays.asList(AUTO),
                    "'auto'"
            );
            return new ASTLocalVariableType(loc, autoKeyword);
        }
        else {
            ASTDataType dt = getTypesParser().parseDataType();
            return new ASTLocalVariableType(loc, dt);
        }
    }

    /**
     * Parses a <code>Statement</code>.
     * <em>
     * Statement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FallthroughStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AssertStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ThrowStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DoWhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CriticalStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TryStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;YieldStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseStatement
     * </em>
     * @return An <code>ASTStatement</code> representing the Statement.
     */
    public ASTStatement parseStatement() {
        return switch (curr().getType()) {
            case OPEN_BRACE -> parseBlock();
            case RETURN -> parseReturnStatement();
            case THROW -> parseThrowStatement();
            case BREAK -> parseBreakStatement();
            case CONTINUE -> parseContinueStatement();
            case FALLTHROUGH -> parseFallthroughStatement();
            case ASSERT -> parseAssertStatement();
            case IF -> parseIfStatement();
            case WHILE -> parseWhileStatement();
            case FOR -> parseForStatement();
            case DO -> parseDoStatement();
            case CRITICAL -> parseCriticalStatement();
            case TRY -> parseTryStatement();
            case SWITCH -> parseSwitchStatement();
            case USE -> parseUseStatement();
            case YIELD -> parseYieldStatement();
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
     * &nbsp;&nbsp;&nbsp;&nbsp;FallthroughStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ThrowStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DoWhileStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CriticalStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TryStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatement
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatement</code> representing an ExpressionStatement.
     */
    public ASTStatement parseStatement(ASTPrimary primary) {
        return parseExpressionStatement(primary);
    }

    /**
     * Parses a <code>SwitchStatement</code>.
     * <em>
     * SwitchStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;switch ValueExpression SwitchBlock
     * </em>
     * @return An <code>ASTSwitchStatement</code>.
     */
    public ASTSwitchStatement parseSwitchStatement() {
        Location loc = curr().getLocation();
        if (accept(SWITCH) == null) {
            throw internalError(SWITCH);
        }
        return new ASTSwitchStatement(loc, getExpressionsParser().parseValueExpression(), parseSwitchStatementBlock());
    }

    /**
     * Parses a <code>SwitchStatementBlock</code>.
     * <em>
     * SwitchStatementBlock:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchStatementRules }
     * </em>
     * @return An <code>ASTSwitchStatementRules</code>.
     */
    public ASTSwitchStatementRules parseSwitchStatementBlock() {
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTSwitchStatementRules switchStmtRules = parseSwitchStatementRules();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return switchStmtRules;
    }

    /**
     * Parses a <code>SwitchStatementRules</code>.
     * <em>
     * SwitchStatementRules:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatementRule {SwitchStatementRule}<br>
     * </em>
     * @return A <code>ASTSwitchStatementRules</code>.
     */
    public ASTSwitchStatementRules parseSwitchStatementRules() {
        return parseMultiple(
                t -> test(t, Arrays.asList(CASE, DEFAULT, MUT, VAR, IDENTIFIER)),
                "Expected a switch case.",
                this::parseSwitchStatementRule,
                Arrays.asList(CLOSE_BRACE,
                        ASSERT, BREAK, CONTINUE, CRITICAL, DO, FALLTHROUGH, FOR, IF, RETURN, THROW, TRY, USE, WHILE, YIELD,
                        EOF
                ),
                ASTSwitchStatementRules::new
        );
    }

    /**
     * Parses a <code>SwitchStatementRule</code>.
     * <em>
     * SwitchStatementRule:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ExpressionStatement<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
     * </em>
     * @return A <code>ASTSwitchStatementRule</code>.
     */
    public ASTSwitchStatementRule parseSwitchStatementRule() {
        Location loc = curr().getLocation();
        ASTSwitchLabel switchLabel = getExpressionsParser().parseSwitchLabel();
        if (accept(ARROW) == null) {
            error(curr().getLocation(), "Expected arrow (->).");
        }
        switch(curr().getType()) {
            case OPEN_BRACE -> {
                return new ASTSwitchStatementRule(loc, switchLabel, parseBlock());
            }
            case THROW -> {
                return new ASTSwitchStatementRule(loc, switchLabel, parseThrowStatement());
            }
            default -> {
                ASTExpressionStatement exprStmt = parseExpressionStatement();
                return new ASTSwitchStatementRule(loc, switchLabel, exprStmt);
            }
        }
    }

    /**
     * Parses a <code>TryStatement</code>.
     * <em>
     * TryStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;try Block Catches<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;try Block [Catches] Finally<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;try ResourceSpecification Block [Catches] [Finally]
     * </em>
     * @return An <code>ASTTryStatement</code>.
     */
    public ASTTryStatement parseTryStatement() {
        Location loc = curr().getLocation();
        ASTTryStatement.Builder builder = new ASTTryStatement.Builder()
                .setLocation(loc);
        boolean atLeastOne = false;
        if (accept(TRY) == null) {
            throw internalError(TRY);
        }
        if (isCurr(OPEN_PARENTHESIS)) {
            builder.setResourceSpec(parseResourceSpecification());
            atLeastOne = true;
        }
        builder.setBlock(parseBlock());
        if (isCurr(CATCH)) {
            builder.setCatches(parseCatches());
            atLeastOne = true;
        }
        if (isCurr(TokenType.FINALLY)) {
            builder.setFinallyBlock(parseFinally());
            atLeastOne = true;
        }
        if (!atLeastOne) {
            Location errorLoc = curr().getLocation();
            error(errorLoc, "Expected 'catch' and/or 'finally' block.");
            // Dummy finally.
            builder.setFinallyBlock(new ASTBlock(errorLoc,
                    new ASTBlockStatements(errorLoc, Collections.emptyList())));
        }
        return builder.build();
    }

    /**
     * Parses a <code>ResourceSpecification</code>.
     * <em>
     * ResourceSpecification:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;( ResourceList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;( ResourceList ; )
     * </em>
     * @return An <code>ASTResourceList</code>.
     */
    public ASTResourceList parseResourceSpecification() {
        if (accept(OPEN_PARENTHESIS) == null) {
            throw internalError(OPEN_PARENTHESIS);
        }
        ASTResourceList resourceList = parseResourceList();
        if (isCurr(SEMICOLON)) {
            accept(SEMICOLON);
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return resourceList;
    }

    /**
     * Parses a <code>ResourceList</code>.
     * <em>
     * ResourceList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Resource {; Resource}
     * </em>
     * @return An <code>ASTResourceList</code>.
     */
    public ASTResourceList parseResourceList() {
        return parseList(
                t -> isPrimary(t) || isAcceptedOperator(Arrays.asList(MUT, CONSTANT, VAR)) != null,
                "Expected a value expression.",
                SEMICOLON,
                this::parseResource,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, OPEN_BRACE, SEMICOLON, EOF),
                ASTResourceList::new
        );
    }

    /**
     * Parses a <code>Resource</code>.
     * <em>
     * Resource:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ResourceDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
     * </em>
     * @return An <code>ASTResource</code>.
     */
    public ASTResource parseResource() {
        ASTPrimary primary;
        switch (curr().getType()) {
        case MUT:
        case CONSTANT:
        case VAR:
            return parseResourceDeclaration();
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return parseResourceDeclaration(dt);
            }
            else {
                // Convert to Expression Name.
                ASTExpressionName exprName = dt.convertToExpressionName();
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                primary = getExpressionsParser().parsePrimary(exprName);
            }
            break;
        default:
            // Maybe an expression name or a field access.
            primary = getExpressionsParser().parsePrimary();
        }

        // Must be an expression name or a field access.
        Node child = primary.getChild();
        switch (child) {
            case ASTExpressionName exprName -> {
                return exprName;
            }
            case ASTFieldAccess fa -> {
                return fa;
            }
            case ASTElementAccess ea -> {
                return ea;
            }
            default -> {
                Location loc = primary.getLocation();
                error(loc, "Expected resource declaration or variable.");
                return new ASTExpressionName(loc, Arrays.asList(
                        new ASTIdentifier(loc, "bad resource")
                ));
            }
        }
    }

    /**
     * Parses a <code>ResourceDeclaration</code>.
     * <em>
     * ResourceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>VariableModifierList LocalVariableType Identifier = Expression</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableType Identifier = Expression
     * </em>
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration() {
        Location loc = curr().getLocation();
        ASTVariableModifierList varModifierList = parseVariableModifierList();
        ASTLocalVariableType localVariableType = parseLocalVariableType();
        ASTIdentifier resourceName = getNamesParser().parseIdentifier();
        if (accept(EQUAL) == null) {
            error(curr().getLocation(), "Expected '='.");
        }
        ASTExpression expr = getExpressionsParser().parseExpression();
        return new ASTResourceDeclaration(loc, varModifierList, localVariableType, resourceName, expr);
    }

    /**
     * Parses a <code>ResourceDeclaration</code>, given an already parsed
     * <code>ASTDataType</code>.
     * <em>
     * ResourceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList LocalVariableType Identifier = Expression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>LocalVariableType Identifier = Expression</strong>
     * </em>
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration(ASTDataType dt) {
        Location loc = curr().getLocation();
        ASTVariableModifierList varModifierList = new ASTVariableModifierList(loc, Collections.emptyList());
        ASTLocalVariableType localVarType = new ASTLocalVariableType(loc, dt);
        ASTIdentifier resourceName = getNamesParser().parseIdentifier();
        if (accept(EQUAL) == null) {
            error(curr().getLocation(), "Expected '='.");
        }
        ASTExpression expr = getExpressionsParser().parseExpression();
        return new ASTResourceDeclaration(loc, varModifierList, localVarType, resourceName, expr);
    }

    /**
     * Parses a <code>Catches</code>.
     * <em>
     * Catches:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CatchClause {CatchClause}
     * </em>
     * @return An <code>ASTCatches</code>.
     */
    public ASTCatches parseCatches() {
        return parseMultiple(
                t -> test(t, CATCH),
                "Expected catch clause.",
                this::parseCatchClause,
                Arrays.asList(OPEN_BRACE, CLOSE_BRACE, FINALLY,
                        ASSERT, BREAK, CONTINUE, CRITICAL, DO, FALLTHROUGH, FOR, IF, RETURN, THROW, TRY, USE, WHILE, YIELD,
                        EOF
                ),
                ASTCatches::new
        );
    }

    /**
     * Parses a <code>CatchClause</code>.
     * <em>
     * CatchClause:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;catch ( CatchFormalParameter ) Block
     * </em>
     * @return An <code>ASTCatchClause</code>.
     */
    public ASTCatchClause parseCatchClause() {
        Location loc = curr().getLocation();
        if (accept(CATCH) == null) {
            throw internalError(CATCH);
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('");
        }
        ASTCatchFormalParameter cfp = parseCatchFormalParameter();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'");
        }
        ASTBlock block = parseBlock();
        return new ASTCatchClause(loc, cfp, block);
    }

    /**
     * Parses a <code>CatchFormalParameter</code>.
     * <em>
     * CatchFormalParameter:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList CatchType Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CatchType Identifier
     * </em>
     * @return An <code>ASTCatchFormalParameter</code>.
     */
    public ASTCatchFormalParameter parseCatchFormalParameter() {
        Location loc = curr().getLocation();
        ASTVariableModifierList varModifierList =  parseVariableModifierList();
        ASTCatchType catchType = parseCatchType();
        ASTIdentifier varName = getNamesParser().parseIdentifier();
        return new ASTCatchFormalParameter(loc, varModifierList, catchType, varName);
    }

    /**
     * Parses a <code>CatchType</code>.
     * <em>
     * CatchType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType {| DataType}
     * </em>
     * @return An <code>ASTCatchType</code>.
     */
    public ASTCatchType parseCatchType() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected data type.",
                PIPE,
                getTypesParser()::parseDataType,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, OPEN_BRACE, SEMICOLON, FINALLY, EOF),
                ASTCatchType::new
        );
    }

    /**
     * Parses a <code>Finally</code>.
     * <em>
     * Finally:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;finally Block
     * </em>
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock parseFinally() {
        if (accept(FINALLY) == null) {
            throw internalError(FINALLY);
        }
        return parseBlock();
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
     * Parses a <code>DoStatement</code>.
     * <em>
     * DoStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;do Block while ValueExpression ;
     * </em>
     * @return An <code>ASTDoStatement</code>.
     */
    public ASTDoStatement parseDoStatement() {
        Location loc = curr().getLocation();
        if (accept(DO) == null) {
            throw internalError(DO);
        }
        ASTBlock block = parseBlock();
        if (accept(WHILE) == null) {
            error(curr().getLocation(), "Expected while.");
        }
        ASTValueExpression valueExpr = getExpressionsParser().parseValueExpression();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected semicolon.");
        }
        return new ASTDoStatement(loc, block, valueExpr);
    }

    /**
     * Parses a <code>CriticalStatement</code>.
     * <em>
     * CriticalStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;critical ValueExpression Block
     * </em>
     * @return An <code>ASTCriticalStatement</code>.
     */
    public ASTCriticalStatement parseCriticalStatement() {
        Location loc = curr().getLocation();
        if (accept(CRITICAL) == null) {
            throw internalError(CRITICAL);
        }
        ASTValueExpression valueExpr = getExpressionsParser().parseValueExpression();
        ASTBlock block = parseBlock();
        return new ASTCriticalStatement(loc, valueExpr, block);
    }

    /**
     * Parses a <code>ThrowStatement</code>.
     * <em>
     * ThrowStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;throw ValueExpression ;
     * </em>
     * @return An <code>ASTThrowStatement</code>.
     */
    public ASTThrowStatement parseThrowStatement() {
        return parseUnaryStatement(THROW, getExpressionsParser()::parseValueExpression, ASTThrowStatement::new);
    }

    /**
     * Parses a <code>YieldStatement</code>.
     * <em>
     * YieldStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;yield Expression ;
     * </em>
     * @return An <code>ASTYieldStatement</code>.
     */
    public ASTYieldStatement parseYieldStatement() {
        return parseUnaryStatement(YIELD, getExpressionsParser()::parseExpression, ASTYieldStatement::new);
    }

    /**
     * Parses a <code>UseStatement</code>.
     * <em>
     * UseStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use Expression ;
     * </em>
     * @return An <code>ASTUseStatement</code>.
     */
    public ASTUseStatement parseUseStatement() {
        return parseUnaryStatement(USE, getExpressionsParser()::parseExpression, ASTUseStatement::new);
    }

    /**
     * Helper method to parse similar "unary" statements that consist of
     * exactly one child.  Expects:
     * <em>keyword child ;</em>
     * @param keyword A <code>TokenType</code> representing the keyword to parse first.
     * @param childParser A <code>Supplier</code> that parses the child, some kind of expression.
     * @param stmtConstructor A <code>BiFunction</code> representing a constructor
     *                        of the statement type to create.
     * @param <T> The type of statement to create.
     * @param <E> The type of expression.
     * @return An <code>ASTStatement</code> of type <code>T</code>.
     */
    private <T extends ASTStatement, E extends ASTExpression> T parseUnaryStatement(
            TokenType keyword, Supplier<E> childParser, BiFunction<Location, E, T> stmtConstructor) {
        Location loc = curr().getLocation();
        if (accept(keyword) == null) {
            throw internalError(keyword);
        }
        E child = childParser.get();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Missing ';'.");
        }
        return stmtConstructor.apply(loc, child);
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
     * Parses a <code>FallthroughStatement</code>.
     * <em>
     * FallthroughStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;fallthrough ;<br>
     * </em>
     * @return An <code>ASTFallthroughStatement</code>.
     */
    public ASTFallthroughStatement parseFallthroughStatement() {
        return parseKeywordStatement(FALLTHROUGH, ASTFallthroughStatement::new);
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
     * Parses an <code>ASTAssertStatement</code>.
     * <em>
     * AssertStatement:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;assert ValueExpression ;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;assert ValueExpression else ValueExpression;
     * </em>
     * @return An <code>ASTAssertStatement</code>.
     */
    public ASTAssertStatement parseAssertStatement() {
        Location loc = curr().getLocation();
        if (accept(ASSERT) == null) {
            throw internalError(ASSERT);
        }
        ASTAssertStatement node;
        ASTValueExpression condition = getExpressionsParser().parseValueExpression();
        if (isCurr(ELSE)) {
            accept(ELSE);
            ASTValueExpression message = getExpressionsParser().parseValueExpression();
            node = new ASTAssertStatement(loc, condition, message);
        }
        else {
            node = new ASTAssertStatement(loc, condition);
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return node;
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
        case MUT:
        case CONSTANT:
        case VAR:
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
     * Parses a <code>StatementExpressionList</code>.
     * <em>
     * StatementExpressionList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression {, StatementExpression}
     * </em>
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList() {
        return parseList(
                t -> test(t, Arrays.asList(INCREMENT, DECREMENT)) || isPrimary(t),
                "Expected a statement expression.",
                COMMA,
                this::parseStatementExpression,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, OPEN_BRACE, SEMICOLON, COLON, EOF),
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
     * Parses a <code>StatementExpression</code>.
     * <em>
     * StatementExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Postfix<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
     * </em>
     * @return An <code>ASTStatementExpression</code> representing either an Assignment,
     *     a PostFix, a Method Invocation Expression, or a Class Instance Creation Expression.
     */
    public ASTStatementExpression parseStatementExpression() {
        ASTPrimary primary = getExpressionsParser().parsePrimary();
        return parseStatementExpression(primary);
    }

    /**
     * Parses a <code>StatementExpression</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * <em>
     * StatementExpression:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Postfix<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
     * </em>
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpression</code> representing either an Assignment,
     *     a PostFix, a Method Invocation Expression, or a Class Instance Creation Expression.
     */
    public ASTStatementExpression parseStatementExpression(ASTPrimary primary) {
        Location loc = primary.getLocation();
        if (isCurr(INCREMENT) || isCurr(DECREMENT)) {
            if (!primary.isLeftHandSide()) {
                error(primary.getLocation(), "Expected a LeftHandSide.");
                return parsePostfix(loc, ASTExpressionName.badExpressionName(primary.getLocation()));
            }
            return parsePostfix(loc, primary.getLeftHandSide());
        }
        else {
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
    }

    /**
     * Parses an <code>Assignment</code>, given an <code>ASTLeftHandSide</code>
     * that has already been parsed and its <code>Location</code>.
     * <em>
     * Assignment:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide AssignmentOperator AssignmentExpression
     * </em>
     * <em>
     * AssignmentOperator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(One of) = += -= *= /= %= &= |= ^= &lt;&lt;= &gt;&gt;=
     * </em>
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTAssignment</code>.
     */
    public ASTAssignment parseAssignment(Location loc, ASTLeftHandSide lhs) {
        TokenType currToken = curr().getType();
        switch (currToken) {
        case EQUAL, PLUS_EQUALS, MINUS_EQUALS, STAR_EQUALS, SLASH_EQUALS, PERCENT_EQUALS,
                SHIFT_LEFT_EQUALS, SHIFT_RIGHT_EQUALS, AMPERSAND_EQUALS, PIPE_EQUALS, CARET_EQUALS ->
            accept(currToken);
        default -> {
            error(curr().getLocation(), "Expected assignment operator.");
            accept(currToken);
        }
        }
        ASTExpression expr = getExpressionsParser().parseExpression();
        return new ASTAssignment(loc, lhs, expr, currToken);
    }

    /**
     * Parses a <code>Postfix</code>.
     * <em>
     * Postfix:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide ++<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide --
     * </em>
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTPostfix</code>.
     */
    public ASTPostfix parsePostfix(Location loc, ASTLeftHandSide lhs) {
        if (isCurr(INCREMENT)) {
            accept(INCREMENT);
            return new ASTPostfix(loc, lhs, INCREMENT);
        }
        else if (isCurr(DECREMENT)) {
            accept(DECREMENT);
            return new ASTPostfix(loc, lhs, DECREMENT);
        }
        else {
            throw internalError("'++' or '--'.");
        }
    }
}
