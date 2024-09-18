package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInferredParameterList</code> is a list of identifiers
 * separated by commas.</p>
 *
 * <em>
 * InferredParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}<br>
 * </em>
 */
public class ASTInferredParameterList extends ASTParentNode {
    /**
     * Constructs an <code>ASTFormalParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTInferredParameterList(Location location, List<ASTNode> children) {
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
