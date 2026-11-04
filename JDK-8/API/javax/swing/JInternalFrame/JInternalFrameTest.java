/*
 * @test
 * @summary javax.swing.JInternalFrame conformance: type shape via reflection (headless-safe)
 *          plus behavioral contract (Metal LaF, EDT discipline, headless).
 * @run main JInternalFrameTest
 */

import java.awt.BorderLayout;
import java.awt.IllegalComponentStateException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalInternalFrameUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JInternalFrameTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            try { behavior(); }
            catch (PropertyVetoException e) { throw new AssertionError("unexpected PVE", e); }
        }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JInternalFrame", false, JInternalFrameTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.swing.WindowConstants"), "implements javax.swing.WindowConstants");
        check(hasInterface(c, "javax.swing.RootPaneContainer"), "implements javax.swing.RootPaneContainer");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() throws PropertyVetoException {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        // constructor parameter order: title, resizable, closable, maximizable, iconifiable
        JInternalFrame f0 = new JInternalFrame();
        check(f0.getTitle().equals(""), "no-arg title is empty string");
        check(!f0.isResizable() && !f0.isClosable() && !f0.isMaximizable() && !f0.isIconifiable(),
              "no-arg flags all false");
        check(f0.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE,
              "default close operation DISPOSE_ON_CLOSE(2)");
        JInternalFrame f1 = new JInternalFrame("t1");
        check(f1.getTitle().equals("t1"), "1-arg title");
        check(!f1.isResizable(), "2nd param default resizable=false");
        JInternalFrame f2 = new JInternalFrame("t2", true);
        check(f2.isResizable(), "2-arg: resizable position");
        check(!f2.isClosable(), "3rd param default closable=false");
        JInternalFrame f3 = new JInternalFrame("t3", false, true);
        check(!f3.isResizable() && f3.isClosable(), "3-arg: closable position");
        check(!f3.isMaximizable(), "4th param default maximizable=false");
        JInternalFrame f4 = new JInternalFrame("t4", false, false, true);
        check(f4.isMaximizable() && !f4.isClosable(), "4-arg: maximizable position");
        check(!f4.isIconifiable(), "5th param default iconifiable=false");
        JInternalFrame f5 = new JInternalFrame("t5", true, true, true, true);
        check(f5.isResizable() && f5.isClosable() && f5.isMaximizable() && f5.isIconifiable(),
              "5-arg: iconifiable position");

        // UI plumbing
        check(f0.getUIClassID().equals("InternalFrameUI"), "getUIClassID");
        check(f0.getUI() instanceof MetalInternalFrameUI, "Metal LaF installs MetalInternalFrameUI");
        f0.updateUI();
        check(f0.getUI() != null, "updateUI no-throw");

        // root pane containment contract
        check(f0.getRootPane() != null, "rootPane non-null");
        check(f0.getContentPane() != null, "contentPane non-null");
        check(f0.getContentPane().getLayout() instanceof BorderLayout, "contentPane layout BorderLayout");
        JPanel cp = new JPanel();
        f0.setContentPane(cp);
        check(f0.getContentPane() == cp, "setContentPane roundtrip");
        checkThrows(IllegalComponentStateException.class, new Runnable() {
            public void run() { f0.setContentPane(null); }});
        check(f0.getGlassPane() != null, "glassPane non-null");
        checkThrows(NullPointerException.class, new Runnable() {
            public void run() { f0.setGlassPane(null); }});
        check(f0.getLayeredPane() != null, "layeredPane non-null");
        checkThrows(IllegalComponentStateException.class, new Runnable() {
            public void run() { f0.setLayeredPane(null); }});
        check(f0.getJMenuBar() == null, "jMenuBar default null");
        JMenuBar mb = new JMenuBar();
        f0.setJMenuBar(mb);
        check(f0.getJMenuBar() == mb, "setJMenuBar roundtrip");
        f0.setJMenuBar(null);
        check(f0.getJMenuBar() == null, "setJMenuBar(null) allowed");

        // title + property event names
        final List<String> names = new ArrayList<String>();
        f0.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { names.add(e.getPropertyName()); }});
        f0.setTitle("X");
        check(f0.getTitle().equals("X"), "setTitle roundtrip");
        f0.setTitle(null);
        check(f0.getTitle() == null, "setTitle(null)");
        check(names.contains("title"), "setTitle fires \"title\"");
        f0.setClosable(true);
        check(f0.isClosable(), "setClosable roundtrip");
        check(names.contains("closable"), "setClosable fires \"closable\"");
        f0.setResizable(true);
        check(f0.isResizable(), "setResizable roundtrip");
        check(names.contains("resizable"), "setResizable fires \"resizable\"");
        f0.setMaximizable(true);
        check(f0.isMaximizable(), "setMaximizable roundtrip");
        check(names.contains("maximizable"), "setMaximizable fires \"maximizable\"");
        f0.setIconifiable(true);
        check(f0.isIconifiable(), "setIconifiable roundtrip");
        check(names.contains("iconable"), "setIconifiable fires \"iconable\" (not \"iconifiable\")");
        int dc = f0.getDefaultCloseOperation();
        f0.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        check(f0.getDefaultCloseOperation() == WindowConstants.DO_NOTHING_ON_CLOSE, "setDefaultCloseOperation roundtrip");
        f0.setDefaultCloseOperation(dc);

        // selection: no-throw when unshown, isSelected stays false
        f0.setSelected(true);
        check(!f0.isSelected(), "setSelected(true) unshown: no-throw, isSelected false");
        f0.setSelected(false);
        check(!f0.isSelected(), "setSelected(false) unshown: no-throw");

        // icon: headless no-veto, both directions
        f0.setIcon(true);
        check(f0.isIcon(), "setIcon(true)");
        f0.setIcon(false);
        check(!f0.isIcon(), "setIcon(false)");

        // JDK-8 contract: setMaximum(true) headless throws NPE from the UI path,
        // but the maximized flag itself is already set (JDK-27 completes without throwing)
        JInternalFrame fm = new JInternalFrame("m");
        checkThrows(NullPointerException.class, new Runnable() {
            public void run() {
                try { fm.setMaximum(true); }
                catch (PropertyVetoException e) { throw new RuntimeException(e); }
            }});
        check(fm.isMaximum(), "isMaximum true even though setMaximum threw NPE");
        try { fm.setMaximum(false); passed++; } catch (PropertyVetoException e) { throw new AssertionError(e); }
        check(!fm.isMaximum(), "setMaximum(false) headless recovers");

        // setClosed(true) works even when closable == false
        JInternalFrame fc = new JInternalFrame("c"); // closable=false
        try {
            fc.setClosed(true);
            passed++;
        } catch (PropertyVetoException e) {
            throw new AssertionError("setClosed must not veto when closable==false: " + e);
        }
        check(fc.isClosed(), "setClosed(true) effective even when closable==false");

        // VetoableChangeListener veto keeps isClosed false
        final JInternalFrame fv = new JInternalFrame("v", false, true); // closable
        fv.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                if ("closed".equals(e.getPropertyName())) throw new PropertyVetoException("veto", e);
            }});
        try {
            fv.setClosed(true);
            throw new AssertionError("expected PropertyVetoException");
        } catch (PropertyVetoException expected) { passed++; }
        check(!fv.isClosed(), "vetoed close leaves isClosed false");

        // desktop linkage absent for unattached frame
        check(f0.getDesktopPane() == null, "getDesktopPane null unattached");
        check(f0.getWarningString() == null, "getWarningString null");

        // geometry + no-throw lifecycle methods
        f0.setLocation(11, 22);
        check(f0.getX() == 11 && f0.getY() == 22, "setLocation roundtrip");
        f0.setSize(120, 90);
        check(f0.getWidth() == 120 && f0.getHeight() == 90, "setSize roundtrip");
        f0.pack(); f0.toFront(); f0.toBack(); f0.moveToFront(); f0.moveToBack();
        passed++; // lifecycle methods no-throw headless

        // layer helpers
        f0.setLayer(JLayeredPane.PALETTE_LAYER);
        check(f0.getLayer() == JLayeredPane.PALETTE_LAYER.intValue(), "setLayer/getLayer");

        check(f0.getAccessibleContext() != null, "accessible context non-null");
        JDesktopPane dp = new JDesktopPane();
        dp.add(f0);
        check(f0.getDesktopPane() == dp, "getDesktopPane after add");
    }
}
