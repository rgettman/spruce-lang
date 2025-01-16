package org.spruce.compiler.ast;

import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.common.Location;

/**
 * An <code>ASTAnnotatedNode</code> is an <code>ASTParentNode</code> that has
 * an <code>ASTAnnotationList</code> as one of its children.
 */
public abstract class ASTAnnotatedNode extends ASTParentNode {
    protected final ASTAnnotationList myAnnList;

    /**
     * Constructs an <code>ASTAnnotatedNode</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>.
     * @param loc The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     */
    protected ASTAnnotatedNode(Location loc, ASTAnnotationList annList) {
        super(loc);
        myAnnList = annList;
    }

    /**
     * Because many subclasses of <code>ASTAnnotatedNode</code> use the Builder
     * Pattern to construct themselves, they should subclass this <code>Builder</code>
     * that supplies common attributes: a Location and an AnnotationList.
     * @param <B> The type of concrete Builder.
     */
    public static abstract class Builder<B extends Builder<B>> {
        protected Location myLocation;
        protected ASTAnnotationList myAnnList;

        /**
         * Returns the concrete Builder implementation for a fluent Builder.
         * @return The concrete Builder implementation.
         */
        protected abstract B getThis();

        /**
         * Sets the <code>Location</code>.
         * @param location A <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public B setLocation(Location location) {
            this.myLocation = location;
            return getThis();
        }

        /**
         * Sets the <code>ASTAnnotationList</code>.
         * @param annList An <code>ASTAnnotationList</code>.
         * @return This <code>Builder</code>.
         */
        public B setAnnList(ASTAnnotationList annList) {
            this.myAnnList = annList;
            return getThis();
        }

        /**
         * Builds and returns a new subclass instance of <code>ASTAnnotatedNode</code>.
         * Subclasses must enforce that the productions listed for that particular
         * subclass of <code>ASTAnnotatedNode</code> are created and no others, else an
         * <code>IllegalStateException</code> is thrown.
         * @return A subclass instance of <code>ASTAnnotatedNode</code>.
         */
        public abstract ASTAnnotatedNode build();
    }

    /**
     * Returns an <code>ASTAnnotationList</code>.
     * @return An <code>ASTAnnotationList</code>.
     */
    public ASTAnnotationList getAnnList() {
        return myAnnList;
    }
}
