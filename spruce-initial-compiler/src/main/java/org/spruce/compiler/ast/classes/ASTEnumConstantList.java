package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTEnumConstantList</code> is a comma-separated list of enum constants.</p>
 *
 * <em>
 * EnumConstantList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumConstant {, EnumConstant}
 * </em>
 */
public class ASTEnumConstantList extends ASTListNode<ASTEnumConstant> {
    /**
     * Constructs an <code>ASTEnumConstantList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children A <code>List</code> of <code>ASTEnumConstant</code>s.
     */
    public ASTEnumConstantList(Location location, List<ASTEnumConstant> children) {
        super(location, children, Type.ENUM_CONSTANTS);
    }
}
