package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTTypeToInstantiate</code> is a TypeName optionally followed by
 * type arguments or diamond.
 * <em>
 * TypeToInstantiate:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName TypeArgumentsOrDiamond
 * </em>
 */
public class ASTTypeToInstantiate extends ASTParentNode {
    private final ASTTypeName myTypeName;

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTTypeName typeName) {
        super(location);
        myTypeName = typeName;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        children.add(myTypeName);
        return children;
    }
}
