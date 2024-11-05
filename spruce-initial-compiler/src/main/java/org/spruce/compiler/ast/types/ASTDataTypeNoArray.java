package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataTypeNoArray</code> is a simple or fully qualified
 * type.</p>
 *
 * <em>
 * DataTypeNoArray:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
 * </em>
 */
public final class ASTDataTypeNoArray extends ASTListNode<ASTSimpleType> implements ASTDataType {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTSimpleType</code>s.
     */
    public ASTDataTypeNoArray(Location location, List<ASTSimpleType> children) {
        super(location, children, Type.SIMPLE_TYPES);
    }
}
