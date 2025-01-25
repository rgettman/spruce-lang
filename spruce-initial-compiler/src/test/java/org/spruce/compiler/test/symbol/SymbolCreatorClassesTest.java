package org.spruce.compiler.test.symbol;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.symbol.ClassesSymbolCreator;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolCreator;
import org.spruce.compiler.symbol.TopLevelSymbolTable;
import org.spruce.compiler.test.parser.ParserTopLevelTest;

import static org.spruce.compiler.symbol.Symbol.*;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.checkSymbol;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.ensureNoErrors;

/**
 * All tests for the classes symbol creator.
 */
public class SymbolCreatorClassesTest {
    /**
     * Tests final class, no access modifier.
     */
    @Test
    public void testFinalClass() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("final class FinalClass {}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "FinalClass";
        long expFlags = FLAG_ACCESS_INTERNAL | FLAG_MOD_FINAL;

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.CLASS, expFlags, 0);
    }

    /**
     * Tests public sealed interface.
     */
    @Test
    public void testPublicSealedInterface() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("public sealed interface PublicSealedInterface {}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "PublicSealedInterface";
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT | FLAG_MOD_SEALED;

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.INTERFACE, expFlags, 0);
    }

    /**
     * Tests private shared annotation.
     */
    @Test
    public void testPrivateSharedAnnotation() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("private shared annotation PrivateSharedAnnotation {}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "PrivateSharedAnnotation";
        long expFlags = FLAG_ACCESS_PRIVATE | FLAG_MOD_ABSTRACT | FLAG_MOD_SHARED;

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.ANNOTATION, expFlags, 0);
    }

    /**
     * Tests protected record.
     */
    @Test
    public void testProtectedRecord() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("protected record ProtectedRecord(String foo) {}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "ProtectedRecord";
        long expFlags = FLAG_ACCESS_PROTECTED | FLAG_MOD_FINAL;

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.RECORD, expFlags, 0);
    }

    /**
     * Tests internal enum.
     */
    @Test
    public void testInternalEnum() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("internal enum InternalEnum {FOO}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "InternalEnum";

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.ENUM, FLAG_ACCESS_INTERNAL, 0);
    }

    /**
     * Tests public ADT.
     */
    @Test
    public void testAdt() {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("public adt PublicAdt {One(), Two()}");
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        ClassesSymbolCreator creator = getClassesSymbolCreator();

        String expSymbolName = "PublicAdt";
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_SEALED;

        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        Symbol symbol = creator.createSymbolsForTypeDeclaration(typeDecl, parent);
        ensureNoErrors(symbol, creator);
        checkSymbol(symbol, expSymbolName, Type.ADT, expFlags, 0);
    }

    /**
     * Helper method to get a <code>ClassesSymbolCreator</code>.
     * @return A <code>ClassesSymbolCreator</code>.
     */
    public static ClassesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer()).getClassesSymbolCreator();
    }
}
