package org.spruce.compiler.test.symbol;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.symbol.ClassesSymbolCreator;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolCreator;
import org.spruce.compiler.symbol.SymbolTable;
import org.spruce.compiler.symbol.TopLevelSymbolTable;
import org.spruce.compiler.test.parser.ParserTopLevelTest;

import static org.spruce.compiler.symbol.Symbol.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.TYPE;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.checkSymbolTable;
import static org.spruce.compiler.test.util.TestUtility.ensureIsa;

/**
 * All tests for the classes symbol creator.
 */
public class SymbolCreatorClassesTest {
    /**
     * Tests final class, no access modifier.
     */
    @Test
    public void testFinalClass() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("final class FinalClass {}");
        String expSymbolName = "FinalClass";
        long expFlags = FLAG_ACCESS_INTERNAL | FLAG_MOD_FINAL;
        checkSymbol(symbol, expSymbolName, Type.CLASS, expFlags, 0);
    }

    /**
     * Tests public sealed interface.
     */
    @Test
    public void testPublicSealedInterface() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("public sealed interface PublicSealedInterface {}");
        String expSymbolName = "PublicSealedInterface";
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT | FLAG_MOD_SEALED;
        checkSymbol(symbol, expSymbolName, Type.INTERFACE, expFlags, 0);
    }

    /**
     * Tests private shared annotation.
     */
    @Test
    public void testPrivateSharedAnnotation() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("private shared annotation PrivateSharedAnnotation {}");
        String expSymbolName = "PrivateSharedAnnotation";
        long expFlags = FLAG_ACCESS_PRIVATE | FLAG_MOD_ABSTRACT | FLAG_MOD_SHARED;
        checkSymbol(symbol, expSymbolName, Type.ANNOTATION, expFlags, 0);
    }

    /**
     * Tests protected record.
     */
    @Test
    public void testProtectedRecord() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("protected record ProtectedRecord(String foo) {}");
        String expSymbolName = "ProtectedRecord";
        long expFlags = FLAG_ACCESS_PROTECTED | FLAG_MOD_FINAL;
        checkSymbol(symbol, expSymbolName, Type.RECORD, expFlags, 1);
    }

    /**
     * Tests internal enum.
     */
    @Test
    public void testInternalEnum() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("internal enum InternalEnum {FOO}");
        String expSymbolName = "InternalEnum";
        checkSymbol(symbol, expSymbolName, Type.ENUM, FLAG_ACCESS_INTERNAL, 1);
    }

    /**
     * Tests public ADT.
     */
    @Test
    public void testAdt() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public adt PublicAdt {
                    One() {},
                    Two() {}
                }
                """);
        String expSymbolName = "PublicAdt";
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_SEALED;
        checkSymbol(symbol, expSymbolName, Type.ADT, expFlags, 2);
    }

    /**
     * Tests member of inner class.
     */
    @Test
    public void testMemberInnerClass() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class Outer {
                    class Inner {}
                }
                """);
        checkSymbol(symbol, "Outer", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        checkSymbol(inner, "Inner", Type.CLASS, FLAG_ACCESS_INTERNAL, 0);
    }

    /**
     * Tests member of nested class.
     */
    @Test
    public void testMemberNestedClass() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class Outer {
                    shared class Inner {}
                }
                """);
        checkSymbol(symbol, "Outer", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        checkSymbol(inner, "Inner", Type.CLASS, FLAG_ACCESS_INTERNAL | FLAG_MOD_SHARED, 0);
    }

    /**
     * Tests member of nested interface.
     */
    @Test
    public void testMemberNestedInterface() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class Outer {
                    interface Inner {}
                }
                """);
        checkSymbol(symbol, "Outer", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        long expFlags = FLAG_ACCESS_INTERNAL | FLAG_MOD_ABSTRACT | FLAG_MOD_SHARED;
        checkSymbol(inner, "Inner", Type.INTERFACE, expFlags, 0);
    }

    /**
     * Tests member of shared constructor.
     */
    @Test
    public void testMemberSharedConstructor() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    shared constructor () {}
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(NAME_SHARED_CONSTRUCTOR));

        Symbol sharedConstr = innerTable.get(NAME_SHARED_CONSTRUCTOR);
        checkSymbol(sharedConstr, NAME_SHARED_CONSTRUCTOR, Type.SHARED_CONSTRUCTOR, FLAG_MOD_SHARED, 0);
    }

    /**
     * Tests member of constructor.
     */
    @Test
    public void testMemberConstructor() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    constructor() {}
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(constructorDecl, symbolName, Type.CONSTRUCTOR, FLAG_ACCESS_PUBLIC, 0);
    }

    /**
     * Tests member of overloaded constructors.
     */
    @Test
    public void testMemberConstructorOverload() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    constructor() {}
                    constructor(Integer foo) {}
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 2);

        String symbolName = NAME_CONSTRUCTOR + "()";
        String symbolName2 = NAME_CONSTRUCTOR + "(Integer)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName, symbolName2));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(constructorDecl, symbolName, Type.CONSTRUCTOR, FLAG_ACCESS_PUBLIC, 0);

        Symbol constructorDecl2 = innerTable.get(symbolName2);
        checkSymbol(constructorDecl2, symbolName2, Type.CONSTRUCTOR, FLAG_ACCESS_PUBLIC, 0);
    }

    /**
     * Tests members of field declarations with variable modifiers.
     */
    @Test
    public void testMemberFieldDeclarationVarMods() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    private String immutable;
                    private var String replaceable;
                    private mut String mutable;
                    private var mut String both;
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 4);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 4, Arrays.asList("immutable", "replaceable", "mutable", "both"));

        Symbol immutSymbol = innerTable.get("immutable");
        long expFlags = FLAG_ACCESS_PRIVATE;
        checkSymbol(immutSymbol, "immutable", Type.FIELD, expFlags, 0);

        Symbol replaceSymbol = innerTable.get("replaceable");
        expFlags = FLAG_ACCESS_PRIVATE | FLAG_VARIABLE_VAR;
        checkSymbol(replaceSymbol, "replaceable", Type.FIELD, expFlags, 0);

        Symbol mutSymbol = innerTable.get("mutable");
        expFlags = FLAG_ACCESS_PRIVATE | FLAG_VARIABLE_MUT;
        checkSymbol(mutSymbol, "mutable", Type.FIELD, expFlags, 0);

        Symbol bothSymbol = innerTable.get("both");
        expFlags = FLAG_ACCESS_PRIVATE | FLAG_VARIABLE_MUT | FLAG_VARIABLE_VAR;
        checkSymbol(bothSymbol, "both", Type.FIELD, expFlags, 0);
    }

    /**
     * Tests members of field declarations.
     */
    @Test
    public void testMemberFieldDeclaration() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    String foo;
                    protected Int bar, jazz;
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "bar", "jazz"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Type.FIELD, FLAG_ACCESS_PRIVATE, 0);

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Type.FIELD, FLAG_ACCESS_PROTECTED, 0);

        Symbol jazzSymbol = innerTable.get("jazz");
        checkSymbol(jazzSymbol, "jazz", Type.FIELD, FLAG_ACCESS_PROTECTED, 0);
    }

    /**
     * Tests duplicate field names.
     */
    @Test
    public void testMemberFieldsSameName() {
        createTopLevelTypeSymbolWithErrors("""
                public class DupeFieldSymbols {
                    String foo;
                    Int foo;
                }
                """, 1);
    }

    /**
     * Tests member of constant declaration.
     */
    @Test
    public void testMemberConstant() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    public constant String BAR = "bar";
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("BAR"));

        Symbol constantSymbol = innerTable.get("BAR");
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_SHARED;
        checkSymbol(constantSymbol, "BAR", Type.FIELD, expFlags, 0);

    }

    /**
     * Tests member of annotation type element declarations.
     */
    @Test
    public void testMemberATED() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public annotation HasElements {
                    String foo();
                    Int bar() default 0;
                }
                """);
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT;
        checkSymbol(symbol, "HasElements", Type.ANNOTATION, expFlags, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("foo", "bar"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Type.ANNOTATION_TYPE_ELEMENT, expFlags, 0);

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Type.ANNOTATION_TYPE_ELEMENT, expFlags, 0);
    }

    /**
     * Tests member of method declaration.
     */
    @Test
    public void testMemberOfMethodDeclaration() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    private void foo(String bar) mut {}
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        long expFlags = FLAG_ACCESS_PRIVATE | FLAG_METHOD_MUT;
        checkSymbol(fooSymbol, symbolName, Type.METHOD, expFlags, 0);
    }

    /**
     * Tests member of overloaded methods.
     */
    @Test
    public void testMemberOfMethodOverloads() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public class HasMember {
                    private mut String foo;
                    public void foo() mut {}
                    public void foo(String bar) mut {}
                }
                """);
        checkSymbol(symbol, "HasMember", Type.CLASS, FLAG_ACCESS_PUBLIC, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "foo()", "foo(String)"));

        String symbolName = "foo";
        Symbol fooSymbol = innerTable.get(symbolName);
        long expFlags = FLAG_ACCESS_PRIVATE | FLAG_VARIABLE_MUT;
        checkSymbol(fooSymbol, symbolName, Type.FIELD, expFlags, 0);

        symbolName = "foo()";
        Symbol fooSymbol2 = innerTable.get(symbolName);
        long expFlags2 = FLAG_ACCESS_PUBLIC | FLAG_METHOD_MUT;
        checkSymbol(fooSymbol2, symbolName, Type.METHOD, expFlags2, 0);

        symbolName = "foo(String)";
        Symbol fooSymbol3 = innerTable.get(symbolName);
        long expFlags3 = FLAG_ACCESS_PUBLIC | FLAG_METHOD_MUT;
        checkSymbol(fooSymbol3, symbolName, Type.METHOD, expFlags3, 0);
    }

    /**
     * Tests member of interface method declaration.
     */
    @Test
    public void testMemberOfInterfaceMethod() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public interface HasMember {
                    void foo(String bar) mut;
                }
                """);
        checkSymbol(symbol, "HasMember", Type.INTERFACE, FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT | FLAG_METHOD_MUT;
        checkSymbol(fooSymbol, symbolName, Type.METHOD, expFlags, 0);
    }

    /**
     * Tests member of enum constant.
     */
    @Test
    public void testMemberOfEnumConstant() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public enum TrafficLight {
                    RED, YELLOW, GREEN
                }
                """);
        checkSymbol(symbol, "TrafficLight", Type.ENUM, FLAG_ACCESS_PUBLIC, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();

        String name1 = "RED", name2 = "YELLOW", name3 = "GREEN";
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList(name1, name2, name3));

        Symbol symbol1 = innerTable.get(name1);
        long expFlags = FLAG_ACCESS_PUBLIC | FLAG_MOD_SHARED;
        checkSymbol(symbol1, name1, Type.ENUM_CONSTANT, expFlags, 0);

        Symbol symbol2 = innerTable.get(name2);
        checkSymbol(symbol2, name2, Type.ENUM_CONSTANT, expFlags, 0);

        Symbol symbol3 = innerTable.get(name3);
        checkSymbol(symbol3, name3, Type.ENUM_CONSTANT, expFlags, 0);
    }

    /**
     * Tests member of record component.
     */
    @Test
    public void testMemberOfRecordComponent() {
        Symbol symbol = createTopLevelTypeSymbolNoErrors("""
                public record Person(String first, String last, Integer age) {
                }
                """);
        checkSymbol(symbol, "Person", Type.RECORD, FLAG_ACCESS_PUBLIC | FLAG_MOD_FINAL, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();

        String name1 = "first", name2 = "last", name3 = "age";
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList(name1, name2, name3));

        Symbol symbol1 = innerTable.get(name1);
        long expFlags = FLAG_ACCESS_PUBLIC;
        checkSymbol(symbol1, name1, Type.RECORD_COMPONENT, expFlags, 0);

        Symbol symbol2 = innerTable.get(name2);
        checkSymbol(symbol2, name2, Type.RECORD_COMPONENT, expFlags, 0);

        Symbol symbol3 = innerTable.get(name3);
        checkSymbol(symbol3, name3, Type.RECORD_COMPONENT, expFlags, 0);
    }

    /**
     * Helper method to get a <code>ClassesSymbolCreator</code>.
     * @return A <code>ClassesSymbolCreator</code>.
     */
    public static ClassesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer()).getClassesSymbolCreator();
    }

    /**
     * Helper method to get a <code>Symbol</code> for a top level type declaration
     * directly from code.  Ensures no errors.
     * @param code The code for the top level type declaration.
     * @return A <code>Symbol</code> for the top level type declaration.
     */
    public static Symbol createTopLevelTypeSymbolNoErrors(String code) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Symbol symbol = createTopLevelTypeSymbol(code, creator);
        ensureNoErrors(symbol, creator);
        return symbol;
    }

    /**
     * Helper method to get a <code>Symbol</code> for a top level type declaration
     * directly from code.  Expects the given number of errors.
     * @param code The code for the top level type declaration.
     * @param expNumErrors The number of errors expected.
     */
    public static void createTopLevelTypeSymbolWithErrors(String code, int expNumErrors) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Symbol symbol = createTopLevelTypeSymbol(code, creator);
        expectError(symbol, creator, expNumErrors);
    }

    private static Symbol createTopLevelTypeSymbol(String code, ClassesSymbolCreator creator) {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser(code);
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTAnnotationList annList = classesParser.parseAnnotationList();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration(annList);
        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        return creator.createSymbolsForTopLevelTypeDeclaration(typeDecl, parent);
    }
}
