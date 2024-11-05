package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValueList</code> is a comma-separated list of
 * element values.</p>
 *
 * <em>
 * ElementValueList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValue {, ElementValue}
 * </em>
 */
public final class ASTElementValueList extends ASTListNode<ASTElementValue> implements ASTElementValue {
    /**
     * Constructs an <code>ASTElementValueList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTElementValue</code>s.
     */
    public ASTElementValueList(Location location, List<ASTElementValue> children) {
        super(location, children, Type.ELEMENT_VALUES);
    }
}
