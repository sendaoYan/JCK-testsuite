/*
 * @test
 * @summary com.sun.nio.file.ExtendedCopyOption conformance: enum constants.
 * @run main ExtendedCopyOptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedCopyOptionTest {
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
        com.sun.nio.file.ExtendedCopyOption[] v = com.sun.nio.file.ExtendedCopyOption.values();
        check(v.length == 1, "1 constant");
        check(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE.name().equals("INTERRUPTIBLE"), "INTERRUPTIBLE");
        check(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE instanceof java.nio.file.CopyOption, "CopyOption");
        check(com.sun.nio.file.ExtendedCopyOption.valueOf("INTERRUPTIBLE") == com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
