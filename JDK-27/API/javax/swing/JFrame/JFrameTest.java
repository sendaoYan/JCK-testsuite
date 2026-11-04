/*
 * @test
 * @summary javax.swing.JFrame conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JFrameTest
 */

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JPanel;

public class JFrameTest {
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
        Class<?> c = Class.forName("javax.swing.JFrame");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Frame"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.WindowConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.RootPaneContainer"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.TransferHandler$HasGetTransferHandler"), "interface 3");
        // supplementary structural checks
        check(c.getConstructor(String.class) != null, "String ctor present");
        check(c.getMethod("setDefaultCloseOperation", int.class) != null, "setDefaultCloseOperation present");
        check(c.getField("EXIT_ON_CLOSE").getInt(null) == 3, "EXIT_ON_CLOSE constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        try {
            // defaultCloseOperation: default + constant round trips + illegal values
            check(frame.getDefaultCloseOperation() == JFrame.HIDE_ON_CLOSE, "default close op is HIDE_ON_CLOSE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            check(frame.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE, "EXIT_ON_CLOSE round trip");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            check(frame.getDefaultCloseOperation() == JFrame.DO_NOTHING_ON_CLOSE, "DO_NOTHING round trip");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            check(frame.getDefaultCloseOperation() == JFrame.DISPOSE_ON_CLOSE, "DISPOSE round trip");
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            check(frame.getDefaultCloseOperation() == JFrame.HIDE_ON_CLOSE, "HIDE round trip");
            check(throwsIAE(() -> frame.setDefaultCloseOperation(7)), "close op 7 -> IAE");
            check(throwsIAE(() -> frame.setDefaultCloseOperation(-1)), "close op -1 -> IAE");
            check(frame.getDefaultCloseOperation() == JFrame.HIDE_ON_CLOSE, "close op unchanged after IAE");

            // root pane
            check(frame.getRootPane() != null, "rootPane non-null");
            check(frame.getRootPane() instanceof JRootPane, "rootPane instanceof JRootPane");
            JRootPane rp = frame.getRootPane();
            check(frame.getRootPane() == rp, "getRootPane stable instance");

            // title
            check("".equals(frame.getTitle()), "default title empty string");
            frame.setTitle("first");
            check("first".equals(frame.getTitle()), "setTitle round trip");
            check(new JFrame("titled").getTitle().equals("titled"), "ctor title");

            // iconImages default empty; setIconImage makes a 1-element list
            check(frame.getIconImages().isEmpty(), "iconImages default empty");
            BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            frame.setIconImage(icon);
            List<java.awt.Image> icons = frame.getIconImages();
            check(icons.size() == 1 && icons.contains(icon), "iconImages after setIconImage");

            // rootPaneCheckingEnabled behavior: add/setLayout/remove go to the content pane
            Container cp = frame.getContentPane();
            check(cp != null, "contentPane non-null");
            JLabel label = new JLabel("hi");
            frame.add(label);
            check(label.getParent() == cp, "frame.add lands in contentPane");
            check(cp.getComponentCount() == 1, "contentPane component count after add");
            FlowLayout flow = new FlowLayout();
            frame.setLayout(flow);
            check(cp.getLayout() == flow, "frame.setLayout goes to contentPane");
            frame.remove(label);
            check(cp.getComponentCount() == 0, "frame.remove drops from contentPane");

            // contentPane replacement + null rejection
            JPanel newCp = new JPanel(new BorderLayout());
            frame.setContentPane(newCp);
            check(frame.getContentPane() == newCp, "setContentPane round trip");
            JLabel label2 = new JLabel("two");
            frame.add(label2);
            check(label2.getParent() == newCp, "add lands in replaced contentPane");
            check(throwsICSE(() -> frame.setContentPane(null)), "setContentPane(null) -> IllegalComponentStateException");

            // JMenuBar
            JMenuBar mb = new JMenuBar();
            frame.setJMenuBar(mb);
            check(frame.getJMenuBar() == mb, "setJMenuBar round trip");
            frame.setJMenuBar(null);
            check(frame.getJMenuBar() == null, "setJMenuBar(null)");

            // layeredPane / glassPane chain
            check(frame.getLayeredPane() != null, "layeredPane non-null");
            javax.swing.JLayeredPane newLp = new javax.swing.JLayeredPane();
            frame.setLayeredPane(newLp);
            check(frame.getLayeredPane() == newLp, "setLayeredPane round trip");
            check(frame.getContentPane() == newCp, "contentPane unchanged by setLayeredPane");
            check(throwsICSE(() -> frame.setLayeredPane(null)), "setLayeredPane(null) -> IllegalComponentStateException");

            check(frame.getGlassPane() != null, "glassPane non-null");
            check(frame.getGlassPane().isVisible() == false, "default glassPane invisible");
            Canvas glass = new Canvas();
            frame.setGlassPane(glass);
            check(frame.getGlassPane() == glass, "setGlassPane round trip");
            check(glass.isVisible() == false, "replacement glassPane inherits invisibility");
            check(throwsNPE(() -> frame.setGlassPane(null)), "setGlassPane(null) -> NPE");

            // pack + setSize + visibility cycle
            newCp.add(new JButton("ok"), BorderLayout.CENTER);
            frame.pack();
            frame.setSize(300, 200);
            check(frame.getWidth() == 300 && frame.getHeight() == 200, "setSize round trip");
            check(frame.isVisible() == false, "not visible before show");
            frame.setVisible(true);
            check(frame.isVisible(), "setVisible(true)");
            frame.setVisible(false);
            check(!frame.isVisible(), "setVisible(false)");
        } finally {
            frame.dispose();
        }
        check(!frame.isDisplayable(), "disposed frame not displayable");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JFrameTest::testWhiteBox);
        runOnEDT(JFrameTest::testBlackBox);
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
