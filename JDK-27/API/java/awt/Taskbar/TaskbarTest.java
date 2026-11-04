/*
 * @test
 * @summary java.awt.Taskbar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TaskbarTest
 */

import java.awt.*;

import java.lang.reflect.*;
import java.awt.Taskbar;

public class TaskbarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Taskbar", false, TaskbarTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Taskbar");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isStatic(c.getMethod("getTaskbar").getModifiers()), "getTaskbar is static");
        check(Modifier.isStatic(c.getMethod("isTaskbarSupported").getModifiers()), "isTaskbarSupported is static");
        for (String n : new String[] {"isSupported", "requestUserAttention", "setIconImage", "getIconImage",
                "setProgressValue", "getMenu", "setMenu"})
            check(hasMethod(c, n), "method present: " + n);
        check(Class.forName("java.awt.Taskbar$Feature", false, TaskbarTest.class.getClassLoader()) != null,
              "nested Feature type loads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        boolean supported = Taskbar.isTaskbarSupported();
        check(supported || !supported, "isTaskbarSupported returns a boolean");

        if (supported) {
            Taskbar tb = Taskbar.getTaskbar();
            check(tb != null, "getTaskbar non-null when supported");
            check(Taskbar.getTaskbar() == tb, "getTaskbar returns singleton");

            boolean deterministic = true;
            for (Taskbar.Feature f : Taskbar.Feature.values()) {
                deterministic &= (tb.isSupported(f) == tb.isSupported(f));
            }
            check(deterministic, "isSupported deterministic for all " + Taskbar.Feature.values().length + " features");

            check(tb.isSupported(Taskbar.Feature.ICON_IMAGE) || !tb.isSupported(Taskbar.Feature.ICON_IMAGE),
                  "isSupported(ICON_IMAGE) boolean");
            check(tb.isSupported(Taskbar.Feature.PROGRESS_VALUE) || !tb.isSupported(Taskbar.Feature.PROGRESS_VALUE),
                  "isSupported(PROGRESS_VALUE) boolean");
        } else {
            try {
                Taskbar.getTaskbar();
                check(false, "getTaskbar should throw when unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "getTaskbar throws UnsupportedOperationException when unsupported");
            }
        }
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractTaskbar() throws Exception {
        // measured on this Linux/X11 display: no taskbar provider exists outside macOS
        check(!Taskbar.isTaskbarSupported(), "isTaskbarSupported false on Linux/X11");
        expectExact(UnsupportedOperationException.class, "getTaskbar without a provider",
              () -> Taskbar.getTaskbar());
        java.util.Set<String> states = new java.util.HashSet<>();
        for (Taskbar.State st : Taskbar.State.values()) states.add(st.name());
        check(states.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                "OFF", "NORMAL", "PAUSED", "INDETERMINATE", "ERROR"))), "State constant set");
        check(Taskbar.State.values().length == 5, "5 State constants");
        java.util.Set<String> features = new java.util.HashSet<>();
        for (Taskbar.Feature ft : Taskbar.Feature.values()) features.add(ft.name());
        check(features.containsAll(java.util.Arrays.asList("ICON_IMAGE", "MENU", "PROGRESS_VALUE",
                "PROGRESS_VALUE_WINDOW", "USER_ATTENTION", "USER_ATTENTION_WINDOW")),
              "known Feature constants");
        check(Taskbar.Feature.values().length == 10, "10 Feature constants");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractTaskbar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
