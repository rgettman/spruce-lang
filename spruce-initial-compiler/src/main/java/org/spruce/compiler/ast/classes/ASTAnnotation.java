package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTAnnotation</code> is a marker annotation, a single element
 * annotation, or a normal annotation.</p>
 *
 * <em>
 * Annotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MarkerAnnotation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SingleElementAnnotation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NormalAnnotation
 * </em>
 */
public sealed interface ASTAnnotation extends ParentNode, ASTElementValue
        permits ASTMarkerAnnotation, ASTSingleElementAnnotation, ASTNormalAnnotation {
}
