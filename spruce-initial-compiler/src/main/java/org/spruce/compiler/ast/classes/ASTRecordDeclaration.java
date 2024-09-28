package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecordDeclaration</code> is an optional AccessModifier followed by
 * an optional ClassModifierList, then "record", an Identifier, optional Type Arguments,
 * optional Superinterfaces, then a ClassBody.</p>
 *
 * <em>
 * RecordDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [ClassModifierList] record Identifier [TypeArguments] [Superinterfaces] ClassBody
 * </em>
 */
public class ASTRecordDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTRecordDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecordDeclaration(Location location, List<ASTNode> children) {
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
