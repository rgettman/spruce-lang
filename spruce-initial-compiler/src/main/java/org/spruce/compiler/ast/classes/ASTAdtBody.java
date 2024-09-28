package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAdtBody</code> is a "{", followed by a variant list,
 * optionally followed by adt body declarations, followed by a "}".</p>
 *
 * <em>
 * AdtBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ VariantList [AdtBodyDeclarations] }
 * </em>
 */
public class ASTAdtBody extends ASTParentNode {
    /**
     * Constructs an <code>ASTAdtBody</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAdtBody(Location location, List<ASTNode> children) {
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
