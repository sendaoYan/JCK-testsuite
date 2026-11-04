/*
 * @test
 * @summary java.util.RandomAccess marker interface conformance: ArrayList implements it,
 *          LinkedList does not.
 * @run main RandomAccessTest
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.RandomAccess;

public class RandomAccessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRandomAccess() {
        check(new ArrayList<>() instanceof RandomAccess, "ArrayList is RandomAccess");
        check(!(new LinkedList<>() instanceof RandomAccess), "LinkedList is not RandomAccess");
    }

    // ---- deepened: marker semantics across implementations and views ----------
    static void testMarkerMatrix() {
        // List implementations with O(1) get(i)
        check(new java.util.Vector<>() instanceof RandomAccess, "Vector is RandomAccess");
        check(new java.util.Stack<>() instanceof RandomAccess, "Stack (extends Vector) is RandomAccess");
        check(java.util.Arrays.asList("a") instanceof RandomAccess, "Arrays.asList is RandomAccess");
        check(java.util.Collections.emptyList() instanceof RandomAccess,
              "Collections.emptyList is RandomAccess");
        check(java.util.Collections.singletonList("a") instanceof RandomAccess,
              "singletonList is RandomAccess");
        check(java.util.Collections.nCopies(3, "a") instanceof RandomAccess,
              "nCopies is RandomAccess");
        check(java.util.Collections.unmodifiableList(new ArrayList<>()) instanceof RandomAccess,
              "unmodifiableList preserves RandomAccess");
        check(java.util.Collections.synchronizedList(new ArrayList<>()) instanceof RandomAccess,
              "synchronizedList preserves RandomAccess");
        // sequential implementations are NOT RandomAccess
        Object sequential = new java.util.AbstractSequentialList<String>() {
            public java.util.ListIterator<String> listIterator(int i) {
                throw new UnsupportedOperationException();
            }
            public int size() { return 0; }
        };
        check(!(sequential instanceof RandomAccess), "AbstractSequentialList subclass not RandomAccess");
        check(!(new LinkedList<>().subList(0, 0) instanceof RandomAccess),
              "LinkedList subList not RandomAccess");
        // views keep the marker of their backing implementation
        check(new ArrayList<>(java.util.Arrays.asList(1)).subList(0, 1) instanceof RandomAccess,
              "ArrayList subList keeps RandomAccess");
        check(!(java.util.Collections.unmodifiableList(new LinkedList<>()) instanceof RandomAccess),
              "unmodifiableList of LinkedList not RandomAccess");
        check(!(java.util.Collections.synchronizedList(new LinkedList<>()) instanceof RandomAccess),
              "synchronizedList of LinkedList not RandomAccess");
    }

    public static void main(String[] args) {
        testRandomAccess();
        testMarkerMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
