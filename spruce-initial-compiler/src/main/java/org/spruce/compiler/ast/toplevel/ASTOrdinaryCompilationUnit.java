package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTOrdinaryCompilationUnit</code> is an optional namespace declaration
 * followed by a (possibly empty) recognize declaration list and a (possibly
 * empty) type declaration list.</p>
 *
 * <em>
 * OrdinaryCompilationUnit:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PackageDeclaration RecognizeDeclarationList TypeDeclarationList<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeDeclarationList TypeDeclarationList
 * </em>
 */
public class ASTOrdinaryCompilationUnit extends ASTParentNode
{
    /**
     * Constructs an <code>ASTOrdinaryCompilationUnit</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTOrdinaryCompilationUnit(Location location, List<ASTNode> children)
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

