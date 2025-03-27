package org.spruce.compiler.symbol;

import java.util.Map;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.expressions.ASTPattern;
import org.spruce.compiler.ast.expressions.ASTRecordPattern;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.ast.expressions.ASTTypePattern;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.common.MessageProducer;

import static org.spruce.compiler.symbol.Symbol.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.SCOPE;

/**
 * A <code>StatementsSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to statement level AST elements.
 */
public class StatementsSymbolCreator extends BasicSymbolCreator {
    private static final Map<Class<? extends ParentNode>, String> SCOPE_NAMES = Map.of(
            ASTBlock.class, "block",
            ASTCatchClause.class, "catch",
            ASTDoStatement.class, "do",
            ASTBasicForStatement.class, "for",
            ASTEnhancedForStatement.class, "for",
            ASTIfStatement.class, "if",
            ASTSwitchStatementRule.class, "rule",
            ASTSwitchStatement.class, "switch",
            ASTTryStatement.class, "try",
            ASTWhileStatement.class, "while"
    );

    /**
     * Constructs a <code>StatementsSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public StatementsSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        super(symbolCreator, msgProducer);
    }

    /**
     * Creates symbols for a <code>Block</code>, and inserts them into the
     * given parent table.
     * @param block An <code>ASTBlock</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForBlock(ASTBlock block, SymbolTable parent) {
        createSymbolsForBlock(block, parent, "");
    }

    /**
     * Creates symbols for a <code>Block</code> and inserts them into the given
     * parent table.  Sub-scopes are named using the given scope prefix.
     * @param block An <code>ASTBlock</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param scopePrefix A prefix name for scope dummy symbol names.
     */
    public void createSymbolsForBlock(ASTBlock block, SymbolTable parent, String scopePrefix) {
        int blockNbr = 0;

        for (ASTBlockStatement blockStmt : block.getBlockStmts().getTypedChildren()) {
            createSymbolsForBlockStatement(blockStmt, parent, scopePrefix, blockNbr);
            switch (blockStmt) {
            case ASTBlock ignored -> blockNbr++;
            case ASTDoStatement ignored -> blockNbr++;
            case ASTForStatement ignored -> blockNbr++;
            case ASTIfStatement ignored -> blockNbr++;
            case ASTSwitchStatement ignored -> blockNbr++;
            case ASTTryStatement ignored -> blockNbr++;
            case ASTWhileStatement ignored -> blockNbr++;
            default -> {}
            }
        }
    }

