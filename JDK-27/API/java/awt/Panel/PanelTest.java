/*
 * @test
 * @summary java.awt.Panel conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PanelTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class PanelTest {
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
        Class<?> c = Class.forName("java.awt.Panel");
        check(c != null, "type loads: java.awt.Panel");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Container.class, "extends Container");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        check(hasMethod(c, "addNotify"), "has addNotify");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Panel p = new Panel();
        check(p.getLayout() instanceof FlowLayout, "default layout FlowLayout");

        Panel p2 = new Panel(new BorderLayout());
        check(p2.getLayout() instanceof BorderLayout, "constructor layout BorderLayout");

        Button b = new Button("x");
        p.add(b);
        check(p.getComponentCount() == 1 && p.getComponent(0) == b, "add/getComponent");

        Frame f = new Frame("PanelTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(p);
            check(p.isDisplayable(), "displayable after add");
            check(p.isShowing(), "showing after add");
            p.setSize(100, 80);
            check(p.getSize().equals(new Dimension(100, 80)), "setSize/getSize");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
