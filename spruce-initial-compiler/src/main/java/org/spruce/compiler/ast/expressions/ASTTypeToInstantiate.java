package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
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
    private final ASTListNode myTypeName;
    private final ASTTypeArgumentsOrDiamond myTaod;

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTListNode</code> of type <code>TYPENAME_IDS</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTListNode typeName) {
        super(location, Arrays.asList(typeName));
        myTypeName = typeName;
        myTaod = null;
    }

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name and a
     * type-arguments-or-diamond.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTListNode</code> of type <code>TYPENAME_IDS</code>.
     * @param taod An <code>ASTTypeArgumentsOrDiamond</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTListNode typeName, ASTTypeArgumentsOrDiamond taod) {
        super(location, Arrays.asList(typeName, taod));
        myTypeName = typeName;
        myTaod = taod;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTListNode</code> representing a type name.
     * @return An <code>ASTListNode</code> of type <code>TYPENAME_IDS</code>.
     */
    public ASTListNode getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTTypeArgumentsOrDiamond</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentsOrDiamond&gt;</code>.
     */
    public Optional<ASTTypeArgumentsOrDiamond> getTaod() {
        return Optional.ofNullable(myTaod);
    }
}
