/*
 * @test
 * @key headful
 * @summary java.awt.TextArea conformance: class reflection.
 * @run main TextAreaTest
 */

import java.awt.*;

public class TextAreaTest {
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
        Class<?> c = Class.forName("java.awt.TextArea");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.TextComponent"), "superclass java.awt.TextComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "insertText"), "method insertText");
        check(hasMethod(c, "append"), "method append");
        check(hasMethod(c, "appendText"), "method appendText");
        check(hasMethod(c, "replaceRange"), "method replaceRange");
        check(hasMethod(c, "replaceText"), "method replaceText");
        check(hasMethod(c, "getRows"), "method getRows");
        check(hasMethod(c, "setRows"), "method setRows");
        check(hasMethod(c, "getColumns"), "method getColumns");
        check(hasMethod(c, "setColumns"), "method setColumns");
        check(hasMethod(c, "getScrollbarVisibility"), "method getScrollbarVisibility");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractTextArea();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