    /**
     * Creates symbols for a <code>BlockStatement</code> and inserts them into
     * the given parent table.  Sub-scopes are named using the given scope prefix.
     * @param blockStmt An <code>ASTBlockStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param scopePrefix A prefix name for scope dummy symbol names.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForBlockStatement(ASTBlockStatement blockStmt, SymbolTable parent,
                                                       String scopePrefix, int blockNbr) {
        switch (blockStmt) {
        case ASTLocalVariableDeclarationStatement localVarDeclStmt ->
            createSymbolsForLocalVarDeclStatement(localVarDeclStmt, parent);
        case ASTBlock subBlock ->
            createSymbolsForSubBlock(subBlock, parent, scopePrefix, blockNbr);
        case ASTDoStatement doStmt ->
            createSymbolsForDoStatement(doStmt, parent, scopePrefix, blockNbr);
        case ASTBasicForStatement basicForStmt ->
            createSymbolsForBasicForStatement(basicForStmt, parent, scopePrefix, blockNbr);
        case ASTEnhancedForStatement enhancedForStmt ->
            createSymbolsForEnhancedForStatement(enhancedForStmt, parent, scopePrefix, blockNbr);
        case ASTIfStatement ifStmt ->
            createSymbolsForIfStatement(ifStmt, parent, scopePrefix, blockNbr);
        case ASTSwitchStatement switchStmt ->
            createSymbolsForSwitchStatement(switchStmt, parent, scopePrefix, blockNbr);
        case ASTTryStatement tryStmt ->
            createSymbolsForTryStatement(tryStmt, parent, scopePrefix, blockNbr);
        case ASTWhileStatement whileStmt ->
            createSymbolsForWhileStatement(whileStmt, parent, scopePrefix, blockNbr);
        // Any other statements that declare symbols go here.

        // All other statements don't declare any symbols.
        default -> {}
        }
    }

    /**
     * Creates a symbol for a sub-<code>Block</code> and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope prefix
     * and block number.
     * @param subBlock An <code>ASTBlock</code> with its own scope (not the
     *     scope of a parent such as a method or constructor).
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForSubBlock(ASTBlock subBlock, SymbolTable parent, String prefix, int blockNbr) {
        createSymbolsForGeneralBlock(subBlock, parent, prefix, blockNbr, Type.BLOCK);
    }

    /**
     * Creates a symbol for a <code>DoStatement</code> and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope prefix
     * and block number.
     * @param doStmt An <code>ASTDoStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForDoStatement(ASTDoStatement doStmt, SymbolTable parent, String prefix, int blockNbr) {
        String name = createScopeSymbolName(doStmt, prefix, blockNbr);
        ParentSymbol symbol = new ParentSymbol(doStmt.getLocation(), name, Type.DO_STMT, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        createSymbolsForBlock(doStmt.getBlock(), subTable, prefix);
    }

    /**
     * Creates symbols for a <code>BasicForStatement</code>,
     * including any variables declared in the initialization section, plus any
     * symbols from its block, and inserts them into the given parent table.
     * Sub-scopes are named using the given scope prefix and block number.
     * @param basicForStmt An <code>ASTBasicForStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForBasicForStatement(ASTBasicForStatement basicForStmt, SymbolTable parent,
                                                    String prefix, int blockNbr) {
        String name = createScopeSymbolName(basicForStmt, prefix, blockNbr);
        ParentSymbol symbol = new ParentSymbol(basicForStmt.getLocation(), name, Type.FOR_STMT, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        if (basicForStmt.getInit().isPresent() &&
                basicForStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, subTable);
        }
        createSymbolsForBlock(basicForStmt.getBlock(), subTable, prefix);
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for a
     * <code>EnhancedForStatement</code>, including the variable declared in the
     * variable declarator, plus any symbols from its block, and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param enhancedForStmt An <code>ASTEnhancedForStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForEnhancedForStatement(ASTEnhancedForStatement enhancedForStmt, SymbolTable parent,
                                                       String prefix, int blockNbr) {
        String name = createScopeSymbolName(enhancedForStmt, prefix, blockNbr);
        ParentSymbol symbol = new ParentSymbol(enhancedForStmt.getLocation(), name, Type.FOR_STMT, parent,
                FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        createSymbolsForLocalVarDecl(enhancedForStmt.getLocalVarDecl(), subTable);
        createSymbolsForBlock(enhancedForStmt.getBlock(), subTable, prefix);
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for an
     * <code>IfStatement</code>, including any variables declared in the
     * initialization section, plus any symbols from its block, and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param ifStmt An <code>ASTIfStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForIfStatement(ASTIfStatement ifStmt, SymbolTable parent, String prefix, int blockNbr) {
        String scopeName = appendScopeName(ifStmt, prefix, blockNbr);
        String name = createScopeSymbolName(scopeName);
        ParentSymbol symbol = new ParentSymbol(ifStmt.getLocation(), name, Type.IF_STMT, parent,
                FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        // Declarations in Init are in scope throughout the entire if statement,
        // including the if block, the else block, and the sub-if statement!
        if (ifStmt.getInit().isPresent() &&
                ifStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, subTable);
        }

        createSymbolsForSubBlock(ifStmt.getIfBlock(), subTable, scopeName, 0);
        if (ifStmt.getElseBlock().isPresent()) {
            createSymbolsForSubBlock(ifStmt.getElseBlock().get(), subTable, scopeName, 1);
        }
        else if (ifStmt.getElseIf().isPresent()) {
            createSymbolsForIfStatement(ifStmt.getElseIf().get(), subTable, scopeName, 1);
        }
    }

    /**
     * Creates symbols for a <code>SwitchStatement</code> and inserts them into
     * the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param switchStmt An <code>ASTSwitchStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForSwitchStatement(ASTSwitchStatement switchStmt, SymbolTable parent,
                                                String prefix, int blockNbr) {
        String scopeName = appendScopeName(switchStmt, prefix, blockNbr);

        int seq = 0;
        for (ASTSwitchStatementRule rule : switchStmt.getSwitchStmtRules().getTypedChildren()) {
            createSymbolForSwitchStatementRule(rule, parent, scopeName, seq);
            seq++;
        }
    }

    /**
     * Creates a symbol for a <code>SwitchStatementRule</code> and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param rule An <code>ASTSwitchStatementRule</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolForSwitchStatementRule(ASTSwitchStatementRule rule, SymbolTable parent,
                                                               String prefix, int blockNbr) {
        String scopeName = appendScopeName(rule, prefix, blockNbr);
        String name = createScopeSymbolName(scopeName);
        ParentSymbol symbol = new ParentSymbol(rule.getLocation(), name, Type.BLOCK, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);

        ASTSwitchLabel label = rule.getSwitchLabel();
        if (label.getPattern().isPresent()) {
            ASTPattern pattern = label.getPattern().get();
            createSymbolsForPattern(pattern, subTable);
        }

        if (rule.getBlock().isPresent()) {
            createSymbolsForBlock(rule.getBlock().get(), subTable, scopeName);
        }
        // TODO: Expression statements!
    }

    /**
     * Creates symbols for a <code>Pattern</code> and inserts them into the
     * given parent table.
     * @param pattern An <code>ASTPattern</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForPattern(ASTPattern pattern, SymbolTable parent) {
        switch (pattern) {
        case ASTTypePattern tp -> createSymbolForTypePattern(tp, parent);
        case ASTRecordPattern rp -> createSymbolsForRecordPattern(rp, parent);
        }
    }

    /**
     * Creates a symbol for a <code>TypePattern</code> and inserts it into the
     * given parent table.
     * @param tp An <code>ASTTypePattern</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolForTypePattern(ASTTypePattern tp, SymbolTable parent) {
        String name = tp.getIdentifier().getValue();
        long flags = FLAG_NONE;
        if (tp.getVarModList().isPresent()) {
            flags = getFlagsForVariableModifierList(tp.getVarModList().get());
        }
        Symbol symbol = new Symbol(tp.getLocation(), name, Type.PATTERN, parent, flags);
        insertSymbol(parent, symbol);
    }

    /**
     * Creates symbols for a <code>RecordPattern</code>, and inserts them into
     * the given parent table.
     * @param rp An <code>ASTRecordPattern</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForRecordPattern(ASTRecordPattern rp, SymbolTable parent) {
        if (rp.getPatternList().isPresent()) {
            rp.getPatternList().get().getTypedChildren().stream()
                    .forEachOrdered(p -> createSymbolsForPattern(p, parent));
        }
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for a
     * <code>TryStatement</code>, including any variable declared in the
     * resource declaration, plus any symbols from its block, and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param tryStmt An <code>ASTTryStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForTryStatement(ASTTryStatement tryStmt, SymbolTable parent,
                                               String prefix, int blockNbr) {
        String scopeName = appendScopeName(tryStmt, prefix, blockNbr);
        String name = createScopeSymbolName(scopeName);
        ParentSymbol symbol = new ParentSymbol(tryStmt.getLocation(), name, Type.TRY_STMT, parent,
                FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        createSymbolsForTryBlock(tryStmt, subTable, scopeName);

        int subBlockNbr = 1;
        if (tryStmt.getCatches().isPresent()) {
            subBlockNbr = createSymbolsForCatches(tryStmt.getCatches().get(), subTable, scopeName, subBlockNbr);
        }
        if (tryStmt.getFinallyBlock().isPresent()) {
            createSymbolsForFinallyBlock(tryStmt.getFinallyBlock().get(), subTable, scopeName, subBlockNbr);
        }
    }

    /**
     * Creates a symbol for the <code>Block</code> of a <code>TryStatement</code>,
     * and inserts it into the given parent table.
     * @param tryStmt An <code>ASTTryStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     */
    public void createSymbolsForTryBlock(ASTTryStatement tryStmt, SymbolTable parent, String prefix) {
        String scopeName = appendScopeName(tryStmt.getBlock(), prefix, 0);
        String name = createScopeSymbolName(scopeName);

        ParentSymbol symbol = new ParentSymbol(tryStmt.getLocation(), name, Type.BLOCK, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);

        if (tryStmt.getResourceSpec().isPresent()) {
            for (ASTResource resource : tryStmt.getResourceSpec().get().getTypedChildren()) {
                if (resource instanceof ASTResourceDeclaration resourceDecl) {
                    createSymbolForResourceDecl(resourceDecl, subTable);
                }
            }
        }

        createSymbolsForBlock(tryStmt.getBlock(), subTable, scopeName);
    }

