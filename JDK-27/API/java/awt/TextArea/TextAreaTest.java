/*
 * @test
 * @summary java.awt.TextArea conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TextAreaTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class TextAreaTest {
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
        Class<?> c = Class.forName("java.awt.TextArea");
        check(c != null, "type loads: java.awt.TextArea");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == TextComponent.class, "extends TextComponent");
        for (String name : new String[]{"insert", "append", "replaceRange", "getRows",
                "setRows", "getColumns", "setColumns", "getScrollbarVisibility"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(TextArea.SCROLLBARS_BOTH == 0, "SCROLLBARS_BOTH 0");
        check(TextArea.SCROLLBARS_VERTICAL_ONLY == 1, "SCROLLBARS_VERTICAL_ONLY 1");
        check(TextArea.SCROLLBARS_HORIZONTAL_ONLY == 2, "SCROLLBARS_HORIZONTAL_ONLY 2");
        check(TextArea.SCROLLBARS_NONE == 3, "SCROLLBARS_NONE 3");

        TextArea t = new TextArea();
        check("".equals(t.getText()), "default empty text");
        check(t.getRows() == 0 && t.getColumns() == 0, "default rows/columns 0");
        check(t.getScrollbarVisibility() == TextArea.SCROLLBARS_BOTH, "default scrollbars BOTH");

        TextArea t2 = new TextArea("abc", 3, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
        check("abc".equals(t2.getText()), "constructor text");
        check(t2.getRows() == 3 && t2.getColumns() == 40, "constructor rows/columns");
        check(t2.getScrollbarVisibility() == TextArea.SCROLLBARS_VERTICAL_ONLY, "constructor scrollbars");

        t2.setRows(5); t2.setColumns(60);
        check(t2.getRows() == 5 && t2.getColumns() == 60, "setRows/setColumns");

        t2.append("def");
        check("abcdef".equals(t2.getText()), "append");
        t2.insert("XY", 2);
        check("abXYcdef".equals(t2.getText()), "insert at index");
        t2.replaceRange("ZZ", 2, 4);
        check("abZZcdef".equals(t2.getText()), "replaceRange");

        t2.setText("hello world");
        t2.select(0, 5);
        check("hello".equals(t2.getSelectedText()), "select/getSelectedText");
        t2.selectAll();
        check(t2.getSelectionEnd() == "hello world".length(), "selectAll end");
        t2.setCaretPosition(3);
        check(t2.getCaretPosition() == 3, "setCaretPosition/getCaretPosition");
        check(t2.isEditable(), "editable default true");
        t2.setEditable(false);
        check(!t2.isEditable(), "setEditable(false)");

        check(t2.getPreferredSize() != null, "getPreferredSize non-null");
        check(t2.getPreferredSize(5, 60) != null, "getPreferredSize(int,int) non-null");
        check(t2.getMinimumSize() != null, "getMinimumSize non-null");

        Frame f = new Frame("TextAreaTest");
        f.setSize(200, 200);
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
    static void testContractTextArea() throws Exception {
        // scrollbar policy roundtrip for every constant
        check(new TextArea("t", 2, 10, TextArea.SCROLLBARS_NONE).getScrollbarVisibility()
              == TextArea.SCROLLBARS_NONE, "policy SCROLLBARS_NONE");
        check(new TextArea("t", 2, 10, TextArea.SCROLLBARS_HORIZONTAL_ONLY).getScrollbarVisibility()
              == TextArea.SCROLLBARS_HORIZONTAL_ONLY, "policy HORIZONTAL_ONLY");
        check(new TextArea("t", 2, 10, TextArea.SCROLLBARS_VERTICAL_ONLY).getScrollbarVisibility()
              == TextArea.SCROLLBARS_VERTICAL_ONLY, "policy VERTICAL_ONLY");
        // rows/columns: 0 allowed, negative rejected
        TextArea t = new TextArea("x");
        check(t.getRows() == 0 && t.getColumns() == 0, "rows/columns default 0");
        t.setRows(0);
        check(t.getRows() == 0, "setRows(0) allowed");
        expectExact(IllegalArgumentException.class, "setRows(-1)", () -> t.setRows(-1));
        t.setColumns(0);
        check(t.getColumns() == 0, "setColumns(0) allowed");
        expectExact(IllegalArgumentException.class, "setColumns(-3)", () -> t.setColumns(-3));
        t.setRows(4);
        t.setColumns(11);
        check(t.getRows() == 4 && t.getColumns() == 11, "setRows/setColumns roundtrip");
        // append/insert; null becomes the literal string "null"
        TextArea a1 = new TextArea("abc");
        a1.append("DEF");
        check("abcDEF".equals(a1.getText()), "append");
        a1.append(null);
        check("abcDEFnull".equals(a1.getText()), "append(null) appends literal null");
        a1.insert("Z", 3);
        check("abcZDEFnull".equals(a1.getText()), "insert");
        a1.insert(null, 0);
        check("nullabcZDEFnull".equals(a1.getText()), "insert(null,0) inserts literal null");
        // index validation
        TextArea a2 = new TextArea("abcdef");
        expectExact(StringIndexOutOfBoundsException.class, "insert(x,-1)", () -> a2.insert("x", -1));
        expectExact(StringIndexOutOfBoundsException.class, "insert(x,99)", () -> a2.insert("x", 99));
        expectExact(StringIndexOutOfBoundsException.class, "replaceRange(-1,3)", () -> a2.replaceRange("x", -1, 3));
        expectExact(StringIndexOutOfBoundsException.class, "replaceRange(0,99)", () -> a2.replaceRange("x", 0, 99));
        a2.replaceRange("XY", 1, 3);
        check("aXYdef".equals(a2.getText()), "replaceRange");
        a2.replaceRange(null, 0, 1);
        check("nullXYdef".equals(a2.getText()), "replaceRange(null) writes literal null");
        // swapped bounds splice as substring(0,start)+str+substring(end)
        TextArea a3 = new TextArea("abcdef");
        a3.replaceRange("XY", 4, 2);
        check("abcdXYcdef".equals(a3.getText()), "swapped bounds splice semantics");
        a3.setText(null);
        check("".equals(a3.getText()), "setText(null) normalized to empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractTextArea();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
