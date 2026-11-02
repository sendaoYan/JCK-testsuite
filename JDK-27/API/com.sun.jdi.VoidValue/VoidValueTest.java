/*
 * @test
 * @summary com.sun.jdi.VoidValue conformance: interface hierarchy and methods.
 * @run main VoidValueTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VoidValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.VoidValue");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Value"), "extends com.sun.jdi.Value");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
