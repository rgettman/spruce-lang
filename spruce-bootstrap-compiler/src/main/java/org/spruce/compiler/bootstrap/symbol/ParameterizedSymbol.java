package org.spruce.compiler.bootstrap.symbol;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>ParameterizedSymbol</code> is a <code>ParentSymbol</code> that keeps
 * track of the order of any child symbols that are parameters.
 */
public class ParameterizedSymbol extends ParentSymbol {
    private final List<Symbol> myParameters;

    /**
     * Constructs a <code>ParameterizedSymbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param loc The <code>Location</code>.
     * @param name The name of this symbol.
     * @param kind The <code>Type</code> of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public ParameterizedSymbol(Location loc, String name, Kind kind, SymbolTable parent, long flags) {
        super(loc, name, kind, parent, flags);
        myParameters = new ArrayList<>();
    }

    /**
     * Adds the given <code>Symbol</code> representing a Parameter to the
     * parameter list.
     * @param paramSymbol A <code>Symbol</code> of type <code>PARAMETER</code>.
     */
    public void addParameter(Symbol paramSymbol) {
        myParameters.add(paramSymbol);
    }

    /**
     * Returns the <code>List</code> of <code>Symbol</code>s representing the
     * parameters.
     * @return A <code>List</code> of <code>Symbol</code>s.
     */
    public List<Symbol> getParameters() {
        return myParameters;
    }

    /**
     * Returns how many parameters exist in this parameterized symbol.
     * @return How many parameters exist in this parameterized symbol.
     */
    public int numParameters() {
        return  myParameters.size();
    }

    /**
     * Helper method to create a string representation of this symbol.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this symbol.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        String newPrefix = prefix + (isTail ? "    " : "|   ");

        buf.append(super.toString(prefix, isTail));
        buf.append(newPrefix).append(numParameters()).append(" parameters:\n");
        int i = 0;
        for (Symbol param : myParameters) {
            buf.append(param.toString(newPrefix, (i == numParameters() - 1)));
            i++;
        }
        return buf.toString();
    }
}
