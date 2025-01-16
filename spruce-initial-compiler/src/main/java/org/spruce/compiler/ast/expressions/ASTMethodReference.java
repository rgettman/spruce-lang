package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTTypeArgumentList;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTMethodReference</code> is a reference to a method or constructor.
 * If [TypeArguments] Identifier is after "::", before the "::" can be an
 * Expression Name, a Primary, a DataType, "super", or TypeName "." super.  If
 * [TypeArguments] "new" is after "::", only a DataType is allowed before "::".</p>
 *
 * <em>
 * MethodReference:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] new<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super :: [TypeArguments] Identifier
 * </em>
 */
public class ASTMethodReference extends ASTParentNode {
    private final ASTTypeName myTypeName;
    private final ASTKeywordNode mySooper;
    private final ASTExpressionName myExprName;
    private final ASTDataType myDataType;
    private final ASTPrimary myPrimary;
    private final ASTTypeArgumentList myTypeArgs;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTMethodReference</code> at the given <code>Location</code>
     * with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param typeName A possibly null <code>ASTTypeName</code>.
     * @param sooper A possibly null <code>ASTKeywordNode</code> of keyword <code>super</code>.
     * @param exprName A possibly null <code>ASTExpressionName</code>.
     * @param dataType A possibly null <code>ASTDataType</code>.
     * @param primary A possibly null <code>ASTPrimary</code>.
     * @param typeArgs A possibly null <code>ASTTypeArgumentList</code>.
     * @param identifier A possibly null <code>ASTIdentifier</code>.
     */
    private ASTMethodReference(Location location, ASTTypeName typeName, ASTKeywordNode sooper, ASTExpressionName exprName,
                               ASTDataType dataType, ASTPrimary primary, ASTTypeArgumentList typeArgs, ASTIdentifier identifier) {
        super(location);
        myTypeName = typeName;
        mySooper = sooper;
        myExprName = exprName;
        myDataType = dataType;
        myPrimary = primary;
        myTypeArgs = typeArgs;
        myIdentifier = identifier;
    }

