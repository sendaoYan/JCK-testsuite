/*
 * @test
 * @summary javax.accessibility.AccessibleKeyBinding conformance: interface reflection.
 * @run main AccessibleKeyBindingTest
 */

public class AccessibleKeyBindingTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleKeyBinding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleKeyBindingCount"), "method getAccessibleKeyBindingCount");
        check(hasMethod(c, "getAccessibleKeyBinding"), "method getAccessibleKeyBinding");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real mnemonic binding via extended component ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleKeyBinding.class.isInterface(), "interface");
        javax.accessibility.AccessibleKeyBinding kb = new javax.accessibility.AccessibleKeyBinding() {
            public int getAccessibleKeyBindingCount() { return 2; }
            public Object getAccessibleKeyBinding(int i) { return i == 0 ? (Object) Integer.valueOf(java.awt.event.KeyEvent.VK_A) : (Object) javax.swing.KeyStroke.getKeyStroke('x', 0); }
        };
        check(kb.getAccessibleKeyBindingCount() == 2, "count");
        check(((Integer) kb.getAccessibleKeyBinding(0)).intValue() == java.awt.event.KeyEvent.VK_A, "mnemonic-code form");
        check(kb.getAccessibleKeyBinding(1) instanceof javax.swing.KeyStroke, "KeyStroke form");
        // key bindings surface through AccessibleExtendedComponent (not AccessibleContext)
        javax.swing.JButton b = new javax.swing.JButton();
        javax.accessibility.AccessibleComponent ac0 = b.getAccessibleContext().getAccessibleComponent();
        check(ac0 instanceof javax.accessibility.AccessibleExtendedComponent, "button component is extended");
        check(((javax.accessibility.AccessibleExtendedComponent) ac0).getAccessibleKeyBinding() == null, "no mnemonic -> no key binding");
        b.setMnemonic(java.awt.event.KeyEvent.VK_P);
        javax.accessibility.AccessibleKeyBinding real = ((javax.accessibility.AccessibleExtendedComponent) ac0).getAccessibleKeyBinding();
        check(real != null && real.getAccessibleKeyBindingCount() == 1, "mnemonic surfaces one binding");
        check(real.getAccessibleKeyBinding(0) instanceof javax.swing.KeyStroke, "binding is a KeyStroke");
        check("pressed P".equals(String.valueOf(real.getAccessibleKeyBinding(0))), "binding toString [pressed P]");
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
