package org.spruce.compiler.ast.statements;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.MUT;

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

    /**
     * Converts this variable modifier list to a more specific keyword node
     * <code>mut</code>, if it exists.
     * @param errorMessage The error message expected.
     * @return A new <code>Optional&lt;ASTKeywordNode&gt;</code> of keyword <code>mut</code>.
     * @throws CompileException If there is a variable modifier that is not <code>mut</code>,
     *     or if there are duplicate modifiers.
     */
    public Optional<ASTKeywordNode> ensureMut(String errorMessage) {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTKeywordNode> children = getTypedChildren();
        ASTKeywordNode mutKeyword = null;
        for (ASTKeywordNode mod : children) {
            TokenType modifier = mod.getKeyword();
            if (!seen.add(modifier)) {
                throw new CompileException(mod.getLocation(), "Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (modifier != MUT) {
                throw new CompileException(mod.getLocation(), errorMessage);
            }
            mutKeyword = mod;
        }
        return Optional.ofNullable(mutKeyword);
    }
}
