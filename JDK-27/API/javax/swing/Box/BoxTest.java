/*
 * @test
 * @summary javax.swing.Box conformance: white-box structure + black-box behavior.
 * @run main BoxTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Modifier;

import javax.swing.Box;
import javax.swing.BoxLayout;

public class BoxTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Box");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // horizontal/vertical boxes install a BoxLayout on the right axis
        Box h = Box.createHorizontalBox();
        Box v = Box.createVerticalBox();
        check(h.getLayout() instanceof BoxLayout, "horizontal uses BoxLayout");
        check(v.getLayout() instanceof BoxLayout, "vertical uses BoxLayout");
        check(((BoxLayout) h.getLayout()).getAxis() == BoxLayout.X_AXIS, "horizontal axis");
        check(((BoxLayout) v.getLayout()).getAxis() == BoxLayout.Y_AXIS, "vertical axis");

        // Box(int) ctor sets the axis directly
        Box custom = new Box(BoxLayout.Y_AXIS);
        check(((BoxLayout) custom.getLayout()).getAxis() == BoxLayout.Y_AXIS, "Box(axis) ctor");

        // rigid area + struts have deterministic preferred sizes
        Component rigid = Box.createRigidArea(new Dimension(5, 6));
        check(new Dimension(5, 6).equals(rigid.getPreferredSize()), "rigid area size");
        Component hstrut = Box.createHorizontalStrut(10);
        check(new Dimension(10, 0).equals(hstrut.getPreferredSize()), "horizontal strut");
        Component vstrut = Box.createVerticalStrut(20);
        check(new Dimension(0, 20).equals(vstrut.getPreferredSize()), "vertical strut");

        // glue components are non-null and zero-sized
        Component glue = Box.createGlue();
        Component hglue = Box.createHorizontalGlue();
        Component vglue = Box.createVerticalGlue();
        check(glue != null && hglue != null && vglue != null, "glue non-null");
    }


    // ---- Part 3: deep contract coverage ----
    static void testFillers() {
        // rigid area: min = pref = max
        Component rigid = Box.createRigidArea(new Dimension(10, 10));
        check(rigid.getPreferredSize().width == 10 && rigid.getPreferredSize().height == 10, "rigid preferred 10x10");
        check(rigid.getMinimumSize().width == 10 && rigid.getMinimumSize().height == 10, "rigid minimum 10x10");
        check(rigid.getMaximumSize().width == 10 && rigid.getMaximumSize().height == 10, "rigid maximum 10x10");

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
        // plain Box stacks rigid children exactly
        Box v2 = Box.createVerticalBox();
        Component e1 = Box.createRigidArea(new Dimension(10, 5));
        Component e2 = Box.createRigidArea(new Dimension(10, 7));
        v2.add(e1); v2.add(e2);
        v2.setBounds(0, 0, 10, 12);
        v2.doLayout();
        check(e1.getY() == 0 && e2.getY() == 5, "vertical Box stacks children y=0,5");
        check(e1.getHeight() == 5 && e2.getHeight() == 7, "vertical Box honors child heights");
        check(Box.createHorizontalBox().getAccessibleContext() != null, "Box is Accessible");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFillers();
        testFillerComposition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
