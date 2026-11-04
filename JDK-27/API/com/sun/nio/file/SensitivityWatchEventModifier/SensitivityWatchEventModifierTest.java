/*
 * @test
 * @summary com.sun.nio.file.SensitivityWatchEventModifier conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.file.SensitivityWatchEventModifier");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(interfaces(c).contains("java.nio.file.WatchEvent$Modifier"), "implements WatchEvent.Modifier");
        check(methodNames(c).contains("sensitivityValueInSeconds"), "sensitivityValueInSeconds method");

        Set<String> fs = fieldNames(c);
        check(fs.contains("HIGH"), "field HIGH");
        check(fs.contains("MEDIUM"), "field MEDIUM");
        check(fs.contains("LOW"), "field LOW");

        com.sun.nio.file.SensitivityWatchEventModifier[] v = com.sun.nio.file.SensitivityWatchEventModifier.values();
        check(v.length == 3, "3 constants");
        check(com.sun.nio.file.SensitivityWatchEventModifier.HIGH.sensitivityValueInSeconds() == 2, "HIGH 2s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.MEDIUM.sensitivityValueInSeconds() == 10, "MEDIUM 10s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.LOW.sensitivityValueInSeconds() == 30, "LOW 30s");
        check(com.sun.nio.file.SensitivityWatchEventModifier.valueOf("HIGH") == com.sun.nio.file.SensitivityWatchEventModifier.HIGH, "valueOf identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.file.SensitivityWatchEventModifier high = com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
        com.sun.nio.file.SensitivityWatchEventModifier medium = com.sun.nio.file.SensitivityWatchEventModifier.MEDIUM;
        com.sun.nio.file.SensitivityWatchEventModifier low = com.sun.nio.file.SensitivityWatchEventModifier.LOW;

        com.sun.nio.file.SensitivityWatchEventModifier[] expected = { high, medium, low };
        check(Arrays.equals(com.sun.nio.file.SensitivityWatchEventModifier.values(), expected), "values() declaration order");

        check(high.ordinal() == 0 && medium.ordinal() == 1 && low.ordinal() == 2, "ordinals 0/1/2");
        check(high.sensitivityValueInSeconds() < medium.sensitivityValueInSeconds(), "HIGH < MEDIUM seconds");
        check(medium.sensitivityValueInSeconds() < low.sensitivityValueInSeconds(), "MEDIUM < LOW seconds");
        check(high.sensitivityValueInSeconds() < low.sensitivityValueInSeconds(), "HIGH < LOW seconds");

        for (com.sun.nio.file.SensitivityWatchEventModifier m : expected) {
            check(m.name().equals(m.toString()), m.name() + " toString == name");
            check(m.getDeclaringClass() == com.sun.nio.file.SensitivityWatchEventModifier.class, m.name() + " declaring class");
            check(m instanceof java.nio.file.WatchEvent.Modifier, m.name() + " instanceof Modifier");
            check(com.sun.nio.file.SensitivityWatchEventModifier.valueOf(m.name()) == m, "valueOf(" + m.name() + ") identity");
        }
        check(high.compareTo(medium) < 0, "compareTo HIGH < MEDIUM");
        check(low.compareTo(high) > 0, "compareTo LOW > HIGH");

        boolean threw = false;
        try { com.sun.nio.file.SensitivityWatchEventModifier.valueOf("HIGHEST"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf bad name -> IllegalArgumentException");

        threw = false;
        try { com.sun.nio.file.SensitivityWatchEventModifier.valueOf(null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "valueOf null -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
