package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTTypeParameters</code> is "&lt;" followed by a TypeParameterList
 * followed by "&gt;".</p>
 *
 * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
 * turn on the type context in the Scanner for the duration of this parsing.</p>
 *
 * <em>
 * TypeParameters:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; TypeParameterList &gt;
 * </em>
 */
public class ASTTypeParameters extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypeParameters</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeParameters(Location location, List<ASTNode> children) {
        super(location, children, TokenType.LESS_THAN);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }
}
