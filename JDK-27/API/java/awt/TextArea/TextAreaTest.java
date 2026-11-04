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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
