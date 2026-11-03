/*
 * @test
 * @summary javax.swing.JLayeredPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JLayeredPaneTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class JLayeredPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JLayeredPaneTest::testWhiteBox);
        runOnEDT(JLayeredPaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLayeredPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        check(JLayeredPane.DEFAULT_LAYER.intValue() == 0, "DEFAULT_LAYER 0");
        check(JLayeredPane.PALETTE_LAYER.intValue() == 100, "PALETTE_LAYER 100");
        check(JLayeredPane.MODAL_LAYER.intValue() == 200, "MODAL_LAYER 200");
        check(JLayeredPane.POPUP_LAYER.intValue() == 300, "POPUP_LAYER 300");
        check(JLayeredPane.DRAG_LAYER.intValue() == 400, "DRAG_LAYER 400");
        check(JLayeredPane.FRAME_CONTENT_LAYER.intValue() == -30000, "FRAME_CONTENT_LAYER -30000");
        check("layeredContainerLayer".equals(JLayeredPane.LAYER_PROPERTY), "LAYER_PROPERTY");

        JLayeredPane pane = new JLayeredPane();
        check(pane.getLayout() == null, "null layout");
        check(pane.isOptimizedDrawingEnabled() == true, "optimized drawing default");
        check(pane.highestLayer() == 0, "highest empty 0");
        check(pane.lowestLayer() == 0, "lowest empty 0");
        check(pane.getComponentCountInLayer(100) == 0, "empty layer count");

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        pane.add(p1, Integer.valueOf(100));
        pane.add(p2, Integer.valueOf(200));
        check(pane.getLayer(p1) == 100, "p1 layer 100");
        check(pane.getLayer(p2) == 200, "p2 layer 200");
        check(pane.getIndexOf(p2) == 0, "higher layer top index 0");
        check(pane.getIndexOf(p1) == 1, "lower layer index 1");
        check(pane.highestLayer() == 200, "highest layer 200");
        check(pane.lowestLayer() == 100, "lowest layer 100");
        check(pane.getComponentCountInLayer(100) == 1, "layer 100 count 1");
        check(pane.getComponentCountInLayer(200) == 1, "layer 200 count 1");
        check(pane.getComponentsInLayer(200)[0] == p2, "component in layer 200");

        pane.setLayer(p1, 200);
        check(pane.getLayer(p1) == 200, "p1 moved to 200");
        check(pane.getComponentCountInLayer(200) == 2, "layer 200 count 2");
        check(pane.getComponentCountInLayer(100) == 0, "layer 100 count 0");
        check(pane.getIndexOf(p2) == 0, "p2 still top of layer 200");
        check(pane.getIndexOf(p1) == 1, "p1 bottom of layer 200");

        pane.moveToFront(p1);
        check(pane.getPosition(p1) == 0, "p1 front position 0");
        check(pane.getPosition(p2) == 1, "p2 position 1");
        check(pane.getIndexOf(p1) == 0, "p1 front index 0");

        pane.setPosition(p2, 0);
        check(pane.getIndexOf(p2) == 0, "p2 back to front");
        check(pane.getIndexOf(p1) == 1, "p1 moved down");

        pane.remove(p1);
        check(pane.getComponentCount() == 1, "one component after remove");
        check(pane.getIndexOf(p1) == -1, "removed index -1");
        check(pane.getLayer(p1) == 200, "removed keeps layer property");

        JPanel p3 = new JPanel();
        JLayeredPane.putLayer(p3, 42);
        check(JLayeredPane.getLayer(p3) == 42, "static putLayer/getLayer");

        check(JLayeredPane.getLayeredPaneAbove(p2) == pane, "getLayeredPaneAbove added");
        check(JLayeredPane.getLayeredPaneAbove(new JPanel()) == null, "getLayeredPaneAbove standalone null");
    }
}
