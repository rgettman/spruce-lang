package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTArgumentList;
import org.spruce.compiler.ast.types.ASTTypeArgumentList;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTConstructorInvocation</code> is a colon, with optional type
 * arguments optionally preceded by Expression Name "." or Primary ".", then
 * "constructor" or "super" followed by a pair parentheses optionally containing
 * an Argument List.</p>
 * <p>The Expression Name or Primary are only provided to supply an enclosing
 * class instance if the superclass is an inner class.</p>
 *
 * <em>
 * ConstructorInvocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] constructor ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] super ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: ExpressionName . [TypeArguments] super ( ArgumentList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;: Primary . [TypeArguments] super ( ArgumentList )
 * </em>
 */
public class ASTConstructorInvocation extends ASTParentNode {
    private final ASTTypeArgumentList myTypeArgs;
    private final ASTKeywordNode myConstructorKeyword;
    private final ASTArgumentList myArgsList;

    /**
     * Constructs an <code>ASTMethodInvocation</code> at the given <code>Location</code>
     * with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param typeArgs A possibly null <code>ASTTypeArgumentList</code>.
     * @param constructorKeyword An <code>ASTKeywordNode</code> with keyword <code>CONSTRUCTOR</code>
     *                           or <code>SUPER</code>.
     * @param argsList An <code>ASTArgumentList</code>.
     */
    private ASTConstructorInvocation(Location location, ASTTypeArgumentList typeArgs, ASTKeywordNode constructorKeyword,
                                     ASTArgumentList argsList) {
        super(location);
        myTypeArgs = typeArgs;
        myConstructorKeyword = constructorKeyword;
        myArgsList = argsList;
    }

    /**
     * Because of the 8 possible cases (4 productions each with optional type
     * arguments), use this <code>Builder</code> to build an instance of
     * <code>ASTConstructorInvocation</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTTypeArgumentList myTypeArgs;
        private ASTKeywordNode myConstructorKeyword;
        private ASTArgumentList myArgsList;

        /**
         * Sets the <code>Location</code>.
         * @param location An <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            this.myLocation = location;
            return this;
        }

        /**
         * Sets the <code>ASTTypeArgumentList</code>.
         * @param typeArgs An <code>ASTTypeArgumentList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTypeArgs(ASTTypeArgumentList typeArgs) {
            this.myTypeArgs = typeArgs;
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing "constructor" or "super".
         * @param constructorKeyword An <code>ASTKeywordNode</code> of keyword
         *                           <code>CONSTRUCTOR</code> or <code>SUPER</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setConstructorKeyword(ASTKeywordNode constructorKeyword) {
            this.myConstructorKeyword = constructorKeyword;
            return this;
        }

        /**
         * Sets the <code>ASTArgumentList</code>.
         * @param argsList An <code>ASTArgumentList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setArgsList(ASTArgumentList argsList) {
            this.myArgsList = argsList;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTConstructorInvocation</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTConstructorInvocation}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTConstructorInvocation</code>.
         */
        public ASTConstructorInvocation build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myConstructorKeyword == null) {
                throw new IllegalStateException("No constructor or super keyword given!");
            }
            if (myArgsList == null) {
                throw new IllegalStateException("No argument list given!");
            }
            return new ASTConstructorInvocation(myLocation, myTypeArgs, myConstructorKeyword, myArgsList);
        }
    }

    /**
     * Returns an <code>ASTTypeArgumentList</code> , if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentList&gt;</code>.
     */
    public Optional<ASTTypeArgumentList> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the type of
     * constructor invocation.
     * @return An <code>ASTKeywordNode</code> of keyword <code>SUPER</code> or
     *     <code>CONSTRUCTOR</code>.
     */
    public ASTKeywordNode getConstructorKeyword() {
        return myConstructorKeyword;
    }

    /**
     * Returns an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList getArgsList() {
        return myArgsList;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        if (myTypeArgs != null) {
            children.add(myTypeArgs);
        }
        children.add(myConstructorKeyword);
        children.add(myArgsList);
        return children;
    }
}
