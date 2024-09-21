package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>TypesParser</code> is a <code>BasicParser</code> that parses types.
 */
public class TypesParser extends BasicParser {
    /**
     * Constructs a <code>TypesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public TypesParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTIntersectionType</code>; they are left-
     * associative with each other.
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType parseIntersectionType() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                AMPERSAND,
                this::parseDataType,
                ASTIntersectionType::new
        );
    }

    /**
     * Parses an <code>ASTTypeParameters</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.
     * @return An <code>ASTTypeArguments</code>.
     */
    public ASTTypeParameters parseTypeParameters() {
        Location loc = curr().getLocation();
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        setInTypeContext(true);
        if (accept(LESS_THAN) != null) {
            ASTTypeParameterList typeParamList = parseTypeParameterList();
            if (accept(GREATER_THAN) == null) {
                throw new CompileException(curr().getLocation(), "Expected \">\".");
            }
            setInTypeContext(false);
            return new ASTTypeParameters(loc, Collections.singletonList(typeParamList));
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected \"<\".");
        }
    }

    /**
     * Parses an <code>ASTTypeParameterList</code>.
     * @return An <code>ASTTypeParameterList</code>.
     */
    public ASTTypeParameterList parseTypeParameterList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier.",
                COMMA,
                this::parseTypeParameter,
                ASTTypeParameterList::new
        );
    }

    /**
     * Parses an <code>ASTTypeParameter</code>.
     * @return An <code>ASTTypeParameter</code>.
     */
    public ASTTypeParameter parseTypeParameter() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getNamesParser().parseIdentifier());
        if (isCurr(SUBTYPE)) {
            children.add(parseTypeBound());
        }
        return new ASTTypeParameter(loc, children);
    }

    /**
     * Parses an <code>ASTTypeBound</code>.
     * @return An <code>ASTTypeBound</code>.
     */
    public ASTTypeBound parseTypeBound() {
        Location loc = curr().getLocation();
        if (accept(SUBTYPE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"<:\".");
        }
        else {
            ASTTypeBound node = new ASTTypeBound(loc, Collections.singletonList(parseIntersectionType()));
            node.setOperation(SUBTYPE);
            return node;
        }
    }

    /**
     * Parses an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType parseDataType() {
        Location loc = curr().getLocation();
        ASTDataTypeNoArray dtna = parseDataTypeNoArray();
        if (isCurr(OPEN_CLOSE_BRACKET)) {
            ASTDims dims = parseDims();
            ASTArrayType arrayType = new ASTArrayType(loc, Arrays.asList(dtna, dims));
            return new ASTDataType(loc, Collections.singletonList(arrayType));
        }
        else {
            return new ASTDataType(loc, Collections.singletonList(dtna));
        }
    }

    /**
     * Parses an <code>ASTArrayType</code>.
     * @return An <code>ASTArrayType</code>.
     */
    public ASTArrayType parseArrayType() {
        Location loc = curr().getLocation();
        if (isCurr(IDENTIFIER)) {
            ASTDataTypeNoArray dtna = parseDataTypeNoArray();
            if (isCurr(OPEN_CLOSE_BRACKET)) {
                ASTDims dims = parseDims();
                return new ASTArrayType(loc, Arrays.asList(dtna, dims));
            }
            else {
                throw new CompileException(curr().getLocation(), "Expected [].");
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Identifier expected.");
        }
    }

    /**
     * Parses an <code>ASTDims</code>.
     * @return An <code>ASTDims</code>.
     */
    public ASTDims parseDims() {
        if (!isCurr(OPEN_CLOSE_BRACKET)) {
            throw new CompileException(curr().getLocation(), "Expected [].");
        }
        ASTDims node = null;
        List<ASTNode> children = null;
        while (isCurr(OPEN_CLOSE_BRACKET)) {
            Location loc = curr().getLocation();
            accept(OPEN_CLOSE_BRACKET);
            if (node == null) {
                children = new ArrayList<>(1);
                node = new ASTDims(loc, children);
                node.setOperation(OPEN_CLOSE_BRACKET);
            }
            else {
                ASTDims dims = new ASTDims(loc, new ArrayList<>(1));
                dims.setOperation(OPEN_CLOSE_BRACKET);
                children.add(dims);
                children = dims.getChildren();
            }
        }
        return node;
    }

    /**
     * Parses an <code>ASTDataTypeNoArrayList</code>.
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseDataTypeNoArrayList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected a data type (no array).",
                COMMA,
                this::parseDataTypeNoArray,
                ASTDataTypeNoArrayList::new
        );
    }

    /**
     * Parses an <code>ASTDataTypeNoArray</code>; they are left-associative
     * with each other.
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray parseDataTypeNoArray() {
        return parseBinaryExpressionLeftAssociative(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                Collections.singletonList(DOT),
                this::parseSimpleType,
                ASTDataTypeNoArray::new
        );
    }

    /**
     * Parses an <code>ASTSimpleType</code>.
     * @return An <code>ASTSimpleType</code>.
     */
    public ASTSimpleType parseSimpleType() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(getNamesParser().parseIdentifier());
        // TypeArguments cases:
        //   exprName < identifier <
        //   exprName < identifier ,
        //   exprName < identifier >
        //   exprName < ?
        // else exprName <    ==> means '<' is treated as "less than", not as beginning of type arguments!
        if (isCurr(LESS_THAN) &&
                ((isNext(IDENTIFIER) && isPeek(LESS_THAN)) ||
                        (isNext(IDENTIFIER) && isPeek(COMMA)) ||
                        (isNext(IDENTIFIER) && isPeek(GREATER_THAN)) ||
                        (isNext(QUESTION_MARK))
                )
                ) {
            children.add(parseTypeArguments());
        }
        return new ASTSimpleType(loc, children);
    }

    /**
     * Parses an <code>ASTTypeArgumentsOrDiamond</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.
     * @return An <code>ASTTypeArgumentsOrDiamond</code>.
     */
    public ASTTypeArgumentsOrDiamond parseTypeArgumentsOrDiamond() {
        Location loc = curr().getLocation();
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        setInTypeContext(true);
        ASTTypeArgumentsOrDiamond node;
        if (isCurr(LESS_THAN) && isNext(GREATER_THAN)) {
            accept(LESS_THAN);
            accept(GREATER_THAN);
            node = new ASTTypeArgumentsOrDiamond(loc, Collections.emptyList());
            node.setOperation(LESS_THAN);
        }
        else {
            ASTTypeArguments ta = parseTypeArguments();
            node = new ASTTypeArgumentsOrDiamond(loc, Collections.singletonList(ta));
        }
        return node;
    }

    /**
     * Parses an <code>ASTTypeArguments</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.
     * @return An <code>ASTTypeArguments</code>.
     */
    public ASTTypeArguments parseTypeArguments() {
        Location loc = curr().getLocation();
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        setInTypeContext(true);
        if (accept(LESS_THAN) != null) {
            ASTTypeArgumentList typeArgList = parseTypeArgumentList();
            if (accept(GREATER_THAN) == null) {
                throw new CompileException(curr().getLocation(), "Expected \">\".");
            }
            setInTypeContext(false);
            return new ASTTypeArguments(loc, Collections.singletonList(typeArgList));
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected \"<\".");
        }
    }

    /**
     * Determines whether the given token can start a type argument.
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a type argument.
     */
    private static boolean isTypeArgument(Token t) {
        return (test(t, QUESTION_MARK, IDENTIFIER));
    }

    /**
     * Parses an <code>ASTTypeArgumentList</code>.
     * @return An <code>ASTTypeArgumentList</code>.
     */
    public ASTTypeArgumentList parseTypeArgumentList() {
        return parseList(
                TypesParser::isTypeArgument,
                "Expected a type argument.",
                COMMA,
                this::parseTypeArgument,
                ASTTypeArgumentList::new);
    }

    /**
     * Parses an <code>ASTTypeArgument</code>.
     * @return An <code>ASTTypeArgument</code>.
     */
    public ASTTypeArgument parseTypeArgument() {
        Location loc = curr().getLocation();
        if (isCurr(QUESTION_MARK)) {
            ASTWildcard wildcard = parseWildcard();
            return new ASTTypeArgument(loc, Collections.singletonList(wildcard));
        }
        else if (isCurr(IDENTIFIER)) {
            ASTDataType dt = parseDataType();
            return new ASTTypeArgument(loc, Collections.singletonList(dt));
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected wildcard or data type.");
        }
    }

    /**
     * Parses an <code>ASTWildcard</code>.
     * @return An <code>ASTWildcard</code>.
     */
    public ASTWildcard parseWildcard() {
        Location loc = curr().getLocation();
        if (accept(QUESTION_MARK) == null) {
            throw new CompileException(curr().getLocation(), "Wildcard expected.");
        }
        ASTWildcard node = new ASTWildcard(loc, new ArrayList<>(1));
        if (isCurr(SUBTYPE) || isCurr(SUPERTYPE)) {
            ASTWildcardBounds wb = parseWildcardBounds();
            node.getChildren().add(wb);
        }
        return node;
    }

    /**
     * Parses an <code>ASTWildcardBounds</code>.
     * @return An <code>ASTWildcardBounds</code>.
     */
    public ASTWildcardBounds parseWildcardBounds() {
        Location loc = curr().getLocation();
        TokenType curr;
        if (isCurr(SUBTYPE)) {
            accept(SUBTYPE);
            curr = SUBTYPE;
        }
        else if (isCurr(SUPERTYPE)) {
            accept(SUPERTYPE);
            curr = SUPERTYPE;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected \"<:\" or \":>\".");
        }
        ASTWildcardBounds node = new ASTWildcardBounds(loc, Collections.singletonList(parseDataType()));
        node.setOperation(curr);
        return node;
    }
}
