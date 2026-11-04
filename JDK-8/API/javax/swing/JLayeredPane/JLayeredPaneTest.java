/*
 * @test
 * @summary javax.swing.JLayeredPane conformance: type shape via reflection + layering
 *          contract (Integer constraint vs int index, layer ordering, setLayer reordering,
 *          position/move ops, static helpers; headless-safe, Metal LaF, EDT).
 * @run main JLayeredPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JLayeredPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLayeredPane", false, JLayeredPaneTest.class.getClassLoader());
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
        check(names.contains("remove"), "method remove");
        check(names.contains("removeAll"), "method removeAll");
        check(names.contains("isOptimizedDrawingEnabled"), "method isOptimizedDrawingEnabled");
        check(names.contains("putLayer"), "method putLayer");
        check(names.contains("getLayer"), "method getLayer");
        check(names.contains("getLayeredPaneAbove"), "method getLayeredPaneAbove");
        check(names.contains("setLayer"), "method setLayer");
    }

    // ---- Part 2: constants and empty pane ----
    static void testConstantsAndEmpty() {
        check(JLayeredPane.DEFAULT_LAYER.intValue() == 0, "DEFAULT_LAYER 0");
        check(JLayeredPane.PALETTE_LAYER.intValue() == 100, "PALETTE_LAYER 100");
        check(JLayeredPane.MODAL_LAYER.intValue() == 200, "MODAL_LAYER 200");
        check(JLayeredPane.POPUP_LAYER.intValue() == 300, "POPUP_LAYER 300");
        check(JLayeredPane.DRAG_LAYER.intValue() == 400, "DRAG_LAYER 400");
        check(JLayeredPane.FRAME_CONTENT_LAYER.intValue() == -30000, "FRAME_CONTENT_LAYER -30000");
        check("layeredContainerLayer".equals(JLayeredPane.LAYER_PROPERTY), "LAYER_PROPERTY name");

        JLayeredPane pane = new JLayeredPane();
        check(pane.getLayout() == null, "null layout manager");
        check(pane.isOptimizedDrawingEnabled(), "optimized drawing default true");
        check(pane.highestLayer() == 0, "highestLayer empty 0");
        check(pane.lowestLayer() == 0, "lowestLayer empty 0");
        check(pane.getComponentCountInLayer(100) == 0, "empty layer count 0");
        check(pane.getComponentsInLayer(100).length == 0, "empty layer components 0");
        check(pane.getIndexOf(new JPanel()) == -1, "getIndexOf non-child -1");
        check(JLayeredPane.getLayer(new JPanel()) == 0, "static getLayer unlayered default 0");
    }

    // ---- Part 3: add() constraint disambiguation and ordering ----
    static void testAddConstraints() {
        // add(comp, int): the int is an INDEX, not a layer - classic trap.
        JLayeredPane pane = new JLayeredPane();
        JPanel b1 = new JPanel();
        pane.add(b1, 5);
        check(pane.getComponentCount() == 1, "add(comp,int) adds component");
        check(pane.getLayer(b1) == 0, "add(comp,int) int is index not layer");
        // add(comp, Integer): sets the layer.
        JPanel b2 = new JPanel();
        pane.add(b2, Integer.valueOf(5));
        check(pane.getLayer(b2) == 5, "add(comp,Integer) sets layer");
        check(pane.getComponentCountInLayer(5) == 1, "layer 5 count (int-added stays default)");
        // Child array is kept in descending layer order (higher layer = front);
        // within one layer, insertion order is preserved. b1 (default 0 via int-add)
        // sits behind b3 (3), which sits behind b2 (5).
        JPanel b3 = new JPanel();
        pane.add(b3, Integer.valueOf(3));
        check(pane.getIndexOf(b2) == 0, "highest layer at front");
        check(pane.getIndexOf(b3) == 1, "middle layer behind higher one");
        check(pane.getIndexOf(b1) == 2, "default layer at the back");
        check(pane.highestLayer() == 5, "highestLayer 5");
        check(pane.lowestLayer() == 0, "lowestLayer 0 (int-added child defaulted)");

        // setLayer re-positions the child within the pane.
        pane.setLayer(b1, 7);
        check(pane.getLayer(b1) == 7, "setLayer updates layer");
        check(JLayeredPane.getLayer(b1) == 7, "static getLayer sees new layer");
        check(pane.getIndexOf(b1) == 0, "raised layer moves child to front");

        // moveToFront/MoveToBack reposition inside the layer.
        JLayeredPane same = new JLayeredPane();
        JPanel s1 = new JPanel();
        JPanel s2 = new JPanel();
        same.add(s1, Integer.valueOf(5));
        same.add(s2, Integer.valueOf(5));
        check(same.getIndexOf(s1) == 0 && same.getIndexOf(s2) == 1, "same-layer insertion order");
        same.moveToBack(s1);
        check(same.getIndexOf(s1) == 1 && same.getIndexOf(s2) == 0, "moveToBack swaps within layer");
        same.moveToFront(s1);
        check(same.getIndexOf(s1) == 0, "moveToFront restores");
        check(same.getPosition(s1) == 0, "position 0 at front");
        same.setPosition(s1, 1);
        check(same.getPosition(s1) == 1, "setPosition round-trip");
    }

    // ---- Part 4: removal and static helpers ----
    static void testRemoveAndStatics() {
        JLayeredPane pane = new JLayeredPane();
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        pane.add(p1, Integer.valueOf(100));
        pane.add(p2, Integer.valueOf(200));
        pane.remove(p1);
        check(pane.getComponentCount() == 1, "remove drops one child");
        check(pane.getIndexOf(p1) == -1, "removed child index -1");
        check(pane.getLayer(p1) == 100, "removed child keeps its layer property");
        check(pane.getComponentCountInLayer(100) == 0, "layer 100 empty after remove");
        check(JLayeredPane.getLayeredPaneAbove(p2) == pane, "getLayeredPaneAbove finds ancestor pane");
        check(JLayeredPane.getLayeredPaneAbove(new JPanel()) == null, "getLayeredPaneAbove standalone null");

        pane.removeAll();
        check(pane.getComponentCount() == 0, "removeAll empties pane");
        check(pane.highestLayer() == 0 && pane.lowestLayer() == 0, "layers reset after removeAll");
        check(JLayeredPane.getLayeredPaneAbove(p2) == null, "getLayeredPaneAbove null after removeAll");

        JPanel p3 = new JPanel();
        JLayeredPane.putLayer(p3, 42);
        check(JLayeredPane.getLayer(p3) == 42, "static putLayer/getLayer round-trip");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testConstantsAndEmpty(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testAddConstraints(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testRemoveAndStatics(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
