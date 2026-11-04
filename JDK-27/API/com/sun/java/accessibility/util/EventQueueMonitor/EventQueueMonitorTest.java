/*
 * @test
 * @summary com.sun.java.accessibility.util.EventQueueMonitor conformance: white-box structure + black-box behavior.
 * @run main EventQueueMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.EventQueueMonitor");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaredConstructor() != null, "public constructor");
        check(interfaces(c).contains("java.awt.event.AWTEventListener"), "implements AWTEventListener");
        Set<String> m = methodNames(c);
        check(m.contains("maybeInitialize"), "maybeInitialize");
        check(m.contains("eventDispatched"), "eventDispatched");
        check(m.contains("getAccessibleAt"), "getAccessibleAt");
        check(m.contains("isGUIInitialized"), "isGUIInitialized");
        check(m.contains("addGUIInitializedListener"), "addGUIInitializedListener");
        check(m.contains("removeGUIInitializedListener"), "removeGUIInitializedListener");
        check(m.contains("addTopLevelWindowListener"), "addTopLevelWindowListener");
        check(m.contains("removeTopLevelWindowListener"), "removeTopLevelWindowListener");
        check(m.contains("getCurrentMousePosition"), "getCurrentMousePosition");
        check(m.contains("getTopLevelWindows"), "getTopLevelWindows");
        check(m.contains("getTopLevelWindowWithFocus"), "getTopLevelWindowWithFocus");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.EventQueueMonitor mon =
            new com.sun.java.accessibility.util.EventQueueMonitor();
        check(mon != null, "new EventQueueMonitor() non-null");

        // Headless cache defaults.
        check(com.sun.java.accessibility.util.EventQueueMonitor.isGUIInitialized() == false,
              "isGUIInitialized false (no GUI events yet)");
        com.sun.java.accessibility.util.EventQueueMonitor.maybeInitialize();
        check(com.sun.java.accessibility.util.EventQueueMonitor.isGUIInitialized() == false,
              "isGUIInitialized still false after maybeInitialize (headless, no events)");
        check(com.sun.java.accessibility.util.EventQueueMonitor.getCurrentMousePosition() == null,
              "current mouse position null");
        Window[] wins = com.sun.java.accessibility.util.EventQueueMonitor.getTopLevelWindows();
        check(wins != null, "top level windows array non-null");
        check(wins.length == 0, "no top level windows in headless");
        check(com.sun.java.accessibility.util.EventQueueMonitor.getTopLevelWindowWithFocus() == null,
              "top level window with focus null");
        check(com.sun.java.accessibility.util.EventQueueMonitor.getAccessibleAt(new Point(0, 0)) == null,
              "getAccessibleAt null in headless");

        // Listener registration/unregistration is callable and does not throw.
        com.sun.java.accessibility.util.GUIInitializedListener gil =
            new com.sun.java.accessibility.util.GUIInitializedListener() {
                public void guiInitialized() {}
            };
        com.sun.java.accessibility.util.EventQueueMonitor.addGUIInitializedListener(gil);
        com.sun.java.accessibility.util.EventQueueMonitor.removeGUIInitializedListener(gil);

        com.sun.java.accessibility.util.TopLevelWindowListener twl =
            new com.sun.java.accessibility.util.TopLevelWindowListener() {
                public void topLevelWindowCreated(Window w) {}
                public void topLevelWindowDestroyed(Window w) {}
            };
        com.sun.java.accessibility.util.EventQueueMonitor.addTopLevelWindowListener(twl);
        com.sun.java.accessibility.util.EventQueueMonitor.removeTopLevelWindowListener(twl);

        // AWTEventListener.eventDispatched is callable; a benign non-window event is ignored.
        mon.eventDispatched(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "cmd"));
        check(com.sun.java.accessibility.util.EventQueueMonitor.getCurrentMousePosition() == null,
              "mouse position still null after benign event");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
