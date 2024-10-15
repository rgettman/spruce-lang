package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTTypeArguments;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.DOUBLE_COLON;

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
    private final ASTListNode myTypeName;
    private final ASTSuper mySooper;
    private final ASTListNode myExprName;
    private final ASTDataType myDataType;
    private final ASTPrimary myPrimary;
    private final ASTTypeArguments myTypeArgs;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTMethodReference</code> at the given <code>Location</code>
     * with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param typeName A possibly null <code>ASTListNode</code> representing a Type Name.
     * @param sooper A possibly null <code>ASTSuper</code>.
     * @param exprName A possibly null <code>ASTListNode</code> representing an Expression Name.
     * @param dataType A possibly null <code>ASTDataType</code>.
     * @param primary A possibly null <code>ASTPrimary</code>.
     * @param typeArgs A possibly null <code>ASTTypeArguments</code>.
     * @param identifier A possibly null <code>ASTIdentifier</code>.
     */
    private ASTMethodReference(Location location, ASTListNode typeName, ASTSuper sooper, ASTListNode exprName,
                               ASTDataType dataType, ASTPrimary primary, ASTTypeArguments typeArgs, ASTIdentifier identifier) {
        super(location, Arrays.asList(), DOUBLE_COLON);
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
        private ASTSuper mySooper;
        private ASTListNode myExprName;
        private ASTDataType myDataType;
        private ASTPrimary myPrimary;
        private ASTListNode myTypeName;
        private ASTTypeArguments myTypeArgs;
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
         * Sets the <code>ASTSuper</code>.
         * @param sooper An <code>ASTSuper</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuper(ASTSuper sooper) {
            mySooper = sooper;
            return this;
        }

        /**
         * Sets the <code>ASTListNode</code> representing an Expression Name.
         * @param exprName An <code>ASTListNode</code> representing an Expression Name.
         * @return This <code>Builder</code>.
         */
        public Builder setExprName(ASTListNode exprName) {
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
         * Sets the <code>ASTListNode</code> representing a Type Name.
         * @param typeName An <code>ASTListNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeName(ASTListNode typeName) {
            this.myTypeName = typeName;
            return this;
        }

        /**
         * Sets the <code>ASTTypeArguments</code>.
         * @param typeArgs An <code>ASTTypeArguments</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeArguments(ASTTypeArguments typeArgs) {
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
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTListNode</code> representing a Type Name, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getTypeName() {
        return Optional.ofNullable(myTypeName);
    }

    /**
     * Returns an <code>ASTSuper</code>, if it exists.
     * @return An <code>Optional&lt;ASTSuper&gt;</code>.
     */
    public Optional<ASTSuper> getSooper() {
        return Optional.ofNullable(mySooper);
    }

    /**
     * Returns an <code>ASTListNode</code> representing an Expression Name., if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getExprName() {
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
     * Returns an <code>ASTTypeArguments</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArguments&gt;</code>.
     */
    public Optional<ASTTypeArguments> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
    }

    /**
     * Returns an <code>ASTIdentifier</code>, if it exists.
     * @return An <code>Optional&lt;ASTIdentifier&gt;</code>.
     */
    public Optional<ASTIdentifier> getIdentifier() {
        return Optional.ofNullable(myIdentifier);
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    public void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
        if (myTypeName != null) {
            myTypeName.print(prefix + (isTail ? "    " : "|   "), false);
        }
        if (mySooper != null) {
            mySooper.print(prefix + (isTail ? "    " : "|   "), false);
        }
        if (myExprName != null) {
            myExprName.print(prefix + (isTail ? "    " : "|   "), false);
        }
        if (myPrimary != null) {
            myPrimary.print(prefix + (isTail ? "    " : "|   "), false);
        }
        if (myDataType != null) {
            myDataType.print(prefix + (isTail ? "    " : "|   "), myTypeArgs == null && myIdentifier == null);
        }
        if (myTypeArgs != null) {
            myTypeArgs.print(prefix + (isTail ? "    " : "|   "), myIdentifier == null);
        }
        if (myIdentifier != null) {
            myIdentifier.print(prefix + (isTail ? "    " : "|   "), true);
        }
    }
}
