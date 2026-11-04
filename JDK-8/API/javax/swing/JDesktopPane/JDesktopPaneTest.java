/*
 * @test
 * @summary javax.swing.JDesktopPane conformance: type shape via reflection (headless-safe)
 *          plus behavioral contract (Metal LaF, EDT discipline, headless).
 * @run main JDesktopPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.DefaultDesktopManager;
import javax.swing.plaf.basic.BasicDesktopPaneUI;

public class JDesktopPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { behavior(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JDesktopPane", false, JDesktopPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLayeredPane"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAllFrames"), "method getAllFrames");
        check(names.contains("getSelectedFrame"), "method getSelectedFrame");
        check(names.contains("getDesktopManager"), "method getDesktopManager");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        JDesktopPane dp = new JDesktopPane();

        // drag mode constants + default
        check(JDesktopPane.LIVE_DRAG_MODE == 0, "LIVE_DRAG_MODE 0");
        check(JDesktopPane.OUTLINE_DRAG_MODE == 1, "OUTLINE_DRAG_MODE 1");
        check(dp.getDragMode() == JDesktopPane.LIVE_DRAG_MODE, "default drag mode LIVE");
        dp.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        check(dp.getDragMode() == JDesktopPane.OUTLINE_DRAG_MODE, "setDragMode roundtrip");
        dp.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        check(dp.getDragMode() == JDesktopPane.LIVE_DRAG_MODE, "setDragMode back to LIVE");

        // desktop manager
        check(dp.getDesktopManager() != null, "default desktop manager non-null");
        check(dp.getDesktopManager() instanceof DesktopManager, "manager implements DesktopManager");
        DefaultDesktopManager dm = new DefaultDesktopManager();
        dp.setDesktopManager(dm);
        check(dp.getDesktopManager() == dm, "setDesktopManager roundtrip");

        // selected frame
        check(dp.getSelectedFrame() == null, "default selected frame null");
        JInternalFrame a = new JInternalFrame("a", true, true, true, true);
        dp.add(a);
        dp.setSelectedFrame(a);
        check(dp.getSelectedFrame() == a, "setSelectedFrame roundtrip");
        dp.setSelectedFrame(null);
        check(dp.getSelectedFrame() == null, "setSelectedFrame(null) allowed");

        // frame tracking: add / remove / removeAll keep getAllFrames in sync
        JInternalFrame b = new JInternalFrame("b");
        JInternalFrame c2 = new JInternalFrame("c");
        dp.add(b);
        dp.add(c2);
        check(dp.getAllFrames().length == 3, "getAllFrames tracks adds");
        check(dp.getComponentCount() == dp.getAllFrames().length, "componentCount == frame count");
        check(a.getDesktopPane() == dp, "added frame reports desktop pane");
        check(b.getDesktopPane() == dp && c2.getDesktopPane() == dp, "all frames linked");
        dp.remove(b);
        check(dp.getAllFrames().length == 2, "getAllFrames tracks remove");
        check(b.getDesktopPane() == null, "removed frame unlinked");
        dp.removeAll();
        check(dp.getAllFrames().length == 0, "getAllFrames empty after removeAll");

        // adding at an explicit layer still tracked
        JInternalFrame layered = new JInternalFrame("L");
        dp.add(layered, JLayeredPane.PALETTE_LAYER);
        check(dp.getAllFrames().length == 1, "explicit-layer add tracked");
        check(layered.getLayer() == JLayeredPane.PALETTE_LAYER.intValue(), "explicit layer preserved");
        dp.remove(layered);

        // UI plumbing
        check(dp.getUIClassID().equals("DesktopPaneUI"), "getUIClassID");
        check(dp.getUI() instanceof BasicDesktopPaneUI, "Metal LaF installs BasicDesktopPaneUI");
        dp.updateUI();
        check(dp.getUI() != null, "updateUI no-throw");
        check(dp.getAccessibleContext() != null, "accessible context non-null");

        // no frames selected-state leakage after clearing
        check(dp.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER.intValue()).length == 0,
              "getAllFramesInLayer empty for unused layer");
    }

    // indirection so the LaF class loads after Swing init in all JDKs
    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
