package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecordPattern</code> is a data type
 * followed by an optional PatternList enclosed in parentheses.</p>
 *
 * <em>
 * RecordPattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType ( [PatternList] )<br>
 * </em>
 */
public class ASTRecordPattern extends ASTParentNode {
    /**
     * Constructs an <code>ASTRecordPattern</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTRecordPattern(Location location, List<ASTNode> children) {
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
