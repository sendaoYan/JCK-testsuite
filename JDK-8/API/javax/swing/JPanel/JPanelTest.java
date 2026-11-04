/*
 * @test
 * @summary javax.swing.JPanel conformance: type shape via reflection + panel contract
 *          (constructors, default FlowLayout, layout swap, child bookkeeping, UI
 *          delegate; headless-safe, Metal LaF, EDT).
 * @run main JPanelTest
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JPanelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JPanel", false, JPanelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    // ---- Part 2: constructors and defaults ----
    static void testDefaults() {
        JPanel p = new JPanel();
        check(p.getLayout() instanceof FlowLayout, "default layout FlowLayout");
        check(p.isDoubleBuffered(), "default double buffered");
        check(p.isOpaque(), "default opaque");
        check(p.getUI() != null, "getUI non-null");
        check(p.getUI() instanceof PanelUI, "getUI instanceof PanelUI");
        check("PanelUI".equals(p.getUIClassID()), "getUIClassID PanelUI");
        check("BasicPanelUI".equals(p.getUI().getClass().getSimpleName()), "Metal installs BasicPanelUI");
        check(p.isOptimizedDrawingEnabled(), "optimized drawing default true");

        JPanel p2 = new JPanel(false);
        check(!p2.isDoubleBuffered(), "JPanel(false) not double buffered");
        check(p2.getLayout() instanceof FlowLayout, "JPanel(false) FlowLayout");
        JPanel p3 = new JPanel(new BorderLayout());
        check(p3.getLayout() instanceof BorderLayout, "JPanel(layout) BorderLayout");
        check(p3.isDoubleBuffered(), "JPanel(layout) double buffered default true");
        JPanel p4 = new JPanel(new BorderLayout(), false);
        check(p4.getLayout() instanceof BorderLayout, "JPanel(layout,false) BorderLayout");
        check(!p4.isDoubleBuffered(), "JPanel(layout,false) not double buffered");
        JPanel p5 = new JPanel(new BorderLayout(), true);
        check(p5.isDoubleBuffered(), "JPanel(layout,true) double buffered");

        // Layout manager swap.
        p.setLayout(new GridLayout(1, 2));
        check(p.getLayout() instanceof GridLayout, "setLayout round-trip");
        p.setLayout(new FlowLayout());
        check(p.getLayout() instanceof FlowLayout, "setLayout back to FlowLayout");
        p.updateUI();
        check(p.getUI() instanceof PanelUI, "updateUI keeps PanelUI");
    }

    // ---- Part 3: properties, children, deterministic layout ----
    static void testPropertiesAndChildren() {
        JPanel p = new JPanel();
        p.putClientProperty("k", "v");
        check("v".equals(p.getClientProperty("k")), "client property round-trip");
        p.putClientProperty("k2", null);
        check(p.getClientProperty("k2") == null, "client property null");
        check(p.getClientProperty("missing") == null, "missing client property null");

        Border b = new LineBorder(Color.RED, 3);
        p.setBorder(b);
        check(p.getBorder() == b, "border round-trip");
        p.setBorder(null);
        check(p.getBorder() == null, "setBorder(null)");

        p.setBackground(Color.RED);
        check(Color.RED.equals(p.getBackground()), "background round-trip");
        p.setForeground(Color.BLUE);
        check(Color.BLUE.equals(p.getForeground()), "foreground round-trip");
        p.setToolTipText("tip");
        check("tip".equals(p.getToolTipText()), "tooltip round-trip");
        p.setEnabled(false);
        check(!p.isEnabled(), "setEnabled(false)");
        p.setEnabled(true);
        check(p.isEnabled(), "setEnabled(true)");

        p.setPreferredSize(new Dimension(50, 60));
        check(new Dimension(50, 60).equals(p.getPreferredSize()), "preferred size round-trip");
        p.setMinimumSize(new Dimension(10, 20));
        check(new Dimension(10, 20).equals(p.getMinimumSize()), "minimum size round-trip");
        p.setMaximumSize(new Dimension(100, 120));
        check(new Dimension(100, 120).equals(p.getMaximumSize()), "maximum size round-trip");

        // Child bookkeeping.
        Icon dot = new Icon() {
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {}
            public int getIconWidth() { return 4; }
            public int getIconHeight() { return 4; }
        };
        p.add(new JLabel("a", dot, JLabel.LEFT));
        p.add(new JLabel("b"));
        check(p.getComponentCount() == 2, "component count after adds");
        check(p.getComponent(1) instanceof JLabel, "getComponent(int) type");
        check("b".equals(((JLabel) p.getComponent(1)).getText()), "getComponent(int) identity");
        p.remove(1);
        check(p.getComponentCount() == 1, "component count after remove(int)");
        p.removeAll();
        check(p.getComponentCount() == 0, "component count after removeAll");

        // Deterministic BorderLayout geometry with a fixed-size child.
        JPanel layoutPanel = new JPanel(new BorderLayout());
        JLabel child = new JLabel("x");
        child.setPreferredSize(new Dimension(120, 80));
        layoutPanel.add(child, BorderLayout.CENTER);
        check(layoutPanel.getComponentCount() == 1, "center add count");
        layoutPanel.setSize(200, 150);
        layoutPanel.doLayout();
        check(new Rectangle(0, 0, 200, 150).equals(child.getBounds()), "BorderLayout CENTER fills panel");
        check(p.getAccessibleContext() != null, "accessible context non-null");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDefaults(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testPropertiesAndChildren(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
