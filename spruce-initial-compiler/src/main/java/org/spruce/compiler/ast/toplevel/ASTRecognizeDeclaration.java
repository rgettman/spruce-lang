package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeDeclaration</code> is a recognize type declaration,
 * a recognize mult declaration, a recognize all declaration, or the shared
 * version of any of those three.</p>
 *
 * <em>
 * RecognizeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeAllDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeSharedTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeSharedMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeSharedAllDeclaration
 * </em>
 */
public class ASTRecognizeDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeDeclaration(Location location, List<ASTNode> children)
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

