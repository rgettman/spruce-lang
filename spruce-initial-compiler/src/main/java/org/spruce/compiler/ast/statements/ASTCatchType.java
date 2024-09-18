package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatchType</code> is a pipe-separated list of data types.</p>
 *
 * <em>
 * CatchType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType {| DataType}
 * </em>
 */
public class ASTCatchType extends ASTParentNode {
    /**
     * Constructs an <code>ASTCatchType</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCatchType(Location location, List<ASTNode> children) {
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
