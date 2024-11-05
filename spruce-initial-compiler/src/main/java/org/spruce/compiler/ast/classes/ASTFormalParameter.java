package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFormalParameter</code> is optionally "take", an optional
 * variable modifier list, a data type, possibly an ellipsis, and an identifier.</p>
 *
 * <em>
 * FormalParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[take] [VariableModifierList] DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[take] [VariableModifierList] DataType ... Identifier<br>
 * </em>
 */
public class ASTFormalParameter extends ASTParentNode {
    private final ASTKeywordNode myTakeMod;
    private final ASTVariableModifierList myVarModList;
    private final ASTDataType myDataType;
    private final ASTKeywordNode myEllipsisMod;
    private final ASTIdentifier myName;

    /**
     * Constructs an <code>ASTFormalParameter</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param takeMod A possibly null <code>ASTKeywordNode</code> of type <code>TAKE</code>.
     * @param varModList An <code>ASTVariableModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param ellipsisMod A possibly null <code>ASTKeywordNode</code> of type <code>ELLIPSIS</code>.
     * @param name An <code>ASTIdentifier</code> representing the formal parameter's name.
     */
    private ASTFormalParameter(Location location, ASTKeywordNode takeMod, ASTVariableModifierList varModList,
                              ASTDataType dataType, ASTKeywordNode ellipsisMod, ASTIdentifier name) {
        super(location);
        myTakeMod = takeMod;
        myVarModList = varModList;
        myDataType = dataType;
        myEllipsisMod = ellipsisMod;
        myName = name;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTFormalParameter</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTKeywordNode myTakeMod;
        private ASTVariableModifierList myVarModList;
        private ASTDataType myDataType;
        private ASTKeywordNode myEllipsisMod;
        private ASTIdentifier myName;

        /**
         * Sets the <code>Location</code>.
         * @param location The <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            this.myLocation = location;
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
         * Sets the <code>ASTVariableModifierList</code>.
         * @param varModList An <code>ASTVariableModifierList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setVarModList(ASTVariableModifierList varModList) {
            this.myVarModList = varModList;
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
         * Sets the <code>ASTIdentifier</code> representing the formal parameter name.
         * @param name An <code>ASTIdentifier</code> representing the formal parameter name.
         * @return This <code>Builder</code>.
         */
        public Builder setName(ASTIdentifier name) {
            this.myName = name;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTFormalParameter</code>.  Enforces
         * that the productions listed for {@link org.spruce.compiler.ast.classes.ASTFormalParameter}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTFormalParameter</code>.
         */
        public ASTFormalParameter build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myVarModList == null) {
                throw new IllegalStateException("No Variable Modifier List given!");
            }
            if (myDataType == null) {
                throw new IllegalStateException("No Data Type given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Formal Parameter Name given!");
            }
            return new ASTFormalParameter(myLocation, myTakeMod, myVarModList, myDataType, myEllipsisMod, myName);
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
     * Returns an <code>ASTVariableModifierList</code>.
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList getVarModList() {
        return myVarModList;
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
     * Returns an <code>ASTIdentifier</code> representing the formal parameter name.
     * @return An <code>ASTIdentifier</code> representing the formal parameter name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        if (myTakeMod != null) {
            children.add(myTakeMod);
        }
        children.add(myVarModList);
        children.add(myDataType);
        if (myEllipsisMod != null) {
            children.add(myEllipsisMod);
        }
        children.add(myName);
        return children;
    }
}
