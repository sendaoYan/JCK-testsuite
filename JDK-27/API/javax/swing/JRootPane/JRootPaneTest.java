/*
 * @test
 * @summary javax.swing.JRootPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JRootPaneTest
 */

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JPanel;

public class JRootPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
    }
    static boolean throwsICSE(Runnable r) {
        try { r.run(); return false; }
        catch (java.awt.IllegalComponentStateException e) { return true; }
    }
    static boolean throwsNPE(Runnable r) {
        try { r.run(); return false; }
        catch (NullPointerException e) { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JRootPane");
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
        // supplementary structural checks: decoration style constants
        check(c.getField("NONE").getInt(null) == 0, "NONE == 0");
        check(c.getField("FRAME").getInt(null) == 1, "FRAME == 1");
        check(c.getField("QUESTION_DIALOG").getInt(null) == 7, "QUESTION_DIALOG == 7");
        check(c.getField("WARNING_DIALOG").getInt(null) == 8, "WARNING_DIALOG == 8");
        check(c.getMethod("setDefaultButton", JButton.class) != null, "setDefaultButton signature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JRootPane root = new JRootPane();
        try {
            // default pane children
            Container cp = root.getContentPane();
            check(cp != null, "default contentPane non-null");
            JLayeredPane lp = root.getLayeredPane();
            check(lp != null && lp instanceof JLayeredPane, "default layeredPane non-null");
            check(root.getGlassPane() != null, "default glassPane non-null");
            check(root.getUI() != null, "RootPaneUI non-null");

            // contentPane replacement: new pane is parented into layeredPane, old pane detached
            check(cp.getParent() == lp, "default contentPane parented by layeredPane");
            JPanel newCp = new JPanel();
            root.setContentPane(newCp);
            check(root.getContentPane() == newCp, "setContentPane round trip");
            check(newCp.getParent() == lp, "replacement contentPane parented by layeredPane");
            check(cp.getParent() != lp, "old contentPane detached from layeredPane");
            check(lp.getComponentCountInLayer(JLayeredPane.FRAME_CONTENT_LAYER.intValue()) == 1,
                  "one component in FRAME_CONTENT_LAYER after swap");
            check(throwsICSE(() -> root.setContentPane(null)), "setContentPane(null) -> IllegalComponentStateException");

            // layeredPane replacement: contentPane reference survives the swap
            JLayeredPane newLp = new JLayeredPane();
            root.setLayeredPane(newLp);
            check(root.getLayeredPane() == newLp, "setLayeredPane round trip");
            check(root.getContentPane() == newCp, "contentPane reference survives setLayeredPane");
            check(newLp.getParent() == root, "replacement layeredPane parented by rootPane");
            check(throwsICSE(() -> root.setLayeredPane(null)), "setLayeredPane(null) -> IllegalComponentStateException");

            // glassPane accepts any Component; visibility of old pane propagates; null rejected
            Canvas canvas = new Canvas();
            root.setGlassPane(canvas);
            check(root.getGlassPane() == canvas, "setGlassPane round trip");
            check(!canvas.isVisible(), "glassPane inherits old pane's invisibility");
            check(canvas.getParent() == root, "replacement glassPane parented by rootPane");
            JLabel visiblePane = new JLabel();
            root.getGlassPane().setVisible(true);
            root.setGlassPane(visiblePane);
            check(visiblePane.isVisible(), "visible glass pane propagates visibility");
            check(throwsNPE(() -> root.setGlassPane(null)), "setGlassPane(null) -> NPE");

            // menu bar (modern + deprecated accessors agree)
            JMenuBar mb = new JMenuBar();
            root.setJMenuBar(mb);
            check(root.getJMenuBar() == mb, "setJMenuBar round trip");
            check(root.getMenuBar() == mb, "deprecated getMenuBar agrees");
            root.setMenuBar(null);
            check(root.getJMenuBar() == null, "setMenuBar(null) clears");

            // default button
            check(root.getDefaultButton() == null, "default defaultButton null");
            JButton def = new JButton("default");
            root.setDefaultButton(def);
            check(root.getDefaultButton() == def, "setDefaultButton round trip");
            root.setDefaultButton(null);
            check(root.getDefaultButton() == null, "setDefaultButton(null)");

            // windowDecorationStyle: default + illegal values (no real style applied standalone)
            check(root.getWindowDecorationStyle() == JRootPane.NONE, "default decoration style NONE");
            check(throwsIAE(() -> root.setWindowDecorationStyle(-1)), "decoration style -1 -> IAE");
            check(throwsIAE(() -> root.setWindowDecorationStyle(99)), "decoration style 99 -> IAE");
            check(root.getWindowDecorationStyle() == JRootPane.NONE, "decoration style unchanged after IAE");

            // JRootPane inside a real window: decoration styles + pack + size chain
            JFrame frame = new JFrame("JRootPaneTest");
            try {
                JRootPane frameRoot = frame.getRootPane();
                check(frameRoot != null && frameRoot instanceof JRootPane, "frame rootPane is a JRootPane");
                check(frameRoot.isValidateRoot(), "frame rootPane isValidateRoot true");
                int[] styles = { JRootPane.FRAME, JRootPane.PLAIN_DIALOG, JRootPane.INFORMATION_DIALOG,
                                 JRootPane.ERROR_DIALOG, JRootPane.COLOR_CHOOSER_DIALOG, JRootPane.FILE_CHOOSER_DIALOG,
                                 JRootPane.QUESTION_DIALOG, JRootPane.WARNING_DIALOG };
                for (int style : styles) {
                    frameRoot.setWindowDecorationStyle(style);
                    check(frameRoot.getWindowDecorationStyle() == style, "decoration style round trip " + style);
                }
                frameRoot.setWindowDecorationStyle(JRootPane.NONE);
                check(frameRoot.getWindowDecorationStyle() == JRootPane.NONE, "decoration style back to NONE");
                frame.getContentPane().add(new JButton("content"), java.awt.BorderLayout.CENTER);
                frame.pack();
                frame.setSize(180, 120);
                check(frame.getContentPane().getComponentCount() == 1, "frame contentPane holds the added button");
                check(frameRoot.getContentPane() == frame.getContentPane(), "frameRoot.contentPane is frame contentPane");
                check(root.isValidateRoot(), "standalone rootPane isValidateRoot true");
            } finally {
                frame.dispose();
            }
        } finally {
            // no native resources: root pane itself needs no dispose when not in a window
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JRootPaneTest::testWhiteBox);
        runOnEDT(JRootPaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
