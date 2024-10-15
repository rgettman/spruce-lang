package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypePattern</code> is an optional variable modifier list,
 * a data type, and an identifier.</p>
 *
 * <em>
 * TypePattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
 * </em>
 */
public class ASTTypePattern extends ASTParentNode {
    private final ASTListNode myVarModList;
    private final ASTDataType myDataType;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTTypePattern</code> with the given Location,
     * VariableModifierList, DataType, and Identifier.
     * @param location A <code>Location</code>.
     * @param varModList An <code>ASTVariableModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTTypePattern(Location location, ASTListNode varModList, ASTDataType dataType, ASTIdentifier identifier) {
        super(location, Arrays.asList(varModList, dataType, identifier));
        myVarModList = varModList;
        myDataType = dataType;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTTypePattern</code> with the given Location,
     * DataType, and Identifier, but no VariableModifierList.
     * @param location A <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTTypePattern(Location location, ASTDataType dataType, ASTIdentifier identifier) {
        super(location, Arrays.asList(dataType, identifier));
        myVarModList = null;
        myDataType = dataType;
        myIdentifier = identifier;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns the <code>VariableModifierList</code>, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getVarModList() {
        return Optional.ofNullable(myVarModList);
    }

    /**
     * Returns the <code>DataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns the identifier.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    public void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
        if (myVarModList != null) {
            myVarModList.print(prefix + (isTail ? "    " : "|   "), false);
        }
        myDataType.print(prefix + (isTail ? "    " : "|   "), false);
        myIdentifier.print(prefix + (isTail ? "    " : "│   "), true);
    }
}
