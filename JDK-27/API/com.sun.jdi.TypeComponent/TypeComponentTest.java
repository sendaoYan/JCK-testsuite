/*
 * @test
 * @summary com.sun.jdi.TypeComponent conformance: interface hierarchy and methods.
 * @run main TypeComponentTest
 */

import java.lang.reflect.*;
import java.util.*;

public class TypeComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.TypeComponent");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.contains("com.sun.jdi.Accessible"), "extends com.sun.jdi.Accessible");
        check(si.size() == 2, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("signature"), "method signature");
        check(dm.contains("genericSignature"), "method genericSignature");
        check(dm.contains("declaringType"), "method declaringType");
        check(dm.contains("isStatic"), "method isStatic");
        check(dm.contains("isFinal"), "method isFinal");
        check(dm.contains("isSynthetic"), "method isSynthetic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
