/*
 * @test
 * @summary com.sun.jdi.JDIPermission conformance: white-box structure + black-box behavior.
 * @run main JDIPermissionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class JDIPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.JDIPermission");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getDeclaredConstructors().length == 2, "constructor count");
        Set<String> dm = methodNames(c);
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JDIPermission p = new JDIPermission("virtualMachineManager");
        check("virtualMachineManager".equals(p.getName()), "name");
        check(p.getActions().length() == 0, "empty actions");
        JDIPermission p2 = new JDIPermission("virtualMachineManager");
        check(p.equals(p2), "equals");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        check(p.implies(p2), "implies");
        boolean threw = false;
        try { new JDIPermission("bad", "read"); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "IllegalArgumentException on non-empty actions");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
