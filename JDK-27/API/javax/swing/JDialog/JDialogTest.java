/*
 * @test
 * @summary javax.swing.JDialog conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JDialogTest
 */

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JPanel;

public class JDialogTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JDialog");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Dialog"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.WindowConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.RootPaneContainer"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.TransferHandler$HasGetTransferHandler"), "interface 3");
        // supplementary structural checks
        check(c.getConstructor(java.awt.Frame.class, String.class, boolean.class) != null, "Frame,String,boolean ctor");
        check(c.getMethod("setDefaultCloseOperation", int.class) != null, "setDefaultCloseOperation present");
        check(c.getMethod("setModal", boolean.class) != null, "setModal inherited");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame owner = new JFrame("owner");
        JDialog plain = new JDialog();
        JDialog owned = new JDialog(owner, "TestDialog", true);
        try {
            // modeless default / modal construction
            check(plain.isModal() == false, "no-arg dialog is modeless");
            check(plain.getModalityType() == Dialog.ModalityType.MODELESS, "default modality type MODELESS");
            check(owned.isModal(), "modal ctor sets modal");
            check(owned.getModalityType() == Dialog.ModalityType.APPLICATION_MODAL,
                  "modal ctor -> APPLICATION_MODAL");
            check(owned.getOwner() == owner, "owner retained");

            // defaultCloseOperation: default + round trips + rejected values
            check(plain.getDefaultCloseOperation() == JDialog.HIDE_ON_CLOSE, "default close op HIDE_ON_CLOSE");
            plain.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            check(plain.getDefaultCloseOperation() == JDialog.DO_NOTHING_ON_CLOSE, "DO_NOTHING round trip");
            plain.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            check(plain.getDefaultCloseOperation() == JDialog.DISPOSE_ON_CLOSE, "DISPOSE round trip");
            plain.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            check(plain.getDefaultCloseOperation() == JDialog.HIDE_ON_CLOSE, "HIDE round trip");
            check(throwsIAE(() -> plain.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE)),
                  "EXIT_ON_CLOSE rejected by JDialog -> IAE");
            check(throwsIAE(() -> plain.setDefaultCloseOperation(42)), "close op 42 -> IAE");
            check(plain.getDefaultCloseOperation() == JDialog.HIDE_ON_CLOSE, "close op unchanged after IAE");

            // title
            check("TestDialog".equals(owned.getTitle()), "ctor title");
            owned.setTitle("renamed");
            check("renamed".equals(owned.getTitle()), "setTitle round trip");

            // resizable
            check(plain.isResizable(), "default resizable");
            plain.setResizable(false);
            check(!plain.isResizable(), "setResizable(false)");
            plain.setResizable(true);
            check(plain.isResizable(), "setResizable(true)");

            // content pane chain
            Container cp = plain.getContentPane();
            check(cp != null, "contentPane non-null");
            JLabel label = new JLabel("l");
            plain.add(label);
            check(label.getParent() == cp, "dialog.add lands in contentPane");
            JPanel newCp = new JPanel(new BorderLayout());
            plain.setContentPane(newCp);
            check(plain.getContentPane() == newCp, "setContentPane round trip");
            check(throwsICSE(() -> plain.setContentPane(null)), "setContentPane(null) -> IllegalComponentStateException");
            newCp.add(new JButton("ok"), BorderLayout.CENTER);

            // JMenuBar
            JMenuBar mb = new JMenuBar();
            plain.setJMenuBar(mb);
            check(plain.getJMenuBar() == mb, "setJMenuBar round trip");

            // root / layered / glass panes
            check(plain.getRootPane() != null && plain.getRootPane() instanceof JRootPane, "rootPane non-null JRootPane");
            check(plain.getLayeredPane() != null, "layeredPane non-null");
            javax.swing.JLayeredPane lp2 = new javax.swing.JLayeredPane();
            plain.setLayeredPane(lp2);
            check(plain.getLayeredPane() == lp2, "setLayeredPane round trip");
            check(throwsICSE(() -> plain.setLayeredPane(null)), "setLayeredPane(null) -> IllegalComponentStateException");
            Canvas glass = new Canvas();
            plain.setGlassPane(glass);
            check(plain.getGlassPane() == glass, "setGlassPane round trip");
            check(!glass.isVisible(), "replacement glassPane invisible");

            // pack + visibility off (never shown, and setVisible(false) keeps it hidden)
            plain.pack();
            check(!plain.isVisible(), "not visible before show");
            plain.setSize(220, 140);
            check(plain.getWidth() == 220 && plain.getHeight() == 140, "setSize round trip");
            plain.setVisible(false);
            check(!plain.isVisible(), "setVisible(false) -> !isVisible");
        } finally {
            plain.dispose();
            owned.dispose();
            owner.dispose();
        }
        check(!plain.isDisplayable() && !owned.isDisplayable() && !owner.isDisplayable(),
              "all disposed windows not displayable");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JDialogTest::testWhiteBox);
        runOnEDT(JDialogTest::testBlackBox);
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
