package org.spruce.compiler.ast.classes;

import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTGeneralModifierList</code> is a list of general modifiers.</p>
 *
 * <em>
 * MethodModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTGeneralModifierList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTGeneralModifierList(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is NOT collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }

    /**
     * Converts this general modifier list to a more specific modifier list,
     * giving an error is we have a modifier that is not in a more specific
     * list, or if there are duplicate modifiers.
     * @param errorMessage The error message expected.
     * @param expectedModifiers A List of expected modifiers (token types).
     * @param nodeSupplier A BiFunction that constructs a specific node type
     *     given a Location and a list of child nodes.
     * @param <T> The specific node type to create.
     * @return The newly created specific modifier list.
     * @throws CompileException If there is a general modifier not in the more
     *     specific list, or if there are duplicate modifiers.
     */
    public <T extends ASTParentNode> T convertToSpecificList(String errorMessage, List<TokenType> expectedModifiers,
                                                             BiFunction<Location, List<ASTNode>, T> nodeSupplier)
    {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTNode> children = getChildren();
        for (ASTNode child : children)
        {
            ASTGeneralModifier mod = (ASTGeneralModifier) child;
            TokenType modifier = mod.getOperation();
            if (!seen.add(modifier))
            {
                throw new CompileException("Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (!expectedModifiers.contains(modifier))
            {
                throw new CompileException(errorMessage);
            }
        }
        return nodeSupplier.apply(getLocation(), children);
    }
}
