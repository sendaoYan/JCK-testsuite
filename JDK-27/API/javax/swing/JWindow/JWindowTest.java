/*
 * @test
 * @summary javax.swing.JWindow conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JWindowTest
 */

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.JPanel;

public class JWindowTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsICSE(Runnable r) {
        try { r.run(); return false; }
        catch (java.awt.IllegalComponentStateException e) { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JWindow");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Window"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.RootPaneContainer"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.TransferHandler$HasGetTransferHandler"), "interface 2");
        // supplementary structural checks
        check(c.getConstructor(java.awt.Window.class) != null, "Window ctor present");
        check(c.getMethod("getGlassPane") != null, "getGlassPane present");
        check(java.awt.Window.class.isAssignableFrom(c), "is a java.awt.Window");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame owner = new JFrame("JWindowTestOwner");
        JWindow plain = new JWindow();
        JWindow owned = new JWindow(owner);
        try {
            check(owned.getOwner() == owner, "owner retained via ctor");
            check(plain.getOwner() != null, "shared-owner frame assigned for no-arg ctor");

            // root pane / panes chain
            JRootPane rp = plain.getRootPane();
            check(rp != null && rp instanceof JRootPane, "rootPane non-null JRootPane");
            check(plain.getRootPane() == rp, "getRootPane stable instance");

            Container cp = plain.getContentPane();
            check(cp != null, "contentPane non-null");
            check(plain.getLayeredPane() != null && plain.getLayeredPane() instanceof JLayeredPane,
                  "layeredPane non-null JLayeredPane");
            check(plain.getGlassPane() != null, "glassPane non-null");

            // add lands in the content pane; counts track
            JLabel l1 = new JLabel("one");
            plain.add(l1);
            check(l1.getParent() == cp, "window.add lands in contentPane");
            check(cp.getComponentCount() == 1, "contentPane count 1 after add");
            plain.add(new JButton("b"));
            check(cp.getComponentCount() == 2, "contentPane count 2 after add");
            plain.remove(l1);
            check(cp.getComponentCount() == 1, "contentPane count 1 after remove");

            // contentPane replacement + null rejection
            JPanel newCp = new JPanel();
            plain.setContentPane(newCp);
            check(plain.getContentPane() == newCp, "setContentPane round trip");
            check(throwsICSE(() -> plain.setContentPane(null)), "setContentPane(null) -> IllegalComponentStateException");

            // layeredPane / glassPane round trips
            JLayeredPane newLp = new JLayeredPane();
            plain.setLayeredPane(newLp);
            check(plain.getLayeredPane() == newLp, "setLayeredPane round trip");
            check(throwsICSE(() -> plain.setLayeredPane(null)), "setLayeredPane(null) -> IllegalComponentStateException");
            Canvas glass = new Canvas();
            plain.setGlassPane(glass);
            check(plain.getGlassPane() == glass, "setGlassPane round trip");
            check(!glass.isVisible(), "replacement glassPane invisible");

            // setLocation / setSize / setBounds round trips
            owned.setLocation(120, 140);
            check(owned.getX() == 120 && owned.getY() == 140, "setLocation round trip");
            owned.setSize(210, 220);
            check(owned.getWidth() == 210 && owned.getHeight() == 220, "setSize round trip");
            owned.setBounds(10, 20, 30, 40);
            check(owned.getX() == 10 && owned.getY() == 20 && owned.getWidth() == 30 && owned.getHeight() == 40,
                  "setBounds round trip");

            // visibility
            check(!owned.isVisible(), "not visible before show");
            owned.setVisible(false);
            check(!owned.isVisible(), "setVisible(false) -> !isVisible");

            // component listener registration (event delivery is not required here)
            final int[] moved = { 0 };
            ComponentListener cl = new ComponentListener() {
                public void componentMoved(ComponentEvent e) { moved[0]++; }
                public void componentResized(ComponentEvent e) { }
                public void componentShown(ComponentEvent e) { }
                public void componentHidden(ComponentEvent e) { }
            };
            owned.addComponentListener(cl);
            check(owned.getComponentListeners().length == 1, "component listener registered");
            owned.setLocation(15, 25);
            owned.removeComponentListener(cl);
            check(owned.getComponentListeners().length == 0, "component listener removed");
            check(owned.getX() == 15 && owned.getY() == 25, "location after listener cycle");
            Component child = cp.getComponent(0);
            check(child instanceof JButton, "remaining child is the button");
        } finally {
            plain.dispose();
            owned.dispose();
            owner.dispose();
        }
        check(!plain.isDisplayable() && !owned.isDisplayable() && !owner.isDisplayable(),
              "all windows disposed");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JWindowTest::testWhiteBox);
        runOnEDT(JWindowTest::testBlackBox);
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
