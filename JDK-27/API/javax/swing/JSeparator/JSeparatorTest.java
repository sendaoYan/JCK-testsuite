/*
 * @test
 * @summary javax.swing.JSeparator conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JSeparatorTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class JSeparatorTest {
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
        runOnEDT(JSeparatorTest::testWhiteBox);
        runOnEDT(JSeparatorTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSeparator");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JSeparator s = new JSeparator();
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "default orientation");
        check(s.isFocusable() == false, "not focusable");
        check("SeparatorUI".equals(s.getUIClassID()), "ui class id");
        check(s.getUI() != null, "ui non-null");

        JSeparator v = new JSeparator(SwingConstants.VERTICAL);
        check(v.getOrientation() == SwingConstants.VERTICAL, "vertical ctor");

        s.setOrientation(SwingConstants.VERTICAL);
        check(s.getOrientation() == SwingConstants.VERTICAL, "setOrientation vertical");
        s.setOrientation(SwingConstants.HORIZONTAL);
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "setOrientation horizontal");
        boolean iae = false;
        try { s.setOrientation(3); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid orientation IAE");

        s.setMinimumSize(new Dimension(5, 5));
        check(new Dimension(5, 5).equals(s.getMinimumSize()), "minimum size roundtrip");
        s.setMaximumSize(new Dimension(20, 20));
        check(new Dimension(20, 20).equals(s.getMaximumSize()), "maximum size roundtrip");
        s.setPreferredSize(new Dimension(10, 10));
        check(new Dimension(10, 10).equals(s.getPreferredSize()), "preferred size roundtrip");

        s.setMinimumSize(new Dimension(6, 7));
        check(new Dimension(6, 7).equals(s.getMinimumSize()), "minimum size roundtrip 2");
        s.setMaximumSize(new Dimension(21, 22));
        check(new Dimension(21, 22).equals(s.getMaximumSize()), "maximum size roundtrip 2");
        s.setPreferredSize(new Dimension(11, 12));
        check(new Dimension(11, 12).equals(s.getPreferredSize()), "preferred size roundtrip 2");

        s.setEnabled(false);
        check(s.isEnabled() == false, "disabled");
        s.setEnabled(true);
        check(s.isEnabled() == true, "enabled");

        s.setVisible(true);
        check(s.isVisible() == true, "visible on");
        s.setVisible(false);
        check(s.isVisible() == false, "visible off");

        s.setToolTipText("sep");
        check("sep".equals(s.getToolTipText()), "tooltip set");
        s.setToolTipText(null);
        check(s.getToolTipText() == null, "tooltip cleared");

        s.setBackground(Color.RED);
        check(Color.RED.equals(s.getBackground()), "background set");
        s.setForeground(Color.BLUE);
        check(Color.BLUE.equals(s.getForeground()), "foreground set");

        s.setOpaque(true);
        check(s.isOpaque() == true, "opaque on");
        s.setOpaque(false);
        check(s.isOpaque() == false, "opaque off");

        s.setName("sepname");
        check("sepname".equals(s.getName()), "name set");
    }
}
