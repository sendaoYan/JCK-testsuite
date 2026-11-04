/*
 * @test
 * @summary java.awt.TextField conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TextFieldTest
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.*;

public class TextFieldTest {
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
        Class<?> c = Class.forName("java.awt.TextField");
        check(c != null, "type loads: java.awt.TextField");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == TextComponent.class, "extends TextComponent");
        for (String name : new String[]{"getEchoChar", "setEchoChar", "echoCharIsSet",
                "getColumns", "setColumns", "getPreferredSize", "getMinimumSize",
                "addActionListener", "removeActionListener", "getActionListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TextField t = new TextField();
        check("".equals(t.getText()), "default empty text");
        check(t.getColumns() == 0, "default columns 0");

        TextField t2 = new TextField("hello", 20);
        check("hello".equals(t2.getText()), "constructor text");
        check(t2.getColumns() == 20, "constructor columns");
        check(!t2.echoCharIsSet(), "echo char not set default");
        check(t2.getEchoChar() == 0, "default echo char 0");

        t2.setText("world");
        check("world".equals(t2.getText()), "setText/getText");
        t2.setColumns(30);
        check(t2.getColumns() == 30, "setColumns/getColumns");
        t2.setEchoChar('*');
        check(t2.getEchoChar() == '*', "setEchoChar/getEchoChar");
        check(t2.echoCharIsSet(), "echoCharIsSet after set");

        // TextComponent selection/caret/editable
        t2.setText("abcdef");
        t2.select(1, 3);
        check(t2.getSelectionStart() == 1 && t2.getSelectionEnd() == 3, "select range");
        check("bc".equals(t2.getSelectedText()), "getSelectedText");
        t2.selectAll();
        check(t2.getSelectionStart() == 0 && t2.getSelectionEnd() == 6, "selectAll");
        t2.setCaretPosition(4);
        check(t2.getCaretPosition() == 4, "setCaretPosition/getCaretPosition");
        check(t2.isEditable(), "editable default true");
        t2.setEditable(false);
        check(!t2.isEditable(), "setEditable(false)");
        t2.setEditable(true);
        check(t2.isEditable(), "setEditable(true)");

        check(t2.getPreferredSize() != null, "getPreferredSize non-null");
        check(t2.getPreferredSize(10) != null, "getPreferredSize(int) non-null");
        check(t2.getMinimumSize() != null, "getMinimumSize non-null");

        ActionListener l = e -> {};
        t2.addActionListener(l);
        check(t2.getActionListeners().length == 1 && t2.getActionListeners()[0] == l,
              "addActionListener/getActionListeners");
        t2.removeActionListener(l);
        check(t2.getActionListeners().length == 0, "removeActionListener");

        Frame f = new Frame("TextFieldTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(t2);
            check(t2.isDisplayable(), "displayable after add");
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