    /**
     * Creates symbols for a <code>Catches</code> and inserts them into the
     * given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param catches An <code>ASTCatches</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     * @return The next block number.
     */
    public int createSymbolsForCatches(ASTCatches catches, SymbolTable parent, String prefix, int blockNbr) {
        for (ASTCatchClause catchClause : catches.getTypedChildren()) {
            createSymbolForCatchClause(catchClause, parent, prefix, blockNbr);
            blockNbr++;
        }
        return blockNbr;
    }

    /**
     * Creates a symbol for a <code>CatchClause</code>, and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param catchClause An <code>ASTCatchClause</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolForCatchClause(ASTCatchClause catchClause, SymbolTable parent, String prefix, int blockNbr) {
        String scopeName = appendScopeName(catchClause, prefix, blockNbr);
        String name = createScopeSymbolName(scopeName);

        ParameterizedSymbol symbol = new ParameterizedSymbol(catchClause.getLocation(), name, Type.CATCH, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);

        Symbol catchParamSymbol = createSymbolForCatchParameter(catchClause.getCatchFormalParam(), subTable);
        insertSymbol(subTable, catchParamSymbol);
        symbol.addParameter(catchParamSymbol);

        createSymbolsForBlock(catchClause.getBlock(), subTable, scopeName);
    }

    /**
     * Creates a symbol for a <code>CatchFormalParameter</code>.
     * @param param An <code>ASTCatchFormalParameter</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @return A <code>Symbol</code>.
     */
    public Symbol createSymbolForCatchParameter(ASTCatchFormalParameter param, SymbolTable parent) {
        String name = param.getVarName().getValue();
        long flags = getFlagsForVariableModifierList(param.getVarModifierList());
        return new Symbol(param.getLocation(), name, Symbol.Type.PARAMETER, parent, flags);
    }

