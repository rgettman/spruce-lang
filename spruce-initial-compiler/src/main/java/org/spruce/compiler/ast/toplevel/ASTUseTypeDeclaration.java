package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseTypeDeclaration</code> is "use" followed
 * by a Type Name, then a semicolon.</p>
 *
 * <em>
 * UseTypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use TypeName ;
 * </em>
 */
public class ASTUseTypeDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseTypeDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseTypeDeclaration(Location location, List<ASTNode> children) {
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

