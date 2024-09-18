package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeName</code> is a node representing a simple type name or
 * a name that could be a namespace name or a type name.</p>
 *
 * <em>
 * TypeName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier
 * </em>
 */
public class ASTTypeName extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypeName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeName(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Converts this type name to a package or type name.
     * @return A package or type name.
     */
    public ASTNamespaceOrTypeName convertToNamespaceOrTypeName() {
        ASTNamespaceOrTypeName notn = new ASTNamespaceOrTypeName(getLocation(), getChildren());
        notn.setOperation(getOperation());
        return notn;
    }
}
