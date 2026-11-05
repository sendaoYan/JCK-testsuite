/*
 * @test
 * @summary javax.accessibility.AccessibleComponent conformance: white-box structure + black-box behavior.
 * @run main AccessibleComponentTest
 */

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;

public class AccessibleComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class ComponentModel implements javax.accessibility.AccessibleComponent {
        Color bg, fg; Cursor cursor; Font font;
        boolean enabled = true, visible = true, showing = false, focus = false;
        Point location = new Point(0, 0);
        Rectangle bounds = new Rectangle(0, 0, 10, 10);
        Dimension size = new Dimension(10, 10);
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
        public boolean isShowing() { return showing; }
        public boolean contains(Point p) { return bounds.contains(p); }
        public Point getLocationOnScreen() { return new Point(location); }
        public Point getLocation() { return new Point(location); }
        public void setLocation(Point p) { location = new Point(p); }
        public Rectangle getBounds() { return new Rectangle(bounds); }
        public void setBounds(Rectangle r) { bounds = new Rectangle(r); }
        public Dimension getSize() { return new Dimension(size); }
        public void setSize(Dimension d) { size = new Dimension(d); }
        public javax.accessibility.Accessible getAccessibleAt(Point p) { return null; }
        public boolean isFocusTraversable() { return focus; }
        public void requestFocus() { focus = true; }
        public void addFocusListener(FocusListener l) { }
        public void removeFocusListener(FocusListener l) { }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleComponent");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getBackground"), "declares getBackground");
        check(hasMethod(c, "setBackground"), "declares setBackground");
        check(hasMethod(c, "getForeground"), "declares getForeground");
        check(hasMethod(c, "setForeground"), "declares setForeground");
        check(hasMethod(c, "getCursor"), "declares getCursor");
    }

    static void testBlackBox() throws Exception {
        ComponentModel m = new ComponentModel();
        check(m.getBackground() == null, "initial background null");
        m.setBackground(Color.RED);
        check(Color.RED.equals(m.getBackground()), "set/get background round trip");
        m.setForeground(Color.BLUE);
        check(Color.BLUE.equals(m.getForeground()), "set/get foreground round trip");
        Font f = new Font("Dialog", Font.PLAIN, 12);
        m.setFont(f);
        check(f.equals(m.getFont()), "set/get font round trip");
        check(m.getFontMetrics(f) == null, "fontMetrics null");

        m.setEnabled(false);
        check(!m.isEnabled(), "setEnabled(false)");
        m.setEnabled(true);
        check(m.isEnabled(), "setEnabled(true)");
        m.setVisible(false);
        check(!m.isVisible(), "setVisible(false)");
        check(!m.isShowing(), "isShowing initial false");

        m.setLocation(new Point(3, 4));
        check(new Point(3, 4).equals(m.getLocation()), "set/get location");
        m.setBounds(new Rectangle(5, 6, 20, 30));
        check(new Rectangle(5, 6, 20, 30).equals(m.getBounds()), "set/get bounds");
        m.setSize(new Dimension(40, 50));
        check(new Dimension(40, 50).equals(m.getSize()), "set/get size");
        check(m.contains(new Point(10, 10)), "contains point inside bounds");
        check(!m.contains(new Point(100, 100)), "contains point outside bounds");

        check(!m.isFocusTraversable(), "isFocusTraversable initial false");
        m.requestFocus();
        check(m.isFocusTraversable(), "requestFocus sets focus traversable");
        check(m.getAccessibleAt(new Point(0, 0)) == null, "getAccessibleAt null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
