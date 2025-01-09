package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDims;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArrayCreationExpression</code> is a data type (no array),
 * possibly multiple dimension expressions, possibly multiple "[]", and
 * possibly an array initializer.</p>
 *
 * <em>
 * ArrayCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate DimExprs<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new TypeToInstantiate DimExprs Dims
 * </em>
 */
public class ASTArrayCreationExpression extends ASTParentNode {
    private final ASTTypeToInstantiate myTti;
    private final ASTDimExprs myDimExprs;
    private final ASTDims myDims;

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code> and an <code>ASTDimExprs</code>.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dimExprs An <code>ASTDimExprs</code>.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTDimExprs dimExprs) {
        super(location);
        myTti = tti;
        myDimExprs = dimExprs;
        myDims = null;
    }

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code>, an <code>ASTDimExprs</code>,
     * and an <code>ASTDims</code>.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dimExprs An <code>ASTDimExprs</code>.
     * @param dims An <code>ASTDims</code>.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTDimExprs dimExprs, ASTDims dims) {
        super(location);
        myTti = tti;
        myDimExprs = dimExprs;
        myDims = dims;
    }

    /**
     * Returns an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate getTypeToInstantiate() {
        return myTti;
    }

    /**
     * Returns an <code>ASTDimExprs</code>.
     * @return An <code>ASTDimExprs</code>.
     */
    public ASTDimExprs getDimExprs() {
        return myDimExprs;
    }

    /**
     * Returns an <code>ASTDims</code>, if it exists.
     * @return An <code>Optional&lt;ASTDims&gt;</code>.
     */
    public Optional<ASTDims> getDims() {
        return Optional.ofNullable(myDims);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myTti);
        children.add(myDimExprs);
        if (myDims != null) {
            children.add(myDims);
        }
        return children;
    }
}
