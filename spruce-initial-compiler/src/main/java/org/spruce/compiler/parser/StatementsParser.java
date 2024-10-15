package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.ast.expressions.ASTAssignment;
import org.spruce.compiler.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTFieldAccess;
import org.spruce.compiler.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.expressions.ASTUnqualifiedClassInstanceCreationExpression;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public StatementsParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock parseBlock() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        ASTBlock node = new ASTBlock(loc, children);
        node.setOperation(OPEN_BRACE);
        if (!isCurr(CLOSE_BRACE)) {
            children.add(parseBlockStatements());
        }
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'.");
        }
        return node;
    }

    /**
     * Parses a <code>BlockStatements</code>.
     * <em>
     * BlockStatements:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BlockStatement {BlockStatement}
     * </em>
     * @return An <code>ASTListNode</code> of type <code>BLOCK_STATEMENTS</code>.
     */
    public ASTListNode parseBlockStatements() {
        return parseMultiple(
                t -> !test(t, CLOSE_BRACE) && !test(t, DEFAULT) && !test(t, CASE),
                "Expected statement or local variable declaration.",
                this::parseBlockStatement,
                BLOCK_STATEMENTS,
                false
        );
    }

    /**
     * Parses an <code>ASTBlockStatement</code>.
     * @return An <code>ASTBlockStatement</code>.
     */
    public ASTBlockStatement parseBlockStatement() {
        Location loc = curr().getLocation();
        switch(curr().getType()) {
        case MUT:
        case VAR:
            return new ASTBlockStatement(loc, Arrays.asList(parseLocalVariableDeclarationStatement()));
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return new ASTBlockStatement(loc, Arrays.asList(parseLocalVariableDeclarationStatement(dt)));
            }
            else {
                // Convert to Expression Name.
                ASTListNode exprName = getTypesParser().convertToExpressionName(dt);
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = getExpressionsParser().parsePrimary(exprName);
                return new ASTBlockStatement(loc, Arrays.asList(parseStatement(primary)));
            }
        default:
            return new ASTBlockStatement(loc, Arrays.asList(parseStatement()));
        }
    }

    /**
     * Parses an <code>ASTLocalVariableDeclarationStatement</code>.
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement() {
        Location loc = curr().getLocation();
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration();
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTLocalVariableDeclarationStatement node = new ASTLocalVariableDeclarationStatement(loc, Arrays.asList(localVarDecl));
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableDeclarationStatement</code>, given an
     * already parsed <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclarationStatement</code>.
     */
    public ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement(ASTDataType dt) {
        Location loc = dt.getLocation();
        ASTLocalVariableDeclaration localVarDecl = parseLocalVariableDeclaration(dt);
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTLocalVariableDeclarationStatement node = new ASTLocalVariableDeclarationStatement(loc, Arrays.asList(localVarDecl));
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableDeclaration</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            children.add(parseVariableModifierList());
        }
        children.add(parseLocalVariableType());
        children.add(parseVariableDeclaratorList());
        return new ASTLocalVariableDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTLocalVariableDeclaration</code>, given an already
     * parsed <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration parseLocalVariableDeclaration(ASTDataType dt) {
        Location loc = dt.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(new ASTLocalVariableType(loc, Arrays.asList(dt)));
        children.add(parseVariableDeclaratorList());
        return new ASTLocalVariableDeclaration(loc, children);
    }

    /**
     * Parses a <code>VariableModifierList</code>.
     * <em>
     * VariableModifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifier {VariableModifier}
     * </em>
     * @return An <code>ASTListNode</code> with type <code>VARIABLE_MODIFIERS</code>.
     */
    public ASTListNode parseVariableModifierList() {
        return parseMultiple(
                t -> test(t, MUT, VAR),
                "Expected mut or var.",
                this::parseVariableModifier,
                VARIABLE_MODIFIERS,
                false
        );
    }

    /**
     * Parses a <code>VariableModifier</code>.
     * <em>
     * VariableModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;var<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;mut
     * </em>
     * @return An <code>ASTModifierNode</code>.
     */
    public ASTModifierNode parseVariableModifier() {
        return parseModifier(
                Arrays.asList(MUT, VAR),
                "Expected mut or var.",
                ASTModifierNode::new
        );
    }

    /**
     * Parses a <code>VariableDeclaratorList</code>.
     * <em>
     * VariableDeclaratorList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableDeclarator {, VariableDeclarator}
     * </em>
     * @return An <code>ASTListNode</code> of type <code>VARIABLE_DECLARATORS</code>.
     */
    public ASTListNode parseVariableDeclaratorList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier",
                COMMA,
                this::parseVariableDeclarator,
                VARIABLE_DECLARATORS
        );
    }

    /**
     * Parses an <code>ASTVariableDeclarator</code>.
     * @return An <code>ASTVariableDeclarator</code>.
     */
    public ASTVariableDeclarator parseVariableDeclarator() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getNamesParser().parseIdentifier());
        ASTVariableDeclarator node = new ASTVariableDeclarator(loc, children);
        if (isCurr(EQUAL)) {
            accept(EQUAL);
            children.add(getExpressionsParser().parseVariableInitializer());
            node.setOperation(EQUAL);
        }
        return node;
    }

    /**
     * Parses an <code>ASTLocalVariableType</code>.
     * @return An <code>ASTLocalVariableType</code>.
     */
    public ASTLocalVariableType parseLocalVariableType() {
        Location loc = curr().getLocation();
        if (isCurr(AUTO)) {
            accept(AUTO);
            ASTLocalVariableType node = new ASTLocalVariableType(loc, Collections.emptyList());
            node.setOperation(AUTO);
            return node;
        }
        else {
            ASTDataType dt = getTypesParser().parseDataType();
            return new ASTLocalVariableType(loc, Arrays.asList(dt));
        }
    }

    /**
     * Parses an <code>ASTStatement</code>.
     * @return An <code>ASTStatement</code>.
     */
    public ASTStatement parseStatement() {
        Location loc = curr().getLocation();
        return switch (curr().getType()) {
            case OPEN_BRACE -> {
                ASTBlock block = parseBlock();
                yield new ASTStatement(loc, Arrays.asList(block));
            }
            case RETURN -> {
                ASTReturnStatement retnStmt = parseReturnStatement();
                yield new ASTStatement(loc, Arrays.asList(retnStmt));
            }
            case THROW -> {
                ASTThrowStatement throwStmt = parseThrowStatement();
                yield new ASTStatement(loc, Arrays.asList(throwStmt));
            }
            case BREAK -> {
                ASTBreakStatement breakStmt = parseBreakStatement();
                yield new ASTStatement(loc, Arrays.asList(breakStmt));
            }
            case CONTINUE -> {
                ASTContinueStatement contStmt = parseContinueStatement();
                yield new ASTStatement(loc, Arrays.asList(contStmt));
            }
            case FALLTHROUGH -> {
                ASTFallthroughStatement ftStmt = parseFallthroughStatement();
                yield new ASTStatement(loc, Arrays.asList(ftStmt));
            }
            case ASSERT -> {
                ASTAssertStatement assertStmt = parseAssertStatement();
                yield new ASTStatement(loc, Arrays.asList(assertStmt));
            }
            case IF -> {
                ASTIfStatement ifStmt = parseIfStatement();
                yield new ASTStatement(loc, Arrays.asList(ifStmt));
            }
            case WHILE -> {
                ASTWhileStatement whileStmt = parseWhileStatement();
                yield new ASTStatement(loc, Arrays.asList(whileStmt));
            }
            case FOR -> {
                ASTForStatement forStmt = parseForStatement();
                yield new ASTStatement(loc, Arrays.asList(forStmt));
            }
            case DO -> {
                ASTDoStatement doStmt = parseDoStatement();
                yield new ASTStatement(loc, Arrays.asList(doStmt));
            }
            case CRITICAL -> {
                ASTCriticalStatement doStmt = parseCriticalStatement();
                yield new ASTStatement(loc, Arrays.asList(doStmt));
            }
            case TRY -> {
                ASTTryStatement tryStmt = parseTryStatement();
                yield new ASTStatement(loc, Arrays.asList(tryStmt));
            }
            case SWITCH -> {
                ASTSwitchStatement switchStmt = parseSwitchStatement();
                yield new ASTStatement(loc, Arrays.asList(switchStmt));
            }
            case USE -> {
                ASTUseStatement useStmt = parseUseStatement();
                yield new ASTStatement(loc, Arrays.asList(useStmt));
            }
            case YIELD -> {
                ASTYieldStatement yieldStmt = parseYieldStatement();
                yield new ASTStatement(loc, Arrays.asList(yieldStmt));
            }
            default -> {
                ASTExpressionStatement exprStmt = parseExpressionStatement();
                yield new ASTStatement(loc, Arrays.asList(exprStmt));
            }
        };
    }

    /**
     * Parses an <code>ASTStatement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatement</code>.
     */
    public ASTStatement parseStatement(ASTPrimary primary) {
        return new ASTStatement(primary.getLocation(), Arrays.asList(parseExpressionStatement(primary)));
    }

    /**
     * Parses an <code>ASTSwitchStatement</code>.
     * @return An <code>ASTSwitchStatement</code>.
     */
    public ASTSwitchStatement parseSwitchStatement() {
        Location loc = curr().getLocation();
        if (accept(SWITCH) == null) {
            throw new CompileException(curr().getLocation(), "Expected switch.");
        }
        List<ASTNode> children = Arrays.asList(
            getExpressionsParser().parseExpression(),
            parseSwitchStatementBlock()
        );
        ASTSwitchStatement node = new ASTSwitchStatement(loc, children);
        node.setOperation(SWITCH);
        return node;
    }

    /**
     * Parses an <code>ASTSwitchStatementBlock</code>.
     * @return An <code>ASTSwitchStatementBlock</code>.
     */
    public ASTSwitchStatementBlock parseSwitchStatementBlock() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '{'.");
        }
        List<ASTNode> children = Arrays.asList(parseSwitchStatementRules());
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'.");
        }
        ASTSwitchStatementBlock node = new ASTSwitchStatementBlock(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses a <code>SwitchStatementRules</code>.
     * <em>
     * SwitchStatementRules:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatementRule {SwitchStatementRule}<br>
     * </em>
     * @return A <code>ASTListNode</code> of type <code>SWITCH_STMT_RULES</code>.
     */
    public ASTListNode parseSwitchStatementRules() {
        return parseMultiple(
                t -> test(t, CASE, DEFAULT, MUT, VAR, IDENTIFIER) || isPrimary(curr()),
                "Expected a switch case.",
                this::parseSwitchStatementRule,
                SWITCH_STMT_RULES
        );
    }

    /**
     * Parses an <code>ASTSwitchStatementRule</code>.
     * @return A <code>ASTSwitchStatementRule</code>.
     */
    public ASTSwitchStatementRule parseSwitchStatementRule() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getExpressionsParser().parseSwitchLabel());
        if (accept(ARROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected arrow (->).");
        }
        switch(curr().getType()) {
        case OPEN_BRACE -> children.add(parseBlock());
        case THROW ->  children.add(parseThrowStatement());
        default -> children.add(parseExpressionStatement());
        }
        ASTSwitchStatementRule ssr = new ASTSwitchStatementRule(loc, children);
        ssr.setOperation(ARROW);
        return ssr;
    }

    /**
     * Parses an <code>ASTTryStatement</code>.
     * @return An <code>ASTTryStatement</code>.
     */
    public ASTTryStatement parseTryStatement() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(TRY) == null) {
            throw new CompileException(curr().getLocation(), "Expected try.");
        }
        if (isCurr(OPEN_PARENTHESIS)) {
            children.add(parseResourceSpecification());
        }
        children.add(parseBlock());
        if (isCurr(CATCH)) {
            children.add(parseCatches());
        }
        if (isCurr(FINALLY)) {
            children.add(parseFinally());
        }
        if (children.size() <= 1) {
            throw new CompileException(curr().getLocation(), "Expected 'catch' and/or 'finally' block.");
        }
        ASTTryStatement node = new ASTTryStatement(loc, children);
        node.setOperation(TRY);
        return node;
    }

    /**
     * Parses an <code>ASTResourceSpecification</code>.
     * @return An <code>ASTResourceSpecification</code>.
     */
    public ASTResourceSpecification parseResourceSpecification() {
        Location loc = curr().getLocation();
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('.");
        }
        ASTResourceSpecification node = new ASTResourceSpecification(loc, Arrays.asList(parseResourceList()));
        if (isCurr(SEMICOLON)) {
            accept(SEMICOLON);
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }
        return node;
    }

    /**
     * Parses a <code>ResourceList</code>.
     * <em>
     * ResourceList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Resource {; Resource}
     * </em>
     * @return An <code>ASTListNode</code> of type <code></code>.
     */
    public ASTListNode parseResourceList() {
        return parseList(
                t -> isPrimary(t) || isAcceptedOperator(Arrays.asList(MUT, CONSTANT, VAR)) != null,
                "Expected an expression.",
                SEMICOLON,
                this::parseResource,
                RESOURCES
        );
    }

    /**
     * Parses an <code>ASTResource</code>.
     * @return An <code>ASTResource</code>.
     */
    public ASTResource parseResource() {
        Location loc = curr().getLocation();
        ASTPrimary primary;
        switch(curr().getType()) {
        case MUT:
        case CONSTANT:
        case VAR:
            return new ASTResource(loc, Arrays.asList(parseResourceDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return new ASTResource(loc, Arrays.asList(parseResourceDeclaration(dt)));
            }
            else {
                // Convert to Expression Name.
                ASTListNode exprName = getTypesParser().convertToExpressionName(dt);
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
        List<ASTNode> children = primary.getChildren();
        if (children.size() == 1) {
            ASTNode child = children.get(0);
            if ((child instanceof ASTListNode exprName && exprName.getType() == EXPR_NAME_IDS)|| child instanceof ASTFieldAccess) {
                return new ASTResource(loc, Arrays.asList(child));
            }
        }
        throw new CompileException(curr().getLocation(), "Expected resource declaration or variable.");
    }

    /**
     * Parses an <code>ASTResourceDeclaration</code>.
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        ASTResourceDeclaration node = new ASTResourceDeclaration(loc, children);
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            children.add(parseVariableModifierList());
        }
        children.add(parseLocalVariableType());
        children.add(getNamesParser().parseIdentifier());
        if (accept(EQUAL) == null) {
            throw new CompileException(curr().getLocation(), "Expected '='.");
        }
        children.add(getExpressionsParser().parseExpression());
        node.setOperation(EQUAL);
        return node;
    }

    /**
     * Parses an <code>ASTResourceDeclaration</code>, given an already parsed
     * <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResourceDeclaration</code>.
     */
    public ASTResourceDeclaration parseResourceDeclaration(ASTDataType dt) {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        ASTResourceDeclaration node = new ASTResourceDeclaration(loc, children);
        children.add(new ASTLocalVariableType(loc, Arrays.asList(dt)));
        children.add(getNamesParser().parseIdentifier());
        if (accept(EQUAL) == null) {
            throw new CompileException(curr().getLocation(), "Expected '='.");
        }
        children.add(getExpressionsParser().parseExpression());
        node.setOperation(EQUAL);
        return node;
    }

    /**
     * Parses a <code>Catches</code>.
     * <em>
     * Catches:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CatchClause {CatchClause}
     * </em>
     * @return An <code>ASTListNode</code> of type <code>CATCH_CLAUSES</code>.
     */
    public ASTListNode parseCatches() {
        return parseMultiple(
                t -> test(t, CATCH),
                "Expected catch clause.",
                this::parseCatchClause,
                CATCH_CLAUSES
        );
    }

    /**
     * Parses an <code>ASTCatchClause</code>.
     * @return An <code>ASTCatchClause</code>.
     */
    public ASTCatchClause parseCatchClause() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (accept(CATCH) == null) {
            throw new CompileException(curr().getLocation(), "Expected catch.");
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected '('");
        }
        children.add(parseCatchFormalParameter());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'");
        }
        children.add(parseBlock());
        ASTCatchClause node = new ASTCatchClause(loc, children);
        node.setOperation(CATCH);
        return node;
    }

    /**
     * Parses an <code>ASTCatchFormalParameter</code>.
     * @return An <code>ASTCatchFormalParameter</code>.
     */
    public ASTCatchFormalParameter parseCatchFormalParameter() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isAcceptedOperator(Arrays.asList(MUT, VAR)) != null) {
            children.add(parseVariableModifierList());
        }
        children.add(parseCatchType());
        children.add(getNamesParser().parseIdentifier());
        return new ASTCatchFormalParameter(loc, children);
    }

    /**
     * Parses a <code>ASTCatchType</code>.
     * <em>
     * CatchType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType {| DataType}
     * </em>
     * @return An <code>ASTListNode</code> of type <code>DATA_TYPES</code>.
     */
    public ASTListNode parseCatchType() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected data type.",
                PIPE,
                getTypesParser()::parseDataType,
                DATA_TYPES
        );
    }

    /**
     * Parses an <code>ASTFinally</code>.
     * @return An <code>ASTFinally</code>.
     */
    public ASTFinally parseFinally() {
        Location loc = curr().getLocation();
        if (accept(FINALLY) == null) {
            throw new CompileException(curr().getLocation(), "Expected finally.");
        }
        ASTBlock block = parseBlock();
        ASTFinally node = new ASTFinally(loc, Arrays.asList(block));
        node.setOperation(FINALLY);
        return node;
    }

    /**
     * Parses an <code>ASTForStatement</code>.
     * @return An <code>ASTForStatement</code>.
     */
    public ASTForStatement parseForStatement() {
        Location loc = curr().getLocation();
        ASTForStatement node;
        if (accept(FOR) == null) {
            System.out.println("Expected for.");
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            System.out.println("Expected '('.");
        }
        if (isCurr(SEMICOLON)) {
            ASTBasicForStatement basicForStmt = parseBasicForStatement(loc);
            node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
        }
        else {
            ASTInit init = parseInit();
            if (isCurr(SEMICOLON)) {
                ASTBasicForStatement basicForStmt = parseBasicForStatement(loc, init);
                node = new ASTForStatement(loc, Arrays.asList(basicForStmt));
            }
            else if (isCurr(COLON)) {
                ASTEnhancedForStatement enhForStmt = parseEnhancedForStatement(loc, init);
                node = new ASTForStatement(loc, Arrays.asList(enhForStmt));
            }
            else {
                throw new CompileException(curr().getLocation(), "Expected semicolon or colon.");
            }
        }
        node.setOperation(FOR);
        return node;
    }

    /**
     * Parses an <code>ASTEnhancedForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the colon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTEnhancedForStatement</code>.
     */
    public ASTEnhancedForStatement parseEnhancedForStatement(Location locFor, ASTInit init) {
        List<ASTNode> children = new ArrayList<>(3);

        List<ASTNode> initChildren = init.getChildren();
        ASTNode child = initChildren.get(0);
        if (child instanceof ASTLocalVariableDeclaration varDecl) {
            List<ASTNode> varDeclChildren = varDecl.getChildren();
            ASTListNode variables = (ASTListNode) varDeclChildren.get(varDeclChildren.size() - 1);
            List<ASTNode> variablesChildren = variables.getChildren();
            if (variablesChildren.size() != 1) {
                throw new CompileException(curr().getLocation(), "Only one variable can be declared in an enhanced for loop.");
            }
            children.add(varDecl);
        }
        else {
            throw new CompileException(curr().getLocation(), "Enhanced for loop requires a variable declaration before the colon.");
        }

        if (accept(COLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected colon.");
        }
        children.add(getExpressionsParser().parseConditionalExpression());
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }
        children.add(parseBlock());
        ASTEnhancedForStatement node = new ASTEnhancedForStatement(locFor, children);
        node.setOperation(COLON);
        return node;
    }

    /**
     * Parses an <code>ASTBasicForStatement</code>, given that "for (" has
     * already been parsed, and no <code>ASTInit</code> was found before the
     * first semicolon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor) {
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        if (!isCurr(SEMICOLON)) {
            children.add(getExpressionsParser().parseConditionalExpression());
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected second semicolon.");
        }
        if (!isCurr(CLOSE_PARENTHESIS)) {
            children.add(parseStatementExpressionList());
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            throw new CompileException(curr().getLocation(), "Expected ')'.");
        }
        children.add(parseBlock());
        ASTBasicForStatement node = new ASTBasicForStatement(locFor, children);
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTBasicForStatement</code>, given that "for (" has
     * already been parsed, and following that the given <code>ASTInit</code>
     * was found and parsed before the first semicolon.
     * @param locFor The location of the "for" keyword, already parsed.
     * @param init An already parsed <code>ASTInit</code>.
     * @return An <code>ASTBasicForStatement</code>.
     */
    public ASTBasicForStatement parseBasicForStatement(Location locFor, ASTInit init) {
        ASTBasicForStatement node = parseBasicForStatement(locFor);
        node.getChildren().add(0, init);
        return node;
    }

    /**
     * Parses an <code>ASTIfStatement</code>.
     * @return An <code>ASTIfStatement</code>.
     */
    public ASTIfStatement parseIfStatement() {
        Location loc = curr().getLocation();
        if (accept(IF) == null) {
            throw new CompileException(curr().getLocation(), "Expected if.");
        }
        List<ASTNode> children = new ArrayList<>(4);
        if (isCurr(OPEN_BRACE)) {
            accept(OPEN_BRACE);
            children.add(parseInit());
            if (accept(CLOSE_BRACE) == null) {
                throw new CompileException(curr().getLocation(), "Expected '}'.");
            }
        }
        children.add(getExpressionsParser().parseConditionalExpression());
        children.add(parseBlock());
        // Greedy else.
        if (isCurr(ELSE)) {
            accept(ELSE);
            if (isCurr(IF)) {
                children.add(parseIfStatement());
            }
            else if (isCurr(OPEN_BRACE)) {
                children.add(parseBlock());
            }
            else {
                throw new CompileException(curr().getLocation(), "Expected 'if' or a block.");
            }
        }
        ASTIfStatement node = new ASTIfStatement(loc, children);
        node.setOperation(IF);
        return node;
    }

    /**
     * Parses an <code>ASTWhileStatement</code>.
     * @return An <code>ASTWhileStatement</code>.
     */
    public ASTWhileStatement parseWhileStatement() {
        Location loc = curr().getLocation();
        if (accept(WHILE) == null) {
            throw new CompileException(curr().getLocation(), "Expected while.");
        }
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(OPEN_BRACE)) {
            accept(OPEN_BRACE);
            children.add(parseInit());
            if (accept(CLOSE_BRACE) == null) {
                throw new CompileException(curr().getLocation(), "Expected '}'.");
            }
        }
        children.add(getExpressionsParser().parseConditionalExpression());
        children.add(parseBlock());
        ASTWhileStatement node = new ASTWhileStatement(loc, children);
        node.setOperation(WHILE);
        return node;
    }

    /**
     * Parses an <code>ASTDoStatement</code>.
     * @return An <code>ASTDoStatement</code>.
     */
    public ASTDoStatement parseDoStatement() {
        Location loc = curr().getLocation();
        if (accept(DO) == null) {
            throw new CompileException(curr().getLocation(), "Expected do.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseBlock());
        if (accept(WHILE) == null) {
            throw new CompileException(curr().getLocation(), "Expected while.");
        }
        children.add(getExpressionsParser().parseConditionalExpression());
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        ASTDoStatement node = new ASTDoStatement(loc, children);
        node.setOperation(DO);
        return node;
    }

    /**
     * Parses an <code>ASTCriticalStatement</code>.
     * @return An <code>ASTCriticalStatement</code>.
     */
    public ASTCriticalStatement parseCriticalStatement() {
        Location loc = curr().getLocation();
        if (accept(CRITICAL) == null) {
            throw new CompileException(curr().getLocation(), "Expected critical.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getExpressionsParser().parseConditionalExpression());
        children.add(parseBlock());
        ASTCriticalStatement node = new ASTCriticalStatement(loc, children);
        node.setOperation(CRITICAL);
        return node;
    }

    /**
     * Parses an <code>ASTThrowStatement</code>.
     * @return An <code>ASTThrowStatement</code>.
     */
    public ASTThrowStatement parseThrowStatement() {
        Location loc = curr().getLocation();
        if (accept(THROW) == null) {
            throw new CompileException(curr().getLocation(), "Expected throw.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        children.add(getExpressionsParser().parseExpression());
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTThrowStatement node = new ASTThrowStatement(loc, children);
        node.setOperation(THROW);
        return node;
    }

    /**
     * Parses an <code>ASTYieldStatement</code>.
     * @return An <code>ASTYieldStatement</code>.
     */
    public ASTYieldStatement parseYieldStatement() {
        Location loc = curr().getLocation();
        if (accept(YIELD) == null) {
            throw new CompileException(curr().getLocation(), "Expected yield.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        children.add(getExpressionsParser().parseExpression());
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTYieldStatement node = new ASTYieldStatement(loc, children);
        node.setOperation(YIELD);
        return node;
    }

    /**
     * Parses an <code>ASTUseStatement</code>.
     * @return An <code>ASTUseStatement</code>.
     */
    public ASTUseStatement parseUseStatement() {
        Location loc = curr().getLocation();
        if (accept(USE) == null) {
            throw new CompileException(curr().getLocation(), "Expected use.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        children.add(getExpressionsParser().parseExpression());
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTUseStatement node = new ASTUseStatement(loc, children);
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTReturnStatement</code>.
     * @return An <code>ASTReturnStatement</code>.
     */
    public ASTReturnStatement parseReturnStatement() {
        Location loc = curr().getLocation();
        if (accept(RETURN) == null) {
            throw new CompileException(curr().getLocation(), "Expected return.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(SEMICOLON)) {
            children.add(getExpressionsParser().parseExpression());
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTReturnStatement node = new ASTReturnStatement(loc, children);
        node.setOperation(RETURN);
        return node;
    }

    /**
     * Parses an <code>ASTBreakStatement</code>.
     * @return An <code>ASTBreakStatement</code>.
     */
    public ASTBreakStatement parseBreakStatement() {
        Location loc = curr().getLocation();
        if (accept(BREAK) == null) {
            throw new CompileException(curr().getLocation(), "Expected break.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTBreakStatement node = new ASTBreakStatement(loc, Collections.emptyList());
        node.setOperation(BREAK);
        return node;
    }

    /**
     * Parses an <code>ASTContinueStatement</code>.
     * @return An <code>ASTContinueStatement</code>.
     */
    public ASTContinueStatement parseContinueStatement() {
        Location loc = curr().getLocation();
        if (accept(CONTINUE) == null) {
            throw new CompileException(curr().getLocation(), "Expected continue.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTContinueStatement node = new ASTContinueStatement(loc, Collections.emptyList());
        node.setOperation(CONTINUE);
        return node;
    }

    /**
     * Parses an <code>ASTFallthroughStatement</code>.
     * @return An <code>ASTFallthroughStatement</code>.
     */
    public ASTFallthroughStatement parseFallthroughStatement() {
        Location loc = curr().getLocation();
        if (accept(FALLTHROUGH) == null) {
            throw new CompileException(curr().getLocation(), "Expected fallthrough.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTFallthroughStatement node = new ASTFallthroughStatement(loc, Collections.emptyList());
        node.setOperation(FALLTHROUGH);
        return node;
    }

    /**
     * Parses an <code>ASTAssertStatement</code>.
     * @return An <code>ASTAssertStatement</code>.
     */
    public ASTAssertStatement parseAssertStatement() {
        Location loc = curr().getLocation();
        if (accept(ASSERT) == null) {
            throw new CompileException(curr().getLocation(), "Expected assert.");
        }
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getExpressionsParser().parseExpression());
        if (isCurr(COLON)) {
            accept(COLON);
            children.add(getExpressionsParser().parseExpression());
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTAssertStatement node = new ASTAssertStatement(loc, children);
        node.setOperation(ASSERT);
        return node;
    }

    /**
     * Parses an <code>ASTExpressionStatement</code>.
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement() {
        Location loc = curr().getLocation();

        ASTStatementExpression stmtExpr = parseStatementExpression();
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Semicolon expected.");
        }
        ASTExpressionStatement exprStmt = new ASTExpressionStatement(loc, Arrays.asList(stmtExpr));
        exprStmt.setOperation(SEMICOLON);
        return exprStmt;
    }

    /**
     * Parses an <code>ASTExpressionStatement</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTExpressionStatement</code>.
     */
    public ASTExpressionStatement parseExpressionStatement(ASTPrimary primary) {
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Semicolon expected.");
        }
        ASTExpressionStatement exprStmt = new ASTExpressionStatement(primary.getLocation(), Arrays.asList(stmtExpr));
        exprStmt.setOperation(SEMICOLON);
        return exprStmt;
    }

    /**
     * Parses an <code>ASTInit</code>.
     * @return An <code>ASTInit</code>.
     */
    public ASTInit parseInit() {
        Location loc = curr().getLocation();
        switch(curr().getType()) {
        case MUT:
        case CONSTANT:
        case VAR:
            return new ASTInit(loc, Arrays.asList(parseLocalVariableDeclaration()));
        case IDENTIFIER:
            ASTDataType dt = getTypesParser().parseDataType();
            // DataType varName ...
            if (isCurr(IDENTIFIER)) {
                return new ASTInit(loc, Arrays.asList(parseLocalVariableDeclaration(dt)));
            }
            else {
                // Convert to Expression Name.
                ASTListNode exprName = getTypesParser().convertToExpressionName(dt);
                // There may be more or a Primary to parse, e.g. method
                // invocation, element access, and/or qualified class instance
                // creation.
                ASTPrimary primary = getExpressionsParser().parsePrimary(exprName);
                return new ASTInit(loc, Arrays.asList(parseStatementExpressionList(primary)));
            }
        default:
            return new ASTInit(loc, Arrays.asList(parseStatementExpressionList()));
        }
    }

    /**
     * Parses a <code>StatementExpressionList</code>.
     * <em>
     * StatementExpressionList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression {, StatementExpression}
     * </em>
     * @return An <code>ASTListNode</code> of type <code>STMT_EXPRS</code>.
     */
    public ASTListNode parseStatementExpressionList() {
        return parseList(
                t -> test(t, INCREMENT, DECREMENT) || isPrimary(t),
                "Expected a statement expression.",
                COMMA,
                this::parseStatementExpression,
                STMT_EXPRS,
                false
        );
    }

    /**
     * Parses an <code>ASTStatementExpressionList</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList parseStatementExpressionList(ASTPrimary primary) {
        Location loc = primary.getLocation();
        ASTStatementExpression stmtExpr = parseStatementExpression(primary);
        ASTStatementExpressionList node;
        if (isCurr(COMMA)) {
            accept(COMMA);
            ASTListNode rest = parseStatementExpressionList();
            List<ASTNode> children = rest.getChildren();
            children.add(0, stmtExpr);
            node = new ASTStatementExpressionList(loc, rest.getChildren());
        }
        else {
            List<ASTNode> children = Arrays.asList(stmtExpr);
            node = new ASTStatementExpressionList(loc, children);
        }
        node.setOperation(COMMA);
        return node;
    }

    /**
     * Parses an <code>ASTStatementExpression</code>.
     * @return An <code>ASTStatementExpression</code>.
     */
    public ASTStatementExpression parseStatementExpression() {
        if (isPrimary(curr())) {
            ASTPrimary primary = getExpressionsParser().parsePrimary();
            return parseStatementExpression(primary);
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected assignment, postfix, method invocation, or class instance creation.");
        }
    }

    /**
     * Parses an <code>ASTStatementExpression</code>, given an already parsed
     * <code>ASTPrimary</code>.
     * @param primary An already parsed <code>ASTPrimary</code>.
     * @return An <code>ASTStatementExpression</code>.
     */
    public ASTStatementExpression parseStatementExpression(ASTPrimary primary) {
        Location loc = primary.getLocation();
        if (isCurr(INCREMENT) || isCurr(DECREMENT)) {
            return new ASTStatementExpression(loc, Arrays.asList(parsePostfix(loc, primary.getLeftHandSide())));
        }
        else {
            // Primary may already be a method invocation or class instance creation expression.
            // If so, retrieve and use it.
            ASTNode child = primary.getChildren().get(0);
            if (child instanceof ASTMethodInvocation ||
                    (child instanceof ASTBinaryNode cice && cice.getOperation() == NEW) ||
                    child instanceof ASTUnqualifiedClassInstanceCreationExpression) {
                return new ASTStatementExpression(loc, Arrays.asList(child));
            }
            else {
                // Assume assignment.
                return new ASTStatementExpression(loc, Arrays.asList(parseAssignment(loc, primary.getLeftHandSide())));
            }
        }
    }

    /**
     * Parses an <code>ASTPrefixExpression</code>, given an <code>ASTLeftHandSide</code>
     * that has already been parsed and its <code>Location</code>.
     * @param loc The <code>Location</code>.
     * @param lhs An already parsed <code>ASTLeftHandSide</code>.
     * @return An <code>ASTAssignment</code>.
     */
    public ASTAssignment parseAssignment(Location loc, ASTLeftHandSide lhs) {
        TokenType currToken = curr().getType();
        switch(currToken) {
        case EQUAL:
        case PLUS_EQUALS:
        case MINUS_EQUALS:
        case STAR_EQUALS:
        case SLASH_EQUALS:
        case PERCENT_EQUALS:
        case SHIFT_LEFT_EQUALS:
        case SHIFT_RIGHT_EQUALS:
        case AMPERSAND_EQUALS:
        case PIPE_EQUALS:
        case CARET_EQUALS:
            List<ASTNode> children = new ArrayList<>(2);
            children.add(lhs);
            accept(currToken);
            children.add(getExpressionsParser().parseExpression());
            ASTAssignment node = new ASTAssignment(loc, children);
            node.setOperation(currToken);
            return node;
        default:
            throw new CompileException(curr().getLocation(), "Expected assignment operator.");
        }
    }

    /**
     * Parses an <code>ASTPostfix</code>.
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
            throw new CompileException(curr().getLocation(), "Operator ++ or -- expected.");
        }
    }
}
