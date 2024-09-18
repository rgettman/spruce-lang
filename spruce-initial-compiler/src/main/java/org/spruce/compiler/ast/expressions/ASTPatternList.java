package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTPatternList</code> is a comma-separated list of
 * patterns.</p>
 *
 * <em>
 * PatternList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Pattern {, Pattern}
 * </em>
 */
public class ASTPatternList extends ASTParentNode {
    /**
     * Constructs an <code>ASTPatternList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTPatternList(Location location, List<ASTNode> children) {
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
