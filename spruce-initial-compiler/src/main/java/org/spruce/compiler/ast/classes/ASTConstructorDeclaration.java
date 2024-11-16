package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTConstructorDeclaration</code> is an optional AnnotationList,
 * followed by an optional AccessModifier, followed by a ConstructorDeclarator,
 * optionally followed by a Constructor Invocation, followed by a Block.</p>
 *
 * <em>
 * ConstructorDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] ConstructorDeclarator [ConstructorInvocation] Block
 * </em>
 */
public final class ASTConstructorDeclaration extends ASTAnnotatedNode implements ASTClassPart {
    private final ASTKeywordNode myAccessMod;
    private final ASTConstructorDeclarator myConstructorDecl;
    private final ASTConstructorInvocation myConstructorInvocation;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTConstructorDeclaration</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing
     *                  the Access Modifier.
     * @param constructorDecl An <code>ASTConstructorDeclarator</code>.
     * @param constructorInvocation A possibly null <code>ASTConstructorInvocation</code>.
     * @param block An <code>ASTBlock</code>.
     */
    private ASTConstructorDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                      ASTConstructorDeclarator constructorDecl,
                                      ASTConstructorInvocation constructorInvocation, ASTBlock block) {
        super(location, annList);
        myAccessMod = accessMod;
        myConstructorDecl = constructorDecl;
        myConstructorInvocation = constructorInvocation;
        myBlock = block;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTConstructorDeclaration</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTKeywordNode myAccessMod;
        private ASTConstructorDeclarator myConstructorDecl;
        private ASTConstructorInvocation myConstructorInvocation;
        private ASTBlock myBlock;

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing the AccessModifier.
         * @param accessMod An <code>ASTKeywordNode</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setAccessMod(ASTKeywordNode accessMod) {
            this.myAccessMod = accessMod;
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
         * Sets the <code>ASTConstructorInvocation</code>.
         * @param constructorInvocation An <code>ASTConstructorInvocation</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setConstructorInvocation(ASTConstructorInvocation constructorInvocation) {
            this.myConstructorInvocation = constructorInvocation;
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
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTConstructorDeclaration}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTConstructorDeclaration</code>.
         */
        @Override
        public ASTConstructorDeclaration build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myConstructorDecl == null) {
                throw new IllegalStateException("No Constructor Declarator given!");
            }
            if (myBlock == null) {
                throw new IllegalStateException("No Block given!");
            }
            return new ASTConstructorDeclaration(myLocation, myAnnList, myAccessMod, myConstructorDecl,
                    myConstructorInvocation, myBlock);
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
     * Returns an <code>ASTConstructorDeclarator</code>.
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator getConstructorDecl() {
        return myConstructorDecl;
    }

    /**
     * Returns an <code>ASTConstructorInvocation</code>, if it exists.
     * @return An <code>Optional&lt;ASTConstructorInvocation&gt;</code>.
     */
    public Optional<ASTConstructorInvocation> getConstructorInvocation() {
        return Optional.ofNullable(myConstructorInvocation);
    }

    /**
     * Returns an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getBlock() {
        return myBlock;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myConstructorDecl);
        if (myConstructorInvocation != null) {
            children.add(myConstructorInvocation);
        }
        children.add(myBlock);
        return children;
    }
}
