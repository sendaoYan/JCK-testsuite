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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
