/*
 * @test
 * @summary javax.swing.JDesktopPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JDesktopPaneTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

public class JDesktopPaneTest {
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
        runOnEDT(JDesktopPaneTest::testWhiteBox);
        runOnEDT(JDesktopPaneTest::testBlackBox);
        runOnEDT(JDesktopPaneTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JDesktopPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLayeredPane"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        check(JDesktopPane.LIVE_DRAG_MODE == 0, "LIVE_DRAG_MODE 0");
        check(JDesktopPane.OUTLINE_DRAG_MODE == 1, "OUTLINE_DRAG_MODE 1");

        JDesktopPane desk = new JDesktopPane();
        check(desk.getDragMode() == JDesktopPane.LIVE_DRAG_MODE, "default drag mode live");
        check("DesktopPaneUI".equals(desk.getUIClassID()), "ui class id");
        check(desk.getDesktopManager() != null, "default desktop manager non-null");

        desk.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        check(desk.getDragMode() == JDesktopPane.OUTLINE_DRAG_MODE, "outline drag mode");
        desk.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        check(desk.getDragMode() == JDesktopPane.LIVE_DRAG_MODE, "live drag mode");

        DesktopManager dm = new DefaultDesktopManager();
        desk.setDesktopManager(dm);
        check(desk.getDesktopManager() == dm, "custom desktop manager");

        check(desk.getAllFrames().length == 0, "no frames initially");
        check(desk.getSelectedFrame() == null, "no selected frame initially");

        JInternalFrame f1 = new JInternalFrame("one", true, true, true, true);
        JInternalFrame f2 = new JInternalFrame("two", true, true, true, true);
        desk.add(f1);
        desk.add(f2);
        JInternalFrame[] frames = desk.getAllFrames();
        check(frames.length == 2, "two frames");
        check(frames[0] == f1, "first added frame topmost");
        check(frames[1] == f2, "second added frame");

        JInternalFrame[] inDefault = desk.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER);
        check(inDefault.length == 2, "two frames in default layer");
        check(inDefault[0] == f1 && inDefault[1] == f2, "default layer order");

        desk.setSelectedFrame(f1);
        check(desk.getSelectedFrame() == f1, "selected frame f1");
        desk.setSelectedFrame(f2);
        check(desk.getSelectedFrame() == f2, "selected frame f2");

        desk.remove(f1);
        check(desk.getAllFrames().length == 1, "one frame after remove");
        check(desk.getAllFrames()[0] == f2, "remaining frame f2");
    }

    // Appended contract section: null selection, unlink on remove, explicit
    // layer tracking, count invariants, UI plumbing.
    static void testContract() throws Exception {
        JDesktopPane dp = new JDesktopPane();
        dp.setSelectedFrame(null);
        check(dp.getSelectedFrame() == null, "setSelectedFrame(null) allowed");

        JInternalFrame a = new JInternalFrame("a");
        JInternalFrame b = new JInternalFrame("b");
        dp.add(a);
        dp.add(b);
        check(dp.getComponentCount() == dp.getAllFrames().length, "componentCount == frame count");
        check(a.getDesktopPane() == dp && b.getDesktopPane() == dp, "added frames linked to desktop");

        // explicit-layer add is still tracked as a frame
        JInternalFrame layered = new JInternalFrame("L");
        dp.add(layered, JLayeredPane.PALETTE_LAYER);
        check(dp.getAllFrames().length == 3, "explicit-layer add tracked");
        check(layered.getLayer() == JLayeredPane.PALETTE_LAYER.intValue(), "explicit layer preserved");
        check(dp.getAllFramesInLayer(JLayeredPane.PALETTE_LAYER.intValue()).length == 1,
              "getAllFramesInLayer finds palette frame");

        dp.remove(a);
        check(a.getDesktopPane() == null, "removed frame unlinked");
        dp.removeAll();
        check(dp.getAllFrames().length == 0, "getAllFrames empty after removeAll");
        check(dp.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER.intValue()).length == 0,
              "getAllFramesInLayer empty after removeAll");

        check(dp.getUI() instanceof javax.swing.plaf.DesktopPaneUI, "getUI is DesktopPaneUI");
        dp.updateUI();
        check(dp.getUI() != null, "updateUI no-throw");
        check(dp.getAccessibleContext() != null, "accessible context non-null");
    }
}
