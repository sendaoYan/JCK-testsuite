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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