    /**
     * Because of the 12 possible cases (6 productions each with optional type
     * arguments), use this <code>Builder</code> to build an instance of
     * <code>ASTMethodReference</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTKeywordNode mySooper;
        private ASTExpressionName myExprName;
        private ASTDataType myDataType;
        private ASTPrimary myPrimary;
        private ASTTypeName myTypeName;
        private ASTTypeArgumentList myTypeArgs;
        private ASTIdentifier myIdentifier;

        /**
         * Sets the <code>Location</code>.
         * @param location An <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            myLocation = location;
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> of type <code>super</code>.
         * @param sooper An <code>ASTKeywordNode</code> of type <code>super</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuper(ASTKeywordNode sooper) {
            mySooper = sooper;
            return this;
        }

        /**
         * Sets the <code>ASTExpressionName</code>.
         * @param exprName An <code>ASTExpressionName</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExprName(ASTExpressionName exprName) {
            this.myExprName = exprName;
            return this;
        }

        /**
         * Sets the <code>ASTDataType</code>.
         * @param dataType An <code>ASTDataType</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setDataType(ASTDataType dataType) {
            this.myDataType = dataType;
            return this;
        }

        /**
         * Sets the <code>ASTPrimary</code>.
         * @param primary An <code>ASTPrimary</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setPrimary(ASTPrimary primary) {
            this.myPrimary = primary;
            return this;
        }

        /**
         * Sets the <code>ASTTypeName</code>.
         * @param typeName An <code>ASTTypeName</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeName(ASTTypeName typeName) {
            this.myTypeName = typeName;
            return this;
        }

        /**
         * Sets the <code>ASTTypeArgumentList</code>.
         * @param typeArgs An <code>ASTTypeArgumentList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeArguments(ASTTypeArgumentList typeArgs) {
            this.myTypeArgs = typeArgs;
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code>.
         * @param identifier An <code>ASTIdentifier</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setIdentifier(ASTIdentifier identifier) {
            this.myIdentifier = identifier;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTMethodReference</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.expressions.ASTMethodReference}
         * are created and no others, else an <code>IllegalStateExcpetion</code>
         * is thrown.
         * @return An <code>ASTMethodReference</code>.
         */
        public ASTMethodReference build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (mySooper != null) {
                // super :: [TypeArguments] Identifier
                // TypeName . super :: [TypeArguments] Identifier
                if (myDataType != null || myExprName != null || myPrimary != null) {
                    throw new IllegalStateException("If super is given, then data type, expression name, and primary can't be given.");
                }
                if (myIdentifier == null) {
                    throw new IllegalStateException("If super is given, then the identifier must be given.");
                }
                return build0();
            }
            // mySooper is null here.
            if (myExprName != null) {
                // ExpressionName :: [TypeArguments] Identifier
                if (myDataType != null || myTypeName != null || myPrimary != null) {
                    throw new IllegalStateException("If expression name is given, then data type, type name, and primary can't be given.");
                }
                if (myIdentifier == null) {
                    throw new IllegalStateException("If expression name is given, then the identifier must be given.");
                }
                return build0();
            }
            // myExprName is null here.
            if (myDataType != null) {
                // DataType :: [TypeArguments] Identifier
                // DataType :: [TypeArguments] new
                if (myTypeName != null || myPrimary != null) {
                    throw new IllegalStateException("If data type is given, then expression name, type name, and primary can't be given.");
                }
                return build0();
            }
            // myDataType is null here.
            if (myPrimary != null) {
                // Primary :: [TypeArguments] Identifier
                if (myTypeName != null) {
                    throw new IllegalStateException("If primary is given, then data type, type name, and expression name can't be given.");
                }
                if (myIdentifier == null) {
                    throw new IllegalStateException("If primary is given, then the identifier must be given.");
                }
                return build0();
            }
            throw new IllegalStateException("Bad method reference argument!");
        }

        private ASTMethodReference build0() {
            return new ASTMethodReference(myLocation, myTypeName, mySooper, myExprName, myDataType, myPrimary, myTypeArgs, myIdentifier);
        }
    }

    /**
     * Returns an <code>ASTTypeName</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeName&gt;</code>.
     */
    public Optional<ASTTypeName> getTypeName() {
        return Optional.ofNullable(myTypeName);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> of type <code>super</code>, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of type <code>super</code>.
     */
    public Optional<ASTKeywordNode> getSooper() {
        return Optional.ofNullable(mySooper);
    }

    /**
     * Returns an <code>ASTExpressionName</code>, if it exists.
     * @return An <code>Optional&lt;ASTExpressionName&gt;</code>.
     */
    public Optional<ASTExpressionName> getExprName() {
        return Optional.ofNullable(myExprName);
    }

    /**
     * Returns an <code>ASTDataType</code>, if it exists.
     * @return An <code>Optional&lt;ASTDataType&gt;</code>.
     */
    public Optional<ASTDataType> getDataType() {
        return Optional.ofNullable(myDataType);
    }

    /**
     * Returns an <code>ASTPrimary</code>, if it exists.
     * @return An <code>Optional&lt;ASTPrimary&gt;</code>.
     */
    public Optional<ASTPrimary> getPrimary() {
        return Optional.ofNullable(myPrimary);
    }

    /**
     * Returns an <code>ASTTypeArgumentList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentList&gt;</code>.
     */
    public Optional<ASTTypeArgumentList> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
    }

    /**
     * Returns an <code>ASTIdentifier</code>, if it exists.
     * @return An <code>Optional&lt;ASTIdentifier&gt;</code>.
     */
    public Optional<ASTIdentifier> getIdentifier() {
        return Optional.ofNullable(myIdentifier);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        if (myTypeName != null) {
            children.add(myTypeName);
        }
        if (mySooper != null) {
            children.add(mySooper);
        }
        if (myExprName != null) {
            children.add(myExprName);
        }
        if (myPrimary != null) {
            children.add(myPrimary);
        }
        if (myDataType != null) {
            children.add(myDataType);
        }
        if (myTypeArgs != null) {
            children.add(myTypeArgs);
        }
        if (myIdentifier != null) {
            children.add(myIdentifier);
        }
        return children;
    }
}
