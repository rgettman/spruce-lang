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
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeToInstantiate DimExprs<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeToInstantiate DimExprs Dims<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeToInstantiate Dims ArrayInitializer
 * </em>
 */
public class ASTArrayCreationExpression extends ASTParentNode {
    private final ASTTypeToInstantiate myTti;
    private final ASTDimExprs myDimExprs;
    private final ASTDims myDims;
    private final ASTArrayInitializer myArrayInitializer;

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code> and an <code>ASTListNode</code> representing
     * the DimExprs.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dimExprs An <code>ASTDimExprs</code>.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTDimExprs dimExprs) {
        super(location);
        myTti = tti;
        myDimExprs = dimExprs;
        myDims = null;
        myArrayInitializer = null;
    }

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code>, an <code>ASTListNode</code>
     * representing the DimExprs, and an <code>ASTListNode</code> representing Dims.
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
        myArrayInitializer = null;
    }

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code>, an <code>ListNode</code> representing Dims,
     * and an <code>ASTListNode</code> representing an array initializer.
     * @param location The <code>Location</code>.
     * @param dims An <code>ASTDims</code>.
     * @param arrayInitializer An <code>ASTArrayInitializer</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTDims dims,
                                      ASTArrayInitializer arrayInitializer) {
        super(location);
        myTti = tti;
        myDimExprs = null;
        myDims = dims;
        myArrayInitializer = arrayInitializer;
    }

    /**
     * Returns an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate getTypeToInstantiate() {
        return myTti;
    }

    /**
     * Returns an <code>ASTDimExprs</code>, if it exists.
     * @return An <code>Optional&lt;ASTDimExprs&gt;</code>.
     */
    public Optional<ASTDimExprs> getDimExprs() {
        return Optional.ofNullable(myDimExprs);
    }

    /**
     * Returns an <code>ASTDims</code>, if it exists.
     * @return An <code>Optional&lt;ASTDims&gt;</code>.
     */
    public Optional<ASTDims> getDims() {
        return Optional.ofNullable(myDims);
    }

    /**
     * Returns an <code>ASTArrayInitializer</code>, if it exists.
     * @return An <code>Optional&lt;ASTArrayInitializer&gt;</code>.
     */
    public Optional<ASTArrayInitializer> getArrayInitializer() {
        return Optional.ofNullable(myArrayInitializer);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myTti);
        if (myDimExprs != null) {
            children.add(myDimExprs);
        }
        if (myDims != null) {
            children.add(myDims);
        }
        if (myArrayInitializer != null) {
            children.add(myArrayInitializer);
        }
        return children;
    }
}
