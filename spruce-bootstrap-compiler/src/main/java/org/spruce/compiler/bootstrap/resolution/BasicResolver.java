package org.spruce.compiler.bootstrap.resolution;

import java.util.List;

import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * A <code>BasicResolver</code> provides basic symbol resolution functionality.
 * Subclasses represent resolvers of various categories of AST elements and
 * can obtain references to each other for resolving data types and expression
 * types outside their category, using the <code>Resolver</code> class.
 */
public class BasicResolver {
    private final Resolver myResolver;
    private final MessageProducer myMsgProducer;
    private final GlobalLookup myGlobalLookup;

    /**
     * Constructs a <code>BasicResolver</code> using a
     * <code>MessageProducer</code>, referring to an <code>Resolver</code>.
     *
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public BasicResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        myResolver = resolver;
        myMsgProducer = msgProducer;
        myGlobalLookup = global;
    }

    /**
     * Returns the <code>TopLevelResolver</code>.
     * @return The <code>TopLevelResolver</code>.
     */
    public TopLevelResolver getTopLevelResolver() {
        return myResolver.getTopLevelResolver();
    }

    /**
     * Returns the <code>ClassesResolver</code>.
     * @return The <code>ClassesResolver</code>.
     */
    public ClassesResolver getClassesResolver() {
        return myResolver.getClassesResolver();
    }

    /**
     * Returns the <code>LiteralsResolver</code>.
     * @return The <code>LiteralsResolver</code>.
     */
    public LiteralsResolver getLiteralsResolver() {
        return myResolver.getLiteralsResolver();
    }

    /**
     * Returns the <code>NamesResolver</code>.
     * @return The <code>NamesResolver</code>.
     */
    public NamesResolver getNamesResolver() {
        return myResolver.getNamesResolver();
    }

    /**
     * Returns the <code>StatementsResolver</code>.
     * @return The <code>StatementsResolver</code>.
     */
    public StatementsResolver getStatementsResolver() {
        return myResolver.getStatementsResolver();
    }
    /**
     * Returns the <code>ExpressionsResolver</code>.
     * @return The <code>ExpressionsResolver</code>.
     */
    public ExpressionsResolver getExpressionsResolver() {
        return myResolver.getExpressionsResolver();
    }

    /**
     * Returns the <code>TypesResolver</code>.
     * @return The <code>TypesResolver</code>.
     */
    public TypesResolver getTypesResolver() {
        return myResolver.getTypesResolver();
    }

    /**
     * Returns the <code>ConstructorsResolver</code>.
     * @return The <code>ConstructorsResolver</code>.
     */
    public ConstructorsResolver getConstructorsResolver() {
        return myResolver.getConstructorsResolver();
    }

    /**
     * Returns the <code>MethodsResolver</code>.
     * @return The <code>MethodsResolver</code>.
     */
    public MethodsResolver getMethodsResolver() {
        return myResolver.getMethodsResolver();
    }

    /**
     * Returns the <code>OperatorsResolver</code>.
     * @return The <code>OperatorsResolver</code>.
     */
    public OperatorsResolver getOperatorsResolver() {
        return myResolver.getOperatorsResolver();
    }

    /**
     * Returns the <code>CiceResolver</code>.
     * @return The <code>CiceResolver</code>.
     */
    public CiceResolver getCiceResolver() {
        return myResolver.getCiceResolver();
    }

    /**
     * Returns the <code>GlobalLookup</code>.
     * @return The <code>GlobalLookup</code>.
     */
    public GlobalLookup getGlobalLookup() {
        return myGlobalLookup;
    }

    /**
     * Creates a <code>CompilerMessage</code> of type <code>ERROR</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void error(Location loc, String msg) {
        myMsgProducer.error(loc, msg);
    }

    /**
     * Creates a <code>CompilerMessage</code> of type <code>WARNING</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void warn(Location loc, String msg) {
        myMsgProducer.error(loc, msg);
    }

    /**
     * Creates a <code>CompilerMessage</code> of type <code>NOTE</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void note(Location loc, String msg) {
        myMsgProducer.note(loc, msg);
    }

    /**
     * Helper method to produce an error message of symbol not found.
     * @param loc The <code>Location</code>.
     * @param symbolName The symbol name that wasn't found.
     */
    public void errorSymbolNotFound(Location loc, String symbolName) {
        error(loc, "Symbol '" + symbolName + "' not found.");
    }

    /**
     * Helper method to produce an error message of mismatched types.
     * @param loc The <code>Location</code>.
     * @param targetTypeName The target type name.
     * @param foundTypeName The found type name.
     */
    public void errorTypeMismatch(Location loc, String targetTypeName, String foundTypeName) {
        error(loc, "Required type: " + targetTypeName + ", found: " + foundTypeName);
    }

