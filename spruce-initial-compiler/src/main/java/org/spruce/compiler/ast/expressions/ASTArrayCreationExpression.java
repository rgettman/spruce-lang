package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.ast.types.ASTDims;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.NEW;

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
    private final ASTListNode myDimExprs;
    private final ASTDims myDims;
    private final ASTUnaryNode myArrayInitializer;

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code> and an <code>ASTDimExprs</code>.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dimExprs An <code>ASTListNode</code> representing dim expressions.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTListNode dimExprs) {
        super(location, Arrays.asList(tti, dimExprs), NEW);
        myTti = tti;
        myDimExprs = dimExprs;
        myDims = null;
        myArrayInitializer = null;
    }

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTTypeToInstantiate</code>, an <code>ASTDimExprs</code>, and an <code>ASTDims</code>.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dimExprs An <code>ASTListNode</code> representing dim expressions.
     * @param dims An <code>ASTDims</code>.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTListNode dimExprs, ASTDims dims) {
        super(location, Arrays.asList(tti, dimExprs, dims), NEW);
        myTti = tti;
        myDimExprs = dimExprs;
        myDims = dims;
        myArrayInitializer = null;
    }

    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * with an <code>ASTDataTypeNoArray</code>, an <code>ASTDims</code>, and an <code>ASTUnaryNode</code>
     * representing an array initializer.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param dims An <code>ASTDims</code>.
     * @param arrayInitializer An <code>ASTUnaryNode</code> representing an array initializer.
     */
    public ASTArrayCreationExpression(Location location, ASTTypeToInstantiate tti, ASTDims dims, ASTUnaryNode arrayInitializer) {
        super(location, Arrays.asList(tti, dims, arrayInitializer), NEW);
        myTti = tti;
        myDimExprs = null;
        myDims = dims;
        myArrayInitializer = arrayInitializer;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate getTypeToInstantiate() {
        return myTti;
    }

    /**
     * Returns an <code>ASTListNode</code> representing dim expressions,
     * if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getDimExprs() {
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
     * Returns an <code>ASTUnaryNode</code> representing an Array Initializer,
     * if it exists.
     * @return An <code>Optional&lt;ASTUnaryNode&gt;</code>.
     */
    public Optional<ASTUnaryNode> getArrayInitializer() {
        return Optional.ofNullable(myArrayInitializer);
    }
}
