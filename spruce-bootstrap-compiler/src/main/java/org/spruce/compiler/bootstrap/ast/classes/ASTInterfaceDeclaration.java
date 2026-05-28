package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTInterfaceDeclaration</code> is "interface", an Identifier,
 * followed by optional ExtendsInterfaces, then an InterfaceBody.</p>
 *
 * <em>
 * InterfaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;interface Identifier [ExtendsInterfaces] InterfaceBody
 * </em>
 */
public final class ASTInterfaceDeclaration extends ASTParentNode implements ASTTypeDeclaration {
    private final ASTIdentifier myName;
    private final ASTDataTypeNoArrayList myExtendsInterfaces;
    private final ASTInterfacePartList myInterfaceParts;
    private TypeSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTInterfaceDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the interface name.
     * @param extendsInterfaces A possibly null <code>ASTDataTypeNoArrayList</code>
     *                   representing an ExtendsInterfaces.
     * @param interfaceParts A possibly empty <code>ASTInterfacePartList</code> representing the class body.
     */
    private ASTInterfaceDeclaration(Location location, ASTIdentifier name,
                                    ASTDataTypeNoArrayList extendsInterfaces, ASTInterfacePartList interfaceParts) {
        super(location);
        myName = name;
        myExtendsInterfaces = extendsInterfaces;
        myInterfaceParts = interfaceParts;
    }

    /**
     * Because of the 16 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTInterfaceDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTIdentifier myName;
        private ASTDataTypeNoArrayList myExtendsInterfaces;
        private ASTInterfacePartList myInterfaceParts;

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
         * Sets the <code>ASTDataTypeNoArrayList</code> representing the ExtendsInterfaces.
         * @param extendsInterfaces An <code>ASTDataTypeNoArrayList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setExtendsInterfaces(ASTDataTypeNoArrayList extendsInterfaces) {
            this.myExtendsInterfaces = extendsInterfaces;
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
         * that the productions listed for {@link ASTInterfaceDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTInterfaceDeclaration</code>.
         */
        public ASTInterfaceDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Name given!");
            }
            if (myInterfaceParts == null) {
                throw new IllegalStateException("No Node Body given!");
            }
            return new ASTInterfaceDeclaration(myLocation, myName, myExtendsInterfaces, myInterfaceParts);
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
     * Returns an <code>ASTDataTypeNoArrayList</code> representing the ExtendsInterfaces, if it exists.
     * @return An <code>Optional&lt;ASTDataTypeNoArrayList&gt;</code>.
     */
    public Optional<ASTDataTypeNoArrayList> getExtendsInterfaces() {
        return Optional.ofNullable(myExtendsInterfaces);
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
        List<Node> children = new ArrayList<>(3);
        children.add(myName);
        if (myExtendsInterfaces != null) {
            children.add(myExtendsInterfaces);
        }
        children.add(myInterfaceParts);
        return children;
    }
}
