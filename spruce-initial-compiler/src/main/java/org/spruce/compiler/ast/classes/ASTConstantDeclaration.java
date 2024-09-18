package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTConstantDeclaration</code> is an optional ConstantModifier, a
 * DataType, and a VariableDeclaratorList.</p>
 *
 * <em>
 * ConstantDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[ConstantModifier] DataType VariableDeclaratorList
 * </em>
 */
public class ASTConstantDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTConstantDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTConstantDeclaration(Location location, List<ASTNode> children) {
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
