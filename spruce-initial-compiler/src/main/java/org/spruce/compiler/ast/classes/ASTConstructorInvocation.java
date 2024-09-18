package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTConstructorInvocation</code> is a colon, with optional type
 * arguments optionally preceded by Expression Name "." or Primary ".", then
 * "constructor" or "super" followed by a pair parentheses optionally containing
 * an Argument List.</p>
 * <p>The Expression Name or Primary are only provided to supply an enclosing
 * class instance if the superclass is an inner class.</p>
 *
 * <em>
 * ConstructorInvocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] constructor ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] super ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: ExpressionName . [TypeArguments] super ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: Primary . [TypeArguments] super ( ArgumentList )
 * </em>
 */
public class ASTConstructorInvocation extends ASTParentNode {
    /**
     * Constructs an <code>ASTConstructorInvocation</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTConstructorInvocation(Location location, List<ASTNode> children) {
        super(location, children, TokenType.OPEN_PARENTHESIS);
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
