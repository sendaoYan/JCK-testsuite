/*
 * @test
 * @summary com.sun.tools.javac.Main conformance: compile entry point.
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

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.tools.javac.Main");
        check(!c.isInterface(), "not an interface");
        check(methodNames(c).contains("compile"), "compile");
        new com.sun.tools.javac.Main();
        check(com.sun.tools.javac.Main.compile(new String[]{"-version"}) == 0, "compile -version == 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
