package org.spruce.compiler.bootstrap.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTUseSharedAllDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * UseSharedAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . * ;
 * </em>
 */
public final class ASTUseSharedAllDeclaration extends ASTParentNode implements ASTUseDeclaration {
    private final ASTTypeName myTypename;

    /**
     * Constructs an <code>ASTUseSharedAllDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     */
    public ASTUseSharedAllDeclaration(Location location, ASTTypeName typeName) {
        super(location);
        myTypename = typeName;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypename() {
        return myTypename;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypename);
    }
}

