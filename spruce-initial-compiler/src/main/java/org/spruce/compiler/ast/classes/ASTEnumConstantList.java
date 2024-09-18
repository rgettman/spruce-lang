package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumConstantList</code> is a comma-separated list of enum constants.
 *
 * <em>
 * EnumConstantList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumConstant {, EnumConstant}
 * </em>
 */
public class ASTEnumConstantList extends ASTParentNode {
    /**
     * Constructs an <code>ASTEnumConstantList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTEnumConstantList(Location location, List<ASTNode> children) {
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
