package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlock;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTMethodBody</code> is a block.</p>
 *
 * <em>
 * MethodBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block
 * </em>
 */
public class ASTMethodBody extends ASTParentNode {
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTMethodBody</code> at the given <code>Location</code>
     * with the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTMethodBody(Location location, ASTBlock block) {
        super(location);
        myBlock = block;
    }

    /**
     * Constructs an <code>ASTMethodBody</code> at the given <code>Location</code>
     * with no Block.
     * @param location The <code>Location</code>.
     */
    public ASTMethodBody(Location location) {
        super(location);
        myBlock = null;
    }

    /**
     * Returns an <code>ASTBlock</code>, if it exists.
     * @return An <code>Optional&lt;ASTBlock&gt;</code>.
     */
    public Optional<ASTBlock> getBlock() {
        return Optional.ofNullable(myBlock);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        if (myBlock != null) {
            children.add(myBlock);
        }
        return children;
    }
}
