package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.symbol.SymbolDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * <p>An <code>ASTFormalParameter</code> is an optional AnnotationList followed
 * by an optional "take", an optional variable modifier list, a data type,
 * and an identifier.</p>
 *
 * <em>
 * FormalParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[VariableModifierList] DataType Identifier
 * </em>
 */
public class ASTFormalParameter extends ASTParentNode implements SymbolDeclaration<VariableSymbol> {
    private final ASTDataType myDataType;
    private final ASTIdentifier myName;
    private VariableSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTFormalParameter</code> with arguments supplied by
     * the <code>Builder</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param name An <code>ASTIdentifier</code> representing the formal parameter's name.
     */
    private ASTFormalParameter(Location location, ASTDataType dataType, ASTIdentifier name) {
        super(location);
        myDataType = dataType;
        myName = name;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTFormalParameter</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTDataType myDataType;
        private ASTIdentifier myName;

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
         * Sets the <code>ASTDataType</code>.
         * @param dataType An <code>ASTDataType</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setDataType(ASTDataType dataType) {
            this.myDataType = dataType;
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
         * that the productions listed for {@link ASTFormalParameter}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTFormalParameter</code>.
         */
        public ASTFormalParameter build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myDataType == null) {
                throw new IllegalStateException("No Data Type given!");
            }
            if (myName == null) {
                throw new IllegalStateException("No Formal Parameter Name given!");
            }
            return new ASTFormalParameter(myLocation, myDataType, myName);
        }
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the formal parameter name.
     * @return An <code>ASTIdentifier</code> representing the formal parameter name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    @Override
    public void setDeclSymbol(VariableSymbol symbol) {
        myDeclSymbol = symbol;
    }

    @Override
    public VariableSymbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(6);
        children.add(myDataType);
        children.add(myName);
        return children;
    }
}
