/*
 * @test
 * @summary java.util.RandomAccess conformance: white-box structure + black-box behavior.
 * @enablePreview
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.RandomAccess");
        check(c.isInterface(), "is an interface");
        check(c.getDeclaredMethods().length == 0, "marker interface, no members");
    }

    static void testBlackBox() {
        check(new ArrayList<>() instanceof RandomAccess, "ArrayList is RandomAccess");
        check(!(new LinkedList<>() instanceof RandomAccess), "LinkedList is not RandomAccess");
        check(RandomAccess.class.isAssignableFrom(ArrayList.class), "assignable from ArrayList");
    }

    // ---- deepened: marker semantics across implementations, views, factories ----
    static void testMarkerMatrix() {
        check(new java.util.Vector<>() instanceof RandomAccess, "Vector is RandomAccess");
        check(new java.util.Stack<>() instanceof RandomAccess, "Stack is RandomAccess");
        check(java.util.Arrays.asList("a") instanceof RandomAccess, "Arrays.asList is RandomAccess");
        check(java.util.List.of("a") instanceof RandomAccess, "List.of is RandomAccess");
        check(java.util.List.of() instanceof RandomAccess, "List.of() is RandomAccess");
        check(java.util.List.ofLazy(1, i -> "x") instanceof RandomAccess, "List.ofLazy is RandomAccess");
        check(java.util.List.copyOf(java.util.Arrays.asList("a")) instanceof RandomAccess,
              "List.copyOf is RandomAccess");
        check(java.util.Collections.emptyList() instanceof RandomAccess, "emptyList is RandomAccess");
        check(java.util.Collections.singletonList("a") instanceof RandomAccess,
              "singletonList is RandomAccess");
        check(java.util.Collections.nCopies(3, "a") instanceof RandomAccess, "nCopies is RandomAccess");
        check(java.util.Collections.unmodifiableList(new ArrayList<>()) instanceof RandomAccess,
              "unmodifiableList preserves RandomAccess");
        check(java.util.Collections.synchronizedList(new ArrayList<>()) instanceof RandomAccess,
              "synchronizedList preserves RandomAccess");
        check(!(new LinkedList<>().subList(0, 0) instanceof RandomAccess),
              "LinkedList subList not RandomAccess");
        check(new ArrayList<>(java.util.Arrays.asList(1)).subList(0, 1) instanceof RandomAccess,
              "ArrayList subList keeps RandomAccess");
        check(!(java.util.Collections.unmodifiableList(new LinkedList<>()) instanceof RandomAccess),
              "unmodifiableList of LinkedList not RandomAccess");
        check(!(java.util.Collections.synchronizedList(new LinkedList<>()) instanceof RandomAccess),
              "synchronizedList of LinkedList not RandomAccess");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
