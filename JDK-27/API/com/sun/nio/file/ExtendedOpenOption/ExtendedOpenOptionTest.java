/*
 * @test
 * @summary com.sun.nio.file.ExtendedOpenOption conformance: white-box structure + black-box behavior.
 * @run main ExtendedOpenOptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedOpenOptionTest {
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
        Class<?> c = load("com.sun.nio.file.ExtendedOpenOption");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(interfaces(c).contains("java.nio.file.OpenOption"), "implements OpenOption");

        Set<String> fs = fieldNames(c);
        check(fs.contains("NOSHARE_READ"), "field NOSHARE_READ");
        check(fs.contains("NOSHARE_WRITE"), "field NOSHARE_WRITE");
        check(fs.contains("NOSHARE_DELETE"), "field NOSHARE_DELETE");
        check(fs.contains("DIRECT"), "field DIRECT");

        com.sun.nio.file.ExtendedOpenOption[] v = com.sun.nio.file.ExtendedOpenOption.values();
        check(v.length == 4, "4 constants");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_READ.name().equals("NOSHARE_READ"), "NOSHARE_READ name");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_WRITE.name().equals("NOSHARE_WRITE"), "NOSHARE_WRITE name");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_DELETE.name().equals("NOSHARE_DELETE"), "NOSHARE_DELETE name");
        check(com.sun.nio.file.ExtendedOpenOption.DIRECT.name().equals("DIRECT"), "DIRECT name");
        check(com.sun.nio.file.ExtendedOpenOption.DIRECT instanceof java.nio.file.OpenOption, "OpenOption instance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.file.ExtendedOpenOption[] expected = {
            com.sun.nio.file.ExtendedOpenOption.NOSHARE_READ,
            com.sun.nio.file.ExtendedOpenOption.NOSHARE_WRITE,
            com.sun.nio.file.ExtendedOpenOption.NOSHARE_DELETE,
            com.sun.nio.file.ExtendedOpenOption.DIRECT,
        };
        com.sun.nio.file.ExtendedOpenOption[] v = com.sun.nio.file.ExtendedOpenOption.values();
        check(Arrays.equals(v, expected), "values() declaration order");

        for (int i = 0; i < expected.length; i++) {
            check(expected[i].ordinal() == i, expected[i].name() + " ordinal == " + i);
            check(expected[i].name().equals(expected[i].toString()), expected[i].name() + " toString == name");
            check(expected[i].getDeclaringClass() == com.sun.nio.file.ExtendedOpenOption.class, expected[i].name() + " declaring class");
            check(expected[i] instanceof java.nio.file.OpenOption, expected[i].name() + " instanceof OpenOption");
            check(com.sun.nio.file.ExtendedOpenOption.valueOf(expected[i].name()) == expected[i], "valueOf(" + expected[i].name() + ") identity");
        }

        check(v != com.sun.nio.file.ExtendedOpenOption.values(), "values() returns a fresh array");
        check(v[0].compareTo(v[1]) < 0, "compareTo ascending order");
        check(v[3].compareTo(v[0]) > 0, "compareTo descending order");

        boolean threw = false;
        try { com.sun.nio.file.ExtendedOpenOption.valueOf("DIRECT2"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf bad name -> IllegalArgumentException");

        threw = false;
        try { com.sun.nio.file.ExtendedOpenOption.valueOf(null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "valueOf null -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
