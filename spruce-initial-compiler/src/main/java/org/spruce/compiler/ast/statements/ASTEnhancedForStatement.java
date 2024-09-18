package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnhancedForStatement</code> is "for (", a local variable
 * declaration, a colon, a Conditional Expression, ")", and a block.  The local
 * variable declaration must declare exactly one variable.</p>
 *
 * <em>
 * EnhancedForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration : ConditionalExpression ) Block<br>
 * </em>
 */
public class ASTEnhancedForStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTEnhancedForStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTEnhancedForStatement(Location location, List<ASTNode> children) {
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
