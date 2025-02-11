package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTRecordComponentList</code> is a comma-separated list of
 * record component instances.  Only the last record component may have an
 * ellipsis.</p>
 *
 * <em>
 * RecordComponentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordComponent {, RecordComponent}
 * </em>
 */
public final class ASTRecordComponentList extends ASTListNode<ASTRecordComponent> {
    /**
     * Constructs an <code>ASTRecordComponentList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTRecordComponent</code>s.
     */
    public ASTRecordComponentList(Location location, List<ASTRecordComponent> children) {
        super(location, children, Type.RECORD_COMPONENTS);
    }
}
