package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeMultDeclaration</code> is "recognize" followed
 * by a Package Name, a dot, then an identifier list within braces, and a semicolon.</p>
 *
 * <em>
 * RecognizeMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;recognize PackageOrTypeName . { IdentifierList } ;
 * </em>
 */
public class ASTRecognizeMultDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeMultDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeMultDeclaration(Location location, List<ASTNode> children)
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

