package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAdtBodyDeclarations</code> is a semicolon followed by an
 * optional interface part list.</p>
 *
 * <em>
 * AdtBodyDeclarations:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;; [InterfacePartList]
 * </em>
 */
public class ASTAdtBodyDeclarations extends ASTParentNode {
    /**
     * Constructs an <code>ASTAdtBodyDeclarations</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAdtBodyDeclarations(Location location, List<ASTNode> children) {
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
