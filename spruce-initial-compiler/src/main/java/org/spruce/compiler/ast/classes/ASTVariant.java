package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariant</code> is a data type or a compact record declaration.</p>
 *
 * <em>
 * Variant:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CompactRecordDeclaration<br>
 * </em>
 */
public class ASTVariant extends ASTParentNode {
    /**
     * Constructs an <code>ASTVariant</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariant(Location location, List<ASTNode> children) {
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
