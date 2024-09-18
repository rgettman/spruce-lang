package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseDeclaration</code> is a use type declaration,
 * a use mult declaration, a use all declaration, or the shared
 * version of any of those three.</p>
 *
 * <em>
 * UseDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseAllDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedAllDeclaration
 * </em>
 */
public class ASTUseDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseDeclaration(Location location, List<ASTNode> children) {
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

