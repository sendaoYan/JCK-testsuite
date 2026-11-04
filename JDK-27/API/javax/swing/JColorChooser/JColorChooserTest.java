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
        runOnEDT(JColorChooserTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
    }
    // Appended contract section: ctor matrix, event counting, panel count
    // rules, removeChooserPanel IAE, JDK-27 defensive-copy getChooserPanels.
    static void testContract() {
        JColorChooser plain = new JColorChooser();
        check(Color.WHITE.equals(plain.getColor()), "no-arg default color WHITE");
        check(plain.getSelectionModel() instanceof DefaultColorSelectionModel,
              "default model DefaultColorSelectionModel");
        DefaultColorSelectionModel m = new DefaultColorSelectionModel(Color.GREEN);
        JColorChooser mc = new JColorChooser(m);
        check(mc.getSelectionModel() == m, "ctor(model) keeps model identity");
        check(Color.GREEN.equals(mc.getColor()), "ctor(model) color follows model");

        // change events: 1 for a different color, 0 for the same color
        final int[] fired = {0};
        plain.getSelectionModel().addChangeListener(e -> fired[0]++);
        plain.setColor(Color.RED);
        check(fired[0] == 1, "setColor different color fires exactly 1 event");
        plain.setColor(Color.RED);
        check(fired[0] == 1, "setColor same color fires 0 events");
        DefaultColorSelectionModel m2 = new DefaultColorSelectionModel(Color.CYAN);
        plain.setSelectionModel(m2);
        check(Color.CYAN.equals(plain.getColor()), "color follows new model");
        final int[] fired2 = {0};
        m2.addChangeListener(e -> fired2[0]++);
        plain.setColor(Color.DARK_GRAY);
        check(fired2[0] == 1, "setColor routes through the new model's listeners");

        // panel count rules on fresh choosers
        JColorChooser p1 = new JColorChooser();
        check(p1.getChooserPanels().length == 5, "Metal default panels == 5");
        AbstractColorChooserPanel nop = new AbstractColorChooserPanel() {
            public void updateChooser() { }
            public void buildChooser() { }
            public String getDisplayName() { return "nop"; }
            public javax.swing.Icon getSmallDisplayIcon() { return null; }
            public javax.swing.Icon getLargeDisplayIcon() { return null; }
        };
        p1.addChooserPanel(nop);
        check(p1.getChooserPanels().length == 6, "addChooserPanel grows list to 6");
        check(p1.removeChooserPanel(nop) == nop, "removeChooserPanel returns removed panel");
        check(p1.getChooserPanels().length == 5, "back to 5 after remove");

        // removing a never-added panel: IllegalArgumentException, list unchanged
        JColorChooser p2 = new JColorChooser();
        try { p2.removeChooserPanel(new AbstractColorChooserPanel() {
                public void updateChooser() { }
                public void buildChooser() { }
                public String getDisplayName() { return "absent"; }
                public javax.swing.Icon getSmallDisplayIcon() { return null; }
                public javax.swing.Icon getLargeDisplayIcon() { return null; }
            });
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { passed++; }
        check(p2.getChooserPanels().length == 5, "failed remove leaves list unchanged");

        // re-adding an already-installed panel: panel rejects the second install
        JColorChooser p3 = new JColorChooser();
        try { p3.addChooserPanel(p3.getChooserPanels()[0]);
              throw new AssertionError("expected RuntimeException"); }
        catch (RuntimeException expected) { passed++; }

        // JDK-27 contract: getChooserPanels() returns a fresh copy each call
        // (JDK-8 returned the same aliased internal array instead)
        JColorChooser p4 = new JColorChooser();
        check(p4.getChooserPanels() != p4.getChooserPanels(),
              "JDK-27: repeated getChooserPanels returns defensive copies");
        check(p4.getChooserPanels().length == 5, "defensive copy same length");

        plain.setPreviewPanel(null);
        check(plain.getPreviewPanel() == null, "setPreviewPanel(null) -> null");
        check(plain.getUI() instanceof javax.swing.plaf.ColorChooserUI, "getUI is ColorChooserUI");
        plain.updateUI();
        check(plain.getUI() != null, "updateUI no-throw");
        check(plain.getAccessibleContext() != null, "accessible context non-null");
    }
}