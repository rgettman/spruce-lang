package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTRecordDeclaration</code> is an optional AnnotationList, followed
 * by an optional AccessModifier, followed by an optional ClassModifierList,
 * then "record", an Identifier, optional Type Arguments, then a RecordHeader,
 * then optional Superinterfaces, then a ClassBody.</p>
 *
 * <em>
 * RecordDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] record Identifier [TypeParameters] RecordHeader [Superinterfaces] ClassBody
 * </em>
 */
public final class ASTRecordDeclaration extends ASTAnnotatedNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessMod;
    private final ASTIdentifier myName;
    private final ASTTypeParameterList myTypeParams;
    private final ASTRecordComponentList myRecordCompList;
    private final ASTDataTypeNoArrayList mySuperinterfaces;
    private final ASTClassPartList myClassParts;

    /**
     * Constructs an <code>ASTRecordDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param name An <code>ASTIdentifier</code> representing the record name.
     * @param typeParams A possibly null <code>ASTTypeParameterList</code>.
     * @param recordCompList An <code>ASTRecordComponentList</code>.
     * @param superinterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing the list of superinterfaces.
     * @param classParts A possibly null <code>ASTClassPartList</code> representing the record body.
     */
    private ASTRecordDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                 ASTIdentifier name, ASTTypeParameterList typeParams, ASTRecordComponentList recordCompList,
                                 ASTDataTypeNoArrayList superinterfaces, ASTClassPartList classParts) {
        super(location, annList);
        myAccessMod = accessMod;
        myName = name;
        myTypeParams = typeParams;
        myRecordCompList = recordCompList;
        mySuperinterfaces = superinterfaces;
        myClassParts = classParts;
    }

    /**
     * Because of the 8 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTRecordDeclaration</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTKeywordNode myAccessMod;
        private ASTIdentifier myName;
        private ASTTypeParameterList myTypeParams;
        private ASTRecordComponentList myRecordCompList;
        private ASTDataTypeNoArrayList mySuperinterfaces;
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
         * Sets the <code>ASTRecordComponentList</code>.
         * @param recordCompList An <code>ASTRecordComponentList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setRecordCompList(ASTRecordComponentList recordCompList) {
            this.myRecordCompList = recordCompList;
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
         * Sets the <code>ASTClassPartList</code>.
         * @param classParts An <code>ASTClassPartList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setClassParts(ASTClassPartList classParts) {
            this.myClassParts = classParts;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTRecordDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTRecordDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTRecordDeclaration</code>.
         */
        @Override
        public ASTRecordDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myRecordCompList  == null) {
                throw new IllegalStateException("No Record Header given!");
            }
            if (myClassParts == null) {
                throw new IllegalStateException("No Record Body given!");
            }
            return new ASTRecordDeclaration(myLocation, myAnnList, myAccessMod, myName, myTypeParams,
                    myRecordCompList, mySuperinterfaces, myClassParts);
        }
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        // In case there's a need to support general modifiers on a record.
//        for (ASTKeywordNode modifier : myClassModifierList.getTypedChildren()) {
//            modifiers.add(modifier.getKeyword());
//        }
        return modifiers;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the record name.
     * @return An <code>ASTIdentifier</code>.
     */
    @Override
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns a <code>List</code> of <code>ASTIdentifier</code> containing
     * only one identifier - the name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myName);
    }

    /**
     * Returns an <code>ASTTypeParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    @Override
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
    }

    /**
     * Returns an <code>ASTRecordComponentList</code>.
     * @return An <code>ASTRecordComponentList</code>.
     */
    public ASTRecordComponentList getRecordCompList() {
        return myRecordCompList;
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the Superinterfaces, if it exists.
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

    /**
     * Returns a <code>List</code> of <code>ASTMembers</code> consisting of all
     * interface parts plus any record components.
     * @return A <code>List</code> of <code>ASTMembers</code>.
     */
    @Override
    public List<ASTMember> getMembers() {
        return Stream.concat(
                        myRecordCompList.getTypedChildren().stream(),
                        myClassParts.getTypedChildren().stream()
                )
                .map(part -> (ASTMember) part)
                .toList();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(7);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myName);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        children.add(myRecordCompList);
        if (mySuperinterfaces != null) {
            children.add(mySuperinterfaces);
        }
        children.add(myClassParts);
        return children;
    }
}
