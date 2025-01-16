package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTSharedConstructor</code> is "shared" followed by "constructor",
 * then a pair of parentheses, then a Block.</p>
 *
 * <em>
 * SharedConstructor:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;shared constructor ( ) Block
 * </em>
 */
public final class ASTSharedConstructor extends ASTParentNode implements ASTClassPart {
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTSharedConstructor</code> at the given <code>Location</code>
     * with the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTSharedConstructor(Location location, ASTBlock block) {
        super(location);
        myBlock = block;
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
        return Arrays.asList(myBlock);
    }
}
