package org.spruce.compiler.parser;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.common.Location;
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
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TypesParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
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
                ExpressionsParser.PRIMARY_STOPPERS,
                ASTIntersectionType::new
        );
    }

    /**
     * <p>Parses a <code>TypeParameters</code>.  This sets the type context in
     * the <code>Scanner</code> for the duration parsing this node.</p>
     * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
     * turn on the type context in the Scanner for the duration of this parsing.</p>
     * <p>It is expected that the parser is at the location of LESS_THAN.</p>
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
                error(curr().getLocation(), "Expected \">\".");
            }
            setInTypeContext(false);
            return typeParamList;
        }
        else {
            throw internalError(LESS_THAN);
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
                t -> Arrays.asList(AT_SIGN, IDENTIFIER).contains(t.getType()),
                "Expected an identifier.",
                COMMA,
                this::parseTypeParameter,
                Arrays.asList(GREATER_THAN, OPEN_BRACE, OPEN_PARENTHESIS, CONSTRUCTOR,
                        EXTENDS, IMPLEMENTS, PERMITS,  // Rest of type declaration
                        VOID, VAR, MUT, // Result of method declaration
                        EOF
                ),
                ASTTypeParameterList::new
        );
    }

    /**
     * Parses a <code>TypeParameter</code>.
     * <em>
     * TypeParameter:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [TypeBound]<br>
     * </em>
     * @return An <code>ASTTypeParameter</code>.
     */
    public ASTTypeParameter parseTypeParameter() {
        Location loc = curr().getLocation();
        ASTAnnotationList annList = getClassesParser().parseAnnotationList();
        ASTIdentifier name = getNamesParser().parseIdentifier();
        if (isCurr(COLON)) {
            return new ASTTypeParameter(loc, annList, name, parseTypeBound());
        }
        return new ASTTypeParameter(loc, annList, name);
    }

    /**
     * Parses a <code>TypeBound</code>.
     * <em>
     * TypeBound:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;: IntersectionType<br>
     * </em>
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType parseTypeBound() {
        if (accept(COLON) == null) {
            error(curr().getLocation(), "Expected ':'.");
        }
        return parseIntersectionType();
    }

    /**
     * Parses a <code>DataType</code>.
     * <em>
     * DataType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType !<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType ?
     * </em>
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType parseDataType() {
        Location loc = curr().getLocation();
        ASTBaseDataType base = parseBaseDataType();
        List<TokenType> suffixes = Arrays.asList(QUESTION_MARK, EXCLAMATION);
        if (isAcceptedOperator(suffixes) != null) {
            ASTKeywordNode suffixOp = parseModifier(suffixes,
                    "'?' or '!'");
            return new ASTDataType(loc, base, suffixOp);
        }
        return new ASTDataType(loc, base);
    }

    /**
     * Parses a <code>BaseDataType</code>.
     * <em>
     * BaseDataType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
     * </em>
     * @return An <code>ASTBaseDataType</code> that could be an
     *     <code>ASTDataTypeNoArray</code> or an <code>ASTArrayType</code>.
     */
    public ASTBaseDataType parseBaseDataType() {
        Location loc = curr().getLocation();
        ASTDataTypeNoArray dtna = parseDataTypeNoArray();
        if (isCurr(OPEN_BRACKET) && isNext(CLOSE_BRACKET)) {
            ASTDims dims = parseDims();
            return new ASTArrayType(loc, dtna, dims);
        }
        else {
            return dtna;
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
        return parseMultiple(t -> test(t, Arrays.asList(OPEN_BRACKET)),
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
     * @return An <code>ASTKeywordNode</code> of keyword <code>OPEN_BRACKET</code>.
     */
    public ASTKeywordNode parseDim() {
        Location loc = curr().getLocation();
        if (isCurr(OPEN_BRACKET)) {
            accept(OPEN_BRACKET);
            if (accept(CLOSE_BRACKET) == null) {
                error(loc, "Expected ']' following '['.");
            }
        }
        else {
            throw internalError(OPEN_BRACKET);
        }
        return new ASTKeywordNode(loc, OPEN_BRACKET);
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
                Arrays.asList(OPEN_BRACE, OPEN_PARENTHESIS,
                        EXTENDS, IMPLEMENTS, PERMITS,  // Rest of type declaration
                        EOF
                ),
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
                Arrays.asList(GREATER_THAN, OPEN_BRACE, OPEN_PARENTHESIS, SEMICOLON, DOUBLE_COLON,
                        OPEN_BRACKET, PIPE, GREATER_THAN, COMMA, AMPERSAND, THREE_DOTS,
                        EXTENDS, IMPLEMENTS, PERMITS,  // Rest of type declaration
                        VOID, VAR, MUT,  // Result of method declaration
                        CLASS, NEW, SUPER, LESS_THAN, SELF,  // parts of primaries
                        EOF
                ),
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
                        (isNext(UNDERSCORE)) ||
                        (isNext(IN)) || (isNext(OUT))
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
                error(curr().getLocation(), "Expected '>'.");
            }
            setInTypeContext(false);
            return typeArgList;
        }
        else {
            throw internalError(LESS_THAN);
        }
    }

    /**
     * Determines whether the given token can start a type argument.
     *
     * @param t A <code>Token</code>.
     * @return Whether the given token can start a type argument.
     */
    private static boolean isTypeArgument(Token t) {
        return test(t, Arrays.asList(UNDERSCORE, IN, OUT, IDENTIFIER));
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
                Arrays.asList(GREATER_THAN, OPEN_BRACE, OPEN_PARENTHESIS, SEMICOLON, DOUBLE_COLON, NEW,
                        OPEN_BRACKET, PIPE, GREATER_THAN, COMMA, AMPERSAND, THREE_DOTS,
                        EXTENDS, IMPLEMENTS, PERMITS,  // Rest of type declaration
                        VOID, VAR, MUT, // Result of method declaration
                        EOF
                ),
                ASTTypeArgumentList::new
        );
    }

    /**
     * Parses a <code>TypeArgument</code>.
     * <em>
     * TypeArgument:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;in DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;out DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType
     * </em>
     * @return An <code>ASTTypeArgument</code>: either an <code>ASTWildcard</code>
     *     or an <code>ASTTypeArgumentBounds</code>.
     */
    public ASTTypeArgument parseTypeArgument() {
        if (isCurr(UNDERSCORE)) {
            return parseWildcard();
        }
        else {
            return parseTypeArgumentBounds();
        }
    }

    /**
     * Parses a <code>TypeArgumentBounds</code>.
     * <em>
     * TypeArgumentBounds:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;in DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;out DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType
     * </em>
     * @return An <code>ASTTypeArgumentBounds</code>.
     */
    public ASTTypeArgumentBounds parseTypeArgumentBounds() {
        Location loc = curr().getLocation();
        if (isAcceptedOperator(Arrays.asList(IN, OUT)) != null) {
            ASTKeywordNode genericModifier = parseModifier(Arrays.asList(IN, OUT),
                    "'in' or 'out'");
            return new ASTTypeArgumentBounds(loc, genericModifier, parseDataType());
        }
        return new ASTTypeArgumentBounds(loc, parseDataType());
    }

    /**
     * Parses a <code>Wildcard</code>.
     * <em>
     * WildCard:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;_
     * </em>
     * @return An <code>ASTWildcard</code>.
     */
    public ASTWildcard parseWildcard() {
        Location loc = curr().getLocation();
        if (isCurr(UNDERSCORE)) {
            ASTKeywordNode wildcard = parseModifier(
                    Arrays.asList(UNDERSCORE),
                    "'_'"
            );
            return new ASTWildcard(loc, wildcard);
        }
        else {
            throw internalError(UNDERSCORE);
        }
    }
}
