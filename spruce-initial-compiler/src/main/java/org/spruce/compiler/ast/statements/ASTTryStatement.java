package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTryStatement</code> is "try", optionally followed by a
 * Resource Specification, followed by a block, optionally followed
 * by a Catches, optionally followed by a Finally.  At least one of a Resource
 * Specification, Catches, and Finally are required.</p>
 *
 * <em>
 * TryStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try Block Catches<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try Block [Catches] Finally<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try ResourceSpecification Block [Catches] [Finally]
 * </em>
 */
public class ASTTryStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTTryStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTryStatement(Location location, List<ASTNode> children) {
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
