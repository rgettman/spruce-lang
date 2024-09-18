package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValue</code> is a conditional expression, an element
 * value array initializer, or an annotation.</p>
 *
 * <em>
 * ElementValue:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValueArrayInitializer<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Annotation
 * </em>
 */
public class ASTElementValue extends ASTParentNode {
    /**
     * Constructs an <code>ASTElementValue</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTElementValue(Location location, List<ASTNode> children) {
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
