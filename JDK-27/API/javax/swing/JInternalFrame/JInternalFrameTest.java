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
}
