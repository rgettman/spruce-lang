package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

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
     * There are no modifiers for a shared constructor.
     * @return An empty <code>List</code>.
     */
    @Override
    public List<TokenType> getModifiers() {
        return List.of();
    }

    /**
     * There is no access modifier for a shared constructor.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.empty();
    }

    /**
     * There are no names on a shared constructor declaration.
     * @return An empty <code>List</code>.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Collections.emptyList();
    }

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
        return Arrays.asList(myBlock);
    }
}
