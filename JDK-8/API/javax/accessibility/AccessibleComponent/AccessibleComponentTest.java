/*
 * @test
 * @summary javax.accessibility.AccessibleComponent conformance: interface reflection.
 * @run main AccessibleComponentTest
 */

public class AccessibleComponentTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleComponent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getForeground"), "method getForeground");
        check(hasMethod(c, "setForeground"), "method setForeground");
        check(hasMethod(c, "getCursor"), "method getCursor");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "isVisible"), "method isVisible");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "isShowing"), "method isShowing");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "getLocationOnScreen"), "method getLocationOnScreen");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "isFocusTraversable"), "method isFocusTraversable");
        check(hasMethod(c, "requestFocus"), "method requestFocus");
        check(hasMethod(c, "addFocusListener"), "method addFocusListener");
        check(hasMethod(c, "removeFocusListener"), "method removeFocusListener");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass full-surface + label no-component ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleComponent c = new RecComp();
        check(c.getBackground() != null && c.getForeground() != null, "colors");
        check(c.getLocation().x == 1 && c.getSize().width == 10, "geometry");
        check(c.isShowing() == false, "showing");
        check(c.contains(new java.awt.Point(1, 1)), "contains(Point)");
        check(c.getCursor() != null, "cursor");
        check(javax.accessibility.AccessibleComponent.class.isInterface(), "interface");
        javax.swing.JLabel l = new javax.swing.JLabel();
        javax.accessibility.AccessibleComponent lc = l.getAccessibleContext().getAccessibleComponent();
        check(lc != null, "JLabel has component");
        javax.accessibility.AccessibleComponent xc = new RecCtx().getAccessibleComponent();
        check(xc == null, "default context component null");
    }
    static class RecComp implements javax.accessibility.AccessibleComponent {
        public java.awt.Color getBackground() { return java.awt.Color.RED; }
        public void setBackground(java.awt.Color c) {}
        public java.awt.Color getForeground() { return java.awt.Color.BLUE; }
        public void setForeground(java.awt.Color c) {}
        public java.awt.Cursor getCursor() { return java.awt.Cursor.getDefaultCursor(); }
        public void setCursor(java.awt.Cursor c) {}
        public java.awt.Font getFont() { return null; }
        public void setFont(java.awt.Font f) {}
        public java.awt.FontMetrics getFontMetrics(java.awt.Font f) { return null; }
        public boolean isEnabled() { return true; }
        public void setEnabled(boolean b) {}
        public boolean isVisible() { return true; }
        public void setVisible(boolean b) {}
        public boolean isShowing() { return false; }
        public boolean contains(java.awt.Point p) { return p.x == 1; }
        public java.awt.Point getLocationOnScreen() { return new java.awt.Point(0, 0); }
        public java.awt.Point getLocation() { return new java.awt.Point(1, 2); }
        public void setLocation(java.awt.Point p) {}
        public java.awt.Rectangle getBounds() { return new java.awt.Rectangle(0, 0, 10, 10); }
        public void setBounds(java.awt.Rectangle r) {}
        public java.awt.Dimension getSize() { return new java.awt.Dimension(10, 10); }
        public void setSize(java.awt.Dimension d) {}
        public javax.accessibility.Accessible getAccessibleAt(java.awt.Point p) { return null; }
        public boolean isFocusTraversable() { return true; }
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
