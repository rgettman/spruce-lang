package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTBlock</code> is a "{", optionally a list of block statements,
 * followed by a "}".</p>
 *
 * <em>
 * Block:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ BlockStatements }
 * </em>
 */
public final class ASTBlock extends ASTParentNode implements ASTStatement {
    private final ASTBlockStatements myBlockStmts;
    private Symbol myDeclSymbol;

    /**
     * Constructs an <code>ASTBlock</code> at the given <code>Location</code>
     * with the given <code>ASTBlockStatements</code>.
     * @param location The <code>Location</code>.
     * @param blockStmts An <code>ASTBlockStatements</code>.
     */
    public ASTBlock(Location location, ASTBlockStatements blockStmts) {
        super(location);
        myBlockStmts = blockStmts;
    }

    /**
     * Returns an <code>ASTBlockStatements</code>.
     * @return An <code>ASTBlockStatements</code>.
     */
    public ASTBlockStatements getBlockStmts() {
        return myBlockStmts;
    }

    /**
     * Sets the declaration <code>Symbol</code>.
     * @param symbol The declaration <code>Symbol</code>.
     */
    public void setDeclSymbol(Symbol symbol) {
        myDeclSymbol = symbol;
    }

    /**
     * Returns the declaration <code>Symbol</code>.
     * @return The declaration <code>Symbol</code>.
     */
    public Symbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myBlockStmts);
    }
}
