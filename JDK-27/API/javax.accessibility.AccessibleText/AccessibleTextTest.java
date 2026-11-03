/*
 * @test
 * @summary javax.accessibility.AccessibleText conformance: white-box structure + black-box behavior.
 * @run main AccessibleTextTest
 */

import java.lang.reflect.*;
import java.awt.*;

public class AccessibleTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class TextModel implements javax.accessibility.AccessibleText {
        String text = ""; int caret = 0; int selStart = 0; int selEnd = 0;
        public int getIndexAtPoint(Point p) { return 0; }
        public Rectangle getCharacterBounds(int i) { return new Rectangle(i, 0, 1, 1); }
        public int getCharCount() { return text.length(); }
        public int getCaretPosition() { return caret; }
        public String getAtIndex(int part, int index) {
            if (part == CHARACTER && index >= 0 && index < text.length()) return text.substring(index, index + 1);
            return "";
        }
        public String getAfterIndex(int part, int index) { return getAtIndex(part, index + 1); }
        public String getBeforeIndex(int part, int index) { return getAtIndex(part, index - 1); }
        public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return new javax.swing.text.SimpleAttributeSet(); }
        public int getSelectionStart() { return selStart; }
        public int getSelectionEnd() { return selEnd; }
        public String getSelectedText() {
            if (selStart < 0 || selEnd > text.length() || selStart > selEnd) return null;
            return text.substring(selStart, selEnd);
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getIndexAtPoint"), "declares getIndexAtPoint");
        check(hasMethod(c, "getCharacterBounds"), "declares getCharacterBounds");
        check(hasMethod(c, "getCharCount"), "declares getCharCount");
        check(hasMethod(c, "getCaretPosition"), "declares getCaretPosition");
        check(hasMethod(c, "getAtIndex"), "declares getAtIndex");
    }

    static void testBlackBox() throws Exception {
        check(javax.accessibility.AccessibleText.CHARACTER == 1, "CHARACTER == 1");
        check(javax.accessibility.AccessibleText.WORD == 2, "WORD == 2");
        check(javax.accessibility.AccessibleText.SENTENCE == 3, "SENTENCE == 3");

        TextModel m = new TextModel();
        m.text = "hello world";
        check(m.getCharCount() == 11, "getCharCount");
        check(m.getCaretPosition() == 0, "initial caret 0");
        m.caret = 5;
        check(m.getCaretPosition() == 5, "caret after set");

        check("h".equals(m.getAtIndex(javax.accessibility.AccessibleText.CHARACTER, 0)), "getAtIndex char 0");
        check("e".equals(m.getAfterIndex(javax.accessibility.AccessibleText.CHARACTER, 0)), "getAfterIndex char");
        check("e".equals(m.getBeforeIndex(javax.accessibility.AccessibleText.CHARACTER, 2)), "getBeforeIndex char");
        check("".equals(m.getAtIndex(javax.accessibility.AccessibleText.WORD, 0)), "getAtIndex word empty");

        m.selStart = 0; m.selEnd = 5;
        check(m.getSelectionStart() == 0, "selection start");
        check(m.getSelectionEnd() == 5, "selection end");
        check("hello".equals(m.getSelectedText()), "getSelectedText");
        check(m.getCharacterAttribute(0) != null, "getCharacterAttribute non-null");
        check(m.getCharacterBounds(0) != null, "getCharacterBounds non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
