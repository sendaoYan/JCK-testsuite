/*
 * @test
 * @summary com.sun.jdi.DoubleValue conformance: interface hierarchy and methods.
 * @run main DoubleValueTest
 */

import java.lang.reflect.*;
import java.util.*;

public class DoubleValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.DoubleValue");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.PrimitiveValue"), "extends com.sun.jdi.PrimitiveValue");
        check(si.contains("java.lang.Comparable"), "extends java.lang.Comparable");
        check(si.size() == 2, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("value"), "method value");
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
