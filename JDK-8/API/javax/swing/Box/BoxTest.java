/*
 * @test
 * @summary javax.swing.Box conformance: type shape via reflection + factory geometry
 *          (rigid areas, struts, glues, Filler), headless-safe.
 * @run main BoxTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.UIManager;

public class BoxTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.Box", false, BoxTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createHorizontalBox"), "method createHorizontalBox");
        check(names.contains("createVerticalBox"), "method createVerticalBox");
        check(names.contains("createRigidArea"), "method createRigidArea");
        check(names.contains("createHorizontalStrut"), "method createHorizontalStrut");
        check(names.contains("createVerticalStrut"), "method createVerticalStrut");
        check(names.contains("createGlue"), "method createGlue");
        check(names.contains("createHorizontalGlue"), "method createHorizontalGlue");
    }

    static void testBoxContainers() {
        Box h = Box.createHorizontalBox();
        Box v = Box.createVerticalBox();
        check(h.getLayout() instanceof BoxLayout, "createHorizontalBox uses BoxLayout");
        check(v.getLayout() instanceof BoxLayout, "createVerticalBox uses BoxLayout");
        check(((BoxLayout) h.getLayout()).getAxis() == BoxLayout.X_AXIS, "horizontal box axis X");
        check(((BoxLayout) v.getLayout()).getAxis() == BoxLayout.Y_AXIS, "vertical box axis Y");
        check(h.getAccessibleContext() != null, "Box is Accessible");
        // a plain Box(axis) is a BoxLayout container built directly
        Box plain = new Box(BoxLayout.Y_AXIS);
        check(plain.getComponentCount() == 0, "plain Box empty");
        check(plain.getLayout() instanceof BoxLayout, "plain Box laid out by BoxLayout");
        // stacking children of a vertical box proves the layout is live
        Component a = Box.createRigidArea(new Dimension(10, 5));
        Component b = Box.createRigidArea(new Dimension(10, 7));
        v.add(a); v.add(b);
        v.setBounds(0, 0, 10, 12);
        v.doLayout();
        check(a.getY() == 0 && b.getY() == 5, "vertical Box stacks children y=0,5");
        check(a.getHeight() == 5 && b.getHeight() == 7, "vertical Box honors child heights");
    }

    static void testFillers() {
        // rigid area: min = pref = max
        Component rigid = Box.createRigidArea(new Dimension(10, 10));
        Dimension rp = rigid.getPreferredSize();
        check(rp.width == 10 && rp.height == 10, "rigid preferred 10x10");
        Dimension rmn = rigid.getMinimumSize();
        check(rmn.width == 10 && rmn.height == 10, "rigid minimum 10x10");
        Dimension rmx = rigid.getMaximumSize();
        check(rmx.width == 10 && rmx.height == 10, "rigid maximum 10x10");

        // horizontal strut: fixed width, zero height, unbounded max
        Component hs = Box.createHorizontalStrut(7);
        check(hs.getPreferredSize().width == 7 && hs.getPreferredSize().height == 0, "hstrut preferred 7x0");
        check(hs.getMinimumSize().width == 7 && hs.getMinimumSize().height == 0, "hstrut minimum 7x0");
        check(hs.getMaximumSize().width == 7 && hs.getMaximumSize().height == 32767, "hstrut maximum 7x32767");
        // vertical strut mirrors it
        Component vs = Box.createVerticalStrut(9);
        check(vs.getPreferredSize().width == 0 && vs.getPreferredSize().height == 9, "vstrut preferred 0x9");
        check(vs.getMaximumSize().width == 32767 && vs.getMaximumSize().height == 9, "vstrut maximum 32767x9");

        // axis-specific glue is unbounded only along its axis
        Component hg = Box.createHorizontalGlue();
        check(hg.getMinimumSize().width == 0 && hg.getMinimumSize().height == 0, "hglue minimum 0x0");
        check(hg.getMaximumSize().width == 32767 && hg.getMaximumSize().height == 0, "hglue maximum 32767x0");
        Component vg = Box.createVerticalGlue();
        check(vg.getMaximumSize().width == 0 && vg.getMaximumSize().height == 32767, "vglue maximum 0x32767");
        // omni-directional glue is unbounded on both axes
        Component glue = Box.createGlue();
        check(glue.getMinimumSize().width == 0 && glue.getMinimumSize().height == 0, "glue minimum 0x0");
        check(glue.getPreferredSize().width == 0 && glue.getPreferredSize().height == 0, "glue preferred 0x0");
        check(glue.getMaximumSize().width == 32767 && glue.getMaximumSize().height == 32767, "glue maximum 32767x32767");

        // Filler: explicit three-size construction, bounds independent of declared sizes
        Box.Filler f = new Box.Filler(new Dimension(3, 4), new Dimension(5, 6), new Dimension(7, 8));
        check(f.getMinimumSize().width == 3 && f.getMinimumSize().height == 4, "filler minimum 3x4");
        check(f.getPreferredSize().width == 5 && f.getPreferredSize().height == 6, "filler preferred 5x6");
        check(f.getMaximumSize().width == 7 && f.getMaximumSize().height == 8, "filler maximum 7x8");
        f.setBounds(0, 0, 9, 9);
        check(f.getBounds().width == 9 && f.getBounds().height == 9, "filler bounds follow setBounds");
        check(f.getMinimumSize().width == 3 && f.getPreferredSize().width == 5 && f.getMaximumSize().width == 7,
                "setBounds does not change declared sizes");
        check(f.getAccessibleContext() != null, "Filler is Accessible");
        // changeShape swaps all three declared sizes at once
        f.changeShape(new Dimension(1, 2), new Dimension(2, 3), new Dimension(3, 4));
        check(f.getMinimumSize().width == 1 && f.getMinimumSize().height == 2, "changeShape minimum 1x2");
        check(f.getPreferredSize().width == 2 && f.getPreferredSize().height == 3, "changeShape preferred 2x3");
        check(f.getMaximumSize().width == 3 && f.getMaximumSize().height == 4, "changeShape maximum 3x4");
    }

    static void testFillerComposition() {
        // glue between two rigid children absorbs all slack (100-wide container)
        Box hb = Box.createHorizontalBox();
        Component a = Box.createRigidArea(new Dimension(10, 10));
        Component b = Box.createRigidArea(new Dimension(10, 10));
        hb.add(a); hb.add(Box.createHorizontalGlue()); hb.add(b);
        hb.setBounds(0, 0, 100, 10);
        hb.doLayout();
        check(a.getX() == 0, "glue composition left child at 0");
        check(b.getX() == 90, "glue composition right child pinned to 90");
        // vertical glue pins the last child to the bottom (50-high container)
        Box vb = Box.createVerticalBox();
        Component c1 = Box.createRigidArea(new Dimension(10, 5));
        Component c2 = Box.createRigidArea(new Dimension(10, 5));
        vb.add(c1); vb.add(Box.createVerticalGlue()); vb.add(c2);
        vb.setBounds(0, 0, 10, 50);
        vb.doLayout();
        check(c1.getY() == 0, "vglue composition top child at 0");
        check(c2.getY() == 45, "vglue composition bottom child pinned to 45");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testBoxContainers();
        testFillers();
        testFillerComposition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
