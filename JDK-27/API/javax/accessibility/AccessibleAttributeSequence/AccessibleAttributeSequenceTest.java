/*
 * @test
 * @summary javax.accessibility.AccessibleAttributeSequence conformance: white-box structure + black-box behavior.
 * @run main AccessibleAttributeSequenceTest
 */

import java.lang.reflect.*;

public class AccessibleAttributeSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getFields()) if (f.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleAttributeSequence");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasField(c, "startIndex"), "public field startIndex");
        check(hasField(c, "endIndex"), "public field endIndex");
        check(hasField(c, "attributes"), "public field attributes");
    }

    static void testBlackBox() throws Exception {
        javax.swing.text.SimpleAttributeSet as = new javax.swing.text.SimpleAttributeSet();
        javax.accessibility.AccessibleAttributeSequence s =
                new javax.accessibility.AccessibleAttributeSequence(2, 7, as);
        check(s.startIndex == 2, "ctor startIndex");
        check(s.endIndex == 7, "ctor endIndex");
        check(s.attributes == as, "ctor attributes identity");

        s.startIndex = 3; s.endIndex = 9; s.attributes = null;
        check(s.startIndex == 3, "mutable startIndex");
        check(s.endIndex == 9, "mutable endIndex");
        check(s.attributes == null, "mutable attributes");

        javax.accessibility.AccessibleAttributeSequence empty =
                new javax.accessibility.AccessibleAttributeSequence(0, 0, null);
        check(empty.startIndex == 0 && empty.endIndex == 0 && empty.attributes == null,
                "zero/null ctor values retained");

        javax.accessibility.AccessibleAttributeSequence neg =
                new javax.accessibility.AccessibleAttributeSequence(-1, -2, as);
        check(neg.startIndex == -1 && neg.endIndex == -2, "negative indices retained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
