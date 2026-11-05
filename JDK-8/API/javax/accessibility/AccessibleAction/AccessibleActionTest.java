/*
 * @test
 * @summary javax.accessibility.AccessibleAction conformance: interface reflection.
 * @run main AccessibleActionTest
 */

public class AccessibleActionTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleAction");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleActionCount"), "method getAccessibleActionCount");
        check(hasMethod(c, "getAccessibleActionDescription"), "method getAccessibleActionDescription");
        check(hasMethod(c, "doAccessibleAction"), "method doAccessibleAction");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constant table + recording + real JButton action ----
    static void testContractDeep() throws Exception {
        check("click".equals(javax.accessibility.AccessibleAction.CLICK), "click");
        check("toggleexpand".equals(javax.accessibility.AccessibleAction.TOGGLE_EXPAND), "toggleexpand (no space)");
        check("increment".equals(javax.accessibility.AccessibleAction.INCREMENT), "increment");
        check("decrement".equals(javax.accessibility.AccessibleAction.DECREMENT), "decrement");
        check("toggle popup".equals(javax.accessibility.AccessibleAction.TOGGLE_POPUP), "toggle popup (with space)");
        check("click".equals(javax.accessibility.AccessibleAction.CLICK.toString()), "toString is value");
        javax.accessibility.AccessibleAction a = new javax.accessibility.AccessibleAction() {
            public int getAccessibleActionCount() { return 1; }
            public String getAccessibleActionDescription(int i) { return javax.accessibility.AccessibleAction.CLICK; }
            public boolean doAccessibleAction(int i) { return true; }
        };
        check(a.getAccessibleActionCount() == 1, "count");
        check(a.doAccessibleAction(0), "do");
        check(javax.accessibility.AccessibleAction.class.isInterface(), "interface");
        javax.swing.JButton b = new javax.swing.JButton("Press");
        javax.accessibility.AccessibleAction ba = b.getAccessibleContext().getAccessibleAction();
        check(ba != null && ba.getAccessibleActionCount() == 1, "button one action");
        check(javax.accessibility.AccessibleAction.CLICK.equals(ba.getAccessibleActionDescription(0)), "button action is click");
        javax.swing.JLabel l = new javax.swing.JLabel();
        check(l.getAccessibleContext().getAccessibleAction() == null, "label no action");
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
