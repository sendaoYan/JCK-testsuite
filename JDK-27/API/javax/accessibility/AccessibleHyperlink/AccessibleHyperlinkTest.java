/*
 * @test
 * @summary javax.accessibility.AccessibleHyperlink conformance: white-box structure + black-box behavior.
 * @run main AccessibleHyperlinkTest
 */

import java.lang.reflect.*;

public class AccessibleHyperlinkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleHyperlink");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasMethod(c, "isValid"), "declares isValid");
        check(hasMethod(c, "getAccessibleActionCount"), "declares getAccessibleActionCount");
        check(hasMethod(c, "doAccessibleAction"), "declares doAccessibleAction");
        check(hasMethod(c, "getAccessibleActionDescription"), "declares getAccessibleActionDescription");
        check(hasMethod(c, "getAccessibleActionObject"), "declares getAccessibleActionObject");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleHyperlink link = new javax.accessibility.AccessibleHyperlink() {
            public boolean isValid() { return true; }
            public int getAccessibleActionCount() { return 2; }
            public boolean doAccessibleAction(int i) { return i == 0; }
            public String getAccessibleActionDescription(int i) { return i == 0 ? "open" : "copy"; }
            public Object getAccessibleActionObject(int i) { return "obj" + i; }
            public Object getAccessibleActionAnchor(int i) { return "anchor" + i; }
            public int getStartIndex() { return 10; }
            public int getEndIndex() { return 20; }
        };
        check(link.isValid(), "isValid");
        check(link.getAccessibleActionCount() == 2, "action count");
        check(link.doAccessibleAction(0), "doAction(0) succeeds");
        check(!link.doAccessibleAction(1), "doAction(1) fails");
        check("open".equals(link.getAccessibleActionDescription(0)), "description[0]");
        check("copy".equals(link.getAccessibleActionDescription(1)), "description[1]");
        check("obj1".equals(link.getAccessibleActionObject(1)), "action object");
        check("anchor0".equals(link.getAccessibleActionAnchor(0)), "action anchor");
        check(link.getStartIndex() == 10, "start index");
        check(link.getEndIndex() == 20, "end index");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass full-surface ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleHyperlink hl = new AccLink();
        check(hl.isValid(), "isValid");
        check(hl.getAccessibleActionCount() == 1, "count");
        check("click".equals(hl.getAccessibleActionDescription(0)), "description");
        check("obj0".equals(hl.getAccessibleActionObject(0)), "action object");
        check("anchor0".equals(hl.getAccessibleActionAnchor(0)), "anchor");
        check(hl.getStartIndex() == 4 && hl.getEndIndex() == 10, "start/end index");
        check(hl.doAccessibleAction(0), "doAction true");
        check(hl.getEndIndex() > hl.getStartIndex(), "end after start");
        check(javax.accessibility.AccessibleHyperlink.class.getSuperclass() == java.lang.Object.class, "extends Object (implements AccessibleAction)");
        check(javax.accessibility.AccessibleAction.class.isAssignableFrom(javax.accessibility.AccessibleHyperlink.class), "implements AccessibleAction");
        javax.accessibility.AccessibleHyperlink invalid = new AccLink() { public boolean isValid() { return false; } };
        check(!invalid.isValid(), "subclass overrides isValid");
    }
    static class AccLink extends javax.accessibility.AccessibleHyperlink {
        public boolean isValid() { return true; }
        public int getAccessibleActionCount() { return 1; }
        public String getAccessibleActionDescription(int i) { return "click"; }
        public Object getAccessibleActionObject(int i) { return "obj0"; }
        public Object getAccessibleActionAnchor(int i) { return "anchor0"; }
        public int getStartIndex() { return 4; }
        public int getEndIndex() { return 10; }
        public boolean doAccessibleAction(int i) { return true; }
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
