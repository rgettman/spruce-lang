package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>final</li>
 *     <li>override</li>
 *     <li>shared</li>
 * </ul>
 *
 * <em>
 * MethodModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTMethodModifierList extends ASTListNode<ASTKeywordNode>
{
    /**
     * Constructs an <code>ASTMethodModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTMethodModifierList(Location location, List<ASTKeywordNode> children)
    {
        super(location, children, Type.METHOD_MODIFIERS);
    }
}
