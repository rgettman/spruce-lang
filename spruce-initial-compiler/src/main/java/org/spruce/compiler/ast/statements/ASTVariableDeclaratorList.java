package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableDeclaratorList</code> is a comma-separated list of
 * variable declarators.</p>
 *
 * <em>
 * VariableDeclaratorList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableDeclarator {, VariableDeclarator}
 * </em>
 */
public class ASTVariableDeclaratorList extends ASTParentNode {
    /**
     * Constructs an <code>ASTVariableDeclaratorList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableDeclaratorList(Location location, List<ASTNode> children) {
        super(location, children);
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
