/*
 * @test
 * @summary javax.swing.SwingUtilities conformance: white-box structure + black-box behavior.
 * @run main SwingUtilitiesTest
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

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


    // ---- Part 3: deep contract coverage ----
    static void testRectangleMath() {
        // computeIntersection intersects in place and returns the same instance
        Rectangle r = new Rectangle(0, 0, 10, 10);
        Rectangle got = SwingUtilities.computeIntersection(5, 5, 10, 10, r);
        check(got == r, "computeIntersection mutates and returns the argument");
        check(r.x == 5 && r.y == 5 && r.width == 5 && r.height == 5, "intersection (0,0,10,10)&(5,5,10,10) = (5,5,5,5)");
        // fully contained rectangle is returned unchanged
        Rectangle big = new Rectangle(0, 0, 20, 20);
        SwingUtilities.computeIntersection(5, 5, 10, 10, big);
        check(big.x == 5 && big.y == 5 && big.width == 10 && big.height == 10, "contained intersection preserved");
        // disjoint rectangles collapse to empty at the origin
        Rectangle dis = new Rectangle(0, 0, 10, 10);
        SwingUtilities.computeIntersection(20, 20, 5, 5, dis);
        check(dis.x == 0 && dis.y == 0 && dis.width == 0 && dis.height == 0, "disjoint intersection is 0x0 at origin");
        // computeUnion unions in place
        Rectangle u = new Rectangle(0, 0, 10, 10);
        SwingUtilities.computeUnion(0, 0, 15, 15, u);
        check(u.x == 0 && u.y == 0 && u.width == 15 && u.height == 15, "union expands to (0,0,15,15)");
        // isRectangleContainingRectangle is inclusive of edges
        check(SwingUtilities.isRectangleContainingRectangle(new Rectangle(0, 0, 20, 20), new Rectangle(0, 0, 20, 20)),
                "equal rectangle reports true");
        check(!SwingUtilities.isRectangleContainingRectangle(new Rectangle(0, 0, 10, 10), new Rectangle(5, 5, 10, 10)),
                "overlapping-but-larger rectangle reports false");
    }

    static void testCoordinateConversion() {
        // fixed bounds, no layout manager interference: pure offset arithmetic
        JPanel pa = new JPanel();
        pa.setLayout(null);
        pa.setBounds(0, 0, 100, 100);
        JPanel pb = new JPanel();
        pb.setLayout(null);
        pb.setBounds(10, 20, 50, 50);
        pa.add(pb);

        Point p1 = SwingUtilities.convertPoint(pb, new Point(5, 5), pa);
        check(p1.x == 15 && p1.y == 25, "convertPoint(child->parent) adds child origin (15,25)");
        Point p2 = SwingUtilities.convertPoint(pa, new Point(15, 25), pb);
        check(p2.x == 5 && p2.y == 5, "convertPoint(parent->child) subtracts child origin (5,5)");
        // int overload agrees with the Point overload
        Point p3 = SwingUtilities.convertPoint(pb, 5, 5, pa);
        check(p3.x == 15 && p3.y == 25, "convertPoint int overload matches");
        // rectangles convert with the same offsets, size preserved
        Rectangle r1 = SwingUtilities.convertRectangle(pb, new Rectangle(5, 5, 10, 10), pa);
        check(r1.x == 15 && r1.y == 25 && r1.width == 10 && r1.height == 10, "convertRectangle(child->parent)");
        Rectangle r2 = SwingUtilities.convertRectangle(pa, new Rectangle(15, 25, 10, 10), pb);
        check(r2.x == 5 && r2.y == 5 && r2.width == 10 && r2.height == 10, "convertRectangle(parent->child)");
    }

    static void testTreeQueries() {
        JPanel root = new JPanel();
        root.setName("G");
        root.setLayout(null);
        JPanel mid = new JPanel();
        mid.setName("M");
        mid.setBounds(0, 0, 100, 100);
        JLabel leaf = new JLabel("x");
        leaf.setBounds(0, 0, 100, 100);
        mid.add(leaf);
        root.add(mid);
        root.setBounds(0, 0, 100, 100);

        check(SwingUtilities.getAncestorOfClass(JPanel.class, leaf) == mid, "getAncestorOfClass finds innermost");
        check(SwingUtilities.getAncestorNamed("G", leaf) == root, "getAncestorNamed finds named ancestor");
        check(SwingUtilities.getAncestorNamed("nope", leaf) == null, "getAncestorNamed absent returns null");
        check(SwingUtilities.getDeepestComponentAt(root, 30, 30) == leaf, "getDeepestComponentAt descends fully");
        check(SwingUtilities.getDeepestComponentAt(root, 0, 0) == leaf, "getDeepestComponentAt (0,0) still descends");
        check(SwingUtilities.getDeepestComponentAt(root, 999, 999) == null, "getDeepestComponentAt outside returns null");

        // getLocalBounds reflects current size, zero for an unshown component
        JLabel standalone = new JLabel();
        check(SwingUtilities.getLocalBounds(standalone).width == 0
                && SwingUtilities.getLocalBounds(standalone).height == 0, "getLocalBounds unshown is 0x0");
        standalone.setSize(20, 10);
        Rectangle lb = SwingUtilities.getLocalBounds(standalone);
        check(lb.x == 0 && lb.y == 0 && lb.width == 20 && lb.height == 10, "getLocalBounds after setSize");

        // string width delegation
        java.awt.FontMetrics fm = standalone.getFontMetrics(standalone.getFont());
        check(SwingUtilities.computeStringWidth(fm, "hello") == fm.stringWidth("hello"),
                "computeStringWidth == FontMetrics.stringWidth");

        SwingUtilities.updateComponentTreeUI(root);
        check(true, "updateComponentTreeUI no-throw");
    }

    static void testMouseButtons() {
        JLabel src = new JLabel();
        MouseEvent left = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, 1L, InputEvent.BUTTON1_MASK, 0, 0, 1, false);
        MouseEvent middle = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, 1L, InputEvent.BUTTON2_MASK, 0, 0, 1, false);
        MouseEvent right = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, 1L, InputEvent.BUTTON3_MASK, 0, 0, 1, false);
        check(SwingUtilities.isLeftMouseButton(left), "BUTTON1 is left");
        check(SwingUtilities.isMiddleMouseButton(middle), "BUTTON2 is middle");
        check(SwingUtilities.isRightMouseButton(right), "BUTTON3 is right");
        check(!SwingUtilities.isLeftMouseButton(middle), "middle is not left");
        check(!SwingUtilities.isMiddleMouseButton(left), "left is not middle");
        check(!SwingUtilities.isRightMouseButton(left), "left is not right");
    }

    static void testCompoundLabel() {
        // icon-only (empty text) layout: pure geometry, independent of font metrics
        java.awt.Graphics2D gg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
        java.awt.FontMetrics fm = gg.getFontMetrics(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        Icon icon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        String ret = SwingUtilities.layoutCompoundLabel(fm, "", icon,
                SwingConstants.TOP, SwingConstants.LEFT, SwingConstants.TOP, SwingConstants.LEFT,
                new Rectangle(0, 0, 100, 20), iconR, textR, 4);
        check(ret != null && ret.isEmpty(), "empty text passes through");
        check(iconR.x == 0 && iconR.y == 0 && iconR.width == 10 && iconR.height == 10, "icon-only TOP/LEFT at origin");
        check(textR.width == 0 && textR.height == 0, "icon-only text rectangle empty");

        Rectangle iconR2 = new Rectangle();
        Rectangle textR2 = new Rectangle();
        SwingUtilities.layoutCompoundLabel(fm, "", icon,
                SwingConstants.BOTTOM, SwingConstants.RIGHT, SwingConstants.BOTTOM, SwingConstants.RIGHT,
                new Rectangle(0, 0, 100, 20), iconR2, textR2, 4);
        check(iconR2.x == 90 && iconR2.y == 10 && iconR2.width == 10 && iconR2.height == 10,
                "icon-only BOTTOM/RIGHT at (90,10)");
        check(textR2.x == 100 && textR2.y == 20, "icon-only text rectangle pinned to far corner");

        Rectangle iconR3 = new Rectangle();
        Rectangle textR3 = new Rectangle();
        SwingUtilities.layoutCompoundLabel(fm, "", icon,
                SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
                new Rectangle(0, 0, 100, 20), iconR3, textR3, 4);
        check(iconR3.x == 45 && iconR3.y == 5 && iconR3.width == 10 && iconR3.height == 10,
                "icon-only CENTER centered at (45,5)");
    }

    static void testEdtHelpers() throws Exception {
        check(!SwingUtilities.isEventDispatchThread(), "main thread is not the EDT");
        final boolean[] onEdt = {false};
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { onEdt[0] = SwingUtilities.isEventDispatchThread(); }
        });
        check(onEdt[0], "invokeAndWait body runs on the EDT");
        // exceptions thrown inside invokeAndWait surface as InvocationTargetException
        boolean wrapped = false;
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() { throw new IllegalStateException("boom"); }
            });
        } catch (InvocationTargetException ex) {
            wrapped = ex.getCause() instanceof IllegalStateException;
        }
        check(wrapped, "invokeAndWait wraps runnable exceptions in InvocationTargetException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testBlackBox();
        testRectangleMath();
        testCoordinateConversion();
        testTreeQueries();
        testMouseButtons();
        testCompoundLabel();
        testEdtHelpers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
