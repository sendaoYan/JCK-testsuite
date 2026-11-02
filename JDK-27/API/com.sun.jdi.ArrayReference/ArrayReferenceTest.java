/*
 * @test
 * @summary com.sun.jdi.ArrayReference conformance: interface hierarchy and methods.
 * @run main ArrayReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ArrayReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ArrayReference");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.ObjectReference"), "extends com.sun.jdi.ObjectReference");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("length"), "method length");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("setValues"), "method setValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
