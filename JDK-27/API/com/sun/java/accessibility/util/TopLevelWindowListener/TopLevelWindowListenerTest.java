/*
 * @test
 * @summary com.sun.java.accessibility.util.TopLevelWindowListener conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.TopLevelWindowListener");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.util.EventListener"), "extends EventListener");
        Set<String> m = methodNames(c);
        check(m.contains("topLevelWindowCreated"), "topLevelWindowCreated");
        check(m.contains("topLevelWindowDestroyed"), "topLevelWindowDestroyed");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final int[] created = {0};
        final int[] destroyed = {0};
        com.sun.java.accessibility.util.TopLevelWindowListener l =
            new com.sun.java.accessibility.util.TopLevelWindowListener() {
                public void topLevelWindowCreated(java.awt.Window w) { created[0]++; }
                public void topLevelWindowDestroyed(java.awt.Window w) { destroyed[0]++; }
            };
        // Callback methods are callable (headless-safe: pass null Window, no display needed).
        l.topLevelWindowCreated(null);
        check(created[0] == 1 && destroyed[0] == 0, "created invoked");
        l.topLevelWindowDestroyed(null);
        check(created[0] == 1 && destroyed[0] == 1, "destroyed invoked");
        l.topLevelWindowCreated(null);
        l.topLevelWindowDestroyed(null);
        check(created[0] == 2 && destroyed[0] == 2, "repeat invocation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
