package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseSharedMultDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, a dot, then an identifier list
 * within braces, and a semicolon.</p>
 *
 * <em>
 * UseSharedMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . { IdentifierList } ;
 * </em>
 */
public class ASTUseSharedMultDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseSharedMultDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseSharedMultDeclaration(Location location, List<ASTNode> children) {
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

