/*
 * @test
 * @summary com.sun.nio.file.SensitivityWatchEventModifier conformance: enum constants and sensitivity.
 * @run main SensitivityWatchEventModifierTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SensitivityWatchEventModifierTest {
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
        com.sun.nio.file.SensitivityWatchEventModifier[] v = com.sun.nio.file.SensitivityWatchEventModifier.values();
        check(v.length == 3, "3 constants");
        check(com.sun.nio.file.SensitivityWatchEventModifier.HIGH.sensitivityValueInSeconds() == 2, "HIGH 2s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.MEDIUM.sensitivityValueInSeconds() == 10, "MEDIUM 10s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.LOW.sensitivityValueInSeconds() == 30, "LOW 30s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.valueOf("HIGH") == com.sun.nio.file.SensitivityWatchEventModifier.HIGH, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
