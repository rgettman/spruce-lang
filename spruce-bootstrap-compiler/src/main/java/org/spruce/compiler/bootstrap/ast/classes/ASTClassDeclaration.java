package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTClassDeclaration</code> is  "class", an Identifier,
 * followed by optional Type Parameters, optional Superclass, then a ClassBody.</p>
 *
 * <em>
 * ClassDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;class Identifier [TypeParameters] [Superclass] ClassBody
 * </em>
 */
public final class ASTClassDeclaration extends ASTParentNode implements ASTTypeDeclaration {
    private final ASTIdentifier myName;
    private final ASTDataTypeNoArray mySuperclass;
    private final ASTClassPartList myClassParts;
    private Symbol myDeclSymbol;

    /**
     * Constructs an <code>ASTClassDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the class name.
     * @param superclass A possibly null <code>ASTDataTypeNoArray</code> representing the superclass name.
     * @param classParts A possibly null <code>ASTClassPartList</code> representing the class body.
     */
    private ASTClassDeclaration(Location location, ASTIdentifier name, ASTDataTypeNoArray superclass,
                               ASTClassPartList classParts) {
        super(location);
        myName = name;
        mySuperclass = superclass;
        myClassParts = classParts;
    }

    /**
     * Because of the 32 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTClassDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTIdentifier myName;
        private ASTDataTypeNoArray mySuperclass;
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
            return new ASTClassDeclaration(myLocation, myName, mySuperclass, myClassParts);
        }
    }

    @Override
    public List<TokenType> getModifiers() {
        return Collections.emptyList();
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
     * Sets the declaration <code>Symbol</code>.
     * @param symbol The declaration <code>Symbol</code>.
     */
    @Override
    public void setDeclSymbol(Symbol symbol) {
        myDeclSymbol = symbol;
    }

    /**
     * Returns the declaration <code>Symbol</code>.
     * @return The declaration <code>Symbol</code>.
     */
    @Override
    public Symbol getDeclSymbol(){
        return myDeclSymbol;
    }

    /**
     * Returns an <code>ASTDataTypeNoArray</code> representing the Superclass, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArray&gt;</code>.
     */
    public Optional<ASTDataTypeNoArray> getSuperclass() {
        return Optional.ofNullable(mySuperclass);
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

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myName);
        if (mySuperclass != null) {
            children.add(mySuperclass);
        }
        children.add(myClassParts);
        return children;
    }
}
