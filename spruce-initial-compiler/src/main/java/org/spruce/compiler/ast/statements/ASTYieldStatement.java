package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTYieldStatement</code> is "yield" followed by an
 * expression, then a semicolon.</p>
 *
 * <em>
 * YieldStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;yield Expression ;
 * </em>
 */
public class ASTYieldStatement extends ASTParentNode{
    /**
     * Constructs an <code>ASTYieldStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTYieldStatement(Location location, List<ASTNode> children) {
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
