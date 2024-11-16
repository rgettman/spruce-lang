package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassDeclaration</code> is an optional AnnotationList, followed
 * by an optional AccessModifier, followed by an optional ClassModifierList,
 * then "class", an Identifier, followed by optional Type Parameters, optional
 * Superclass, optional Superinterfaces, optional Permits, then a ClassBody.</p>
 *
 * <em>
 * ClassDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [ClassModifierList] class Identifier [TypeParameters] [Superclass] [Superinterfaces] [Permits] ClassBody
 * </em>
 */
public final class ASTClassDeclaration extends ASTAnnotatedNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessMod;
    private final ASTClassModifierList myClassModifierList;
    private final ASTIdentifier myName;
    private final ASTTypeParameterList myTypeParams;
    private final ASTDataTypeNoArray mySuperclass;
    private final ASTDataTypeNoArrayList mySuperinterfaces;
    private final ASTDataTypeNoArrayList myPermits;
    private final ASTClassPartList myClassParts;

    /**
     * Constructs an <code>ASTClassDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param classModifierList A possibly empty <code>ASTClassModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the class name.
     * @param typeParams A possibly null <code>ASTTypeParameterList</code>.
     * @param superclass A possibly null <code>ASTDataTypeNoArray</code> representing the superclass name.
     * @param superinterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing the list of superinterfaces.
     * @param permits A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing the list of permitted implementing classes.
     * @param classParts A possibly null <code>ASTClassPartList</code> representing the class body.
     */
    private ASTClassDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod, ASTClassModifierList classModifierList,
                                ASTIdentifier name, ASTTypeParameterList typeParams, ASTDataTypeNoArray superclass,
                                ASTDataTypeNoArrayList superinterfaces, ASTDataTypeNoArrayList permits, ASTClassPartList classParts) {
        super(location, annList);
        myAccessMod = accessMod;
        myClassModifierList = classModifierList;
        myName = name;
        myTypeParams = typeParams;
        mySuperclass = superclass;
        mySuperinterfaces = superinterfaces;
        myPermits = permits;
        myClassParts = classParts;
    }

    /**
     * Because of the 32 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTClassDeclaration</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTKeywordNode myAccessMod;
        private ASTClassModifierList myClassModifierList;
        private ASTIdentifier myName;
        private ASTTypeParameterList myTypeParams;
        private ASTDataTypeNoArray mySuperclass;
        private ASTDataTypeNoArrayList mySuperinterfaces;
        private ASTDataTypeNoArrayList myPermits;
        private ASTClassPartList myClassParts;

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing the Access Modifier.
         * @param accessMod An <code>ASTKeywordNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setAccessMod(ASTKeywordNode accessMod) {
            this.myAccessMod = accessMod;
            return this;
        }

        /**
         * Sets the <code>ASTClassModifierList</code> representing the Superclass.
         * @param classModifierList An <code>ASTClassModifierList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setClassModifierList(ASTClassModifierList classModifierList) {
            this.myClassModifierList = classModifierList;
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code> representing the class name.
         * @param name An <code>ASTIdentifier</code> representing the class name.
         * @return This <code>Builder</code>.
         */
        public Builder setName(ASTIdentifier name) {
            this.myName = name;
            return this;
        }

        /**
         * Sets the <code>ASTTypeParameterList</code>.
         * @param typeParams An <code>ASTTypeParameterList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeParams(ASTTypeParameterList typeParams) {
            this.myTypeParams = typeParams;
            return this;
        }

        /**
         * Sets the <code>ASTDataTypeNoArray</code> representing the Superclass.
         * @param superclass An <code>ASTDataTypeNoArray</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuperclass(ASTDataTypeNoArray superclass) {
            this.mySuperclass = superclass;
            return this;
        }

        /**
         * Sets the <code>ASTDataTypeNoArrayList</code> representing the Superinterfaces.
         * @param superinterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuperinterfaces(ASTDataTypeNoArrayList superinterfaces) {
            this.mySuperinterfaces = superinterfaces;
            return this;
        }

        /**
         * Sets the <code>ASTDataTypeNoArrayList</code> representing the Permits.
         * @param permits An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setPermits(ASTDataTypeNoArrayList permits) {
            this.myPermits = permits;
            return this;
        }

        /**
         * Sets the <code>ASTClassPartList</code>.
         * @param classParts An <code>ASTClassPartList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setClassParts(ASTClassPartList classParts) {
            this.myClassParts = classParts;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTClassDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTClassDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTClassDeclaration</code>.
         */
        @Override
        public ASTClassDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myClassParts == null) {
                throw new IllegalStateException("No Class Body given!");
            }
            if (myClassModifierList == null) {
                throw new IllegalStateException("No Class Modifier List given (can be empty)!");
            }
            return new ASTClassDeclaration(myLocation, myAnnList, myAccessMod, myClassModifierList, myName, myTypeParams,
                    mySuperclass, mySuperinterfaces, myPermits, myClassParts);
        }
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
    }

    /**
     * Returns an <code>ASTClassModifierList</code>.
     * @return An <code>ASTClassModifierList&</code>.
     */
    public ASTClassModifierList getClassModifierList() {
        return myClassModifierList;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the class name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTTypeParameterList</code>.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
    }

    /**
     * Returns an <code>ASTDataTypeNoArray</code> representing the Superclass, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArray&gt;</code>.
     */
    public Optional<ASTDataTypeNoArray> getSuperclass() {
        return Optional.ofNullable(mySuperclass);
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the Superinterfaces, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getSuperinterfaces() {
        return Optional.ofNullable(mySuperinterfaces);
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the Permits, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getPermits() {
        return Optional.ofNullable(myPermits);
    }

    /**
     * Returns an <code>ASTClassPartList</code>.
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList getClassParts() {
        return myClassParts;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(9);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myClassModifierList);
        children.add(myName);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        if (mySuperclass != null) {
            children.add(mySuperclass);
        }
        if (mySuperinterfaces != null) {
            children.add(mySuperinterfaces);
        }
        if (myPermits != null) {
            children.add(myPermits);
        }
        children.add(myClassParts);
        return children;
    }
}
