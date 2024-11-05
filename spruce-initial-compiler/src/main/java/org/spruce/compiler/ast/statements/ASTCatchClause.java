package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatchClause</code> is "catch" followed by a CatchFormalParameter
 * within parentheses, and a block.</p>
 *
 * <em>
 * CatchClause:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;catch ( CatchFormalParameter ) Block
 * </em>
 */
public class ASTCatchClause extends ASTParentNode {
    private final ASTCatchFormalParameter myCatchFormalParam;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTCatchClause</code> at the given <code>Location</code>
     * with the given <code>ASTCatchFormalParameter</code> and the given
     * <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param catchFormalParameter An <code>ASTCatchFormalParameter</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTCatchClause(Location location, ASTCatchFormalParameter catchFormalParameter, ASTBlock block) {
        super(location);
        myCatchFormalParam = catchFormalParameter;
        myBlock = block;
    }

    /**
     * Returns an <code>ASTCatchFormalParameter</code>.
     * @return An <code>ASTCatchFormalParameter</code>.
     */
    public ASTCatchFormalParameter getCatchFormalParam() {
        return myCatchFormalParam;
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
        return Arrays.asList(myCatchFormalParam, myBlock);
    }
}
