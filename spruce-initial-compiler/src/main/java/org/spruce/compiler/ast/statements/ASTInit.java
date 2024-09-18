package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInit</code> is a local variable declaration
 * or a statement expression list.</p>
 *
 * <em>
 * BlockStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpressionList
 * </em>
 */
public class ASTInit extends ASTParentNode {
    /**
     * Constructs an <code>ASTInit</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTInit(Location location, List<ASTNode> children) {
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
