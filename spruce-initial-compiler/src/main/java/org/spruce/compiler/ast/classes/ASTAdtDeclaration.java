package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAdtDeclaration</code> is an optional AccessModifier followed by
 * "adt", followed by an Identifier, followed by optional Type Parameters,
 * optional ExtendsInterfaces, then an AdtBody.</p>
 *
 * <em>
 * AdtDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] adt Identifier [TypeParameters] [ExtendsInterfaces] AdtBody
 * </em>
 */
public class ASTAdtDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTAdtDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTAdtDeclaration(Location location, List<ASTNode> children) {
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
