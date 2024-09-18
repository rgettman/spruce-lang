package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumBody</code> is a "{", optionally a list of enum constants,
 * followed by an optional enum body declaration, then a "}".</p>
 *
 * <em>
 * ClassBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ [EnumConstantList] [EnumBodyDeclarations] }
 * </em>
 */
public class ASTEnumBody extends ASTParentNode {
    /**
     * Constructs an <code>ASTEnumBody</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTEnumBody(Location location, List<ASTNode> children) {
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
