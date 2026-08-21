package org.spruce.compiler.bootstrap.test.resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTConstructorDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTFieldDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTFormalParameter;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlock;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlockStatement;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.resolution.BasicResolver;
import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.resolution.TopLevelResolver;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * Utility methods for resolver tests.  No test entry points.
 */
public class ResolverTestUtility {

    /**
     * Standard classes in spruce.lang for resolution.
     */
    static final String CODE_SPRUCE_LANG =
            """
            namespace spruce.lang;
            class Any {}
            class Boolean {}
            class Character {}
            class Integer {}
            class Double {}
            class String {}
            class Class {}
            """;

    /**
     * Helper method to parse a bunch of code units at once.
     * @param codeUnits A <code>List</code> of strings, each representing code
     *                  for one compilation unit.
     * @return A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    static List<ASTOrdinaryCompilationUnit> parseCodes(List<String> codeUnits) {
        List<ASTOrdinaryCompilationUnit> ocus = new ArrayList<>(codeUnits.size());
        for (String codeUnit : codeUnits) {
            TopLevelParser parser = ParserTopLevelTest.getTopLevelParser(codeUnit);
            ocus.add(parser.parseOrdinaryCompilationUnit());
            for (CompilerMessage msg : parser.getCompilerMessages()) {
                System.out.println(msg);
            }
            assertEquals(0, parser.getCompilerMessages().size());
        }
        return ocus;
    }

    /**
     * Helper method to create the global symbol table using the given
     * <code>OrdinaryCompilationUnit</code>s.
     * @param ocus A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     * @param numSymbolErrorsExpected The number of symbol generation error expected.
     * @return A <code>GlobalLookup</code> representing the global symbol table.
     */
    static GlobalLookup createGlobalSymbolTable(List<ASTOrdinaryCompilationUnit> ocus,
                                                int numSymbolErrorsExpected) {
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new GlobalLookup());
        creator.createSymbolTable(ocus);
        for (CompilerMessage msg : creator.getCompilerMessages()) {
            System.out.println(msg);
        }
        assertEquals(numSymbolErrorsExpected, creator.getCompilerMessages().size());
        return creator.getGlobalLookup();
    }

    /**
     * Prints any compiler messages.  Ensures that there are no compiler
     * messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param resolver A <code>BasicResolver</code>.
     */
    static void ensureNoErrors(SymbolTable table, BasicResolver resolver) {
        System.out.println(table);
        long errorCount = generalCheckForError(resolver);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly one
     * compiler message representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param resolver A <code>BasicResolver</code>.
     */
    static void expectError(SymbolTable table, BasicResolver resolver) {
        expectError(table, resolver, 1);
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly the
     * specified number of compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param resolver A <code>BasicResolver</code>.
     */
    static void expectError(SymbolTable table, BasicResolver resolver, int count) {
        System.out.println(table);
        long errorCount = generalCheckForError(resolver);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    private static long generalCheckForError(BasicResolver resolver) {
        List<CompilerMessage> msgs = resolver.getCompilerMessages();
        for (CompilerMessage msg : msgs) {
            System.out.println(msg);
        }
        return msgs.stream()
                .filter(cm -> cm.getLevel() == CompilerMessage.Level.ERROR)
                .count();
    }

    /**
     * Helper method to parse all codes, create the symbols, and resolve all
     * symbols.  Expect no errors in the symbol generation phase.
     * @param codes A <code>List</code> of string codes, one per compilation unit.
     * @return A <code>Trio</code> consisting of a <code>List</code> of
     *     <code>ASTOrdinaryCompilationUnit</code>s, a <code>GlobalLookup</code>,
     *     and a <code>TopLevelResolver</code>.
     */
    static Trio compileSoFar(List<String> codes) {
        return compileSoFar(codes, 0);
    }

    /**
     * Helper method to parse all codes, create the symbols, and resolve all
     * symbols.  Expect the given number of errors in the symbol generation
     * phase.
     * @param codes A <code>List</code> of string codes, one per compilation unit.
     * @param numSymbolErrorsExpected The number of symbol generation error expected.
     * @return A <code>Trio</code> consisting of a <code>List</code> of
     *     <code>ASTOrdinaryCompilationUnit</code>s, a <code>GlobalLookup</code>,
     *     and a <code>TopLevelResolver</code>.
     */
    static Trio compileSoFar(List<String> codes, int numSymbolErrorsExpected) {
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        GlobalLookup global = createGlobalSymbolTable(ocus, numSymbolErrorsExpected);
        TopLevelResolver resolver = new Resolver(new BaseMessageProducer(), global).getTopLevelResolver();
        resolver.resolveOrdinaryCompilationUnits(ocus);

        return new Trio(ocus, global, resolver);
    }

    /**
     * Helper method to parse all codes and create the symbols, but don't
     * resolve all symbols yet.
     * @param codes A <code>List</code> of string codes, one per compilation unit.
     * @return A <code>Trio</code> consisting of a <code>List</code> of
     *     <code>ASTOrdinaryCompilationUnit</code>s, a <code>GlobalLookup</code>,
     *     and a <code>TopLevelResolver</code>.
     */
    static Trio resolveAllButMembers(List<String> codes) {
        return resolveAllButMembers(codes, 0);
    }

    /**
     * Helper method to parse all codes and create the symbols, but don't
     * resolve all symbols yet.
     * @param codes A <code>List</code> of string codes, one per compilation unit.
     * @param numSymbolErrorsExpected The number of symbol generation error expected.
     * @return A <code>Trio</code> consisting of a <code>List</code> of
     *     <code>ASTOrdinaryCompilationUnit</code>s, a <code>GlobalLookup</code>,
     *     and a <code>TopLevelResolver</code>.
     */
    static Trio resolveAllButMembers(List<String> codes, int numSymbolErrorsExpected) {
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        GlobalLookup global = createGlobalSymbolTable(ocus, numSymbolErrorsExpected);
        TopLevelResolver resolver = new Resolver(new BaseMessageProducer(), global).getTopLevelResolver();
        resolver.resolveSupertypes(ocus);
        return new Trio(ocus, global, resolver);
    }

    public record Trio(List<ASTOrdinaryCompilationUnit> ocus, GlobalLookup global, TopLevelResolver resolver) {
    }

    /**
     * Retrieve a <code>MethodDeclaration</code>.
     * @param trio A <code>Trio</code>.
     * @param ocuIdx The 0-based index into the list of
     *               <code>OrdinaryCompilationUnit</code>s.
     * @param memberIdx The 0-based index into the members of the first
     *                  <code>ClassDeclaration</code>.
     * @return An <code>ASTMethodDeclaration</code>, or fails if not found.
     */
    static ASTMethodDeclaration getMethod(Trio trio, int ocuIdx, int memberIdx) {
        return getMethod(trio, ocuIdx, 0, memberIdx);
    }

    /**
     * Retrieve a <code>MethodDeclaration</code>.
     * @param trio A <code>Trio</code>.
     * @param ocuIdx The 0-based index into the list of
     *               <code>OrdinaryCompilationUnit</code>s.
     * @param memberIdx The 0-based index into the members of the first
     *                  <code>ClassDeclaration</code>.
     * @return An <code>ASTMethodDeclaration</code>, or fails if not found.
     */
    static ASTMethodDeclaration getMethod(Trio trio, int ocuIdx, int typeDeclIdx, int memberIdx) {
        ASTOrdinaryCompilationUnit ocu = trio.ocus().get(ocuIdx);
        ASTClassDeclaration classDecl = ensureIsa(ocu.getTypeDeclList().get(typeDeclIdx), ASTClassDeclaration.class);
        return ensureIsa(classDecl.getClassParts().get(memberIdx), ASTMethodDeclaration.class);
    }

    /**
     * Retrieve a <code>ConstructorDeclaration</code>.
     * @param trio A <code>Trio</code>.
     * @param ocuIdx The 0-based index into the list of
     *               <code>OrdinaryCompilationUnit</code>s.
     * @param memberIdx The 0-based index into the members of the first
     *                  <code>ClassDeclaration</code>.
     * @return An <code>ASTMethodDeclaration</code>, or fails if not found.
     */
    static ASTConstructorDeclaration getConstructor(Trio trio, int ocuIdx, int typeDeclIdx, int memberIdx) {
        ASTOrdinaryCompilationUnit ocu = trio.ocus().get(ocuIdx);
        ASTClassDeclaration classDecl = ensureIsa(ocu.getTypeDeclList().get(typeDeclIdx), ASTClassDeclaration.class);
        return ensureIsa(classDecl.getClassParts().get(memberIdx), ASTConstructorDeclaration.class);
    }

    /**
     * Retrieve the <code>VariableSymbol</code> for a <code>FormalParameter</code>
     * in a <code>MethodDeclaration</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param idx The 0-based index into the list of <code>FormalParameter</code>s.
     * @return A <code>VariableSymbol</code>, or fails if not found.
     */
    static VariableSymbol getFormalParameterSymbol(ASTMethodDeclaration methodDecl, int idx) {
        ASTFormalParameter param = methodDecl.getHeader().getMethodDecl().getFormalParamList().get(idx);
        return param.getDeclSymbol();
    }

    /**
     * Retrieve a <code>FieldDeclaration</code>.
     * @param trio A <code>Trio</code>.
     * @param ocuIdx The 0-based index into the list of
     *               <code>OrdinaryCompilationUnit</code>s.
     * @param typeIdx The 0-based index into the list of
     *                <code>TypeDeclaration</code>s.
     * @param memberIdx The 0-based index into the members of the first
     *                  <code>ClassDeclaration</code>.
     * @return An <code>ASTFieldDeclaration</code>, or fails if not found.
     */
    static ASTFieldDeclaration getField(Trio trio, int ocuIdx, int typeIdx, int memberIdx) {
        ASTOrdinaryCompilationUnit ocu = trio.ocus().get(ocuIdx);
        ASTClassDeclaration classDecl = ensureIsa(ocu.getTypeDeclList().get(typeIdx), ASTClassDeclaration.class);
        return ensureIsa(classDecl.getClassParts().get(memberIdx), ASTFieldDeclaration.class);
    }

    /**
     * Retrieve the <code>BlockStatement</code> in a <code>MethodDeclaration</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param idx The 0-based index into the list of <code>BlockStatement</code>s.
     * @return An <code>ASTBlockStatement</code>, or fails if not found.
     */
    static ASTBlockStatement getBlockStatement(ASTMethodDeclaration methodDecl, int idx) {
        Optional<ASTBlock> optBlock = methodDecl.getBody().getBlock();
        assertTrue(optBlock.isPresent());
        return optBlock.get().getBlockStmts().get(idx);
    }

    /**
     * Retrieve the <code>BlockStatement</code> in a <code>ConstructorDeclaration</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param idx The 0-based index into the list of <code>BlockStatement</code>s.
     * @return An <code>ASTBlockStatement</code>, or fails if not found.
     */
    static ASTBlockStatement getBlockStatement(ASTConstructorDeclaration constrDecl, int idx) {
        ASTBlock optBlock = constrDecl.getBlock();
        return optBlock.getBlockStmts().get(idx);
    }
}