    /**
     * Determines whether the given methods are override-equivalent.
     * Override-equivalent means that the method names are the same, the number
     * of parameters is the same, and the formal parameters are the same by
     * position.
     * @param first A <code>ParameterizedSymbol</code> representing the first method.
     * @param second A <code>ParameterizedSymbol</code> representing the second method.
     * @return Whether the given methods are override-equivalent.
     */
    public boolean isOverrideEquivalent(ParameterizedSymbol first, ParameterizedSymbol second) {
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        List<VariableSymbol> firstParams = first.getParameters();
        List<VariableSymbol> secondParams = second.getParameters();
        if (firstParams.size() != secondParams.size()) {
            return false;
        }
        for (int i = 0; i < firstParams.size(); i++) {
            if (firstParams.get(i).getDataType() != secondParams.get(i).getDataType()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Can there be an identity conversion from the source type to the target
     * type?
     * @param source A <code>TypeSymbol</code> representing the source type.
     * @param target A <code>TypeSymbol</code> representing the target type.
     * @return Whether there is an identity conversion from the source type to
     *     the target type.
     */
    public boolean canConvertIdentity(TypeSymbol source, TypeSymbol target) {
        return source == target;
    }

    /**
     * Can there be a widening reference conversion from the source type to the
     * target type?  Yes, if the source is a subtype of the target.
     * @param source A <code>TypeSymbol</code> representing the source type.
     * @param target A <code>TypeSymbol</code> representing the target type.
     * @return Whether there is a widening reference conversion from the source
     *     type to the target type.
     */
    public boolean canConvertWideningReference(TypeSymbol source, TypeSymbol target) {
        return source.isSubTypeOf(target);
    }

    /**
     * <p>Can there be a narrowing reference conversion from the source type to the
     * target type?  Yes, in the following circumstances:</p>
     * <ul>
     * <li>The source is not a subtype of the target.</li>
     * <li>The two types are not disjoint.</li>
     * <li>Not supported by Spruce yet, but if the types each have parameterized
     * supertypes whose erasures are provably distinct.  Disallows conversion:
     * <code>ArrayList&lt;String&gt;</code> to <code>ArrayList&lt;Any&gt;</code>.
     * </li>
     * </ul>
     * <p>There are other criteria beside disjointedness that are not
     * supportable by Spruce yet, but can substitute for non-disjointedness to
     * allow the conversion:</p>
     * <ul>
     * <li>The target is an array type and the source is the root type or any
     * interfaces implemented by arrays.</li>
     * <li>The types are arrays and the source element type has a narrowing
     * conversion to the target element type.</li>
     * <li>The source is a type variable, and there is a narrowing reference
     * conversion from the upper bound of the source to the type.</li>
     * <li>The target is a type variable, and there is a widening or narrowing
     * conversion from the source to the upper bound of the target.</li>
     * </ul>
     * @param source A <code>TypeSymbol</code> representing the source type.
     * @param target A <code>TypeSymbol</code> representing the target type.
     * @return Whether there is a narrowing reference conversion from the source
     *     type to the target type.
     * @see #areDisjoint(TypeSymbol, TypeSymbol)
     */
    public boolean canConvertNarrowingReference(TypeSymbol source, TypeSymbol target) {
        return !source.isSubTypeOf(target) && !areDisjoint(source, target);
    }

    /**
     * <p>Are the two given types disjoint?  There are if:</p>
     * <ul>
     * <li>Both are classes and one is not a subtype of the other.</li>
     * </ul>
     * <p>Other cases are not yet possible because Spruce doesn't support them
     * yet:</p>
     * <ul>
     * <li>One is a class and the other is an interface, the class is not a
     * subtype of the interface, and the class is either final, or one or both
     * are sealed and all the permitted direct subtypes are also disjoint.</li>
     * <li>Both are interfaces, neither are subtypes of the other, and one or both
     * are sealed and all the permitted direct subtypes are also disjoint.</li>
     * </ul>
     * @param a A <code>TypeSymbol</code>.
     * @param b Another <code>TypeSymbol</code>.
     * @return Returns whether the given types are disjoint.
     */
    public boolean areDisjoint(TypeSymbol a, TypeSymbol b) {
        return a.isClass() && b.isClass() && !a.isSubTypeOf(b) && !b.isSubTypeOf(a);
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }

    /**
     * A helper method that creates and returns, but does not throw, an
     * <code>IllegalStateException</code> with the given error message.
     * @param message The error message.
     * @return An <code>IllegalStateException</code>.
     */
    protected IllegalStateException internalError(String message) {
        return new IllegalStateException("Internal error: " + message);
    }
}
