package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCompactRecordDeclaration</code> is an optional AnnotationList,
 * followed by an Identifier, optional Type Arguments, a RecordHeader, optional
 * Superinterfaces, then a ClassBody.</p>
 *
 * <em>
 * CompactRecordDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [TypeArguments] RecordHeader [Superinterfaces] ClassBody
 * </em>
 */
public final class ASTCompactRecordDeclaration extends ASTAnnotatedNode implements ASTVariant {
    private final ASTIdentifier myName;
    private final ASTTypeParameterList myTypeParams;
    private final ASTFormalParameterList myFormalParamList;
    private final ASTDataTypeNoArrayList mySuperinterfaces;
    private final ASTClassPartList myClassParts;

    /**
     * Constructs an <code>ASTCompactRecordDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the record name.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param typeParams A possibly null <code>ASTTypeParameterList</code>.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     * @param superinterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                        representing the list of superinterfaces.
     * @param classParts A possibly null <code>ASTClassPartList</code> representing the record body.
     */
    private ASTCompactRecordDeclaration(Location location, ASTAnnotationList annList,
                                 ASTIdentifier name, ASTTypeParameterList typeParams, ASTFormalParameterList formalParamList,
                                 ASTDataTypeNoArrayList superinterfaces, ASTClassPartList classParts) {
        super(location, annList);
        myName = name;
        myTypeParams = typeParams;
        myFormalParamList = formalParamList;
        mySuperinterfaces = superinterfaces;
        myClassParts = classParts;
    }

    /**
     * Because of the 8 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTCompactRecordDeclaration</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTIdentifier myName;
        private ASTTypeParameterList myTypeParams;
        private ASTFormalParameterList myFormalParamList;
        private ASTDataTypeNoArrayList mySuperinterfaces;
        private ASTClassPartList myClassParts;

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code> representing the record name.
         * @param name An <code>ASTIdentifier</code> representing the record name.
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
         * Sets the <code>ASTFormalParameterList</code>.
         * @param formalParamList An <code>ASTFormalParameterList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setFormalParamList(ASTFormalParameterList formalParamList) {
            this.myFormalParamList = formalParamList;
            return this;
        }

        /**
         * Sets the <code>ASTDataTypeNoArrayList</code>.
         * @param superinterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuperinterfaces(ASTDataTypeNoArrayList superinterfaces) {
            this.mySuperinterfaces = superinterfaces;
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
         * Builds and returns a new <code>ASTCompactRecordDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTCompactRecordDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTCompactRecordDeclaration</code>.
         */
        @Override
        public ASTCompactRecordDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myFormalParamList == null) {
                throw new IllegalStateException("No Record Header given!");
            }
            if (myClassParts == null) {
                throw new IllegalStateException("No Compact Record Body given!");
            }
            return new ASTCompactRecordDeclaration(myLocation, myAnnList, myName, myTypeParams,
                    myFormalParamList, mySuperinterfaces, myClassParts);
        }
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the record name.
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
     * Returns an <code>ASTFormalParameterList</code>.
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList getFormalParamList() {
        return myFormalParamList;
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code>, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getSuperinterfaces() {
        return Optional.ofNullable(mySuperinterfaces);
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
        List<Node> children = new ArrayList<>(6);
        children.add(myAnnList);
        children.add(myName);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        children.add(myFormalParamList);
        if (mySuperinterfaces != null) {
            children.add(mySuperinterfaces);
        }
        children.add(myClassParts);
        return children;
    }
}
