package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTRecordComponent</code> is an optional AnnotationList followed
 * by an optional "take", a data type, possibly an ellipsis, and an identifier.</p>
 *
 * <em>
 * RecordComponent:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [take] DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [take] DataType ... Identifier<br>
 * </em>
 */
public final class ASTRecordComponent extends ASTAnnotatedNode implements ASTMember {
    private final ASTKeywordNode myTakeMod;
    private final ASTDataType myDataType;
    private final ASTKeywordNode myEllipsisMod;
    private final ASTIdentifier myName;

    /**
     * Constructs an <code>ASTRecordComponent</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param takeMod A possibly null <code>ASTKeywordNode</code> of type <code>TAKE</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param ellipsisMod A possibly null <code>ASTKeywordNode</code> of type <code>ELLIPSIS</code>.
     * @param name An <code>ASTIdentifier</code> representing the record component's name.
     */
    private ASTRecordComponent(Location location, ASTAnnotationList annList, ASTKeywordNode takeMod,
                               ASTDataType dataType, ASTKeywordNode ellipsisMod, ASTIdentifier name) {
        super(location, annList);
        myTakeMod = takeMod;
        myDataType = dataType;
        myEllipsisMod = ellipsisMod;
        myName = name;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTRecordComponent</code>.
     */
    public static class Builder extends ASTAnnotatedNode.Builder<Builder> {
        private ASTKeywordNode myTakeMod;
        private ASTDataType myDataType;
        private ASTKeywordNode myEllipsisMod;
        private ASTIdentifier myName;

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing the take keyword.
         * @param takeMod An <code>ASTKeywordNode</code> of keyword <code>TAKE</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setTakeMod(ASTKeywordNode takeMod) {
            this.myTakeMod = takeMod;
            return this;
        }

        /**
         * Sets the <code>ASTDataType</code>.
         * @param dataType An <code>ASTDataType</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setDataType(ASTDataType dataType) {
            this.myDataType = dataType;
            return this;
        }

        /**
         * Sets the <code>ASTKeywordNode</code> representing the ellipsis.
         * @param ellipsisMod An <code>ASTKeywordNode</code> of keyword <code>ELLIPSIS</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setEllipsisMod(ASTKeywordNode ellipsisMod) {
            this.myEllipsisMod = ellipsisMod;
            return this;
        }

        /**
         * Sets the <code>ASTIdentifier</code> representing the record component name.
         * @param name An <code>ASTIdentifier</code> representing the record component name.
         * @return This <code>Builder</code>.
         */
        public Builder setName(ASTIdentifier name) {
            this.myName = name;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTRecordComponent</code>.  Enforces
         * that the productions listed for {@link ASTRecordComponent}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTRecordComponent</code>.
         */
        public ASTRecordComponent build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myAnnList == null) {
                throw new IllegalStateException("No Annotation List given (can be empty)!");
            }
            if (myDataType == null) {
                throw new IllegalStateException("No Data Type given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Record Component Name given!");
            }
            return new ASTRecordComponent(myLocation, myAnnList, myTakeMod, myDataType, myEllipsisMod, myName);
        }
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the take modifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of keyword <code>TAKE</code>.
     */
    public Optional<ASTKeywordNode> getTakeMod() {
        return Optional.ofNullable(myTakeMod);
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the ellipsis modifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of keyword <code>ELLIPSIS</code>.
     */
    public Optional<ASTKeywordNode> getEllipsisMod() {
        return Optional.ofNullable(myEllipsisMod);
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the record component name.
     * @return An <code>ASTIdentifier</code> representing the record component name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * There are no modifiers on a record component.
     * @return An empty <code>List</code>.
     */
    @Override
    public List<TokenType> getModifiers() {
        return List.of();
    }

    /**
     * There is no access modifier on a record component.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.empty();
    }

    /**
     * Returns a <code>List</code> of exactly one <code>ASTIdentifier</code>
     * representing the record component name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myName);
    }

    /**
     * Returns no <code>ASTTypeParameterList</code>.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.empty();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(6);
        children.add(myAnnList);
        if (myTakeMod != null) {
            children.add(myTakeMod);
        }
        children.add(myDataType);
        if (myEllipsisMod != null) {
            children.add(myEllipsisMod);
        }
        children.add(myName);
        return children;
    }
}
