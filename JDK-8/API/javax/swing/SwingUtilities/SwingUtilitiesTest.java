/*
 * @test
 * @summary javax.swing.SwingUtilities conformance: type shape via reflection + rectangle math,
 *          coordinate conversion, tree queries, layoutCompoundLabel, EDT helpers (headless-safe).
 * @run main SwingUtilitiesTest
 */

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SwingUtilitiesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingUtilities", false, SwingUtilitiesTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isRectangleContainingRectangle"), "method isRectangleContainingRectangle");
        check(names.contains("getLocalBounds"), "method getLocalBounds");
        check(names.contains("getWindowAncestor"), "method getWindowAncestor");
        check(names.contains("convertPoint"), "method convertPoint");
        check(names.contains("convertRectangle"), "method convertRectangle");
        check(names.contains("getAncestorOfClass"), "method getAncestorOfClass");
        check(names.contains("getAncestorNamed"), "method getAncestorNamed");
        check(names.contains("computeIntersection"), "method computeIntersection");
        check(names.contains("computeUnion"), "method computeUnion");
        check(names.contains("layoutCompoundLabel"), "method layoutCompoundLabel");
        check(names.contains("isEventDispatchThread"), "method isEventDispatchThread");
        check(names.contains("invokeAndWait"), "method invokeAndWait");
        check(names.contains("updateComponentTreeUI"), "method updateComponentTreeUI");
        check(names.contains("getDeepestComponentAt"), "method getDeepestComponentAt");
        check(names.contains("computeStringWidth"), "method computeStringWidth");
    }

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
        Rectangle u2 = new Rectangle(0, 0, 20, 20);
        SwingUtilities.computeUnion(5, 5, 10, 10, u2);
        check(u2.width == 20 && u2.height == 20, "union with contained rectangle unchanged");

        // isRectangleContainingRectangle requires full containment (inclusive of edges)
        check(SwingUtilities.isRectangleContainingRectangle(new Rectangle(0, 0, 20, 20), new Rectangle(0, 0, 10, 10)),
                "contained rectangle reports true");
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
        // a standalone component has no window ancestor and no root
        JLabel standalone = new JLabel();
        check(SwingUtilities.getWindowAncestor(standalone) == null, "getWindowAncestor(standalone) null");
        check(SwingUtilities.getRoot(standalone) == null, "getRoot(standalone) null");

        // getLocalBounds reflects current size, zero for an unshown component
        check(SwingUtilities.getLocalBounds(standalone).width == 0
                && SwingUtilities.getLocalBounds(standalone).height == 0, "getLocalBounds unshown is 0x0");
        standalone.setSize(20, 10);
        Rectangle lb = SwingUtilities.getLocalBounds(standalone);
        check(lb.x == 0 && lb.y == 0 && lb.width == 20 && lb.height == 10, "getLocalBounds after setSize");

        // string width delegation
        FontMetrics fm = standalone.getFontMetrics(standalone.getFont());
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
        Graphics2D gg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
        FontMetrics fm = gg.getFontMetrics(new Font("Dialog", Font.PLAIN, 12));
        Icon icon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        Rectangle viewR = new Rectangle(0, 0, 100, 20);

        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        String ret = SwingUtilities.layoutCompoundLabel(fm, "", icon,
                SwingConstants.TOP, SwingConstants.LEFT, SwingConstants.TOP, SwingConstants.LEFT,
                viewR, iconR, textR, 4);
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
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testRectangleMath();
        testCoordinateConversion();
        testTreeQueries();
        testMouseButtons();
        testCompoundLabel();
        testEdtHelpers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
