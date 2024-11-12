package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumDeclaration</code> is an optional AccessModifier followed by
 * an optional ClassModifierList, then "enum", an Identifier, followed by
 * optional Superinterfaces, then an EnumBody.</p>
 *
 * <em>
 * EnumDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [ClassModifierList] enum Identifier [Superinterfaces] EnumBody
 * </em>
 */
public final class ASTEnumDeclaration extends ASTParentNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessMod;
    private final ASTClassModifierList myClassModifierList;
    private final ASTIdentifier myName;
    private final ASTDataTypeNoArrayList mySuperinterfaces;
    private final ASTEnumBody myEnumBody;

    /**
     * Constructs an <code>ASTEnumDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param classModifierList A possibly empty <code>ASTClassModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the enum name.
     * @param superinterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing the list of superinterfaces.
     * @param enumBody An <code>ASTEnumBody</code>.
     */
    private ASTEnumDeclaration(Location location, ASTKeywordNode accessMod, ASTClassModifierList classModifierList,
                                ASTIdentifier name, ASTDataTypeNoArrayList superinterfaces, ASTEnumBody enumBody) {
        super(location);
        myAccessMod = accessMod;
        myClassModifierList = classModifierList;
        myName = name;
        mySuperinterfaces = superinterfaces;
        myEnumBody = enumBody;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTEnumDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTKeywordNode myAccessMod;
        private ASTClassModifierList myClassModifierList;
        private ASTIdentifier myName;
        private ASTDataTypeNoArrayList mySuperinterfaces;
        private ASTEnumBody myEnumBody;

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
         * Sets the <code>ASTKeywordNode</code> representing the Access Modifier.
         * @param accessMod An <code>ASTKeywordNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setAccessMod(ASTKeywordNode accessMod) {
            this.myAccessMod = accessMod;
            return this;
        }

        /**
         * Sets the <code>ASTClassModifierList</code>.
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
         * Sets the <code>ASTDataTypeNoArrayList</code>.
         * @param superinterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setSuperinterfaces(ASTDataTypeNoArrayList superinterfaces) {
            this.mySuperinterfaces = superinterfaces;
            return this;
        }

        /**
         * Sets the <code>ASTEnumBody</code>.
         * @param enumBody An <code>ASTEnumBody</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setEnumBody(ASTEnumBody enumBody) {
            this.myEnumBody = enumBody;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTEnumDeclaration</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTEnumDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         *
         * @return An <code>ASTEnumDeclaration</code>.
         */
        public ASTEnumDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myEnumBody == null) {
                throw new IllegalStateException("No Enum Body given!");
            }
            if (myClassModifierList == null) {
                throw new IllegalStateException("No Class Modifier List given (can be empty)!");
            }
            return new ASTEnumDeclaration(myLocation, myAccessMod, myClassModifierList, myName, mySuperinterfaces, myEnumBody);
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
     * Returns an <code>ASTDataTypeNoArrayList</code>, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getSuperinterfaces() {
        return Optional.ofNullable(mySuperinterfaces);
    }

    /**
     * Returns an <code>ASTEnumBody</code>.
     * @return An <code>ASTEnumBody</code>.
     */
    public ASTEnumBody getEnumBody() {
        return myEnumBody;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myClassModifierList);
        children.add(myName);
        if (mySuperinterfaces != null) {
            children.add(mySuperinterfaces);
        }
        children.add(myEnumBody);
        return children;
    }
}
