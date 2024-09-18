package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSharedConstructor</code> is "shared" followed by "constructor",
 * then a pair of parentheses, then a Block.</p>
 *
 * <em>
 * SharedConstructor:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;shared constructor ( ) Block
 * </em>
 */
public class ASTSharedConstructor extends ASTParentNode {
    /**
     * Constructs an <code>ASTSharedConstructor</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTSharedConstructor(Location location, List<ASTNode> children) {
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
