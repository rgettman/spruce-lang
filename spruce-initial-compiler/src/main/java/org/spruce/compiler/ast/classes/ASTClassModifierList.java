package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTClassModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>final</li>
 *     <li>shared</li>
 *     <li>sealed</li>
 * </ul>
 *
 * <em>
 * ClassModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTClassModifierList extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTClassModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTClassModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.CLASS_MODIFIERS);
    }
}