    /**
     * Creates a symbol for a finally-<code>Block</code> and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope prefix
     * and block number.
     * @param finallyBlock A <code>finally</code> <code>ASTBlock</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForFinallyBlock(ASTBlock finallyBlock, SymbolTable parent, String prefix, int blockNbr) {
        createSymbolsForGeneralBlock(finallyBlock, parent, prefix, blockNbr, Type.FINALLY);
    }

    private void createSymbolsForGeneralBlock(ASTBlock subBlock, SymbolTable parent, String prefix, int blockNbr,
                                              Symbol.Type type) {
        String name = createScopeSymbolName(subBlock, prefix, blockNbr);
        ParentSymbol symbol = new ParentSymbol(subBlock.getLocation(), name, type, parent, FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        createSymbolsForBlock(subBlock, subTable, symbol.getName());
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for a
     * <code>WhileStatement</code>, including any variables declared in the
     * init section, plus any symbols from its block, and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param whileStmt An <code>ASTWhileStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForWhileStatement(ASTWhileStatement whileStmt, SymbolTable parent,
                                                 String prefix, int blockNbr) {
        String name = createScopeSymbolName(whileStmt, prefix, blockNbr);
        ParentSymbol symbol = new ParentSymbol(whileStmt.getLocation(), name, Type.WHILE_STMT, parent,
                FLAG_NONE);
        insertSymbol(parent, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        if (whileStmt.getInit().isPresent() &&
                whileStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, subTable);
        }
        createSymbolsForBlock(whileStmt.getBlock(), subTable, prefix);
    }

    /**
     * Creates a <code>Symbol</code> for a <code>ResourceDeclaration</code>,
     * and inserts it into the given parent table.
     * @param resourceDecl An <code>ASTResourceDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolForResourceDecl(ASTResourceDeclaration resourceDecl, SymbolTable parent) {
        String name = resourceDecl.getResourceName().getValue();
        long flags = getFlagsForVariableModifierList(resourceDecl.getVarModifierList());
        Symbol symbol =  new Symbol(resourceDecl.getLocation(), name, Type.LOCAL, parent, flags);
        insertSymbol(parent, symbol);
    }

    /**
     * Creates symbols for a <code>LocalVariableDeclarationStatement</code>,
     * and inserts them into the given parent table.
     * @param localVarDeclStmt An <code>ASTLocalVariableDeclarationStatement</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForLocalVarDeclStatement(
            ASTLocalVariableDeclarationStatement localVarDeclStmt, SymbolTable parent) {
        createSymbolsForLocalVarDecl(localVarDeclStmt.getLocalVarDecl(), parent);
    }

    /**
     * Creates symbols for a <code>LocalVariableDeclaration</code>, and inserts
     * them into the given parent table.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForLocalVarDecl(ASTLocalVariableDeclaration localVarDecl, SymbolTable parent) {
        long flags = getFlagsForVariableModifierList(localVarDecl.getVarModifierList());
        for (ASTVariableDeclarator varDecl : localVarDecl.getVarDeclList().getTypedChildren()) {
            String name = varDecl.getVarName().getValue();
            Symbol symbol = new Symbol(varDecl.getLocation(), name, Type.LOCAL, parent, flags);
            insertSymbol(parent, symbol);
        }
    }

    /**
     * Creates a symbol name for a <code>BlockStatement</code> that has its own
     * scope.
     * @param blockStmt An <code>ASTBlockStatement</code>.
     * @param prefix The scope prefix name.
     * @param seq A sequence number.
     * @return The symbol name.
     */
    private String createScopeSymbolName(ASTBlockStatement blockStmt, String prefix, int seq) {
        // Create a dummy symbol name for the sub-block.
        // Place angle-brackets around the new scope name.
        // That makes it an invalid identifier, so it won't collide with any
        // possible identifier.
        return "<" + appendScopeName(blockStmt, prefix, seq) + ">";
    }

