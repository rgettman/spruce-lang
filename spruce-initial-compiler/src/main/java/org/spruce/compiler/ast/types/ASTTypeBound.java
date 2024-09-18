package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTTypeBound</code> is a "&lt;:" or ":&gt;" followed by an
 * IntersectionType.</p>
 *
 * <em>
 * TypeBound:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;: IntersectionType<br>
 * </em>
 */
public class ASTTypeBound extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypeBound</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeBound(Location location, List<ASTNode> children) {
        super(location, children, TokenType.QUESTION_MARK);
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
