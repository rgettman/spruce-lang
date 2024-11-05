package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.types.ASTTypeArgumentsOrDiamond;
import org.spruce.compiler.scanner.Location;

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
    private final ASTTypeArgumentsOrDiamond myTaod;

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTTypeName typeName) {
        super(location);
        myTypeName = typeName;
        myTaod = null;
    }

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name and a
     * type-arguments-or-diamond.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     * @param taod An <code>ASTTypeArgumentsOrDiamond</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTTypeName typeName, ASTTypeArgumentsOrDiamond taod) {
        super(location);
        myTypeName = typeName;
        myTaod = taod;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTTypeArgumentsOrDiamond</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentsOrDiamond&gt;</code>.
     */
    public Optional<ASTTypeArgumentsOrDiamond> getTaod() {
        return Optional.ofNullable(myTaod);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myTypeName);
        if (myTaod != null) {
            children.add(myTaod);
        }
        return children;
    }
}
