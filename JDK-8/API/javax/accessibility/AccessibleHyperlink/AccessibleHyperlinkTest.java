/*
 * @test
 * @summary javax.accessibility.AccessibleHyperlink conformance: abstract class reflection.
 * @run main AccessibleHyperlinkTest
 */

public class AccessibleHyperlinkTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleHyperlink");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleAction"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "getAccessibleActionCount"), "method getAccessibleActionCount");
        check(hasMethod(c, "doAccessibleAction"), "method doAccessibleAction");
        check(hasMethod(c, "getAccessibleActionDescription"), "method getAccessibleActionDescription");
        check(hasMethod(c, "getAccessibleActionObject"), "method getAccessibleActionObject");
        check(hasMethod(c, "getAccessibleActionAnchor"), "method getAccessibleActionAnchor");
        check(hasMethod(c, "getStartIndex"), "method getStartIndex");
        check(hasMethod(c, "getEndIndex"), "method getEndIndex");
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
