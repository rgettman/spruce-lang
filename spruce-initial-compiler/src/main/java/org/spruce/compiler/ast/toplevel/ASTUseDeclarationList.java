package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseDeclarationList</code> is multiple use declarations.</p>
 *
 * <em>
 * UseDeclarationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseDeclaration {UseDeclaration}
 * </em>
 */
public class ASTUseDeclarationList extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseDeclarationList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseDeclarationList(Location location, List<ASTNode> children) {
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

