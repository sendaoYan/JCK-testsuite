/*
 * @test
 * @summary com.sun.management.VMOption.Origin conformance: enum constants.
 * @run main OriginTest
 */

import java.lang.reflect.*;
import java.util.*;

public class OriginTest {
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
        com.sun.management.VMOption.Origin[] v = com.sun.management.VMOption.Origin.values();
        check(v.length == 8, "8 constants");
        check(com.sun.management.VMOption.Origin.DEFAULT.name().equals("DEFAULT"), "DEFAULT");
        check(com.sun.management.VMOption.Origin.VM_CREATION.name().equals("VM_CREATION"), "VM_CREATION");
        check(com.sun.management.VMOption.Origin.valueOf("ERGONOMIC") == com.sun.management.VMOption.Origin.ERGONOMIC, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
