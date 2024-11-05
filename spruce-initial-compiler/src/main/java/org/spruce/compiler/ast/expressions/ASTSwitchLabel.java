package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchLabel</code> is "case" followed by Case Constants,
 * "default", or a Pattern.</p>
 *
 * <em>
 * SwitchLabel:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;case CaseConstants<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;default
 * &nbsp;&nbsp;&nbsp;&nbsp;Pattern [Guard]
 * </em>
 */
public class ASTSwitchLabel extends ASTParentNode {
    private final ASTCaseConstants myCaseConstants;
    private final ASTPattern myPattern;
    private final ASTGuard myGuard;
    private final ASTKeywordNode myKeyword;

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with
     * CaseConstants as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param defaultKeyword A <code>ASTKeywordNode</code> of keyword <code>DEFAULT</code>.
     */
    public ASTSwitchLabel(Location location, ASTKeywordNode defaultKeyword) {
        super(location);
        myCaseConstants = null;
        myPattern = null;
        myGuard = null;
        myKeyword = defaultKeyword;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with
     * CaseConstants as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param caseKeyword A <code>ASTKeywordNode</code> of keyword <code>CASE</code>.
     * @param caseConstants An <code>ASTCaseConstants</code>.
     */
    public ASTSwitchLabel(Location location, ASTKeywordNode caseKeyword, ASTCaseConstants caseConstants) {
        super(location);
        myCaseConstants = caseConstants;
        myPattern = null;
        myGuard = null;
        myKeyword = caseKeyword;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with the
     * given Pattern as its child.
     * @param location The <code>Location</code> marking the start of this node.
     * @param pattern An <code>ASTPattern</code>.
     */
    public ASTSwitchLabel(Location location, ASTPattern pattern) {
        super(location);
        myCaseConstants = null;
        myPattern = pattern;
        myGuard = null;
        myKeyword = null;
    }

    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given Location, with the
     * given Pattern and Guard as its children.
     * @param location The <code>Location</code> marking the start of this node.
     * @param pattern The <code>ASTPattern</code>.
     * @param guard The <code>ASTGuard</code>.
     */
    public ASTSwitchLabel(Location location, ASTPattern pattern, ASTGuard guard) {
        super(location);
        myCaseConstants = null;
        myPattern = pattern;
        myGuard = guard;
        myKeyword = null;
    }

    /**
     * Returns an <code>ASTCaseConstants</code>, if it exists.
     * @return An <code>Optional&lt;ASTCaseConstants&gt;</code>.
     */
    public Optional<ASTCaseConstants> getCaseConstants() {
        return Optional.ofNullable(myCaseConstants);
    }

    /**
     * Returns an <code>ASTPattern</code>, if it exists.
     * @return An <code>Optional&lt;ASTPattern&gt;</code>.
     */
    public Optional<ASTPattern> getPattern() {
        return Optional.ofNullable(myPattern);
    }

    /**
     * Returns an <code>ASTGuard</code>, if it exists.
     * @return An <code>Optional&lt;ASTGuard&gt;</code>.
     */
    public Optional<ASTGuard> getGuard() {
        return Optional.ofNullable(myGuard);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing <code>default</code>
     * or <code>case</code>, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getKeyword() {
        return Optional.ofNullable(myKeyword);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myCaseConstants != null) {
            children.add(myCaseConstants);
        }
        if (myPattern != null) {
            children.add(myPattern);
        }
        if (myGuard != null) {
            children.add(myGuard);
        }
        if (myKeyword != null) {
            children.add(myKeyword);
        }
        return children;
    }
}
