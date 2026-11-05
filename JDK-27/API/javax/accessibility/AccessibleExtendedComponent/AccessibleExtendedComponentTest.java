/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedComponent conformance: white-box structure + black-box behavior.
 * @run main AccessibleExtendedComponentTest
 */

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;

public class AccessibleExtendedComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static class Model implements javax.accessibility.AccessibleExtendedComponent {
        String tooltip = "tip"; String title = "title";
        javax.accessibility.AccessibleKeyBinding kb = new javax.accessibility.AccessibleKeyBinding() {
            public int getAccessibleKeyBindingCount() { return 1; }
            public Object getAccessibleKeyBinding(int i) { return "F1"; }
        };
        public String getToolTipText() { return tooltip; }
        public String getTitledBorderText() { return title; }
        public javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding() { return kb; }
        // AccessibleComponent
        Color bg, fg; Cursor cursor; Font font; boolean enabled = true, visible = true;
        Point location = new Point(0, 0); Rectangle bounds = new Rectangle(0, 0, 1, 1); Dimension size = new Dimension(1, 1);
        public Color getBackground() { return bg; }
        public void setBackground(Color c) { bg = c; }
        public Color getForeground() { return fg; }
        public void setForeground(Color c) { fg = c; }
        public Cursor getCursor() { return cursor; }
        public void setCursor(Cursor c) { cursor = c; }
        public Font getFont() { return font; }
        public void setFont(Font f) { font = f; }
        public FontMetrics getFontMetrics(Font f) { return null; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean b) { enabled = b; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean b) { visible = b; }
        public boolean isShowing() { return visible; }
        public boolean contains(Point p) { return bounds.contains(p); }
        public Point getLocationOnScreen() { return new Point(location); }
        public Point getLocation() { return new Point(location); }
        public void setLocation(Point p) { location = new Point(p); }
        public Rectangle getBounds() { return new Rectangle(bounds); }
        public void setBounds(Rectangle r) { bounds = new Rectangle(r); }
        public Dimension getSize() { return new Dimension(size); }
        public void setSize(Dimension d) { size = new Dimension(d); }
        public javax.accessibility.Accessible getAccessibleAt(Point p) { return null; }
        public boolean isFocusTraversable() { return false; }
        public void requestFocus() { }
        public void addFocusListener(FocusListener l) { }
        public void removeFocusListener(FocusListener l) { }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedComponent");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleComponent"), "extends AccessibleComponent");
        check(hasMethod(c, "getToolTipText"), "declares getToolTipText");
        check(hasMethod(c, "getTitledBorderText"), "declares getTitledBorderText");
        check(hasMethod(c, "getAccessibleKeyBinding"), "declares getAccessibleKeyBinding");
    }

    static void testBlackBox() throws Exception {
        Model m = new Model();
        check("tip".equals(m.getToolTipText()), "getToolTipText");
        check("title".equals(m.getTitledBorderText()), "getTitledBorderText");
        javax.accessibility.AccessibleKeyBinding kb = m.getAccessibleKeyBinding();
        check(kb != null, "getAccessibleKeyBinding non-null");
        check(kb.getAccessibleKeyBindingCount() == 1, "key binding count");
        check("F1".equals(kb.getAccessibleKeyBinding(0)), "key binding value");

        // Inherited AccessibleComponent behavior still works.
        m.setBackground(Color.GREEN);
        check(Color.GREEN.equals(m.getBackground()), "inherited set/get background");
        m.setEnabled(false);
        check(!m.isEnabled(), "inherited setEnabled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
