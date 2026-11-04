/*
 * @test
 * @summary com.sun.tools.javac.Main conformance: white-box structure + black-box behavior.
 * @run main MainTest
 */

import java.lang.reflect.*;
import java.util.*;

public class MainTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.tools.javac.Main");
        check(!c.isInterface(), "not an interface");
        check(methodNames(c).contains("compile"), "compile");
        new com.sun.tools.javac.Main();
        check(com.sun.tools.javac.Main.compile(new String[]{"-version"}) == 0, "compile -version == 0");
    }

    static void testBlackBox() throws Exception {
        check(com.sun.tools.javac.Main.compile(new String[]{"-version"}) == 0, "compile -version == 0");
        java.io.StringWriter sw = new java.io.StringWriter();
        int rc = com.sun.tools.javac.Main.compile(new String[]{"-version"}, new java.io.PrintWriter(sw));
        check(rc == 0, "compile -version (PrintWriter) == 0");
        check(sw.toString().length() > 0, "PrintWriter captured output");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
