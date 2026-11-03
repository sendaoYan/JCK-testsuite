/*
 * @test
 * @summary java.awt.Button conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ButtonTest
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.*;

public class ButtonTest {
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
        Class<?> c = Class.forName("java.awt.Button");
        check(c != null, "type loads: java.awt.Button");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"getLabel", "setLabel", "setActionCommand",
                "getActionCommand", "addActionListener", "removeActionListener", "getActionListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Button b = new Button("press");
        check("press".equals(b.getLabel()), "constructor label");
        b.setLabel("pressed");
        check("pressed".equals(b.getLabel()), "setLabel/getLabel");

        check("pressed".equals(b.getActionCommand()), "action command defaults to label");
        b.setActionCommand("cmd");
        check("cmd".equals(b.getActionCommand()), "setActionCommand/getActionCommand");

        ActionListener l = e -> {};
        b.addActionListener(l);
        check(b.getActionListeners().length == 1 && b.getActionListeners()[0] == l,
              "addActionListener/getActionListeners");
        b.removeActionListener(l);
        check(b.getActionListeners().length == 0, "removeActionListener");

        Frame f = new Frame("ButtonTest");
        f.setLayout(null);
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            f.add(b);
            check(b.isDisplayable(), "displayable after add");
            check(b.isShowing(), "showing after add");
            b.setEnabled(false);
            check(!b.isEnabled(), "setEnabled(false)");
            b.setEnabled(true);
            check(b.isEnabled(), "setEnabled(true)");
            b.setBackground(Color.ORANGE);
            check(Color.ORANGE.equals(b.getBackground()), "setBackground/getBackground");
            b.setBounds(10, 20, 80, 30);
            check(b.getBounds().equals(new Rectangle(10, 20, 80, 30)), "setBounds/getBounds");
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
