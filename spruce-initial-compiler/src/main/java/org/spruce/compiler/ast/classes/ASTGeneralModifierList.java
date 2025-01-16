package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTGeneralModifierList</code> is a list of general modifiers.</p>
 *
 * <em>
 * GeneralModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTGeneralModifierList extends ASTListNode<ASTKeywordNode> {

    /**
     * Constructs an <code>ASTGeneralModifierList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTGeneralModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.GENERAL_MODIFIERS);
    }
}
