package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.CASE;

/**
 * <p>An <code>ASTSwitchLabel</code> is "case" followed by Switch Constants,
 * "default", or a Pattern.</p>
 *
 * <em>
 * SwitchLabel:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;case SwitchConstants<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;default
 * &nbsp;&nbsp;&nbsp;&nbsp;Pattern [Guard]
 * </em>
 */
public class ASTSwitchLabel extends ASTParentNode {
    private final ASTNode myChild;
    private final ASTUnaryNode myGuard;

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with
     * CaseConstants as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param operation A <code>TokenType</code> representing the operation.
     */
    public ASTSwitchLabel(Location location, TokenType operation) {
        super(location, Arrays.asList(), operation);
        myChild = null;
        myGuard = null;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with
     * CaseConstants as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param caseConstants An <code>ASTListNode</code> representing the CaseConstants.
     */
    public ASTSwitchLabel(Location location, ASTListNode caseConstants) {
        super(location, Arrays.asList(caseConstants), CASE);
        myChild = caseConstants;
        myGuard = null;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with the
     * given Pattern as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param pattern An <code>ASTNode</code> representing the Pattern.
     */
    public ASTSwitchLabel(Location location, ASTNode pattern) {
        super(location, Arrays.asList(pattern), null);
        myChild = pattern;
        myGuard = null;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with the
     * given Pattern and Guard as its children.
     * @param location The <code>Location</code> marking the start of this node.
     * @param pattern The <code>ASTNode</code> representing a Pattern.
     * @param guard The <code>ASTUnaryNode</code> representing a Guard.
     */
    public ASTSwitchLabel(Location location, ASTNode pattern, ASTUnaryNode guard) {
        super(location, Arrays.asList(pattern), null);
        myChild = pattern;
        myGuard = guard;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns the child: either a CaseConstants, Default, or a Pattern.
     * @return An <code>ASTNode</code>.
     */
    public Optional<ASTNode> getChild() {
        return Optional.ofNullable(myChild);
    }

    /**
     * Returns the Guard, if it exists.
     * @return An <code>ASTNode</code>.
     */
    public Optional<ASTNode> getGuard() {
        return Optional.ofNullable(myGuard);
    }
}
