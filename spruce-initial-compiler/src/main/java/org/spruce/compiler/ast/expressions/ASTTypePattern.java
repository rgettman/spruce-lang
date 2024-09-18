package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypePattern</code> is an optional variable modifier list,
 * a data type, and an identifier.</p>
 *
 * <em>
 * TypePattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
 * </em>
 */
public class ASTTypePattern extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypePattern</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypePattern(Location location, List<ASTNode> children) {
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