    /**
     * Creates a symbol name for a scope name that has already been created.
     * @param scopeName A scope name that has already been created.
     * @return The symbol name.
     * @see #appendScopeName
     */
    private String createScopeSymbolName(String scopeName) {
        // Create a dummy symbol name for the sub-block.
        // Place angle-brackets around the new scope name.
        // That makes it an invalid identifier, so it won't collide with any
        // possible identifier.
        return "<" + scopeName + ">";
    }

    /**
     * Appends a scope name for a <code>BlockStatement</code> that has its own
     * scope.
     * @param node A <code>ParentNode</code> that defines its own scope.
     * @param prefix The scope prefix name.
     * @param seq A sequence number.
     * @return The symbol name.
     */
    private String appendScopeName(ParentNode node, String prefix, int seq) {
        // Append the next scope name for the sub-block.
        // Naming convention for scopes directly under the method/constructor:
        // <typen>, where type could be "block", "if", etc., and n is a sequence number.
        // Recursively apply this convention to sub-scopes, e.g.
        // <block0> can have <block0_if0> and <block0_if1> but
        // <block1> might only have <block1_block0>.
        // This applies to other sub-scopes for other statements that
        // contain their own scopes as in SCOPE_NAMES.
        StringBuilder buf = new StringBuilder();
        buf.append(prefix);
        if (!prefix.isEmpty()) {
            buf.append("_");
        }
        Class<? extends ParentNode> key = node.getClass();
        if (SCOPE_NAMES.containsKey(key)) {
            buf.append(SCOPE_NAMES.get(key));
        }
        else {
            throw internalError("ParentNode representing a scope!");
        }
        buf.append(seq);
        return buf.toString();
    }

    /**
     * Returns the flags for a <code>VariableModifierList</code>, which could
     * include <code>VAR</code> and/or <code>MUT</code>.
     * @param varMods An <code>ASTVariableModifierList</code>.
     * @return A long containing flags for the variable modifier list.
     */
    public long getFlagsForVariableModifierList(ASTVariableModifierList varMods) {
        long flags = FLAG_NONE;
        for (ASTKeywordNode varMod : varMods.getTypedChildren()) {
            switch (varMod.getKeyword()) {
            case VAR -> flags |= FLAG_VARIABLE_VAR;
            case MUT -> flags |= FLAG_VARIABLE_MUT;
            }
        }
        return flags;
    }
}
