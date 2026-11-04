/*
 * @test
 * @summary javax.swing.JColorChooser conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JColorChooserTest
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.DefaultColorSelectionModel;

public class JColorChooserTest {
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
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JColorChooser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JColorChooser cc = new JColorChooser(Color.RED);
        check(Color.RED.equals(cc.getColor()), "ctor color");
        cc.setColor(Color.BLUE);
        check(Color.BLUE.equals(cc.getColor()), "setColor(Color)");
        cc.setColor(255, 0, 0);
        check(Color.RED.equals(cc.getColor()), "setColor(r,g,b)");
        cc.setColor(0x00FF00);
        check(new Color(0x00FF00).equals(cc.getColor()), "setColor(int)");

        check(cc.getSelectionModel() != null, "selection model non-null");
        check(cc.getSelectionModel().getSelectedColor().equals(cc.getColor()), "selection model tracks color");
        cc.setSelectionModel(new DefaultColorSelectionModel(Color.YELLOW));
        check(Color.YELLOW.equals(cc.getSelectionModel().getSelectedColor()), "setSelectionModel");
        check("ColorChooserUI".equals(cc.getUIClassID()), "UIClassID");

        check(cc.getPreviewPanel() != null, "preview panel non-null");
        JPanel preview = new JPanel();
        cc.setPreviewPanel(preview);
        check(cc.getPreviewPanel() == preview, "setPreviewPanel");

        check(cc.getChooserPanels() != null, "chooser panels non-null");
        AbstractColorChooserPanel panel = new AbstractColorChooserPanel() {
            public void updateChooser() { }
            public void buildChooser() { }
            public String getDisplayName() { return "test"; }
            public javax.swing.Icon getSmallDisplayIcon() { return null; }
            public javax.swing.Icon getLargeDisplayIcon() { return null; }
        };
        cc.addChooserPanel(panel);
        check(cc.removeChooserPanel(panel) == panel, "add/remove chooser panel");

        cc.setDragEnabled(true);
        check(cc.getDragEnabled(), "drag enabled");

        JFrame frame = new JFrame();
        frame.add(cc);
        frame.setSize(120, 120);
        frame.setVisible(true);
        frame.setVisible(false);
        frame.dispose();
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JColorChooserTest::testWhiteBox);
        runOnEDT(JColorChooserTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
