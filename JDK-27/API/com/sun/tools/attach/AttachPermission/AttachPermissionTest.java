/*
 * @test
 * @summary com.sun.tools.attach.AttachPermission conformance: white-box structure + black-box behavior.
 * @run main AttachPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AttachPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        com.sun.tools.attach.AttachPermission p = new com.sun.tools.attach.AttachPermission("attachVirtualMachine");
        check(p.getName().equals("attachVirtualMachine"), "name");
        check(p instanceof java.security.BasicPermission, "extends BasicPermission");
        new com.sun.tools.attach.AttachPermission("attachVirtualMachine", "");
    }

    static void testBlackBox() throws Exception {
        com.sun.tools.attach.AttachPermission p = new com.sun.tools.attach.AttachPermission("attachVirtualMachine");
        check(p.getName().equals("attachVirtualMachine"), "getName");
        check(p.getActions().equals(""), "getActions empty");
        com.sun.tools.attach.AttachPermission p2 = new com.sun.tools.attach.AttachPermission("attachVirtualMachine");
        check(p.equals(p2), "equals");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        check(p.implies(p2), "implies");
        new com.sun.tools.attach.AttachPermission("attachVirtualMachine", "");
        check(p instanceof java.security.BasicPermission, "extends BasicPermission");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
