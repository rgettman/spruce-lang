package org.spruce.compiler.bootstrap.ast.names;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTNamespaceName</code> is a node representing a namespace name.</p>
 *
 * <em>
 * NamespaceName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceName . Identifier
 * </em>
 */
public class ASTNamespaceName extends ASTListNode<ASTIdentifier> {
    private Symbol myDeclSymbol;

    /**
     * Constructs an <code>ASTNamespaceName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTNamespaceName(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.NAMESPACE_IDS);
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
    public Symbol getDeclSymbol() {
        return myDeclSymbol;
    }
}
