package org.spruce.compiler.parser;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

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
     * Parses an <code>IntersectionType</code>.
     * <em>
     * IntersectionType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType {& DataType}
     * </em>
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
     * <p>Parses a <code>TypeParameters</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.</p>
     * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
     * turn on the type context in the Scanner for the duration of this parsing.</p>
     * <em>
     * TypeParameters:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt; TypeParameterList &gt;
     * </em>
     * @return An <code>ASTTypeParameterList</code>.
     */
    public ASTTypeParameterList parseTypeParameters() {
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        setInTypeContext(true);
        if (accept(LESS_THAN) != null) {
            ASTTypeParameterList typeParamList = parseTypeParameterList();
            if (accept(GREATER_THAN) == null) {
                throw new CompileException(curr().getLocation(), "Expected \">\".");
            }
            setInTypeContext(false);
            return typeParamList;
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected \"<\".");
        }
    }

    /**
     * Parses a <code>TypeParameterList</code>.
     * <em>
     * TypeParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameter {, TypeParameter}
     * </em>
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
     * Parses a <code>TypeParameter</code>.
     * <em>
     * TypeParameter:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeBound
     * </em>
     * @return An <code>ASTTypeParameter</code>.
     */
    public ASTTypeParameter parseTypeParameter() {
        Location loc = curr().getLocation();
        ASTIdentifier name = getNamesParser().parseIdentifier();
        if (isCurr(SUBTYPE)) {
            return new ASTTypeParameter(loc, name, parseTypeBound());
        }
        return new ASTTypeParameter(loc, name);
    }

    /**
     * Parses a <code>TypeBound</code>.
     * <em>
     * TypeBound:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;: IntersectionType<br>
     * </em>
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType parseTypeBound() {
        if (accept(SUBTYPE) == null) {
            throw new CompileException(curr().getLocation(), "Expected \"<:\".");
        }
        else {
            return parseIntersectionType();
        }
    }

    /**
     * Parses a <code>DataType</code>.
     * <em>
     * DataType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
     * </em>
     * @return An <code>ASTDataType</code> that could be an
     *     <code>ASTDataTypeNoArray</code> or an <code>ASTArrayType</code>.
     */
    public ASTDataType parseDataType() {
        Location loc = curr().getLocation();
        ASTDataTypeNoArray dtna = parseDataTypeNoArray();
        if (isCurr(OPEN_CLOSE_BRACKET) || (isCurr(OPEN_BRACKET) && isNext(CLOSE_BRACKET)) ) {
            ASTDims dims = parseDims();
            return new ASTArrayType(loc, dtna, dims);
        }
        else {
            return dtna;
        }
    }

    /**
     * Parses an <code>ArrayType</code>.
     * <em>
     * ArrayType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims
     * </em>
     * @return An <code>ASTArrayType</code>.
     */
    public ASTArrayType parseArrayType() {
        Location loc = curr().getLocation();
        if (isCurr(IDENTIFIER)) {
            ASTDataTypeNoArray dtna = parseDataTypeNoArray();
            if (isCurr(OPEN_CLOSE_BRACKET) || isCurr(OPEN_BRACKET)) {
                ASTDims dims = parseDims();
                return new ASTArrayType(loc, dtna, dims);
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
     * Parses a <code>Dims</code>.
     * <em>
     * Dims:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[]<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Dims []
     * </em>
     * @return An <code>ASTDims</code>.
     */
    public ASTDims parseDims() {
        return parseMultiple(t -> List.of(OPEN_CLOSE_BRACKET, OPEN_BRACKET).contains(t.getType()),
                "Expected [].",
                this::parseDim,
                ASTDims::new
                );
    }

    /**
     * Parses a <code>Dim</code>.
     * <em>
     * Dim:
     * &nbsp;&nbsp;&nbsp;&nbsp;[]
     * </em>
     * @return An <code>ASTKeywordNode</code> of keyword <code>OPEN_CLOSE_BRACKET</code>.
     */
    public ASTKeywordNode parseDim() {
        Location loc = curr().getLocation();
        if (isCurr(OPEN_CLOSE_BRACKET)) {
            accept(OPEN_CLOSE_BRACKET);
            return new ASTKeywordNode(loc, OPEN_CLOSE_BRACKET);
        }
        else if (isCurr(OPEN_BRACKET) && isNext(CLOSE_BRACKET)) {
            accept(OPEN_BRACKET);
            accept(CLOSE_BRACKET);
            return new ASTKeywordNode(loc, OPEN_CLOSE_BRACKET);
        }
        else {
            throw new CompileException(loc, "Expected '[]'.");
        }
    }

    /**
     * Parses a <code>DataTypeNoArrayList</code>.
     * <em>
     * DataTypeNoArrayList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray {, DataTypeNoArray}
     * </em>
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseDataTypeNoArrayList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected a data type (no array).",
                COMMA,
                getTypesParser()::parseDataTypeNoArray,
                ASTDataTypeNoArrayList::new
        );
    }

    /**
     * Parses a <code>DataTypeNoArray</code>.
     * <em>
     * DataTypeNoArray:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
     * </em>
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray parseDataTypeNoArray() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                DOT,
                this::parseSimpleType,
                ASTDataTypeNoArray::new
        );
    }

    /**
     * Parses a <code>SimpleType</code>.
     * <em>
     * SimpleType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeArguments
     * </em>
     * @return An <code>ASTSimpleType</code>.
     */
    public ASTSimpleType parseSimpleType() {
        Location loc = curr().getLocation();
        ASTIdentifier name = getNamesParser().parseIdentifier();
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
            return new ASTSimpleType(loc, name, parseTypeArguments());
        }
        return new ASTSimpleType(loc, name);
    }

    /**
     * <p>Parses a <code>TypeArgumentsOrDiamond</code></p>
     * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
     * turn on the type context in the Scanner for the duration of this parsing.</p>
     *
     * <em>
     * TypeArgumentsOrDiamond:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeArguments<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt; &gt;
     * </em>
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
            node = new ASTTypeArgumentsOrDiamond(loc);
        }
        else {
            node = new ASTTypeArgumentsOrDiamond(loc, parseTypeArguments());
        }
        return node;
    }

    /**
     * <p>Parses a <code>TypeArguments</code>.</p>
     * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
     * turn on the type context in the Scanner for the duration of this parsing.</p>
     *
     * <em>
     * TypeArguments:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt; TypeArgumentList &gt;
     * </em>
     * @return An <code>ASTTypeArgumentList</code>.
     */
    public ASTTypeArgumentList parseTypeArguments() {
        // TODO: Move this higher up in the parsing, to prevent nested type
        // arguments from turning this off too early.
        setInTypeContext(true);
        if (accept(LESS_THAN) != null) {
            ASTTypeArgumentList typeArgList = parseTypeArgumentList();
            if (accept(GREATER_THAN) == null) {
                throw new CompileException(curr().getLocation(), "Expected \">\".");
            }
            setInTypeContext(false);
            return typeArgList;
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
     * Parses a <code>TypeArgumentList</code>.
     * <em>
     * TypeArgumentList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeArgument {, TypeArgument}
     * </em>
     * @return An <code>ASTTypeArgumentList</code>.
     */
    public ASTTypeArgumentList parseTypeArgumentList() {
        return parseList(
                TypesParser::isTypeArgument,
                "Expected a type argument.",
                COMMA,
                this::parseTypeArgument,
                ASTTypeArgumentList::new
        );
    }

    /**
     * Parses a <code>TypeArgument</code>.
     * <em>
     * TypeArgument:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Wildcard<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType
     * </em>
     * @return An <code>ASTTypeArgument</code>: either an <code>ASTWildcard</code>
     *     or an <code>ASTDataType</code>.
     */
    public ASTTypeArgument parseTypeArgument() {
        if (isCurr(QUESTION_MARK)) {
            return parseWildcard();
        }
        else if (isCurr(IDENTIFIER)) {
            return parseDataType();
        }
        else {
            throw new CompileException(curr().getLocation(), "Expected wildcard or data type.");
        }
    }

    /**
     * Parses a <code>Wildcard</code>.
     * <em>
     * WildCard:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;?<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;? WildcardBounds
     * </em>
     * @return An <code>ASTWildcard</code>.
     */
    public ASTWildcard parseWildcard() {
        Location loc = curr().getLocation();
        if (isCurr(QUESTION_MARK)) {
            ASTKeywordNode wildcard = parseModifier(
                    Arrays.asList(QUESTION_MARK),
                    "Wildcard expected.",
                    ASTKeywordNode::new
            );
            if (isCurr(SUBTYPE) || isCurr(SUPERTYPE)) {
                return new ASTWildcard(loc, wildcard, parseWildcardBounds());
            }
            else {
                return new ASTWildcard(loc, wildcard);
            }
        }
        else {
            throw new CompileException(curr().getLocation(), "Wildcard expected.");
        }
    }

    /**
     * Parses a <code>WildcardBounds</code>.
     * <em>
     * WildcardBounds:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;: DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;:&gt; DataType
     * </em>
     * @return An <code>ASTWildcardBounds</code>.
     */
    public ASTWildcardBounds parseWildcardBounds() {
        Location loc = curr().getLocation();
        ASTKeywordNode boundKeyword = parseModifier(
                Arrays.asList(SUBTYPE, SUPERTYPE),
                "Expected \"<:\" or \":>\".",
                ASTKeywordNode::new);
        return new ASTWildcardBounds(loc, boundKeyword, parseDataType());
    }
}
