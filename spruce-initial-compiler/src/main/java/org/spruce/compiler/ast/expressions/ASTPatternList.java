package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
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
public class ASTPatternList extends ASTListNode<ASTPattern> {
    /**
     * Constructs an <code>ASTPatternList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTPattern</code>s.
     */
    public ASTPatternList(Location location, List<ASTPattern> children) {
        super(location, children, Type.PATTERNS);
    }
}
