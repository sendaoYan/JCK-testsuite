/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedText conformance: interface reflection.
 * @run main AccessibleExtendedTextTest
 */

public class AccessibleExtendedTextTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTextRange"), "method getTextRange");
        check(hasMethod(c, "getTextSequenceAt"), "method getTextSequenceAt");
        check(hasMethod(c, "getTextSequenceAfter"), "method getTextSequenceAfter");
        check(hasMethod(c, "getTextSequenceBefore"), "method getTextSequenceBefore");
        check(hasMethod(c, "getTextBounds"), "method getTextBounds");
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
