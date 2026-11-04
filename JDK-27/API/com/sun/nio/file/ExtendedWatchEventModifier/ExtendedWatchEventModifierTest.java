/*
 * @test
 * @summary com.sun.nio.file.ExtendedWatchEventModifier conformance: white-box structure + black-box behavior.
 * @run main ExtendedWatchEventModifierTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedWatchEventModifierTest {
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
        Class<?> c = load("com.sun.nio.file.ExtendedWatchEventModifier");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(interfaces(c).contains("java.nio.file.WatchEvent$Modifier"), "implements WatchEvent.Modifier");
        check(fieldNames(c).contains("FILE_TREE"), "field FILE_TREE");

        com.sun.nio.file.ExtendedWatchEventModifier[] v = com.sun.nio.file.ExtendedWatchEventModifier.values();
        check(v.length == 1, "1 constant");
        check(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE.name().equals("FILE_TREE"), "FILE_TREE name");
        check(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE instanceof java.nio.file.WatchEvent.Modifier, "WatchEvent.Modifier instance");
        check(com.sun.nio.file.ExtendedWatchEventModifier.valueOf("FILE_TREE") == com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE, "valueOf identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.file.ExtendedWatchEventModifier mod = com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
        check(mod.name().equals("FILE_TREE"), "name()");
        check(mod.toString().equals("FILE_TREE"), "toString() == name");
        check(mod.ordinal() == 0, "ordinal 0");
        check(mod.getDeclaringClass() == com.sun.nio.file.ExtendedWatchEventModifier.class, "declaring class");
        check(mod.compareTo(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE) == 0, "compareTo self == 0");
        check(mod.equals(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE), "equals self");

        com.sun.nio.file.ExtendedWatchEventModifier[] a = com.sun.nio.file.ExtendedWatchEventModifier.values();
        com.sun.nio.file.ExtendedWatchEventModifier[] b = com.sun.nio.file.ExtendedWatchEventModifier.values();
        check(a != b, "values() returns a fresh array");
        check(Arrays.equals(a, b), "values() same contents");

        boolean threw = false;
        try { com.sun.nio.file.ExtendedWatchEventModifier.valueOf("FILE"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf bad name -> IllegalArgumentException");

        threw = false;
        try { com.sun.nio.file.ExtendedWatchEventModifier.valueOf(null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "valueOf null -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
