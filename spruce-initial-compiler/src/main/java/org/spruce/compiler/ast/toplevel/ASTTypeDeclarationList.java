package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeDeclarationList</code> is multiple type declarations.</p>
 *
 * <em>
 * TypeDeclarationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration {TypeDeclaration}
 * </em>
 */
public class ASTTypeDeclarationList extends ASTListNode<ASTTypeDeclaration> {
    /**
     * Constructs an <code>ASTTypeDeclarationList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTTypeDeclaration</code>s.
     */
    public ASTTypeDeclarationList(Location location, List<ASTTypeDeclaration> children) {
        super(location, children, Type.TYPE_DECLARATIONS);
    }
}
