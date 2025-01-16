package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTElementValuePairList</code> is a comma-separated list of
 * element value pairs.</p>
 *
 * <em>
 * ElementValuePairList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValuePair {, ElementValuePair}
 * </em>
 */
public class ASTElementValuePairList extends ASTListNode<ASTElementValuePair> {
    /**
     * Constructs an <code>ASTElementValuePairList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTElementValuePair</code>s.
     */
    public ASTElementValuePairList(Location location, List<ASTElementValuePair> children) {
        super(location, children, Type.ELEMENT_VALUE_PAIRS);
    }
}
