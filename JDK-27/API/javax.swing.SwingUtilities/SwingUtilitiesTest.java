/*
 * @test
 * @summary javax.swing.SwingUtilities conformance: white-box structure + black-box behavior.
 * @run main SwingUtilitiesTest
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SwingUtilitiesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingUtilities");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Rectangle outer = new Rectangle(0, 0, 10, 10);
        Rectangle inner = new Rectangle(1, 1, 2, 2);
        check(SwingUtilities.isRectangleContainingRectangle(outer, inner), "contains rectangle");
        check(!SwingUtilities.isRectangleContainingRectangle(inner, outer), "not contains rectangle");

        Rectangle inter = SwingUtilities.computeIntersection(0, 0, 10, 10, new Rectangle(5, 5, 10, 10));
        check(inter.x == 5 && inter.y == 5 && inter.width == 5 && inter.height == 5, "computeIntersection");
        Rectangle union = SwingUtilities.computeUnion(0, 0, 10, 10, new Rectangle(5, 5, 10, 10));
        check(union.x == 0 && union.y == 0 && union.width == 15 && union.height == 15, "computeUnion");
        Rectangle[] diff = SwingUtilities.computeDifference(new Rectangle(0, 0, 10, 10), new Rectangle(2, 2, 4, 4));
        check(diff != null && diff.length == 4, "computeDifference");

        JPanel p = new JPanel();
        check(SwingUtilities.getRoot(p) == null, "getRoot null for non-displayable");
        check(SwingUtilities.getLocalBounds(p) != null, "getLocalBounds");
        check(SwingUtilities.getWindowAncestor(p) == null, "getWindowAncestor null headless");
        check(SwingUtilities.convertPoint(p, new Point(1, 1), p).equals(new Point(1, 1)), "convertPoint");
        check(SwingUtilities.convertRectangle(p, new Rectangle(0, 0, 5, 5), p).equals(new Rectangle(0, 0, 5, 5)), "convertRectangle");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
