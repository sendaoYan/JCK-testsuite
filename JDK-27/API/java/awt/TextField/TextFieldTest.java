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
    static void testContractTextField() throws Exception {
        check(new TextField().getColumns() == 0, "default columns 0");
        check(new TextField("abcde").getColumns() == 5, "columns default to text length");
        check("".equals(new TextField(null).getText()), "TextField(null) empty text");
        TextField t = new TextField("hi");
        t.setColumns(0);
        check(t.getColumns() == 0, "setColumns(0) allowed");
        expectExact(IllegalArgumentException.class, "setColumns(-3)", () -> t.setColumns(-3));
        t.setColumns(12);
        check(t.getColumns() == 12, "setColumns roundtrip");
        // echo char lifecycle
        check(!t.echoCharIsSet() && t.getEchoChar() == '\0', "echo unset by default");
        t.setEchoChar('*');
        check(t.getEchoChar() == '*' && t.echoCharIsSet(), "setEchoChar/getEchoChar");
        t.setEchoChar('\0');
        check(t.getEchoChar() == '\0' && !t.echoCharIsSet(), "NUL echo char cancels echo");
        // text null normalization
        t.setText(null);
        check("".equals(t.getText()), "setText(null) normalized to empty");
        // ActionListener registry with duplicates
        java.awt.event.ActionListener l = e -> { };
        t.addActionListener(l); t.addActionListener(l);
        check(t.getActionListeners().length == 2, "duplicate ActionListeners registered");
        t.removeActionListener(l);
        check(t.getActionListeners().length == 1, "removeActionListener removes one instance");
        check(t.getListeners(java.awt.event.ActionListener.class).length == 1, "getListeners agrees");
        // echo char does not leak into the model text
        t.setText("secret");
        t.setEchoChar('#');
        t.select(0, 6);
        check("secret".equals(t.getSelectedText()), "getSelectedText unaffected by echo char");
        check("secret".equals(t.getText()), "getText unaffected by echo char");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractTextField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
