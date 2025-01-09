package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataTypeNoArrayList</code> is a comma-separated list of
 * data types (no array).</p>
 *
 * <em>
 * DataTypeNoArrayList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray {, DataTypeNoArray}
 * </em>
 */
public class ASTDataTypeNoArrayList extends ASTListNode<ASTDataTypeNoArray> {
    /**
     * Constructs an <code>ASTDataTypeNoArrayList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTDataTypeNoArray</code>s.
     */
    public ASTDataTypeNoArrayList(Location location, List<ASTDataTypeNoArray> children) {
        super(location, children, Type.DATA_TYPES_NO_ARRAY);
    }
}
