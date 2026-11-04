/*
 * @test
 * @summary javax.swing.JRootPane conformance: type shape via reflection + root pane
 *          contract (style constants, pane wiring/swap matrix, null-setter reverse
 *          cases, default button events; headless-safe, Metal LaF, EDT).
 * @run main JRootPaneTest
 */

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JRootPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JRootPane", false, JRootPaneTest.class.getClassLoader());
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
        check(names.contains("setDoubleBuffered"), "method setDoubleBuffered");
        check(names.contains("getWindowDecorationStyle"), "method getWindowDecorationStyle");
        check(names.contains("setWindowDecorationStyle"), "method setWindowDecorationStyle");
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    // ---- Part 2: style constants and default wiring ----
    static void testConstantsAndDefaults() {
        check(JRootPane.NONE == 0, "NONE 0");
        check(JRootPane.FRAME == 1, "FRAME 1");
        check(JRootPane.PLAIN_DIALOG == 2, "PLAIN_DIALOG 2");
        check(JRootPane.INFORMATION_DIALOG == 3, "INFORMATION_DIALOG 3");
        check(JRootPane.ERROR_DIALOG == 4, "ERROR_DIALOG 4");
        check(JRootPane.COLOR_CHOOSER_DIALOG == 5, "COLOR_CHOOSER_DIALOG 5");
        check(JRootPane.FILE_CHOOSER_DIALOG == 6, "FILE_CHOOSER_DIALOG 6");
        check(JRootPane.QUESTION_DIALOG == 7, "QUESTION_DIALOG 7");
        check(JRootPane.WARNING_DIALOG == 8, "WARNING_DIALOG 8");

        JRootPane root = new JRootPane();
        check(root.getContentPane() != null, "default contentPane non-null");
        check(root.getLayeredPane() != null && root.getLayeredPane() instanceof JLayeredPane,
                "default layeredPane is JLayeredPane");
        check(root.getGlassPane() != null, "default glassPane non-null");
        check(root.getGlassPane() instanceof JComponent, "glassPane is JComponent");
        check(!((JComponent) root.getGlassPane()).isOpaque(), "glassPane not opaque");
        check(!root.getGlassPane().isVisible(), "glassPane not visible by default");
        check(root.getContentPane().getLayout() instanceof BorderLayout, "contentPane layout is BorderLayout");
        check(root.getContentPane().getParent() == root.getLayeredPane(), "contentPane parented by layeredPane");
        check(root.getLayeredPane().getParent() == root, "layeredPane parented by root");
        check(root.isValidateRoot(), "isValidateRoot true");
        check(root.getWindowDecorationStyle() == JRootPane.NONE, "default decoration style NONE");
        check(root.getJMenuBar() == null, "default menuBar null");
        check(root.getDefaultButton() == null, "default defaultButton null");
        check("RootPaneUI".equals(root.getUIClassID()), "getUIClassID RootPaneUI");
        check("MetalRootPaneUI".equals(root.getUI().getClass().getSimpleName()), "Metal installs MetalRootPaneUI");
    }

    // ---- Part 3: pane swap matrix and reverse cases ----
    static void testPaneSwaps() {
        final JRootPane root = new JRootPane();
        java.awt.Container cp = root.getContentPane();
        JLayeredPane lp = root.getLayeredPane();
        JPanel newCp = new JPanel();
        root.setContentPane(newCp);
        check(root.getContentPane() == newCp, "setContentPane round-trip");
        check(newCp.getParent() == lp, "replacement contentPane parented by layeredPane");
        check(cp.getParent() == null, "old contentPane detached");
        check(lp.getComponentCountInLayer(JLayeredPane.FRAME_CONTENT_LAYER.intValue()) == 1,
                "one component in FRAME_CONTENT_LAYER after swap");
        checkThrows(IllegalComponentStateException.class,
                new Runnable() { public void run() { root.setContentPane(null); } },
                "setContentPane(null) ICSE");

        JLayeredPane newLp = new JLayeredPane();
        root.setLayeredPane(newLp);
        check(root.getLayeredPane() == newLp, "setLayeredPane round-trip");
        check(newLp.getParent() == root, "replacement layeredPane parented by root");
        check(root.getContentPane() == newCp, "contentPane survives setLayeredPane");
        checkThrows(IllegalComponentStateException.class,
                new Runnable() { public void run() { root.setLayeredPane(null); } },
                "setLayeredPane(null) ICSE");

        JPanel gp = new JPanel();
        root.setGlassPane(gp);
        check(root.getGlassPane() == gp, "setGlassPane round-trip");
        check(gp.getParent() == root, "replacement glassPane parented by root");
        checkThrows(NullPointerException.class,
                new Runnable() { public void run() { root.setGlassPane(null); } },
                "setGlassPane(null) NPE");

        JMenuBar mb = new JMenuBar();
        root.setJMenuBar(mb);
        check(root.getJMenuBar() == mb, "setJMenuBar round-trip");
        check(root.getMenuBar() == mb, "deprecated getMenuBar agrees");
        root.setJMenuBar(null);
        check(root.getJMenuBar() == null, "setJMenuBar(null) legal");
    }

    // ---- Part 4: default button + decoration style reverse cases ----
    static void testDefaultButtonAndStyle() {
        final JRootPane root = new JRootPane();
        JButton def = new JButton("default");
        final AtomicReference<PropertyChangeEvent> ev = new AtomicReference<PropertyChangeEvent>();
        root.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if ("defaultButton".equals(e.getPropertyName())) ev.set(e);
            }
        });
        root.setDefaultButton(def);
        check(root.getDefaultButton() == def, "setDefaultButton round-trip");
        check(ev.get() != null, "setDefaultButton fires defaultButton event");
        check(ev.get().getNewValue() == def, "defaultButton event new value");
        check(ev.get().getOldValue() == null, "defaultButton event old value null");
        root.setDefaultButton(null);
        check(root.getDefaultButton() == null, "setDefaultButton(null)");

        // Headless reverse cases: valid styles are stored and then throw while applying
        // (FRAME -> HeadlessException; dialog styles -> NPE); the invalid 99 is
        // rejected up front and leaves the previous value in place.
        checkThrows(HeadlessException.class,
                new Runnable() { public void run() { root.setWindowDecorationStyle(JRootPane.FRAME); } },
                "setWindowDecorationStyle(FRAME) headless");
        check(root.getWindowDecorationStyle() == JRootPane.FRAME, "FRAME stored despite headless throw");
        checkThrows(NullPointerException.class,
                new Runnable() { public void run() { root.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG); } },
                "setWindowDecorationStyle(PLAIN_DIALOG) NPE");
        check(root.getWindowDecorationStyle() == JRootPane.PLAIN_DIALOG, "PLAIN_DIALOG stored despite NPE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { root.setWindowDecorationStyle(99); } },
                "setWindowDecorationStyle(99) IAE");
        check(root.getWindowDecorationStyle() == JRootPane.PLAIN_DIALOG, "invalid 99 leaves style unchanged");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testConstantsAndDefaults(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testPaneSwaps(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDefaultButtonAndStyle(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
