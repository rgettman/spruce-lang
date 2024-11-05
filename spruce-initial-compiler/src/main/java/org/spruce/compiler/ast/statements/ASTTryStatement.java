package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTryStatement</code> is "try", optionally followed by a
 * Resource Specification, followed by a block, optionally followed
 * by a Catches, optionally followed by a Finally.  At least one of a Resource
 * Specification, Catches, and Finally are required.</p>
 *
 * <em>
 * TryStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try Block Catches<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try Block [Catches] Finally<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try ResourceSpecification Block [Catches] [Finally]
 * </em>
 */
public final class ASTTryStatement extends ASTParentNode implements ASTStatement {
    private final ASTResourceList myResourceSpec;
    private final ASTBlock myBlock;
    private final ASTCatches myCatches;
    private final ASTBlock myFinallyBlock;

    /**
     * Constructs an <code>ASTTryStatement</code> with argument supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param resourceSpec A possibly null <code>ASTResourceList</code>.
     * @param block An <code>ASTBlock</code>.
     * @param catches A possibly null <code>ASTCatches</code>.
     * @param finallyBlock A possibly null <code>ASTBlock</code>.
     */
    private ASTTryStatement(Location location, ASTResourceList resourceSpec, ASTBlock block,
                            ASTCatches catches, ASTBlock finallyBlock) {
        super(location);
        myResourceSpec = resourceSpec;
        myBlock = block;
        myCatches = catches;
        myFinallyBlock = finallyBlock;
    }

    /**
     * Because of the 7 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTTryStatement</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTResourceList myResourceSpec;
        private ASTBlock myBlock;
        private ASTCatches myCatches;
        private ASTBlock myFinallyBlock;

        /**
         * Sets the <code>Location</code>.
         * @param location A <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            this.myLocation = location;
            return this;
        }

        /**
         * Sets the <code>ASTResourceList</code>.
         * param resourceSpec An <code>ASTResourceList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setResourceSpec(ASTResourceList resourceSpec) {
            this.myResourceSpec = resourceSpec;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code>.
         * @param block An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setBlock(ASTBlock block) {
            this.myBlock = block;
            return this;
        }

        /**
         * Sets the <code>ASTCatches</code>.
         * @param catches An <code>ASTCatches</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setCatches(ASTCatches catches) {
            this.myCatches = catches;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code> representing a Finally Block.
         * @param finallyBlock An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setFinallyBlock(ASTBlock finallyBlock) {
            this.myFinallyBlock = finallyBlock;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTTryStatement</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.statements.ASTTryStatement}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTTryStatement</code>.
         */
        public ASTTryStatement build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myBlock == null) {
                throw new IllegalStateException("No Block given!");
            }
            if (myResourceSpec == null && myCatches == null && myFinallyBlock == null) {
                throw new IllegalStateException("Must supply at least one of a Resource Specification, Catches, and/or Finally!");
            }
            return new ASTTryStatement(myLocation, myResourceSpec, myBlock, myCatches, myFinallyBlock);
        }
    }

    /**
     * Returns an <code>ASTResourceList</code>, if it exists.
     * @return An <code>Optional&lt;ASTResourceList&gt;</code>.
     */
    public Optional<ASTResourceList> getResourceSpec() {
        return Optional.ofNullable(myResourceSpec);
    }

    /**
     * Returns an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getBlock() {
        return myBlock;
    }

    /**
     * Returns an <code>ASTCatches</code>, if it exists.
     * @return An <code>Optional&lt;ASTCatches&gt;</code>.
     */
    public Optional<ASTCatches> getCatches() {
        return Optional.ofNullable(myCatches);
    }

    /**
     * Returns an <code>ASTBlock</code> representing a Finally Block, if it exists.
     * @return An <code>Optional&lt;ASTBlock&gt;</code>.
     */
    public Optional<ASTBlock> getFinallyBlock() {
        return Optional.ofNullable(myFinallyBlock);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(4);
        if (myResourceSpec != null) {
            children.add(myResourceSpec);
        }
        children.add(myBlock);
        if (myCatches != null) {
            children.add(myCatches);
        }
        if (myFinallyBlock != null) {
            children.add(myFinallyBlock);
        }
        return children;
    }
}
