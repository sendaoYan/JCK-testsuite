/*
 * @test
 * @summary javax.swing.JLayer conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JLayerTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.LayerUI;

public class JLayerTest {
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
        runOnEDT(JLayerTest::testWhiteBox);
        runOnEDT(JLayerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLayer");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.Scrollable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.beans.PropertyChangeListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.accessibility.Accessible"), "interface 2");
    }

    static void testBlackBox() throws Exception {
        JLayer<JPanel> empty = new JLayer<JPanel>();
        check(empty.getView() == null, "empty view null");
        check(empty.getGlassPane() != null, "empty glass pane non-null");
        check(empty.getUI() != null, "empty ui non-null");
        check(empty.getGlassPane().isOpaque() == false, "glass pane not opaque");
        check(empty.getGlassPane() instanceof JPanel, "glass pane is JPanel");
        check(empty.getLayerEventMask() == 0L, "event mask default 0");

        JPanel view = new JPanel();
        JLayer<JPanel> layer = new JLayer<JPanel>(view);
        check(layer.getView() == view, "view roundtrip");
        check(view.getParent() == layer, "view parented to layer");

        JPanel view2 = new JPanel();
        layer.setView(view2);
        check(layer.getView() == view2, "setView new");
        check(view2.getParent() == layer, "new view parented");
        check(view.getParent() == null, "old view removed");
        layer.setView(null);
        check(layer.getView() == null, "setView null");

        JPanel gp = new JPanel();
        layer.setGlassPane(gp);
        check(layer.getGlassPane() == gp, "setGlassPane roundtrip");
        JPanel created = layer.createGlassPane();
        check(created != null, "createGlassPane non-null");
        check(created.isOpaque() == false, "createGlassPane not opaque");
        layer.setGlassPane(null);
        check(layer.getGlassPane() == null, "glass pane null");

        layer.setLayerEventMask(12345L);
        check(layer.getLayerEventMask() == 12345L, "event mask roundtrip");
        layer.setLayerEventMask(0L);
        check(layer.getLayerEventMask() == 0L, "event mask reset");

        LayerUI<JPanel> ui = new LayerUI<JPanel>();
        layer.setUI(ui);
        check(layer.getUI() == ui, "setUI/getUI roundtrip");

        JPanel v3 = new JPanel();
        LayerUI<JPanel> ui2 = new LayerUI<JPanel>();
        JLayer<JPanel> full = new JLayer<JPanel>(v3, ui2);
        check(full.getView() == v3, "view+ui ctor view");
        check(full.getUI() == ui2, "view+ui ctor ui");

        boolean iae = false;
        try { layer.setLayout(new java.awt.FlowLayout()); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setLayout non-null IAE");
        layer.setLayout(null);
        check(true, "setLayout null ok");

        JPanel bordered = new JPanel();
        JLayer<JPanel> bl = new JLayer<JPanel>(bordered);
        Border border = new EmptyBorder(1, 2, 3, 4);
        bl.setBorder(border);
        check(bl.getBorder() == border, "getBorder delegated");
        check(bordered.getBorder() == border, "view border set");

        bl.updateUI();
        check(true, "updateUI ok");
    }
}
