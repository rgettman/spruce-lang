package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;

import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTInterfaceDeclaration</code> is an optional AnnotationList,
 * followed by an optional AccessModifier, followed by an optional
 * InterfaceModifierList, then "interface", an Identifier, followed by optional
 * Type Parameters, optional ExtendsInterfaces, optional Permits, then an InterfaceBody.</p>
 *
 * <em>
 * InterfaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceModifierList] interface Identifier [TypeParameters] [ExtendsInterfaces] [Permits] InterfaceBody
 * </em>
 */
public final class ASTInterfaceDeclaration extends ASTAnnotatedNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessMod;
    private final ASTInterfaceModifierList myInterfaceModList;
    private final ASTIdentifier myName;
    private final ASTTypeParameterList myTypeParams;
    private final ASTDataTypeNoArrayList myExtendsInterfaces;
    private final ASTDataTypeNoArrayList myPermits;
    private final ASTInterfacePartList myInterfaceParts;

    /**
     * Constructs an <code>ASTInterfaceDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param interfaceModList A possibly empty <code>ASTInterfaceModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the interface name.
     * @param typeParams A possibly null <code>ASTTypeParameterList</code>.
     * @param extendsInterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing an ExtendsInterfaces.
     * @param permits A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing the list of permitted implementing classes.
     * @param interfaceParts A possibly empty <code>ASTInterfacePartList</code> representing the class body.
     */
    private ASTInterfaceDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod, ASTInterfaceModifierList interfaceModList,
                                    ASTIdentifier name, ASTTypeParameterList typeParams, ASTDataTypeNoArrayList extendsInterfaces,
                                    ASTDataTypeNoArrayList permits, ASTInterfacePartList interfaceParts) {
        super(location, annList);
        myAccessMod = accessMod;
        myInterfaceModList = interfaceModList;
        myName = name;
        myTypeParams = typeParams;
        myExtendsInterfaces = extendsInterfaces;
        myPermits = permits;
        myInterfaceParts = interfaceParts;
    }

    /**
     * Because of the 16 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTInterfaceDeclaration</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTKeywordNode myAccessMod;
        private ASTInterfaceModifierList myInterfaceModifierList;
        private ASTIdentifier myName;
        private ASTTypeParameterList myTypeParams;
        private ASTDataTypeNoArrayList myExtendsInterfaces;
        private ASTDataTypeNoArrayList myPermits;
        private ASTInterfacePartList myInterfaceParts;

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
         * Sets the <code>ASTInterfaceModifierList</code>.
         * @param interfaceModifierList An <code>ASTInterfaceModifierList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setInterfaceModifierList(ASTInterfaceModifierList interfaceModifierList) {
            this.myInterfaceModifierList = interfaceModifierList;
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
         * Sets the <code>ASTDataTypeNoArrayList</code> representing the ExtendsInterfaces.
         * @param extendsInterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExtendsInterfaces(ASTDataTypeNoArrayList extendsInterfaces) {
            this.myExtendsInterfaces = extendsInterfaces;
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
         * Sets the <code>ASTInterfacePartList</code>.
         * @param interfaceParts An <code>ASTInterfacePartList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setInterfaceParts(ASTInterfacePartList interfaceParts) {
            this.myInterfaceParts = interfaceParts;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTInterfaceDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTInterfaceDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTInterfaceDeclaration</code>.
         */
        @Override
        public ASTInterfaceDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myInterfaceParts == null) {
                throw new IllegalStateException("No Node Body given!");
            }
            if (myInterfaceModifierList == null) {
                throw new IllegalStateException("No Class Modifier List given (can be empty)!");
            }
            return new ASTInterfaceDeclaration(myLocation, myAnnList, myAccessMod, myInterfaceModifierList, myName,
                    myTypeParams, myExtendsInterfaces, myPermits, myInterfaceParts);
        }
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        for (ASTKeywordNode modifier : myInterfaceModList.getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
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
     * Returns an <code>ASTInterfaceModifierList</code>.
     * @return An <code>ASTInterfaceModifierList</code>.
     */
    public ASTInterfaceModifierList getInterfaceModifierList() {
        return myInterfaceModList;
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
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the ExtendsInterfaces, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getExtendsInterfaces() {
        return Optional.ofNullable(myExtendsInterfaces);
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the Permits, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getPermits() {
        return Optional.ofNullable(myPermits);
    }

    /**
     * Returns an <code>ASTInterfacePartList</code>.
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList getInterfaceParts() {
        return myInterfaceParts;
    }

    /**
     * Returns a <code>List</code> of <code>ASTMembers</code> consisting of all
     * class parts.
     * @return A <code>List</code> of <code>ASTMembers</code>.
     */
    @Override
    public List<ASTMember> getMembers() {
        return myInterfaceParts.getTypedChildren().stream()
                .map(part -> (ASTMember) part)
                .toList();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(8);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myInterfaceModList);
        children.add(myName);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        if (myExtendsInterfaces != null) {
            children.add(myExtendsInterfaces);
        }
        if (myPermits != null) {
            children.add(myPermits);
        }
        children.add(myInterfaceParts);
        return children;
    }
}
