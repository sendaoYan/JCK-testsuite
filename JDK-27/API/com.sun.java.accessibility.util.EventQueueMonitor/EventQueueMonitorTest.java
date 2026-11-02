/*
 * @test
 * @summary com.sun.java.accessibility.util.EventQueueMonitor conformance: monitor methods.
 * @run main EventQueueMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventQueueMonitorTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.EventQueueMonitor");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        check(m.contains("maybeInitialize"), "maybeInitialize");
        check(m.contains("eventDispatched"), "eventDispatched");
        check(m.contains("getAccessibleAt"), "getAccessibleAt");
        check(m.contains("isGUIInitialized"), "isGUIInitialized");
        check(m.contains("addGUIInitializedListener"), "addGUIInitializedListener");
        check(m.contains("removeGUIInitializedListener"), "removeGUIInitializedListener");
        check(m.contains("addTopLevelWindowListener"), "addTopLevelWindowListener");
        check(m.contains("getCurrentMousePosition"), "getCurrentMousePosition");
        check(m.contains("getTopLevelWindows"), "getTopLevelWindows");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
