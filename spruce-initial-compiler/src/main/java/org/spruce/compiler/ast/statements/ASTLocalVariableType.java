package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLocalVariableType</code> is a data type or "auto".</p>
 *
 * <em>
 * LocalVariableType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;auto
 * </em>
 */
public class ASTLocalVariableType extends ASTParentNode {
    private final ASTDataType myDataType;
    private final ASTKeywordNode myKeyword;

    /**
     * Constructs an <code>ASTLocalVariableType</code> at the given <code>Location</code>
     * with the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTLocalVariableType(Location location, ASTDataType dataType) {
        super(location);
        myDataType = dataType;
        myKeyword = null;
    }

    /**
     * Constructs an <code>ASTLocalVariableType</code> at the given <code>Location</code>
     * with the given <code>TokenType</code> as the operation.
     * @param location The <code>Location</code>.
     * @param autoKeyword An <code>ASTKeywordNode</code> with the keyword <code>AUTO</code>.
     */
    public ASTLocalVariableType(Location location, ASTKeywordNode autoKeyword) {
        super(location);
        myDataType = null;
        myKeyword = autoKeyword;
    }

    /**
     * Returns an <code>ASTDataType</code>, if it exists.
     * @return An <code>Optional&lt;ASTDataType&gt;</code>.
     */
    public Optional<ASTDataType> getDataType() {
        return Optional.ofNullable(myDataType);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing "auto", if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getAutoKeyword() {
        return Optional.ofNullable(myKeyword);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        if (myDataType != null) {
            children.add(myDataType);
        }
        else {
            children.add(myKeyword);
        }
        return children;
    }
}