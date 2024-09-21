package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTRecordHeader</code> is an optional formal parameter list
 * within parentheses.</p>
 *
 * <em>
 * RecordHeader:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;( [FormalParameterList] )
 * </em>
 */
public class ASTRecordHeader extends ASTParentNode {
    /**
     * Constructs an <code>ASTRecordHeader</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecordHeader(Location location, List<ASTNode> children) {
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
