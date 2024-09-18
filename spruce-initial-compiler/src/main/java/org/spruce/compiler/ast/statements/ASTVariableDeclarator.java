package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableDeclarator</code> is an identifier optionally
 * followed by assignment to a variable initializer.</p>
 *
 * <em>
 * VariableDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = VariableInitializer
 * </em>
 */
public class ASTVariableDeclarator extends ASTParentNode {
    /**
     * Constructs an <code>ASTVariableDeclarator</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableDeclarator(Location location, List<ASTNode> children) {
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
