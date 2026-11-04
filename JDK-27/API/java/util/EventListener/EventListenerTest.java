/*
 * @test
 * @summary java.util.EventListener conformance: white-box structure + black-box behavior.
 * @run main EventListenerTest
 */

import java.lang.reflect.Modifier;
import java.util.EventListener;

public class EventListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MyListener implements EventListener {
    }

    static interface SubListener extends EventListener {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EventListener");
        check(c.isInterface(), "is an interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(c.getDeclaredFields().length == 0, "marker interface declares no fields");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. implementable by a plain class
        check(new MyListener() instanceof EventListener, "is implementable");
        check(new SubListener() {} instanceof EventListener, "is inheritable via sub-interface");

        // 2. served as the supertype of domain listener interfaces
        // (java.awt.event.ActionListener extends EventListener; verified reflectively below)
        Class<?> actionListener = null;
        try {
            actionListener = Class.forName("java.awt.event.ActionListener");
        } catch (ClassNotFoundException ignored) {
            // headless java.base-only module may not have java.desktop; treat as optional
        }
        if (actionListener != null) {
            check(EventListener.class.isAssignableFrom(actionListener),
                    "java.awt.event.ActionListener extends EventListener");
        }
        check(EventListener.class.isAssignableFrom(MyListener.class), "listener assignability");
        check(!EventListener.class.isAssignableFrom(String.class), "String is not a listener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
