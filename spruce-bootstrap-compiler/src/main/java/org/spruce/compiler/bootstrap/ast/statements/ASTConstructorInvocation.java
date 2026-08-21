package org.spruce.compiler.bootstrap.ast.statements;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTArgumentList;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.EntityResolution;
import org.spruce.compiler.bootstrap.symbol.EntitySymbol;

/**
 * <p>An <code>ASTConstructorInvocation</code> is "self" or "super" followed by
 * a pair of parentheses optionally containing an Argument List, followed by a
 * semicolon.</p>
 *
 * <em>
 * ConstructorInvocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;self ( ArgumentList ) ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super ( ArgumentList ) ;
 * </em>
 */
public final class ASTConstructorInvocation extends ASTParentNode implements ASTBlockStatement, EntityResolution {
    private final ASTKeywordNode myConstructorKeyword;
    private final ASTArgumentList myArgsList;
    private EntitySymbol myResolvedEntity;

    /**
     * Constructs an <code>ASTConstructorInvocation</code> at the given
     * <code>Location</code> with arguments supplied by the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param constructorKeyword An <code>ASTKeywordNode</code> with keyword <code>SELF</code>
     *                           or <code>SUPER</code>.
     * @param argsList An <code>ASTArgumentList</code>.
     */
    private ASTConstructorInvocation(Location location, ASTKeywordNode constructorKeyword,
                                     ASTArgumentList argsList) {
        super(location);
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
         * that the productions listed for {@link ASTConstructorInvocation}
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
            return new ASTConstructorInvocation(myLocation, myConstructorKeyword, myArgsList);
        }
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
    public ASTArgumentList getArgumentsList() {
        return myArgsList;
    }

    @Override
    public void setResolvedEntity(EntitySymbol symbol) {
        myResolvedEntity = symbol;
    }

    @Override
    public EntitySymbol getResolvedEntity() {
        return myResolvedEntity;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myConstructorKeyword);
        children.add(myArgsList);
        return children;
    }
}
