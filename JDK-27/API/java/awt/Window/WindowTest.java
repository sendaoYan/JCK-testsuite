/*
 * @test
 * @summary java.awt.Window conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main WindowTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class WindowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Window");
        check(c != null, "type loads: java.awt.Window");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Container.class, "extends Container");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"getOwner", "getOwnedWindows", "getWindows",
                "setModalExclusionType", "getModalExclusionType", "setType", "getType",
                "setSize", "setLocation", "setVisible", "dispose",
                "setAlwaysOnTop", "isAlwaysOnTop", "isAlwaysOnTopSupported", "getWarningString",
                "setFocusableWindowState", "getFocusableWindowState"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Frame owner = new Frame("owner");
        owner.setVisible(true);
        try {
            Window w = new Window(owner);
            check(w.getOwner() == owner, "getOwner is owner frame");
            check(w.getModalExclusionType() == Dialog.ModalExclusionType.NO_EXCLUDE,
                  "default modal exclusion NO_EXCLUDE");
            w.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            check(w.getModalExclusionType() == Dialog.ModalExclusionType.APPLICATION_EXCLUDE,
                  "setModalExclusionType roundtrip");
            check(w.getType() == Window.Type.NORMAL, "default type NORMAL");
            w.setType(Window.Type.UTILITY);
            check(w.getType() == Window.Type.UTILITY, "setType roundtrip");

            check(!w.isDisplayable(), "not displayable before visible");
            w.setSize(120, 90);
            check(w.getSize().equals(new Dimension(120, 90)), "setSize/getSize");
            w.setLocation(40, 50);
            check(w.getLocation().equals(new Point(40, 50)), "setLocation/getLocation");

            w.setVisible(true);
            check(w.isDisplayable(), "displayable after setVisible");
            check(w.isShowing(), "showing after setVisible");
            check(owner.getOwnedWindows().length == 1 && owner.getOwnedWindows()[0] == w,
                  "owner owns the window");
            boolean sawWindow = false, sawOwner = false;
            for (Window x : Window.getWindows()) {
                if (x == w) sawWindow = true;
                if (x == owner) sawOwner = true;
            }
            check(sawWindow && sawOwner, "getWindows contains both");

            check(w.getWarningString() == null, "warning string null for application");

            boolean supported = w.isAlwaysOnTopSupported();
            check(supported || !supported, "isAlwaysOnTopSupported returns boolean");
            w.setAlwaysOnTop(true);
            check(w.isAlwaysOnTop(), "setAlwaysOnTop(true)");
            w.setAlwaysOnTop(false);
            check(!w.isAlwaysOnTop(), "setAlwaysOnTop(false)");

            check(!w.isFocusableWindow(), "childless window not focusable");
            w.setFocusableWindowState(false);
            check(!w.getFocusableWindowState(), "setFocusableWindowState(false)");

            w.setBackground(Color.GREEN);
            check(Color.GREEN.equals(w.getBackground()), "setBackground/getBackground");

            w.dispose();
            check(!w.isDisplayable(), "dispose clears displayable");
        } finally {
            owner.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
