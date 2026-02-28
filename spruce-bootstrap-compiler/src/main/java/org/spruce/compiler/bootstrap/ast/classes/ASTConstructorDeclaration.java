package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlock;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;

/**
 * <p>An <code>ASTConstructorDeclaration</code> is a ConstructorDeclarator,
 * followed by a Block.</p>
 *
 * <em>
 * ConstructorDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbspConstructorDeclarator Block
 * </em>
 */
public final class ASTConstructorDeclaration extends ASTParentNode implements ASTClassPart {
    private final ASTConstructorDeclarator myConstructorDecl;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTConstructorDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param constructorDecl An <code>ASTConstructorDeclarator</code>.
     * @param block An <code>ASTBlock</code>.
     */
    private ASTConstructorDeclaration(Location location, ASTConstructorDeclarator constructorDecl,
                                      ASTBlock block) {
        super(location);
        myConstructorDecl = constructorDecl;
        myBlock = block;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTConstructorDeclaration</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTConstructorDeclarator myConstructorDecl;
        private ASTBlock myBlock;

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
         * Sets the <code>ASTConstructorDeclarator</code>.
         * @param constructorDecl An <code>ASTConstructorDeclarator</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setConstructorDecl(ASTConstructorDeclarator constructorDecl) {
            this.myConstructorDecl = constructorDecl;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code>.
         * @param block An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setBlock(ASTBlock block) {
            this.myBlock = block;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTConstructorDeclaration</code>.  Enforces
         * that the productions listed for {@link ASTConstructorDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTConstructorDeclaration</code>.
         */
        public ASTConstructorDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myConstructorDecl == null) {
                throw new IllegalStateException("No Constructor Declarator given!");
            }
            if (myBlock == null) {
                throw new IllegalStateException("No Block given!");
            }
            return new ASTConstructorDeclaration(myLocation, myConstructorDecl, myBlock);
        }
    }

    /**
     * Returns an <code>ASTConstructorDeclarator</code>.
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator getConstructorDecl() {
        return myConstructorDecl;
    }

    /**
     * Returns an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getBlock() {
        return myBlock;
    }

    @Override
    public List<TokenType> getModifiers() {
        return Collections.emptyList();
    }

    /**
     * There are no names on a constructor declaration.
     * @return An empty <code>List</code>.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Collections.emptyList();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myConstructorDecl);
        children.add(myBlock);
        return children;
    }
}
