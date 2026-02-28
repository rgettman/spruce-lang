package org.spruce.compiler.bootstrap.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTUseSharedTypeDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, a dot, an identifier, then a semicolon.</p>
 *
 * <p>The Identifier will initially be parsed as part of the Type Name, but
 * will be pulled out during the parsing process.</p>
 *
 * <em>
 * UseSharedTypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . Identifier;
 * </em>
 */
public final class ASTUseSharedTypeDeclaration extends ASTParentNode implements ASTUseDeclaration {
    private final ASTTypeName myTypeName;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTUseSharedTypeDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code>, and the given <code>ASTIdentifier</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code> of type <code>TYPENAME_IDS</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTUseSharedTypeDeclaration(Location location, ASTTypeName typeName, ASTIdentifier identifier) {
        super(location);
        myTypeName = typeName;
        myIdentifier = identifier;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypeName, myIdentifier);
    }
}

