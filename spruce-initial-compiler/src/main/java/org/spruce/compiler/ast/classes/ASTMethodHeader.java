package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTMethodHeader</code> is an optional TypeParameters followed by
 * a Result and a MethodDeclarator.</p>
 *
 * <em>
 * MethodHeader:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameters Result MethodDeclarator
 * </em>
 */
public class ASTMethodHeader extends ASTParentNode {
    /**
     * Constructs an <code>ASTMethodHeader</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTMethodHeader(Location location, List<ASTNode> children) {
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
