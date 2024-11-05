package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAdtDeclaration</code> is an optional AccessModifier followed by
 * "adt", followed by an Identifier, followed by optional Type Parameters,
 * optional ExtendsInterfaces, then an AdtBody.</p>
 *
 * <em>
 * AdtDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] adt Identifier [TypeParameters] [ExtendsInterfaces] AdtBody
 * </em>
 */
public final class ASTAdtDeclaration extends ASTParentNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessModifier;
    private final ASTIdentifier myName;
    private final ASTTypeParameterList myTypeParams;
    private final ASTDataTypeNoArrayList myExtendsInterfaces;
    private final ASTAdtBody myAdtBody;

    /**
     * Constructs an <code>ASTAdtDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param accessModifier A possibly null <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param name An <code>ASTIdentifier</code> representing the ADT Name.
     * @param typeParams A possibly null <code>ASTTypeParameterList</code>.
     * @param extendsInterfaces A possibly null <code>ASTDataTypeNoArrayList</code>.
     * @param adtBody An <code>ASTAdtBody</code>.
     */
    private ASTAdtDeclaration(Location location, ASTKeywordNode accessModifier, ASTIdentifier name,
                              ASTTypeParameterList typeParams, ASTDataTypeNoArrayList extendsInterfaces, ASTAdtBody adtBody) {
        super(location);
        myAccessModifier = accessModifier;
        myName = name;
        myTypeParams = typeParams;
        myExtendsInterfaces = extendsInterfaces;
        myAdtBody = adtBody;
    }

    /**
     * Because of the 8 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTAdtDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTKeywordNode myAccessModifier;
        private ASTIdentifier myName;
        private ASTTypeParameterList myTypeParams;
        private ASTDataTypeNoArrayList myExtendsInterfaces;
        private ASTAdtBody myAdtBody;

        /**
         * Sets the <code>Location</code>.
         * @param location The <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            this.myLocation = location;
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing the Access Modifier.
         * @param accessModifier An <code>ASTKeywordNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setAccessModifier(ASTKeywordNode accessModifier) {
            this.myAccessModifier = accessModifier;
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code> representing the ADT Name.
         * @param name An <code>ASTIdentifier</code>.
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
         * Sets the <code>ASTDataTypeNoArrayList</code> representing the interfaces extended.
         * @param extendsInterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExtendsInterfaces(ASTDataTypeNoArrayList extendsInterfaces) {
            this.myExtendsInterfaces = extendsInterfaces;
            return this;
        }

        /**
         * Sets the <code>ASTAdtBody</code>.
         * @param adtBody An <code>ASTAdtBody</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setAdtBody(ASTAdtBody adtBody) {
            this.myAdtBody = adtBody;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTAdtDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTAdtDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTAdtDeclaration</code>.
         */
        public ASTAdtDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            return new ASTAdtDeclaration(myLocation, myAccessModifier, myName, myTypeParams, myExtendsInterfaces, myAdtBody);
        }
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing an Access Modifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getAccessModifier() {
        return Optional.ofNullable(myAccessModifier);
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the ADT Name.
     * @return An <code>ASTIdentifier</code> representing the ADT Name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTTypeParameterList</code> representing the TypeParameters, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
    }

    /**
     * Returns an <code>ASTDataTypeNoArrayList</code> representing interfaces extended, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getExtendsInterfaces() {
        return Optional.ofNullable(myExtendsInterfaces);
    }

    /**
     * Returns an <code>ASTAdtBody</code>.
     * @return An <code>ASTAdtBody</code>.
     */
    public ASTAdtBody getAdtBody() {
        return myAdtBody;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        if (myAccessModifier != null) {
            children.add(myAccessModifier);
        }
        children.add(myName);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        if (myExtendsInterfaces != null) {
            children.add(myExtendsInterfaces);
        }
        children.add(myAdtBody);
        return children;
    }
}

