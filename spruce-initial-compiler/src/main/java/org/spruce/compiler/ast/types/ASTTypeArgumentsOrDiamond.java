package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTTypeArgumentsOrDiamond</code> is a TypeArguments or "&lt;&gt;".
 *
 * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
 * turn on the type context in the Scanner for the duration of this parsing.</p>
 *
 * <em>
 * TypeArgumentsOrDiamond:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeArguments<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; &gt;
 * </em>
 */
public class ASTTypeArgumentsOrDiamond extends ASTParentNode {
    private final ASTTypeArgumentList myTypeArgs;

    /**
     * Constructs an <code>ASTTypeArgumentsOrDiamond</code> at the given <code>Location</code>
     * with an <code>ASTTypeArgumentList</code>.
     * @param location The <code>Location</code>.
     * @param typeArgs An <code>ASTTypeArgumentList</code>.
     */
    public ASTTypeArgumentsOrDiamond(Location location, ASTTypeArgumentList typeArgs) {
        super(location);
        myTypeArgs = typeArgs;
    }

    /**
     * Constructs an <code>ASTTypeArgumentsOrDiamond</code> at the given <code>Location</code>
     * representing the "diamond".
     * @param location The <code>Location</code>.
     */
    public ASTTypeArgumentsOrDiamond(Location location) {
        super(location);
        myTypeArgs = null;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        if (myTypeArgs != null) {
            children.add(myTypeArgs);
        }
        return children;
    }

    /**
     * Returns an <code>ASTTypeArgumentList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentList&gt;</code>.
     */
    public Optional<ASTTypeArgumentList> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
    }
}
