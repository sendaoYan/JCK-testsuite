/*
 * @test
 * @summary javax.accessibility.AccessibleTextSequence conformance: class reflection.
 * @run main AccessibleTextSequenceTest
 */

public class AccessibleTextSequenceTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTextSequence");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: public fields + real sequence from extended text ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleTextSequence s = new javax.accessibility.AccessibleTextSequence(2, 5, "abc");
        check(s.startIndex == 2 && s.endIndex == 5 && "abc".equals(s.text), "all three fields");
        s.text = "xyz";
        check("xyz".equals(s.text), "public writable");
        check(javax.accessibility.AccessibleTextSequence.class.getDeclaredFields().length == 3, "3 fields");
        javax.swing.JTextArea ta = new javax.swing.JTextArea("Hello world. Second sentence here.");
        javax.accessibility.AccessibleExtendedText et = (javax.accessibility.AccessibleExtendedText) ta.getAccessibleContext().getAccessibleText();
        javax.accessibility.AccessibleTextSequence real = et.getTextSequenceAt(javax.accessibility.AccessibleText.WORD, 6);
        check(real.startIndex == 6 && real.endIndex == 11 && "world".equals(real.text), "real sequence fields");
        javax.accessibility.AccessibleTextSequence beyond = et.getTextSequenceAt(javax.accessibility.AccessibleText.WORD, 100);
        check(beyond == null || beyond.text != null, "out-of-range probe");
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
