package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.ast.statements.ASTStatementExpression;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.EntityResolution;
import org.spruce.compiler.bootstrap.symbol.EntitySymbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
public final class ASTMethodInvocation extends ASTParentNode
        implements ASTStatementExpression, ASTPrimaryChild, EntityResolution {
    private final ASTTypeName myTypeName;
    private final ASTKeywordNode mySooper;
    private final ASTExpressionName myExprName;
    private final ASTPrimary myPrimary;
    //private final ASTTypeArgumentList myTypeArgs;
    private final ASTIdentifier myIdentifier;
    private final ASTArgumentList myArgsList;
    private EntitySymbol myResolvedEntity;
    private boolean isSharedContextOnly;
    private boolean isNonsharedContextOnly;

    /**
     * Constructs an <code>ASTMethodInvocation</code> at the given <code>Location</code>
     * with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param typeName A possibly null <code>ASTTypeName</code>.
     * @param sooper A possibly null <code>ASTKeywordNode</code> of type <code>super</code>.
     * @param exprName A possibly null <code>ASTExpressionName</code>.
     * @param primary A possibly null <code>ASTPrimary</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     * @param argList An <code>ASTArgumentList</code>.
     */
    private ASTMethodInvocation(Location location, ASTTypeName typeName, ASTKeywordNode sooper, ASTExpressionName exprName,
                                ASTPrimary primary, ASTIdentifier identifier, ASTArgumentList argList) {
        super(location);
        myTypeName = typeName;
        mySooper = sooper;
        myExprName = exprName;
        myPrimary = primary;
        myIdentifier = identifier;
        myArgsList = argList;

        isSharedContextOnly = false;
        isNonsharedContextOnly = false;
    }

    /**
     * Because of the 10 possible cases (6 productions each with optional type
     * arguments), use this <code>Builder</code> to build an instance of
     * <code>ASTMethodInvocation</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTTypeName myTypeName;
        private ASTKeywordNode mySooper;
        private ASTExpressionName myExprName;
        private ASTPrimary myPrimary;
        private ASTIdentifier myIdentifier;
        private ASTArgumentList myArgsList;

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
         * Sets the <code>ASTTypeName</code>.
         * @param typeName A <code>ASTTypeName</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeName(ASTTypeName typeName) {
            this.myTypeName = typeName;
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> of type <code>super</code>.
         * @param sooper A <code>ASTKeywordNode</code> of type <code>super</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSooper(ASTKeywordNode sooper) {
            this.mySooper = sooper;
            return this;
        }

        /**
         * Sets the <code>ASTExpressionName</code>.
         * @param exprName A <code>ASTExpressionName</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExprName(ASTExpressionName exprName) {
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
         * Sets the <code>ASTIdentifier</code>.
         * @param identifier A <code>ASTIdentifier</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setIdentifier(ASTIdentifier identifier) {
            this.myIdentifier = identifier;
            return this;
        }

        /**
         * Sets the <code>ASTArgumentList</code>.
         * @param argsList A <code>ASTArgumentList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setArgsList(ASTArgumentList argsList) {
            this.myArgsList = argsList;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTMethodInvocation</code>.  Enforces
         * that the productions listed for {@link ASTMethodInvocation}
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
            return new ASTMethodInvocation(myLocation, myTypeName, mySooper, myExprName, myPrimary, myIdentifier, myArgsList);
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
     * Returns an <code>ASTKeywordNode</code> of type <code>super</code>., if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of keyword <code>super</code>..
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
     * Returns an <code>ASTPrimary</code>, if it exists.
     * @return An <code>Optional&lt;ASTPrimary&gt;</code>.
     */
    public Optional<ASTPrimary> getPrimary() {
        return Optional.ofNullable(myPrimary);
    }

    /**
     * Returns an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }

    /**
     * Returns an <code>ASTArgumentList</code>, if it exists.
     * @return An <code>Optional&lt;ASTArgumentList&gt;</code>.
     */
    public Optional<ASTArgumentList> getArgumentList() {
        return Optional.ofNullable(myArgsList);
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedEntity.setDataType(symbol);
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedEntity != null ? myResolvedEntity.getDataType() : null;
    }

    @Override
    public void setResolvedEntity(EntitySymbol symbol) {
        myResolvedEntity = symbol;
    }

    @Override
    public EntitySymbol getResolvedEntity() {
        return myResolvedEntity;
    }

    /**
     * Sets whether this method invocation must match only shared methods
     * during resolution.  Must not have sharedContextOnly and
     * nonSharedContextOnly both be true at the same time!
     * @param isSharedContextOnly Whether this must match only shared methods
     *     during resolution.
     */
    public void setSharedContextOnly(boolean isSharedContextOnly) {
        this.isSharedContextOnly = isSharedContextOnly;
    }

    /**
     * Returns whether this method invocation must match only shared methods
     * during resolution.
     * @return Whether this method invocation must match only shared methods
     *     during resolution.
     */
    public boolean isSharedContextOnly() {
        return isSharedContextOnly;
    }

    /**
     * Sets whether this method invocation must match only non-shared methods
     * during resolution.  Must not have sharedContextOnly and
     * nonSharedContextOnly both be true at the same time!
     * @param isNonsharedContextOnly Whether this must match only non-shared methods
     *     during resolution.
     */
    public void setNonsharedContextOnly(boolean isNonsharedContextOnly) {
        this.isNonsharedContextOnly = isNonsharedContextOnly;
    }

    /**
     * Returns whether this method invocation must match only non-shared methods
     * during resolution.
     * @return Whether this method invocation must match only non-shared methods
     *     during resolution.
     */
    public boolean isNonsharedContextOnly() {
        return isNonsharedContextOnly;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(4);
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
        if (myIdentifier != null) {
            children.add(myIdentifier);
        }
        if (myArgsList != null) {
            children.add(myArgsList);
        }
        return children;
    }
}
