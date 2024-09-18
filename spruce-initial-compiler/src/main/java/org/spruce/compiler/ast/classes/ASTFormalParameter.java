package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFormalParameter</code> is an optional variable modifier list,
 * a data type, possibly an ellipsis, and an identifier.</p>
 *
 * <em>
 * FormalParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType ... Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType ... Identifier
 * </em>
 */
public class ASTFormalParameter extends ASTParentNode {
    /**
     * Constructs an <code>ASTFormalParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTFormalParameter(Location location, List<ASTNode> children) {
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
