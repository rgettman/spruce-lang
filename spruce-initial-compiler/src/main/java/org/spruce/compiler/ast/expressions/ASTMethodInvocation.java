package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTTypeArguments;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.OPEN_PARENTHESIS;

/**
 * <p>An <code>ASTMethodInvocation</code> is a primary with an argument list
 * within parentheses.</p>
 *
 * <em>
 * MethodInvocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName . [TypeArguments] Identifier ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary . [TypeArguments] Identifier ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super . [TypeArguments] Identifier ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . [TypeArguments] Identifier ( ArgumentList )
 * </em>
 */
public class ASTMethodInvocation extends ASTParentNode {
    private final ASTListNode myTypeName;
    private final ASTSuper mySooper;
    private final ASTListNode myExprName;
    private final ASTPrimary myPrimary;
    private final ASTTypeArguments myTypeArgs;
    private final ASTIdentifier myIdentifier;
    private final ASTListNode myArgsList;

    /**
     * Constructs an <code>ASTMethodInvocation</code> at the given <code>Location</code>
     * with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param typeName A possibly null <code>ASTListNode</code> representing a Type Name.
     * @param sooper A possibly null <code>ASTSuper</code>.
     * @param exprName A possibly null <code>ASTListNode</code> representing an Expression Name.
     * @param primary A possibly null <code>ASTPrimary</code>.
     * @param typeArgs A possibly null <code>ASTTypeName</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     * @param argList An <code>ASTListNode</code> representing an argument list.
     */
    private ASTMethodInvocation(Location location, ASTListNode typeName, ASTSuper sooper, ASTListNode exprName,
                                ASTPrimary primary, ASTTypeArguments typeArgs, ASTIdentifier identifier, ASTListNode argList) {
        super(location, Arrays.asList(), OPEN_PARENTHESIS);
        myTypeName = typeName;
        mySooper = sooper;
        myExprName = exprName;
        myPrimary = primary;
        myTypeArgs = typeArgs;
        myIdentifier = identifier;
        myArgsList = argList;
    }

    /**
     * Because of the 10 possible cases (6 productions each with optional type
     * arguments), use this <code>Builder</code> to build an instance of
     * <code>ASTMethodInvocation</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTListNode myTypeName;
        private ASTSuper mySooper;
        private ASTListNode myExprName;
        private ASTPrimary myPrimary;
        private ASTTypeArguments myTypeArgs;
        private ASTIdentifier myIdentifier;
        private ASTListNode myArgsList;

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
         * Sets the <code>ASTListNode</code> representing a Type Name.
         * @param typeName A <code>ASTListNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeName(ASTListNode typeName) {
            this.myTypeName = typeName;
            return this;
        }

        /**
         * Sets the <code>ASTSuper</code>.
         * @param sooper A <code>ASTSuper</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSooper(ASTSuper sooper) {
            this.mySooper = sooper;
            return this;
        }

        /**
         * Sets the Expression Name as a <code>ASTListNode</code>.
         * @param exprName A <code>ASTListNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExprName(ASTListNode exprName) {
            this.myExprName = exprName;
            return this;
        }

        /**
         * Sets the <code>ASTPrimary</code>.
         * @param primary A <code>ASTPrimary</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setPrimary(ASTPrimary primary) {
            this.myPrimary = primary;
            return this;
        }

        /**
         * Sets the <code>ASTTypeArguments</code>.
         * @param typeArgs A <code>ASTTypeArguments</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeArgs(ASTTypeArguments typeArgs) {
            this.myTypeArgs = typeArgs;
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code>.
         * @param identifier A <code>ASTIdentifier</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setIdentifier(ASTIdentifier identifier) {
            this.myIdentifier = identifier;
            return this;
        }

        /**
         * Sets the <code>ASTListNode</code>.
         * @param argsList A <code>ASTListNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setArgsList(ASTListNode argsList) {
            this.myArgsList = argsList;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTMethodInvocation</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.expressions.ASTMethodInvocation}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTMethodInvocation</code>.
         */
        public ASTMethodInvocation build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myIdentifier == null) {
                throw new IllegalStateException("No identifier for method name given!");
            }
            if (mySooper != null) {
                // super . [TypeArguments] Identifier ( ArgumentList )
                // TypeName . super . [TypeArguments] Identifier ( ArgumentList )
                if (myExprName != null || myPrimary != null) {
                    throw new IllegalStateException("If super is given then expression name and primary can't be given.");
                }
                return build0();
            }
            // mySooper is null here.
            if (myExprName != null) {
                // ExpressionName . [TypeArguments] Identifier ( ArgumentList )
                if (myPrimary != null) {
                    throw new IllegalStateException("If expression name is given then super and primary can't be given.");
                }
                return build0();
            }
            // myExprName is null here.
            // Primary . [TypeArguments] Identifier ( ArgumentList )
            // Identifier ( ArgumentList )
            return build0();
        }

        private ASTMethodInvocation build0() {
            return new ASTMethodInvocation(myLocation, myTypeName, mySooper, myExprName, myPrimary, myTypeArgs, myIdentifier, myArgsList);
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
     * Returns an <code>ASTListNode</code> representing the Type Name, if it exists.
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
     * Returns an <code>ASTListNode</code> representing an Expression Name, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getExprName() {
        return Optional.ofNullable(myExprName);
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
     * Returns an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }

    /**
     * Returns an <code>ASTListNode</code>, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code> with type
     * <code>ARGUMENTS</code>, representing an argument list.
     */
    public Optional<ASTListNode> getArgumentList() {
        return Optional.ofNullable(myArgsList);
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
        if (myTypeArgs != null) {
            myTypeArgs.print(prefix + (isTail ? "    " : "|   "), false);
        }
        myIdentifier.print(prefix + (isTail ? "    " : "|   "), myArgsList == null);
        if (myArgsList != null) {
            myArgsList.print(prefix + (isTail ? "    " : "│   "), true);
        }
    }
}
