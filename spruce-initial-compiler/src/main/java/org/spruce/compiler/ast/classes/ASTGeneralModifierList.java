package org.spruce.compiler.ast.classes;

import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

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
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTGeneralModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.GENERAL_MODIFIERS);
    }

    /**
     * Converts this general modifier list to a more specific modifier list,
     * giving an error if a found modifier is not in a more specific
     * list, or if there are duplicate modifiers.
     * @param errorMessage The error message expected.
     * @param expectedModifiers A List of expected modifiers (token types).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of <code>ASTKeywordNode</code>s
     *                     that constructs and returns the desired list node type.
     * @return A new <code>ASTListNode</code> of the given type.
     * @throws CompileException If there is a general modifier not in the more
     *     specific list, or if there are duplicate modifiers.
     */
    public <T extends ASTListNode<ASTKeywordNode>> T convertToSpecificList(String errorMessage,
           List<TokenType> expectedModifiers, BiFunction<Location, List<ASTKeywordNode>, T> nodeSupplier) {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTKeywordNode> children = getTypedChildren();
        for (ASTKeywordNode mod : children) {
            TokenType modifier = mod.getKeyword();
            if (!seen.add(modifier)) {
                throw new CompileException(mod.getLocation(), "Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (!expectedModifiers.contains(modifier)) {
                throw new CompileException(mod.getLocation(), errorMessage);
            }
        }
        return nodeSupplier.apply(getLocation(), children);
    }
}
