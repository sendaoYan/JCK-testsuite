/*
 * @test
 * @summary javax.swing.plaf.RootPaneUI conformance: white-box structure + black-box behavior.
 * @run main RootPaneUITest
 */

import java.lang.reflect.Modifier;

public class RootPaneUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.RootPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ComponentUI"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        // Minimal anonymous subclass: its implicit constructor directly invokes the
        // protected <init>() of javax.swing.plaf.RootPaneUI (owner + name + desc in bytecode),
        // which is the method this tranche must re-cover. The base class declares no
        // abstract methods, so an empty anonymous body is sufficient.
        javax.swing.plaf.RootPaneUI ui = new javax.swing.plaf.RootPaneUI() {};
        check(ui != null, "RootPaneUI anonymous subclass instantiates");
        check(ui instanceof javax.swing.plaf.RootPaneUI, "instanceof RootPaneUI");

        // Lightweight, never-displayed component + offscreen graphics keep this headless.
        javax.swing.JComponent c = new javax.swing.JPanel();
        java.awt.Graphics g = new java.awt.image.BufferedImage(
                1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            // Default ComponentUI install/paint/update/uninstall are no-ops and must not throw.
            ui.installUI(c);
            ui.paint(g, c);
            ui.update(g, c);
            ui.uninstallUI(c);
        } finally {
            g.dispose();
        }

        // Default sizing queries return null (no look-and-feel delegate installed).
        check(ui.getPreferredSize(c) == null, "getPreferredSize default null");
        check(ui.getMinimumSize(c) == null, "getMinimumSize default null");
        check(ui.getMaximumSize(c) == null, "getMaximumSize default null");

        // Hit detection for a 0x0, never-validated component reports not contained.
        check(!ui.contains(c, 0, 0), "contains default false for empty bounds");

        // Baseline defaults: no reasonable baseline; resize behavior OTHER.
        check(ui.getBaseline(c, 10, 10) == -1, "getBaseline default -1");
        check(ui.getBaselineResizeBehavior(c) == java.awt.Component.BaselineResizeBehavior.OTHER,
                "getBaselineResizeBehavior default OTHER");

        // Accessible children of a childless panel: zero, and index 0 resolves to null.
        check(ui.getAccessibleChildrenCount(c) == 0, "getAccessibleChildrenCount default 0");
        check(ui.getAccessibleChild(c, 0) == null, "getAccessibleChild default null");

        // Parameter-validation contracts on baseline helpers.
        boolean npeBaseline = false;
        try { ui.getBaseline(null, 0, 0); } catch (NullPointerException e) { npeBaseline = true; }
        check(npeBaseline, "getBaseline(null) throws NPE");

        boolean npeBehavior = false;
        try { ui.getBaselineResizeBehavior(null); } catch (NullPointerException e) { npeBehavior = true; }
        check(npeBehavior, "getBaselineResizeBehavior(null) throws NPE");

        boolean iaeBaseline = false;
        try { ui.getBaseline(c, -1, 0); } catch (IllegalArgumentException e) { iaeBaseline = true; }
        check(iaeBaseline, "getBaseline negative width throws IAE");
    }
}
