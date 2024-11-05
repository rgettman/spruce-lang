package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCompactConstructorDeclaration</code> is an optional AccessModifier
 * followed by "constructor", followed by a Block.</p>
 *
 * <em>
 * CompactConstructorDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] constructor Block
 * </em>
 */
public final class ASTCompactConstructorDeclaration extends ASTParentNode implements ASTClassPart {
    private final ASTKeywordNode myAccessMod;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTCompactConstructorDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param location The <code>Location</code>.
     * @param accessMod An <code>ASTKeywordNode</code> representing the Access Modifier.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTCompactConstructorDeclaration(Location location, ASTKeywordNode accessMod, ASTBlock block) {
        super(location);
        myAccessMod = accessMod;
        myBlock = block;
    }

    /**
     * Constructs an <code>ASTCompactConstructorDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param location The <code>Location</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTCompactConstructorDeclaration(Location location, ASTBlock block) {
        super(location);
        myAccessMod = null;
        myBlock = block;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
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
        List<Node> children = new ArrayList<>(2);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myBlock);
        return children;
    }
}
