/*
 * @test
 * @summary javax.accessibility.AccessibleText conformance: interface reflection.
 * @run main AccessibleTextTest
 */

public class AccessibleTextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getIndexAtPoint"), "method getIndexAtPoint");
        check(hasMethod(c, "getCharacterBounds"), "method getCharacterBounds");
        check(hasMethod(c, "getCharCount"), "method getCharCount");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "getAtIndex"), "method getAtIndex");
        check(hasMethod(c, "getAfterIndex"), "method getAfterIndex");
        check(hasMethod(c, "getBeforeIndex"), "method getBeforeIndex");
        check(hasMethod(c, "getCharacterAttribute"), "method getCharacterAttribute");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
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
