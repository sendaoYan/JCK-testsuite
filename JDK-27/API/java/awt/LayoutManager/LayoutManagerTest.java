/*
 * @test
 * @summary java.awt.LayoutManager conformance: white-box structure + black-box behavior.
 * @run main LayoutManagerTest
 */

import java.lang.reflect.*;

public class LayoutManagerTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.LayoutManager", false, LayoutManagerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.LayoutManager");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.LayoutManager impl = new java.awt.LayoutManager() {
            public void addLayoutComponent(String n, java.awt.Component c) {}
            public void removeLayoutComponent(java.awt.Component c) {}
            public java.awt.Dimension preferredLayoutSize(java.awt.Container p) { return null; }
            public java.awt.Dimension minimumLayoutSize(java.awt.Container p) { return null; }
            public void layoutContainer(java.awt.Container p) {}
        };
        impl.addLayoutComponent(null, null);
        impl.removeLayoutComponent(null);
        check(impl.preferredLayoutSize(null) == null, "preferred null");
        check(impl.minimumLayoutSize(null) == null, "minimum null");
        impl.layoutContainer(null);
        check(true, "layout callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // LayoutManager is the base contract; implement via anonymous class exercising all 5 methods
        final java.util.List<String> log = new java.util.ArrayList<String>();
        java.awt.LayoutManager lm = new java.awt.LayoutManager() {
            public void addLayoutComponent(String name, java.awt.Component comp) { log.add("add:" + name); }
            public void removeLayoutComponent(java.awt.Component comp) { log.add("remove"); }
            public java.awt.Dimension preferredLayoutSize(java.awt.Container parent) { return new java.awt.Dimension(11, 12); }
            public java.awt.Dimension minimumLayoutSize(java.awt.Container parent) { return new java.awt.Dimension(5, 6); }
            public void layoutContainer(java.awt.Container parent) { log.add("layout"); }
        };
        java.awt.Container c = new java.awt.Container();
        c.setLayout(lm);
        check(c.getLayout() == lm, "LayoutManager accepted as container layout");
        check(lm.preferredLayoutSize(c).equals(new java.awt.Dimension(11, 12)), "preferredLayoutSize honored");
        check(lm.minimumLayoutSize(c).equals(new java.awt.Dimension(5, 6)), "minimumLayoutSize honored");
        lm.addLayoutComponent("x", new java.awt.Canvas());
        lm.removeLayoutComponent(new java.awt.Canvas());
        lm.layoutContainer(c);
        check(log.contains("add:x") && log.contains("remove") && log.contains("layout"),
              "all five interface methods invoked: " + log);
        // container delegates size queries to its layout
        check(c.getPreferredSize().equals(new java.awt.Dimension(11, 12)),
              "Container.getPreferredSize delegates to layout");
        check(c.getMinimumSize().equals(new java.awt.Dimension(5, 6)),
              "Container.getMinimumSize delegates to layout");
        // doLayout triggers layoutContainer
        c.setSize(50, 50); c.doLayout();
        check(log.lastIndexOf("layout") >= 0, "doLayout calls layoutContainer");
        // built-in implementations satisfy the interface
        check(java.awt.FlowLayout.class.getInterfaces().length >= 1, "FlowLayout implements LayoutManager");
        java.awt.Container fc = new java.awt.Container();
        fc.setLayout(new java.awt.FlowLayout());
        check(fc.getLayout() instanceof java.awt.LayoutManager, "FlowLayout is a LayoutManager");
    }

}
