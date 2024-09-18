package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseSharedTypeDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, a dot, an identifier, then a semicolon.</p>
 *
 * <p>The Identifier will initially be parsed as part of the Type Name, but
 * will be pulled out during the parsing process.</p>
 *
 * <em>
 * UseSharedTypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . Identifier;
 * </em>
 */
public class ASTUseSharedTypeDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseSharedTypeDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseSharedTypeDeclaration(Location location, List<ASTNode> children) {
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

