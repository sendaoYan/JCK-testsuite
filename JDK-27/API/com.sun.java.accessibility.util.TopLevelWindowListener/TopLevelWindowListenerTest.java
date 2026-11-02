/*
 * @test
 * @summary com.sun.java.accessibility.util.TopLevelWindowListener conformance: listener interface.
 * @run main TopLevelWindowListenerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class TopLevelWindowListenerTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.TopLevelWindowListener");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.util.EventListener"), "extends EventListener");
        Set<String> m = methodNames(c);
        check(m.contains("topLevelWindowCreated"), "topLevelWindowCreated");
        check(m.contains("topLevelWindowDestroyed"), "topLevelWindowDestroyed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
