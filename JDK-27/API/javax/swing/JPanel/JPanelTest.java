/*
 * @test
 * @summary javax.swing.JPanel conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JPanelTest
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.PanelUI;

public class JPanelTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JPanel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default construction: FlowLayout + double buffered + opaque.
        JPanel p = new JPanel();
        check(p.getLayout() instanceof FlowLayout, "default layout FlowLayout");
        check(p.isDoubleBuffered(), "default double buffered");
        check(p.isOpaque(), "default opaque");
        check(p.getUI() != null, "getUI non-null");
        check(p.getUI() instanceof PanelUI, "getUI instanceof PanelUI");
        check("PanelUI".equals(p.getUIClassID()), "getUIClassID");

        // Non-default buffering / layout constructors.
        JPanel p2 = new JPanel(false);
        check(!p2.isDoubleBuffered(), "JPanel(false) not double buffered");
        check(p2.getLayout() instanceof FlowLayout, "JPanel(false) FlowLayout");
        JPanel p3 = new JPanel(new BorderLayout());
        check(p3.getLayout() instanceof BorderLayout, "JPanel(layout) BorderLayout");
        JPanel p4 = new JPanel(new BorderLayout(), false);
        check(p4.getLayout() instanceof BorderLayout, "JPanel(layout,false) BorderLayout");
        check(!p4.isDoubleBuffered(), "JPanel(layout,false) not double buffered");

        // Client property roundtrip.
        p.putClientProperty("k", "v");
        check("v".equals(p.getClientProperty("k")), "putClientProperty/getClientProperty");
        p.putClientProperty("k2", null);
        check(p.getClientProperty("k2") == null, "client property null");
        check(p.getClientProperty("missing") == null, "missing client property null");

        // Border roundtrip.
        Border b = new LineBorder(Color.RED, 3);
        p.setBorder(b);
        check(p.getBorder() == b, "setBorder/getBorder same ref");
        p.setBorder(null);
        check(p.getBorder() == null, "setBorder(null)");

        // Colors and tooltip.
        p.setBackground(Color.RED);
        check(Color.RED.equals(p.getBackground()), "setBackground/getBackground");
        p.setForeground(Color.BLUE);
        check(Color.BLUE.equals(p.getForeground()), "setForeground/getForeground");
        p.setToolTipText("tip");
        check("tip".equals(p.getToolTipText()), "setToolTipText/getToolTipText");

        // Enabled roundtrip.
        p.setEnabled(false);
        check(!p.isEnabled(), "setEnabled(false)");
        p.setEnabled(true);
        check(p.isEnabled(), "setEnabled(true)");

        // Preferred/min/max size roundtrip.
        p.setPreferredSize(new Dimension(50, 60));
        check(new Dimension(50, 60).equals(p.getPreferredSize()), "setPreferredSize/getPreferredSize");
        p.setMinimumSize(new Dimension(10, 20));
        check(new Dimension(10, 20).equals(p.getMinimumSize()), "setMinimumSize/getMinimumSize");
        p.setMaximumSize(new Dimension(100, 120));
        check(new Dimension(100, 120).equals(p.getMaximumSize()), "setMaximumSize/getMaximumSize");

        // Child add/remove + deterministic BorderLayout layout.
        JPanel layoutPanel = new JPanel(new BorderLayout());
        JLabel child = new JLabel("x");
        child.setPreferredSize(new Dimension(120, 80));
        layoutPanel.add(child, BorderLayout.CENTER);
        check(layoutPanel.getComponentCount() == 1, "component count after add");
        layoutPanel.setSize(200, 150);
        layoutPanel.doLayout();
        check(new Rectangle(0, 0, 200, 150).equals(child.getBounds()), "BorderLayout CENTER fills");
        layoutPanel.removeAll();
        check(layoutPanel.getComponentCount() == 0, "component count after removeAll");

        // Accessible context present.
        check(p.getAccessibleContext() != null, "getAccessibleContext non-null");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JPanelTest::testWhiteBox);
        runOnEDT(JPanelTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
