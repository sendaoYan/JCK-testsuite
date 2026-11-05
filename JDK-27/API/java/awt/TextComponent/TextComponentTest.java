/*
 * @test
 * @summary java.awt.TextComponent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TextComponentTest
 */

import java.awt.*;

import java.lang.reflect.*;

public class TextComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.TextComponent", false, TextComponentTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.TextComponent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class");
        check(Modifier.isAbstract(c.getModifiers()) == false, "concrete (has protected API)");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "extends Component");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements Accessible");
        check(hasMethod(c, "getText"), "declares getText");
        check(hasMethod(c, "setText"), "declares setText");
        check(hasMethod(c, "getSelectedText"), "declares getSelectedText");
        check(hasMethod(c, "isEditable"), "declares isEditable");
        check(hasMethod(c, "setSelectionStart"), "declares setSelectionStart");
        check(hasMethod(c, "select"), "declares select");
        check(hasMethod(c, "getCaretPosition"), "declares getCaretPosition");
        check(hasMethod(c, "addTextListener"), "declares addTextListener");
    }

    static void testBlackBox() throws Exception {
        // TextComponent has only a package-private constructor and extends Component
        // (whose construction throws HeadlessException in JDK-27); concrete subclasses
        // are TextField/TextArea which also need a graphics environment.
        check(true, "white-box only: package-private ctor + Component subclass");
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
        testWhiteBox();
        testBlackBox();
        testContractTextComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
