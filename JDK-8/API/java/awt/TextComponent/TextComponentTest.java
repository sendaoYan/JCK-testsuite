/*
 * @test
 * @key headful
 * @summary java.awt.TextComponent conformance: class reflection.
 * @run main TextComponentTest
 */

import java.awt.*;

public class TextComponentTest {
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

    // ---- Part 3: contract deepening (TextComponent API exercised through TextArea/TextField) ----
    static void testContractTextComponent() throws Exception {
        TextArea a = new TextArea();
        check("".equals(a.getText()), "TextArea default text empty");
        a.setText(null);
        check("".equals(a.getText()), "TextArea.setText(null) normalized to empty");
        TextField b = new TextField();
        b.setText(null);
        check("".equals(b.getText()), "TextField.setText(null) normalized to empty");
        check(a.isEditable() && b.isEditable(), "editable default true");
        a.setEditable(false);
        check(!a.isEditable(), "TextArea.setEditable(false)");
        b.setEditable(false);
        b.setEditable(true);
        check(b.isEditable(), "TextField editable toggle");

        // caret and selection contract shared by both concrete subclasses
        for (TextComponent t : new TextComponent[]{new TextArea("abcdef"), new TextField("abcdef")}) {
            String kind = t instanceof TextArea ? "TextArea" : "TextField";
            t.setCaretPosition(3);
            check(t.getCaretPosition() == 3, kind + " caret set/get");
            expectExact(IllegalArgumentException.class, kind + " setCaretPosition(-1)",
                  () -> t.setCaretPosition(-1));
            t.setCaretPosition(999);
            check(t.getCaretPosition() == 6, kind + " caret clamps to length");
            t.select(1, 4);
            check("bcd".equals(t.getSelectedText()), kind + " select/getSelectedText");
            check(t.getSelectionStart() == 1 && t.getSelectionEnd() == 4, kind + " selection bounds");
            t.select(-1, 0);
            check(t.getSelectionStart() == 0 && t.getSelectionEnd() == 0,
                  kind + " select(-1,0) collapses to (0,0)");
            t.select(4, 2);
            check(t.getSelectionStart() == 4 && t.getSelectionEnd() == 4,
                  kind + " select(start>end) collapses to (start,start)");
            t.select(0, 6);
            check("abcdef".equals(t.getSelectedText()), kind + " full selection text");
            t.setSelectionEnd(3);
            check(t.getSelectionStart() == 0 && t.getSelectionEnd() == 3, kind + " setSelectionEnd");
            t.setSelectionStart(2);
            check(t.getSelectionStart() == 2 && t.getSelectionEnd() == 3, kind + " setSelectionStart");
            t.setSelectionEnd(99);
            check(t.getSelectionEnd() == 6, kind + " setSelectionEnd clamps to length");
            t.setSelectionStart(99);
            check(t.getSelectionStart() == 6 && t.getSelectionEnd() == 6,
                  kind + " setSelectionStart clamps to length");
            t.setText("");
            check(t.getCaretPosition() == 0, kind + " caret 0 after clearing text");
            t.setCaretPosition(5);
            check(t.getCaretPosition() == 0, kind + " caret clamps to 0 on empty text");
            check("".equals(t.getSelectedText()), kind + " no selection text when empty");
        }

        // TextListener registry; programmatic edits dispatch no TextEvent
        boolean[] fired = { false };
        TextArea ta = new TextArea("x");
        java.awt.event.TextListener tl = e -> fired[0] = true;
        ta.addTextListener(tl);
        check(ta.getTextListeners().length == 1, "TextListener registered");
        ta.setText("y"); ta.insert("z", 0); ta.append("w");
        java.awt.EventQueue.invokeAndWait(() -> { });
        check(!fired[0], "no TextEvent from programmatic edits");
        ta.removeTextListener(tl);
        check(ta.getTextListeners().length == 0, "TextListener removed");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.TextComponent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "enableInputMethods"), "method enableInputMethods");
        check(hasMethod(c, "getInputMethodRequests"), "method getInputMethodRequests");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
        check(hasMethod(c, "isEditable"), "method isEditable");
        check(hasMethod(c, "setEditable"), "method setEditable");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "setSelectionStart"), "method setSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "setSelectionEnd"), "method setSelectionEnd");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "selectAll"), "method selectAll");
        check(hasMethod(c, "setCaretPosition"), "method setCaretPosition");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "addTextListener"), "method addTextListener");
        check(hasMethod(c, "removeTextListener"), "method removeTextListener");
        check(hasMethod(c, "getTextListeners"), "method getTextListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractTextComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
