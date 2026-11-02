/*
 * @test
 * @summary com.sun.nio.file.ExtendedWatchEventModifier conformance: enum constants.
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

    public static void main(String[] a) throws Exception {
        com.sun.nio.file.ExtendedWatchEventModifier[] v = com.sun.nio.file.ExtendedWatchEventModifier.values();
        check(v.length == 1, "1 constant");
        check(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE.name().equals("FILE_TREE"), "FILE_TREE");
        check(com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE instanceof java.nio.file.WatchEvent.Modifier, "WatchEvent.Modifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
