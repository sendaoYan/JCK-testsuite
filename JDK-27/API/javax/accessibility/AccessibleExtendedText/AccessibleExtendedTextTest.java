/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedText conformance: white-box structure + black-box behavior.
 * @run main AccessibleExtendedTextTest
 */

import java.lang.reflect.*;
import java.awt.*;

public class AccessibleExtendedTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class Model implements javax.accessibility.AccessibleExtendedText {
        String text = "hello world";
        public String getTextRange(int start, int end) { return text.substring(start, end); }
        public javax.accessibility.AccessibleTextSequence getTextSequenceAt(int start, int end) {
            return new javax.accessibility.AccessibleTextSequence(start, end, text.substring(start, end));
        }
        public javax.accessibility.AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            return new javax.accessibility.AccessibleTextSequence(index, index + 1, "a");
        }
        public javax.accessibility.AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            return new javax.accessibility.AccessibleTextSequence(index - 1, index, "b");
        }
        public Rectangle getTextBounds(int start, int end) { return new Rectangle(start, 0, end - start, 1); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getTextRange"), "declares getTextRange");
        check(hasMethod(c, "getTextSequenceAt"), "declares getTextSequenceAt");
        check(hasMethod(c, "getTextSequenceAfter"), "declares getTextSequenceAfter");
        check(hasMethod(c, "getTextSequenceBefore"), "declares getTextSequenceBefore");
        check(hasMethod(c, "getTextBounds"), "declares getTextBounds");
    }

    static void testBlackBox() throws Exception {
        check(javax.accessibility.AccessibleExtendedText.LINE == 4, "LINE == 4");
        check(javax.accessibility.AccessibleExtendedText.ATTRIBUTE_RUN == 5, "ATTRIBUTE_RUN == 5");

        Model m = new Model();
        check("hello".equals(m.getTextRange(0, 5)), "getTextRange");
        javax.accessibility.AccessibleTextSequence seq = m.getTextSequenceAt(0, 5);
        check(seq.startIndex == 0 && seq.endIndex == 5 && "hello".equals(seq.text), "getTextSequenceAt");
        check("a".equals(m.getTextSequenceAfter(javax.accessibility.AccessibleExtendedText.LINE, 0).text),
                "getTextSequenceAfter");
        check("b".equals(m.getTextSequenceBefore(javax.accessibility.AccessibleExtendedText.LINE, 1).text),
                "getTextSequenceBefore");
        Rectangle r = m.getTextBounds(0, 5);
        check(r.x == 0 && r.width == 5, "getTextBounds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: getTextSequenceAt + ATTRIBUTE_RUN on real text ----
    static void testContractDeep() throws Exception {
        javax.swing.JTextArea ta = new javax.swing.JTextArea("Hello world. Second sentence here.");
        javax.accessibility.AccessibleText t = ta.getAccessibleContext().getAccessibleText();
        check(t instanceof javax.accessibility.AccessibleExtendedText, "JTextArea text is extended");
        javax.accessibility.AccessibleExtendedText et = (javax.accessibility.AccessibleExtendedText) t;
        check(javax.accessibility.AccessibleExtendedText.ATTRIBUTE_RUN == 5, "ATTRIBUTE_RUN 5");
        javax.accessibility.AccessibleTextSequence seq = et.getTextSequenceAt(javax.accessibility.AccessibleText.WORD, 6);
        check(seq != null && seq.startIndex == 6 && seq.endIndex == 11 && "world".equals(seq.text), "WORD sequence 6-11 [world]");
        javax.accessibility.AccessibleTextSequence cseq = et.getTextSequenceAt(javax.accessibility.AccessibleText.CHARACTER, 0);
        check(cseq != null && cseq.startIndex == 0 && cseq.endIndex == 1 && "H".equals(cseq.text), "CHAR sequence 0-1 [H]");
        javax.accessibility.AccessibleTextSequence after = et.getTextSequenceAfter(javax.accessibility.AccessibleText.WORD, 0);
        check(after != null && after.startIndex == 5 && after.endIndex == 6 && " ".equals(after.text), "sequence after WORD@0 is the separator 5-6");
        check(et.getTextSequenceAt(javax.accessibility.AccessibleText.WORD, 100) == null, "out-of-range sequence null");
        check(et.getTextBounds(0, 5) == null, "getTextBounds null headless (no view laid out)");
        check(javax.accessibility.AccessibleExtendedText.class.isInterface(), "interface");
        check(!javax.accessibility.AccessibleText.class.isAssignableFrom(javax.accessibility.AccessibleExtendedText.class), "independent interface: NOT a subinterface of AccessibleText");
        check(javax.accessibility.AccessibleExtendedText.class.getFields().length == 2, "exactly two own constants LINE/ATTRIBUTE_RUN");
        check(javax.accessibility.AccessibleExtendedText.LINE == 4, "LINE = 4 (extended-only part kind)");
        check(javax.accessibility.AccessibleExtendedText.ATTRIBUTE_RUN == 5, "ATTRIBUTE_RUN = 5 (extended-only part kind)");
        check(javax.accessibility.AccessibleText.CHARACTER == 1 && javax.accessibility.AccessibleText.WORD == 2 && javax.accessibility.AccessibleText.SENTENCE == 3, "part constants 1/2/3 (used with extended API)");
        check(t.getCaretPosition() == 0, "caret 0");
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
