/*
 * @test
 * @summary java.util.Enumeration API conformance: hasMoreElements/nextElement, and
 *          Vector.elements.
 * @run main EnumerationTest
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class EnumerationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEnumeration() {
        Enumeration<String> e = Collections.enumeration(Arrays.asList("a", "b", "c"));
        check(e.hasMoreElements(), "hasMoreElements");
        check("a".equals(e.nextElement()), "nextElement 1");
        check("b".equals(e.nextElement()), "nextElement 2");
        check("c".equals(e.nextElement()) && !e.hasMoreElements(), "nextElement 3 / exhausted");
        Enumeration<String> ve = new Vector<>(Arrays.asList("x")).elements();
        check("x".equals(ve.nextElement()), "Vector.elements");
    }

    public static void main(String[] args) {
        testEnumeration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
