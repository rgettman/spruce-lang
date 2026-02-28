package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTFieldModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>constant</li>
 * </ul>
 *
 * <em>
 * FieldModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTFieldModifierList extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTFieldModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTFieldModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.FIELD_MODIFIERS);
    }
}
