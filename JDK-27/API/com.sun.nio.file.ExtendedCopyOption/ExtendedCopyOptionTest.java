/*
 * @test
 * @summary com.sun.nio.file.ExtendedCopyOption conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.file.ExtendedCopyOption");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(interfaces(c).contains("java.nio.file.CopyOption"), "implements CopyOption");
        check(fieldNames(c).contains("INTERRUPTIBLE"), "field INTERRUPTIBLE");

        com.sun.nio.file.ExtendedCopyOption[] v = com.sun.nio.file.ExtendedCopyOption.values();
        check(v.length == 1, "1 constant");
        check(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE.name().equals("INTERRUPTIBLE"), "INTERRUPTIBLE name");
        check(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE instanceof java.nio.file.CopyOption, "CopyOption instance");
        check(com.sun.nio.file.ExtendedCopyOption.valueOf("INTERRUPTIBLE") == com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE, "valueOf identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.file.ExtendedCopyOption opt = com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE;
        check(opt.name().equals("INTERRUPTIBLE"), "name()");
        check(opt.toString().equals("INTERRUPTIBLE"), "toString() == name");
        check(opt.ordinal() == 0, "ordinal 0");
        check(opt.getDeclaringClass() == com.sun.nio.file.ExtendedCopyOption.class, "declaring class");
        check(opt.compareTo(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE) == 0, "compareTo self == 0");
        check(opt.equals(com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE), "equals self");
        check(opt.hashCode() == com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE.hashCode(), "hashCode stable");

        com.sun.nio.file.ExtendedCopyOption[] a = com.sun.nio.file.ExtendedCopyOption.values();
        com.sun.nio.file.ExtendedCopyOption[] b = com.sun.nio.file.ExtendedCopyOption.values();
        check(a != b, "values() returns a fresh array");
        check(Arrays.equals(a, b), "values() same contents");
        check(Arrays.asList(a).equals(Arrays.asList(com.sun.nio.file.ExtendedCopyOption.values())), "values content stable");

        boolean threw = false;
        try { com.sun.nio.file.ExtendedCopyOption.valueOf("NOPE"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf bad name -> IllegalArgumentException");

        threw = false;
        try { com.sun.nio.file.ExtendedCopyOption.valueOf(null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "valueOf null -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
