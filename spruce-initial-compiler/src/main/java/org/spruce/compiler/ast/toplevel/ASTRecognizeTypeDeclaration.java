package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeTypeDeclaration</code> is "recognize" followed
 * by a Type Name, then a semicolon.</p>
 *
 * <em>
 * RecognizeTypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;recognize TypeName ;
 * </em>
 */
public class ASTRecognizeTypeDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeTypeDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeTypeDeclaration(Location location, List<ASTNode> children)
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

