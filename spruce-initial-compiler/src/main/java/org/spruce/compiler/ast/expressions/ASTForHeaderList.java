package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTForHeaderList</code> is a list of for expression headers.</p>
 *
 * <em>
 * ForHeaderList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForHeader {ForHeader}<br>
 * </em>
 */
public class ASTForHeaderList extends ASTListNode<ASTForHeader> {
    /**
     * Constructs an <code>ASTForHeaderList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTForHeader</code>s.
     */
    public ASTForHeaderList(Location location, List<ASTForHeader> children) {
        super(location, children, Type.FOR_EXPR_HEADERS);
    }
}
