package org.spruce.compiler.bootstrap.resolution;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

/**
 * A <code>Resolver</code> resolves all symbols for data types and expression
 * types in the ASTs and symbol tables.  Examples of such errors are symbol not
 * found errors, duplicate simple names in use statements, name clash for a
 * namespace name and a type name, and namespace found when type expected. 
 * All other compiler errors will be detected in the semantic analysis phase,
 * including duplicate class names, unrecognized identifiers, type mismatches,
 * class circularity, local variable not initialized before use, if/for/while
 * conditions aren't type boolean, etc.  There are resolvers for top-level,
 * type-level, name-level, statement-level, and expression-level resolution.
 */
public class Resolver {
    private final TopLevelResolver myTopLevelResolver;
    private final ClassesResolver myClassesResolver;
    private final StatementsResolver myStatementsResolver;
    private final ExpressionsResolver myExpressionsResolver;
    private final TypesResolver myTypesResolver;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs an <code>Resolver</code> with the given
     * <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The global <code>TypeLookup</code>.
     */
    public Resolver(MessageProducer msgProducer, TypeLookup global) {
        myTopLevelResolver = new TopLevelResolver(this, msgProducer, global);
        myClassesResolver = new ClassesResolver(this, msgProducer, global);
        myStatementsResolver = new StatementsResolver(this, msgProducer, global);
        myExpressionsResolver = new ExpressionsResolver(this, msgProducer, global);
        myTypesResolver = new TypesResolver(this, msgProducer, global);

        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>TopLevelResolver</code>.
     * @return The <code>TopLevelResolver</code>.
     */
    public TopLevelResolver getTopLevelResolver() {
        return myTopLevelResolver;
    }

    /**
     * Returns the <code>ClassesResolver</code>.
     * @return The <code>ClassesResolver</code>.
     */
    public ClassesResolver getClassesResolver() {
        return myClassesResolver;
    }

    /**
     * Returns the <code>StatementsResolver</code>.
     * @return The <code>StatementsResolver</code>.
     */
    public StatementsResolver getStatementsResolver() {
        return myStatementsResolver;
    }

    /**
     * Returns the <code>ExpressionsResolver</code>.
     * @return The <code>ExpressionsResolver</code>.
     */
    public ExpressionsResolver getExpressionsResolver() {
        return myExpressionsResolver;
    }

    /**
     * Returns the <code>TypesResolver</code>.
     * @return The <code>TypesResolver</code>.
     */
    public TypesResolver getTypesResolver() {
        return myTypesResolver;
    }

    /**
     * Resolve all data types and expression types first.  Then analyze all <code>OrdinaryCompilationUnit</code>s along with the global
     * <code>TypeLookup</code>.
     * @param units A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void analyze(List<ASTOrdinaryCompilationUnit> units) {
        getTopLevelResolver().resolveOrdinaryCompilationUnits(units);
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }
}
