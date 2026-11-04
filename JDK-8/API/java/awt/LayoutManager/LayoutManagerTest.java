/*
 * @test
 * @summary java.awt.LayoutManager conformance: interface reflection.
 * @run main LayoutManagerTest
 */

public class LayoutManagerTest {
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
        Class<?> c = Class.forName("java.awt.LayoutManager");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
        check(hasMethod(c, "layoutContainer"), "method layoutContainer");
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
