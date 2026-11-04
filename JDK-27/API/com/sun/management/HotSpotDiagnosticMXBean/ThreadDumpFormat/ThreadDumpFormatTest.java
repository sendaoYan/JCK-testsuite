/*
 * @test
 * @summary com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat conformance: enum constants.
 * @run main ThreadDumpFormatTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ThreadDumpFormatTest {
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
        com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat[] v = com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.values();
        check(v.length == 2, "2 constants");
        check(com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.TEXT_PLAIN.name().equals("TEXT_PLAIN"), "TEXT_PLAIN");
        check(com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON.name().equals("JSON"), "JSON");
        check(com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.valueOf("JSON") == com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
