package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUnqualifiedClassInstanceCreationExpression</code> is "new"
 * followed by a type to instantiate, "(", an argument list, and ")".
 *
 * <em>
 * UnqualifiedClassInstanceCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] ) ClassBody
 * </em>
 */
public class ASTUnqualifiedClassInstanceCreationExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTUnqualifiedClassInstanceCreationExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTUnqualifiedClassInstanceCreationExpression(Location location, List<ASTNode> children) {
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
