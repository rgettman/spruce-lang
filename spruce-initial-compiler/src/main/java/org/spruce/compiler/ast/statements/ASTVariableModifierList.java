package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableModifierList</code> is a list of variable modifiers.</p>
 *
 * <em>
 * VariableModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifier {VariableModifier}
 * </em>
 */
public class ASTVariableModifierList extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s
     *                 representing variable modifiers.
     */
    public ASTVariableModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.VARIABLE_MODIFIERS);
    }
}
