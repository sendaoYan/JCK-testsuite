/*
 * @test
 * @key headful
 * @summary java.awt.TextField conformance: class reflection.
 * @run main TextFieldTest
 */

import java.awt.*;

public class TextFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
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
        Class<?> c = Class.forName("java.awt.TextField");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.TextComponent"), "superclass java.awt.TextComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getEchoChar"), "method getEchoChar");
        check(hasMethod(c, "setEchoChar"), "method setEchoChar");
        check(hasMethod(c, "setEchoCharacter"), "method setEchoCharacter");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "echoCharIsSet"), "method echoCharIsSet");
        check(hasMethod(c, "getColumns"), "method getColumns");
        check(hasMethod(c, "setColumns"), "method setColumns");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractTextField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
