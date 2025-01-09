package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatchType</code> is a pipe-separated list of data types.</p>
 *
 * <em>
 * CatchType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType {| DataType}
 * </em>
 */
public class ASTCatchType extends ASTListNode<ASTDataType> {
    /**
     * Constructs an <code>ASTCatchType</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTDataType</code>s.
     */
    public ASTCatchType(Location location, List<ASTDataType> children) {
        super(location, children, Type.INTERSECTION_TYPES);
    }
}
