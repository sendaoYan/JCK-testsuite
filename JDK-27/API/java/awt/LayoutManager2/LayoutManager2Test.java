/*
 * @test
 * @summary java.awt.LayoutManager2 conformance: white-box structure + black-box behavior.
 * @run main LayoutManager2Test
 */

import java.lang.reflect.*;

public class LayoutManager2Test {
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
        Class<?> c = Class.forName("java.awt.LayoutManager2", false, LayoutManager2Test.class.getClassLoader());
        check(c != null, "type loads: java.awt.LayoutManager2");
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
        check(hasInterface(c, "java.awt.LayoutManager"), "extends LayoutManager");
    }

    static void testBlackBox() throws Exception {
        java.awt.LayoutManager2 impl = new java.awt.LayoutManager2() {
            public void addLayoutComponent(String n, java.awt.Component c) {}
            public void addLayoutComponent(java.awt.Component c, Object o) {}
            public void removeLayoutComponent(java.awt.Component c) {}
            public java.awt.Dimension preferredLayoutSize(java.awt.Container p) { return null; }
            public java.awt.Dimension minimumLayoutSize(java.awt.Container p) { return null; }
            public java.awt.Dimension maximumLayoutSize(java.awt.Container p) { return null; }
            public float getLayoutAlignmentX(java.awt.Container p) { return 0f; }
            public float getLayoutAlignmentY(java.awt.Container p) { return 0f; }
            public void invalidateLayout(java.awt.Container p) {}
            public void layoutContainer(java.awt.Container p) {}
        };
        impl.addLayoutComponent((java.awt.Component)null, (Object)"x");
        impl.addLayoutComponent((String)null, (java.awt.Component)null);
        impl.removeLayoutComponent(null);
        check(impl.maximumLayoutSize(null) == null, "maximum null");
        check(impl.getLayoutAlignmentX(null) == 0f, "alignX 0");
        check(impl.getLayoutAlignmentY(null) == 0f, "alignY 0");
        impl.invalidateLayout(null);
        impl.layoutContainer(null);
        check(true, "all callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // LayoutManager2 adds constraint-based add plus alignment and maximum size
        final java.util.List<String> log = new java.util.ArrayList<String>();
        java.awt.LayoutManager2 lm2 = new java.awt.LayoutManager2() {
            public void addLayoutComponent(java.awt.Component comp, Object constraints) { log.add("add2:" + constraints); }
            public void addLayoutComponent(String name, java.awt.Component comp) { log.add("add1"); }
            public void removeLayoutComponent(java.awt.Component comp) { log.add("remove"); }
            public java.awt.Dimension preferredLayoutSize(java.awt.Container parent) { return new java.awt.Dimension(20, 21); }
            public java.awt.Dimension minimumLayoutSize(java.awt.Container parent) { return new java.awt.Dimension(8, 9); }
            public java.awt.Dimension maximumLayoutSize(java.awt.Container parent) { return new java.awt.Dimension(1000, 1001); }
            public float getLayoutAlignmentX(java.awt.Container target) { return 0.25f; }
            public float getLayoutAlignmentY(java.awt.Container target) { return 0.75f; }
            public void invalidateLayout(java.awt.Container target) { log.add("invalidate"); }
            public void layoutContainer(java.awt.Container parent) { log.add("layout"); }
        };
        check(lm2 instanceof java.awt.LayoutManager, "LayoutManager2 extends LayoutManager");
        java.awt.Container c = new java.awt.Container();
        c.setLayout(lm2);
        check(c.getLayout() == lm2, "LayoutManager2 accepted as layout");
        check(lm2.maximumLayoutSize(c).equals(new java.awt.Dimension(1000, 1001)), "maximumLayoutSize honored");
        check(lm2.getLayoutAlignmentX(c) == 0.25f, "getLayoutAlignmentX honored");
        check(lm2.getLayoutAlignmentY(c) == 0.75f, "getLayoutAlignmentY honored");
        lm2.invalidateLayout(c);
        check(log.contains("invalidate"), "invalidateLayout invoked");
        // constraint add path
        c.add(new java.awt.Canvas(), "key");
        check(log.contains("add2:key"), "Container.add(Component,Object) routes to 2-arg addLayoutComponent: " + log);
        // Container.getAlignmentX delegates to LayoutManager2.getLayoutAlignmentX (observed)
        check(c.getAlignmentX() == 0.25f, "Container.getAlignmentX delegates to LM2: " + c.getAlignmentX());
        check(c.getAlignmentY() == 0.75f, "Container.getAlignmentY delegates to LM2: " + c.getAlignmentY());
        // built-in LayoutManager2 implementations
        java.awt.Container bc = new java.awt.Container(); bc.setLayout(new java.awt.BorderLayout());
        check(bc.getLayout() instanceof java.awt.LayoutManager2, "BorderLayout is LayoutManager2");
        java.awt.Container gb = new java.awt.Container(); gb.setLayout(new java.awt.GridBagLayout());
        check(gb.getLayout() instanceof java.awt.LayoutManager2, "GridBagLayout is LayoutManager2");
        java.awt.Container cc = new java.awt.Container(); cc.setLayout(new java.awt.CardLayout());
        check(cc.getLayout() instanceof java.awt.LayoutManager2, "CardLayout is LayoutManager2");
        // but FlowLayout is only LayoutManager (no LM2)
        check(!(new java.awt.FlowLayout() instanceof java.awt.LayoutManager2),
              "FlowLayout is NOT a LayoutManager2 (observed)");
    }

}
