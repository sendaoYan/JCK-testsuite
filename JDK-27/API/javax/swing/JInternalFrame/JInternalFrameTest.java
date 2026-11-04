/*
 * @test
 * @summary javax.swing.JInternalFrame conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JInternalFrameTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

public class JInternalFrameTest {
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
        runOnEDT(JInternalFrameTest::testWhiteBox);
        runOnEDT(JInternalFrameTest::testBlackBox);
        runOnEDT(JInternalFrameTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JInternalFrame");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.WindowConstants"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.RootPaneContainer"), "interface 2");
    }

    static void testBlackBox() throws Exception {
        JInternalFrame d = new JInternalFrame();
        check("".equals(d.getTitle()), "default empty title");
        check(d.isClosable() == false, "default not closable");
        check(d.isIconifiable() == false, "default not iconifiable");
        check(d.isMaximizable() == false, "default not maximizable");
        check(d.isResizable() == false, "default not resizable");

        JInternalFrame f = new JInternalFrame("one", true, true, true, true);
        check("one".equals(f.getTitle()), "title one");
        check(f.isResizable() == true, "resizable");
        check(f.isClosable() == true, "closable");
        check(f.isMaximizable() == true, "maximizable");
        check(f.isIconifiable() == true, "iconifiable");
        check(f.isClosed() == false, "not closed");
        check(f.isMaximum() == false, "not maximum");
        check(f.isIcon() == false, "not icon");
        check(f.isSelected() == false, "not selected");
        check(f.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE, "default close op");

        check(f.getRootPane() != null, "root pane");
        check(f.getContentPane() != null, "content pane");
        check(f.getLayeredPane() != null, "layered pane");
        check(f.getGlassPane() != null, "glass pane");
        check(f.getMenuBar() == null, "default menu bar null");

        f.setTitle("two");
        check("two".equals(f.getTitle()), "setTitle");
        f.setClosable(false);
        check(f.isClosable() == false, "closable off");
        f.setIconifiable(false);
        check(f.isIconifiable() == false, "iconifiable off");
        f.setMaximizable(false);
        check(f.isMaximizable() == false, "maximizable off");
        f.setResizable(false);
        check(f.isResizable() == false, "resizable off");

        JMenuBar mb = new JMenuBar();
        f.setMenuBar(mb);
        check(f.getMenuBar() == mb, "menu bar roundtrip");

        f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        check(f.getDefaultCloseOperation() == WindowConstants.HIDE_ON_CLOSE, "hide on close");

        check(f.getLayer() == 0, "default layer 0");
        f.setLayer(5);
        check(f.getLayer() == 5, "setLayer 5");

        check(f.getDesktopPane() == null, "desktop pane null before add");
        check(f.getNormalBounds() != null, "normal bounds non-null default");
        f.setNormalBounds(new Rectangle(1, 2, 3, 4));
        check(new Rectangle(1, 2, 3, 4).equals(f.getNormalBounds()), "normal bounds roundtrip");

        Icon ic = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {}
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 8; }
        };
        f.setFrameIcon(ic);
        check(f.getFrameIcon() == ic, "frame icon roundtrip");

        JFrame window = new JFrame();
        JDesktopPane desk = new JDesktopPane();
        JInternalFrame sel = new JInternalFrame("sel", true, true, true, true);
        desk.add(sel);
        window.setContentPane(desk);
        window.setSize(400, 300);
        window.setVisible(true);
        sel.setVisible(true);
        check(sel.isShowing() == true, "internal frame showing");
        check(sel.getDesktopPane() == desk, "desktop pane after add");
        sel.setSelected(true);
        check(sel.isSelected() == true, "selected after setSelected(true)");
        sel.setSelected(false);
        check(sel.isSelected() == false, "deselected after setSelected(false)");
        window.dispose();
    }

    // Appended contract section: parameter-order matrix, bound-property event
    // names, pane null contracts, close/veto semantics, JDK-27 setMaximum.
    static void testContract() throws Exception {
        // constructor parameter order (title, resizable, closable, maximizable, iconifiable)
        JInternalFrame f1 = new JInternalFrame("p1", true);
        check(f1.isResizable() && !f1.isClosable() && !f1.isMaximizable() && !f1.isIconifiable(),
              "2-arg: resizable position");
        JInternalFrame f2 = new JInternalFrame("p2", false, true);
        check(!f2.isResizable() && f2.isClosable() && !f2.isMaximizable() && !f2.isIconifiable(),
              "3-arg: closable position");
        JInternalFrame f3 = new JInternalFrame("p3", false, false, true);
        check(!f3.isResizable() && !f3.isClosable() && f3.isMaximizable() && !f3.isIconifiable(),
              "4-arg: maximizable position");
        JInternalFrame f4 = new JInternalFrame("p4", false, false, false, true);
        check(!f4.isResizable() && !f4.isClosable() && !f4.isMaximizable() && f4.isIconifiable(),
              "5-arg: iconifiable position");

        JInternalFrame f = new JInternalFrame("ct");
        // bound-property event names (note: setIconifiable fires "iconable")
        final java.util.List<String> names = new java.util.ArrayList<String>();
        f.addPropertyChangeListener(e -> names.add(e.getPropertyName()));
        f.setTitle("T");
        check(names.contains("title"), "setTitle fires \"title\"");
        f.setTitle(null);
        check(f.getTitle() == null, "setTitle(null) -> null");
        f.setClosable(true);
        check(names.contains("closable"), "setClosable fires \"closable\"");
        f.setResizable(true);
        check(names.contains("resizable"), "setResizable fires \"resizable\"");
        f.setMaximizable(true);
        check(names.contains("maximizable"), "setMaximizable fires \"maximizable\"");
        f.setIconifiable(true);
        check(names.contains("iconable"), "setIconifiable fires \"iconable\" (not \"iconifiable\")");

        // root pane container null contracts
        check(f.getContentPane().getLayout() instanceof java.awt.BorderLayout,
              "contentPane default layout BorderLayout");
        try { f.setContentPane(null); throw new AssertionError("expected IllegalComponentStateException"); }
        catch (java.awt.IllegalComponentStateException expected) { passed++; }
        try { f.setGlassPane(null); throw new AssertionError("expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
        try { f.setLayeredPane(null); throw new AssertionError("expected IllegalComponentStateException"); }
        catch (java.awt.IllegalComponentStateException expected) { passed++; }
        f.setJMenuBar(new JMenuBar());
        f.setJMenuBar(null);
        check(f.getJMenuBar() == null, "setJMenuBar(null) allowed");

        // setClosed(true) is effective even when closable == false
        JInternalFrame fc = new JInternalFrame("cc"); // closable == false
        fc.setClosed(true);
        check(fc.isClosed(), "setClosed(true) effective even when closable==false");

        // vetoing VetoableChangeListener keeps isClosed false
        JInternalFrame fv = new JInternalFrame("vv", false, true);
        fv.addVetoableChangeListener(e -> {
            if ("closed".equals(e.getPropertyName())) throw new java.beans.PropertyVetoException("veto", e);
        });
        try { fv.setClosed(true); throw new AssertionError("expected PropertyVetoException"); }
        catch (java.beans.PropertyVetoException expected) { passed++; }
        check(!fv.isClosed(), "vetoed close leaves isClosed false");

        // JDK-27 contract: setMaximum(true) completes even without a showing
        // desktop (JDK-8 threw NPE from the UI path instead)
        JInternalFrame fm = new JInternalFrame("mm");
        fm.setMaximum(true);
        check(fm.isMaximum(), "setMaximum(true) effective (JDK-27; JDK-8 threw NPE)");
        fm.setMaximum(false);
        check(!fm.isMaximum(), "setMaximum(false) roundtrip");

        // selection/icon on an unshown frame: no veto escapes
        JInternalFrame fs = new JInternalFrame("ss");
        fs.setSelected(true);
        check(!fs.isSelected(), "setSelected(true) unshown: no-throw, isSelected false");
        fs.setIcon(true);
        check(fs.isIcon(), "setIcon(true) unshown");
        fs.setIcon(false);
        check(!fs.isIcon(), "setIcon(false) unshown");

        // UI plumbing (LaF-independent)
        check(f.getUIClassID().equals("InternalFrameUI"), "getUIClassID");
        check(f.getUI() instanceof javax.swing.plaf.InternalFrameUI, "getUI is InternalFrameUI");
        f.updateUI();
        check(f.getUI() != null, "updateUI no-throw");

        f.setLocation(11, 22);
        check(f.getX() == 11 && f.getY() == 22, "setLocation roundtrip");
        f.setSize(120, 90);
        check(f.getWidth() == 120 && f.getHeight() == 90, "setSize roundtrip");
        f.pack(); f.toFront(); f.toBack(); f.moveToFront(); f.moveToBack();
        passed++; // lifecycle methods no-throw
        check(f.getWarningString() == null, "getWarningString null");
        check(f.getAccessibleContext() != null, "accessible context non-null");
    }
}
