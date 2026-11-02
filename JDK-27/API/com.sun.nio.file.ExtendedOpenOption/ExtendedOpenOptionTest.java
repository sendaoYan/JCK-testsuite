/*
 * @test
 * @summary com.sun.nio.file.ExtendedOpenOption conformance: enum constants.
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

    public static void main(String[] a) throws Exception {
        com.sun.nio.file.ExtendedOpenOption[] v = com.sun.nio.file.ExtendedOpenOption.values();
        check(v.length == 4, "4 constants");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_READ.name().equals("NOSHARE_READ"), "NOSHARE_READ");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_WRITE.name().equals("NOSHARE_WRITE"), "NOSHARE_WRITE");
        check(com.sun.nio.file.ExtendedOpenOption.NOSHARE_DELETE.name().equals("NOSHARE_DELETE"), "NOSHARE_DELETE");
        check(com.sun.nio.file.ExtendedOpenOption.DIRECT.name().equals("DIRECT"), "DIRECT");
        check(com.sun.nio.file.ExtendedOpenOption.DIRECT instanceof java.nio.file.OpenOption, "OpenOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
