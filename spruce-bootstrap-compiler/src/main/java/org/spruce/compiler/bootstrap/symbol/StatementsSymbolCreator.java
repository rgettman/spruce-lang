package org.spruce.compiler.bootstrap.symbol;

import java.util.Map;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;

import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.SCOPE;

/**
 * A <code>StatementsSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to statement level AST elements.
 */
public class StatementsSymbolCreator extends BasicSymbolCreator {
    private static final Map<Class<? extends ParentNode>, String> SCOPE_NAMES = Map.of(
            ASTBlock.class, "block",
            ASTBasicForStatement.class, "for",
            ASTEnhancedForStatement.class, "for",
            ASTIfStatement.class, "if",
            ASTWhileStatement.class, "while"
    );

    /**
     * Constructs a <code>StatementsSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param globalLookup A <code>GlobalLookup</code>.
     */
    public StatementsSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, GlobalLookup globalLookup) {
        super(symbolCreator, msgProducer, globalLookup);
    }

    /**
     * Creates symbols for a <code>Block</code>, and inserts them into the
     * given parent table.
     * @param block An <code>ASTBlock</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForBlock(ASTBlock block, ParentSymbol parent) {
        createSymbolsForBlock(block, parent, "");
    }

    /**
     * Creates symbols for a <code>Block</code> and inserts them into the given
     * parent table.  Sub-scopes are named using the given scope prefix.
     * @param block An <code>ASTBlock</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param scopePrefix A prefix name for scope dummy symbol names.
     */
    public void createSymbolsForBlock(ASTBlock block, ParentSymbol parent, String scopePrefix) {
        int blockNbr = 0;

        for (ASTBlockStatement blockStmt : block.getBlockStmts().getTypedChildren()) {
            createSymbolsForBlockStatement(blockStmt, parent, scopePrefix, blockNbr);
            switch (blockStmt) {
            case ASTBlock ignored -> blockNbr++;
            case ASTForStatement ignored -> blockNbr++;
            case ASTIfStatement ignored -> blockNbr++;
            case ASTWhileStatement ignored -> blockNbr++;
            default -> {}
            }
        }
    }

    /**
     * Creates symbols for a <code>BlockStatement</code> and inserts them into
     * the given parent table.  Sub-scopes are named using the given scope prefix.
     * @param blockStmt An <code>ASTBlockStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param scopePrefix A prefix name for scope dummy symbol names.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForBlockStatement(ASTBlockStatement blockStmt, ParentSymbol parent,
                                                       String scopePrefix, int blockNbr) {
        switch (blockStmt) {
        case ASTLocalVariableDeclarationStatement localVarDeclStmt ->
            createSymbolsForLocalVarDeclStatement(localVarDeclStmt, parent);
        case ASTBlock subBlock ->
            createSymbolsForSubBlock(subBlock, parent, scopePrefix, blockNbr);
        case ASTBasicForStatement basicForStmt ->
            createSymbolsForBasicForStatement(basicForStmt, parent, scopePrefix, blockNbr);
        case ASTEnhancedForStatement enhancedForStmt ->
            createSymbolsForEnhancedForStatement(enhancedForStmt, parent, scopePrefix, blockNbr);
        case ASTIfStatement ifStmt ->
            createSymbolsForIfStatement(ifStmt, parent, scopePrefix, blockNbr);
        case ASTWhileStatement whileStmt ->
            createSymbolsForWhileStatement(whileStmt, parent, scopePrefix, blockNbr);
        // Any other statements that declare symbols go above.

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
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForSubBlock(ASTBlock subBlock, ParentSymbol parent, String prefix, int blockNbr) {
        createSymbolsForGeneralBlock(subBlock, parent, prefix, blockNbr);
    }

    /**
     * Creates symbols for a <code>BasicForStatement</code>,
     * including any variables declared in the initialization section, plus any
     * symbols from its block, and inserts them into the given parent table.
     * Sub-scopes are named using the given scope prefix and block number.
     * @param basicForStmt An <code>ASTBasicForStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForBasicForStatement(ASTBasicForStatement basicForStmt, ParentSymbol parent,
                                                    String prefix, int blockNbr) {
        String name = createScopeSymbolName(basicForStmt, prefix, blockNbr);
        ChildSymbolTable parentTable = parent.getTable();
        ParentSymbol symbol = new ParentSymbol(basicForStmt.getLocation(), name, Kind.FOR_STMT, parentTable,
                FLAG_NONE);
        insertSymbol(parentTable, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        basicForStmt.setDeclSymbol(symbol);
        if (basicForStmt.getInit().isPresent() &&
                basicForStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, symbol);
        }
        createSymbolsForBlock(basicForStmt.getBlock(), symbol, prefix);
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for a
     * <code>EnhancedForStatement</code>, including the variable declared in the
     * variable declarator, plus any symbols from its block, and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param enhancedForStmt An <code>ASTEnhancedForStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForEnhancedForStatement(ASTEnhancedForStatement enhancedForStmt, ParentSymbol parent,
                                                       String prefix, int blockNbr) {
        String name = createScopeSymbolName(enhancedForStmt, prefix, blockNbr);
        ChildSymbolTable parentTable = parent.getTable();
        ParentSymbol symbol = new ParentSymbol(enhancedForStmt.getLocation(), name, Kind.FOR_STMT, parentTable,
                FLAG_NONE);
        insertSymbol(parentTable, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        enhancedForStmt.setDeclSymbol(symbol);
        createSymbolsForLocalVarDecl(enhancedForStmt.getLocalVarDecl(), symbol);
        createSymbolsForBlock(enhancedForStmt.getBlock(), symbol, prefix);
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for an
     * <code>IfStatement</code>, including any variables declared in the
     * initialization section, plus any symbols from its block, and inserts it
     * into the given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param ifStmt An <code>ASTIfStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForIfStatement(ASTIfStatement ifStmt, ParentSymbol parent, String prefix, int blockNbr) {
        String scopeName = appendScopeName(ifStmt, prefix, blockNbr);
        String name = createScopeSymbolName(scopeName);
        ChildSymbolTable parentTable = parent.getTable();
        ParentSymbol symbol = new ParentSymbol(ifStmt.getLocation(), name, Kind.IF_STMT, parentTable,
                FLAG_NONE);
        insertSymbol(parentTable, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        ifStmt.setDeclSymbol(symbol);
        // Declarations in Init are in scope throughout the entire if statement,
        // including the if block, the else block, and the sub-if statement!
        if (ifStmt.getInit().isPresent() &&
                ifStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, symbol);
        }

        createSymbolsForSubBlock(ifStmt.getIfBlock(), symbol, scopeName, 0);
        if (ifStmt.getElseBlock().isPresent()) {
            createSymbolsForSubBlock(ifStmt.getElseBlock().get(), symbol, scopeName, 1);
        }
        else if (ifStmt.getElseIf().isPresent()) {
            createSymbolsForIfStatement(ifStmt.getElseIf().get(), symbol, scopeName, 1);
        }
    }

    private void createSymbolsForGeneralBlock(ASTBlock subBlock, ParentSymbol parent, String prefix, int blockNbr) {
        String name = createScopeSymbolName(subBlock, prefix, blockNbr);
        ChildSymbolTable parentTable = parent.getTable();
        ParentSymbol symbol = new ParentSymbol(subBlock.getLocation(), name, Kind.BLOCK, parentTable,
                FLAG_NONE);
        insertSymbol(parentTable, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, symbol);
        symbol.setTable(subTable);
        subBlock.setDeclSymbol(symbol);
        createSymbolsForBlock(subBlock, symbol, symbol.getName());
    }

    /**
     * Creates a <code>Symbol</code> representing the scope for a
     * <code>WhileStatement</code>, including any variables declared in the
     * init section, plus any symbols from its block, and inserts it into the
     * given parent table.  Sub-scopes are named using the given scope
     * prefix and block number.
     * @param whileStmt An <code>ASTWhileStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     * @param prefix The scope prefix.
     * @param blockNbr The block number at the current scope level.
     */
    public void createSymbolsForWhileStatement(ASTWhileStatement whileStmt, ParentSymbol parent,
                                                 String prefix, int blockNbr) {
        String name = createScopeSymbolName(whileStmt, prefix, blockNbr);
        ChildSymbolTable parentTable = parent.getTable();
        ParentSymbol symbol = new ParentSymbol(whileStmt.getLocation(), name, Kind.WHILE_STMT, parentTable,
                FLAG_NONE);
        insertSymbol(parentTable, symbol);
        ChildSymbolTable subTable = new ChildSymbolTable(SCOPE, parent);
        symbol.setTable(subTable);
        whileStmt.setDeclSymbol(symbol);
        if (whileStmt.getInit().isPresent() &&
                whileStmt.getInit().get() instanceof ASTLocalVariableDeclaration localVarDecl) {
            createSymbolsForLocalVarDecl(localVarDecl, symbol);
        }
        createSymbolsForBlock(whileStmt.getBlock(), symbol, prefix);
    }

    /**
     * Creates symbols for a <code>LocalVariableDeclarationStatement</code>,
     * and inserts them into the given parent table.
     * @param localVarDeclStmt An <code>ASTLocalVariableDeclarationStatement</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForLocalVarDeclStatement(
            ASTLocalVariableDeclarationStatement localVarDeclStmt, ParentSymbol parent) {
        createSymbolsForLocalVarDecl(localVarDeclStmt.getLocalVarDecl(), parent);
    }

    /**
     * Creates symbols for a <code>LocalVariableDeclaration</code>, and inserts
     * them into the given parent table.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForLocalVarDecl(ASTLocalVariableDeclaration localVarDecl, ParentSymbol parent) {
        for (ASTVariableDeclarator varDecl : localVarDecl.getVarDeclList().getTypedChildren()) {
            String name = varDecl.getVarName().getValue();
            ChildSymbolTable parentTable = parent.getTable();
            VariableSymbol symbol = new VariableSymbol(varDecl.getLocation(), name, Kind.LOCAL, parentTable, FLAG_NONE);
            insertSymbol(parentTable, symbol);
            varDecl.setDeclSymbol(symbol);
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
}
