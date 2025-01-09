package org.spruce.compiler.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifierList;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseSharedMultDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, a dot, then an identifier list
 * within braces, and a semicolon.</p>
 *
 * <em>
 * UseSharedMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . { IdentifierList } ;
 * </em>
 */
public final class ASTUseSharedMultDeclaration extends ASTParentNode implements ASTUseDeclaration {
    private final ASTTypeName myTypeName;
    private final ASTIdentifierList myIdentifierList;

    /**
     * Constructs an <code>ASTUseSharedMultDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code> and the given
     * <code>ASTIdentifierList</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     * @param identifierList An <code>ASTIdentifierList</code>.
     */
    public ASTUseSharedMultDeclaration(Location location, ASTTypeName typeName, ASTIdentifierList identifierList) {
        super(location);
        myTypeName = typeName;
        myIdentifierList = identifierList;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTIdentifierList</code>.
     * @return An <code>ASTIdentifierList</code>.
     */
    public ASTIdentifierList getIdentifierList() {
        return myIdentifierList;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypeName, myIdentifierList);
    }
}

