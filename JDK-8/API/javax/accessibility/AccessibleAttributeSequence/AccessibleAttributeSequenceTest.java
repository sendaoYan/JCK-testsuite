/*
 * @test
 * @summary javax.accessibility.AccessibleAttributeSequence conformance: class reflection.
 * @run main AccessibleAttributeSequenceTest
 */

public class AccessibleAttributeSequenceTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleAttributeSequence");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: public fields ctor + accessor contract ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleAttributeSequence s = new javax.accessibility.AccessibleAttributeSequence(3, 9, null);
        check(s.startIndex == 3 && s.endIndex == 9, "index fields");
        check(s.attributes == null, "null AttributeSet accepted");
        javax.swing.text.SimpleAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
        javax.accessibility.AccessibleAttributeSequence s2 = new javax.accessibility.AccessibleAttributeSequence(0, 4, attrs);
        check(s2.attributes == attrs, "attributes passthrough");
        s2.startIndex = 10;
        s2.endIndex = 20;
        check(s2.startIndex == 10 && s2.endIndex == 20, "public fields writable");
        check(javax.accessibility.AccessibleAttributeSequence.class.getDeclaredFields().length == 3, "3 public fields");
        boolean allPublic = true;
        for (java.lang.reflect.Field f : javax.accessibility.AccessibleAttributeSequence.class.getDeclaredFields()) if (!java.lang.reflect.Modifier.isPublic(f.getModifiers())) allPublic = false;
        check(allPublic, "all fields public");
        javax.swing.JTextArea ta = new javax.swing.JTextArea("abc def");
        javax.accessibility.AccessibleContext ac = ta.getAccessibleContext();
        check(ac instanceof javax.accessibility.AccessibleText, "JTextArea context is text");
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
