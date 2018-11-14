package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationTypeElementDeclaration</code> is a data type, then
 * an identifier, then an empty parentheses pair, optionally followed by a default
 * value, ending with a semicolon.</p>
 *
 * <em>
 * AnnotationTypeElementDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier ( ) ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier ( ) DefaultValue ;
 * </em>
 */
public class ASTAnnotationTypeElementDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAnnotationTypeElementDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTAnnotationTypeElementDeclaration(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
