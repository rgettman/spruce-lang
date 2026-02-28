package org.spruce.compiler.bootstrap.test.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Utility methods for all tests, regardless of compiler phase.  No test entry points.
 */
public class TestUtility {
    /**
     * Fails the test if the given node is <em>not</em> an instance of the
     * given class.  If it is, this method casts the given node to the given
     * class.
     * @param obj An object.
     * @param nodeClass The node should be an instance of this <code>Class</code>.
     * @return The node, down-casted to an instance of the given class.
     * @param <T> The type to cast to.
     */
    public static <T> T ensureIsa(Object obj, Class<T> nodeClass) {
        assertInstanceOf(nodeClass, obj);
        return nodeClass.cast(obj);
    }
}
