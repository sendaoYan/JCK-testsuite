/*
 * @test
 * @summary com.sun.jdi.ArrayType conformance: interface hierarchy and methods.
 * @run main ArrayTypeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ArrayTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ArrayType");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.ReferenceType"), "extends com.sun.jdi.ReferenceType");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("newInstance"), "method newInstance");
        check(dm.contains("componentSignature"), "method componentSignature");
        check(dm.contains("componentTypeName"), "method componentTypeName");
        check(dm.contains("componentType"), "method componentType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
