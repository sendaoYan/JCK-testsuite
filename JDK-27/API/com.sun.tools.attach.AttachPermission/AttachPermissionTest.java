/*
 * @test
 * @summary com.sun.tools.attach.AttachPermission conformance: permission.
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

    public static void main(String[] a) throws Exception {
        com.sun.tools.attach.AttachPermission p = new com.sun.tools.attach.AttachPermission("attachVirtualMachine");
        check(p.getName().equals("attachVirtualMachine"), "name");
        check(p instanceof java.security.BasicPermission, "extends BasicPermission");
        new com.sun.tools.attach.AttachPermission("attachVirtualMachine", "");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
