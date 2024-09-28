package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCompactRecordDeclaration</code> is an Identifier, optional Type Arguments,
 * a RecordHeader, optional Superinterfaces, then a ClassBody.</p>
 *
 * <em>
 * CompactRecordDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier [TypeArguments] RecordHeader [Superinterfaces] ClassBody
 * </em>
 */
public class ASTCompactRecordDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTCompactRecordDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTCompactRecordDeclaration(Location location, List<ASTNode> children) {
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
