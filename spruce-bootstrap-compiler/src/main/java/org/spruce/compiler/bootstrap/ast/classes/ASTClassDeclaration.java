package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTClassDeclaration</code> is an optional ClassModifierList,
 * "class", an Identifier, optional Superclass, optional Superinterfaces, then
 * a ClassBody.</p>
 *
 * <em>
 * ClassDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[ClassModifierList] class Identifier [Superclass] [Superinterfaces] ClassBody
 * </em>
 */
public final class ASTClassDeclaration extends ASTParentNode implements ASTTypeDeclaration {
    private final ASTClassModifierList myClassModifierList;
    private final ASTIdentifier myName;
    private final ASTDataTypeNoArray mySuperclass;
    private final ASTDataTypeNoArrayList mySuperinterfaces;
    private final ASTClassPartList myClassParts;
    private TypeSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTClassDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param classModifierList A possibly empty <code>ASTClassModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the class name.
     * @param superclass A possibly null <code>ASTDataTypeNoArray</code> representing the superclass name.
     * @param superinterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                        representing the list of superinterfaces.
     * @param classParts A possibly null <code>ASTClassPartList</code> representing the class body.
     */
    private ASTClassDeclaration(Location location, ASTClassModifierList classModifierList, ASTIdentifier name,
                                ASTDataTypeNoArray superclass, ASTDataTypeNoArrayList superinterfaces,
                                ASTClassPartList classParts) {
        super(location);
        myClassModifierList = classModifierList;
        myName = name;
        mySuperclass = superclass;
        mySuperinterfaces = superinterfaces;
        myClassParts = classParts;
    }

    /**
     * Because of the 32 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTClassDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTClassModifierList myClassModifierList;
        private ASTIdentifier myName;
        private ASTDataTypeNoArray mySuperclass;
        private ASTDataTypeNoArrayList mySuperinterfaces;
        private ASTClassPartList myClassParts;

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
         * that the productions listed for {@link ASTClassDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTClassDeclaration</code>.
         */
        public ASTClassDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myClassParts == null) {
                throw new IllegalStateException("No Class Body given!");
            }
            return new ASTClassDeclaration(myLocation, myClassModifierList, myName, mySuperclass,
                    mySuperinterfaces, myClassParts);
        }
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        for (ASTKeywordNode modifier : myClassModifierList.getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
    }

    /**
     * Returns an <code>ASTClassModifierList</code>.
     * @return An <code>ASTClassModifierList&</code>.
     */
    public ASTClassModifierList getClassModifierList() {
        return myClassModifierList;
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
     * Returns an <code>ASTClassPartList</code>.
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList getClassParts() {
        return myClassParts;
    }

    /**
     * Returns a <code>List</code> of <code>ASTMembers</code> consisting of all
     * class parts.
     * @return A <code>List</code> of <code>ASTMembers</code>.
     */
    @Override
    public List<ASTMember> getMembers() {
        return myClassParts.getTypedChildren().stream()
                .map(part -> (ASTMember) part)
                .toList();
    }

    /**
     * Sets the declaration <code>TypeSymbol</code>.
     * @param symbol The declaration <code>TypeSymbol</code>.
     */
    @Override
    public void setDeclSymbol(TypeSymbol symbol) {
        myDeclSymbol = symbol;
    }

    /**
     * Returns the declaration <code>TypeSymbol</code>.
     * @return The declaration <code>TypeSymbol</code>.
     */
    @Override
    public TypeSymbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myClassModifierList);
        children.add(myName);
        if (mySuperclass != null) {
            children.add(mySuperclass);
        }
        if (mySuperinterfaces != null) {
            children.add(mySuperinterfaces);
        }
        children.add(myClassParts);
        return children;
    }
}
