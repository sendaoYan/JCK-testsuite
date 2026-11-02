/*
 * @test
 * @summary com.sun.management.VMOption conformance: option bean accessors.
 * @run main VMOptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VMOptionTest {
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
        Class<?> c = load("com.sun.management.VMOption");
        check(!c.isInterface(), "not an interface");
        com.sun.management.VMOption o = new com.sun.management.VMOption("PrintGC", "false", true, com.sun.management.VMOption.Origin.DEFAULT);
        check(o.getName().equals("PrintGC"), "name");
        check(o.getValue().equals("false"), "value");
        check(o.isWriteable(), "writeable");
        check(o.getOrigin() == com.sun.management.VMOption.Origin.DEFAULT, "origin");
        check(o.toString() != null && o.toString().contains("PrintGC"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
