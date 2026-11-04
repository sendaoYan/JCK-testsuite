/*
 * @test
 * @summary com.sun.java.accessibility.util.GUIInitializedListener conformance: white-box structure + black-box behavior.
 * @run main GUIInitializedListenerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class GUIInitializedListenerTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.GUIInitializedListener");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.util.EventListener"), "extends EventListener");
        check(methodNames(c).contains("guiInitialized"), "guiInitialized");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final boolean[] called = {false};
        com.sun.java.accessibility.util.GUIInitializedListener l =
            new com.sun.java.accessibility.util.GUIInitializedListener() {
                public void guiInitialized() { called[0] = true; }
            };
        check(!called[0], "not yet called");
        l.guiInitialized();
        check(called[0], "guiInitialized invoked");
        // callback may fire more than once without error.
        l.guiInitialized();
        check(called[0], "guiInitialized idempotent callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
