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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: CHAR/WORD/SENTENCE split matrix on real Swing text (headless) ----
    static void testContractDeep() throws Exception {
        javax.swing.JTextArea ta = new javax.swing.JTextArea("Hello world. Second sentence here.");
        javax.accessibility.AccessibleText t = ta.getAccessibleContext().getAccessibleText();
        check(t != null, "JTextArea exposes AccessibleText headless");
        check(t.getCharCount() == 34, "char count 34");
        check(javax.accessibility.AccessibleText.CHARACTER == 1 && javax.accessibility.AccessibleText.WORD == 2 && javax.accessibility.AccessibleText.SENTENCE == 3, "part constants 1/2/3");
        // CHARACTER split
        check(" ".equals(t.getAtIndex(javax.accessibility.AccessibleText.CHARACTER, 5)), "CHAR at 5 is space");
        check("w".equals(t.getAfterIndex(javax.accessibility.AccessibleText.CHARACTER, 5)), "CHAR after 5 = w");
        check("o".equals(t.getBeforeIndex(javax.accessibility.AccessibleText.CHARACTER, 5)), "CHAR before 5 = o");
        // WORD split
        check("Hello".equals(t.getAtIndex(javax.accessibility.AccessibleText.WORD, 0)), "WORD at 0");
        check("world".equals(t.getAtIndex(javax.accessibility.AccessibleText.WORD, 6)), "WORD at 6");
        check(" ".equals(t.getAfterIndex(javax.accessibility.AccessibleText.WORD, 0)), "WORD after 0 is the separator space");
        check(" ".equals(t.getBeforeIndex(javax.accessibility.AccessibleText.WORD, 7)), "WORD before 7 is separator");
        check("here".equals(t.getBeforeIndex(javax.accessibility.AccessibleText.WORD, 33)), "WORD before 33 = here");
        // SENTENCE split (trailing space / trailing newline preserved)
        check("Hello world. ".equals(t.getAtIndex(javax.accessibility.AccessibleText.SENTENCE, 0)), "SENT at 0 keeps trailing space");
        check("Second sentence here.\n".equals(t.getAtIndex(javax.accessibility.AccessibleText.SENTENCE, 13)), "SENT at 13 keeps trailing newline");
        check("Second sentence here.\n".equals(t.getAfterIndex(javax.accessibility.AccessibleText.SENTENCE, 0)), "SENT after 0");
        check("Hello world. ".equals(t.getBeforeIndex(javax.accessibility.AccessibleText.SENTENCE, 13)), "SENT before 13");
        // selection defaults
        check(t.getSelectionStart() == 0 && t.getSelectionEnd() == 0, "empty selection 0/0");
        check(t.getSelectedText() == null, "no selection -> null text");
        // JTextField single-line variant: whole field is one sentence, trailing newline synthesized
        javax.swing.JTextField tf = new javax.swing.JTextField("abc def");
        javax.accessibility.AccessibleText t2 = tf.getAccessibleContext().getAccessibleText();
        check("def".equals(t2.getAtIndex(javax.accessibility.AccessibleText.WORD, 4)), "JTextField WORD at 4");
        check("abc def\n".equals(t2.getAtIndex(javax.accessibility.AccessibleText.SENTENCE, 0)), "single-line sentence keeps synthesized newline");
        check(t2.getCharCount() == 7, "field char count 7 (no newline in char count)");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}
