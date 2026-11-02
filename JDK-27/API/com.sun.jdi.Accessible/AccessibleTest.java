/*
 * @test
 * @summary com.sun.jdi.Accessible conformance: interface hierarchy and methods.
 * @run main AccessibleTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AccessibleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.Accessible");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 0, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("modifiers"), "method modifiers");
        check(dm.contains("isPrivate"), "method isPrivate");
        check(dm.contains("isPackagePrivate"), "method isPackagePrivate");
        check(dm.contains("isProtected"), "method isProtected");
        check(dm.contains("isPublic"), "method isPublic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
