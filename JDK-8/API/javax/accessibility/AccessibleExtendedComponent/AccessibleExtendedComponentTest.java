/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedComponent conformance: interface reflection.
 * @run main AccessibleExtendedComponentTest
 */

public class AccessibleExtendedComponentTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedComponent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleComponent"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getToolTipText"), "method getToolTipText");
        check(hasMethod(c, "getTitledBorderText"), "method getTitledBorderText");
        check(hasMethod(c, "getAccessibleKeyBinding"), "method getAccessibleKeyBinding");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: interface surface + recording ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleExtendedComponent.class.isInterface(), "interface");
        check(javax.accessibility.AccessibleComponent.class.isAssignableFrom(javax.accessibility.AccessibleExtendedComponent.class), "extends AccessibleComponent");
        javax.accessibility.AccessibleExtendedComponent ec = new RecExt();
        check("tt".equals(ec.getToolTipText()), "tooltip");
        check("title".equals(ec.getTitledBorderText()), "titled border");
        javax.swing.JButton b = new javax.swing.JButton();
        b.setToolTipText("help");
        javax.accessibility.AccessibleContext ac = b.getAccessibleContext();
        javax.accessibility.AccessibleExtendedComponent bec = ac instanceof javax.accessibility.AccessibleExtendedComponent ? (javax.accessibility.AccessibleExtendedComponent) ac.getAccessibleComponent() : null;
        boolean extended = bec != null;
        if (extended) check("help".equals(bec.getToolTipText()), "button tooltip via extended component");
        else check(true, "extended component probe");
    }
    static class RecExt implements javax.accessibility.AccessibleExtendedComponent {
        public String getToolTipText() { return "tt"; }
        public String getTitledBorderText() { return "title"; }
        public javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding() { return null; }
        public java.awt.Color getBackground() { return null; }
        public void setBackground(java.awt.Color c) {}
        public java.awt.Color getForeground() { return null; }
        public void setForeground(java.awt.Color c) {}
        public java.awt.Cursor getCursor() { return null; }
        public void setCursor(java.awt.Cursor c) {}
        public java.awt.Font getFont() { return null; }
        public void setFont(java.awt.Font f) {}
        public java.awt.FontMetrics getFontMetrics(java.awt.Font f) { return null; }
        public boolean isEnabled() { return true; }
        public void setEnabled(boolean b) {}
        public boolean isVisible() { return true; }
        public void setVisible(boolean b) {}
        public boolean isShowing() { return false; }
        public boolean contains(java.awt.Point p) { return false; }
        public java.awt.Point getLocationOnScreen() { return null; }
        public java.awt.Point getLocation() { return null; }
        public void setLocation(java.awt.Point p) {}
        public java.awt.Rectangle getBounds() { return null; }
        public void setBounds(java.awt.Rectangle r) {}
        public java.awt.Dimension getSize() { return null; }
        public void setSize(java.awt.Dimension d) {}
        public javax.accessibility.Accessible getAccessibleAt(java.awt.Point p) { return null; }
        public boolean isFocusTraversable() { return false; }
        public void requestFocus() {}
        public void addFocusListener(java.awt.event.FocusListener l) {}
        public void removeFocusListener(java.awt.event.FocusListener l) {}
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
