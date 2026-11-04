/*
 * @test
 * @summary javax.swing.JComponent conformance: white-box structure + black-box behavior.
 * @run main JComponentTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class JComponentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JComponent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.TransferHandler$HasGetTransferHandler"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JComponent c = new JComponent() {};
        check("ToolTipText".equals(JComponent.TOOL_TIP_TEXT_KEY), "tooltip key constant");
        check(c.isEnabled() == true, "default enabled");
        check(c.isOpaque() == false, "default opaque false");
        check(c.getToolTipText() == null, "default tooltip null");
        check(c.getClientProperty("anything") == null, "default client property null");
        check(c.getBorder() == null, "default border null");
        check(c.getFont() == null, "default font null");
        check(c.getForeground() == null, "default foreground null");
        check(c.getBackground() == null, "default background null");

        c.setEnabled(false);
        check(c.isEnabled() == false, "disabled");
        c.setEnabled(true);
        check(c.isEnabled() == true, "enabled");

        c.setToolTipText("tip");
        check("tip".equals(c.getToolTipText()), "tooltip set");
        MouseEvent evt = new MouseEvent(c, MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false);
        check("tip".equals(c.getToolTipText(evt)), "tooltip for event");
        c.setToolTipText(null);
        check(c.getToolTipText() == null, "tooltip cleared");

        c.putClientProperty("k", "v");
        check("v".equals(c.getClientProperty("k")), "client property set");
        c.putClientProperty("k", null);
        check(c.getClientProperty("k") == null, "client property removed");
        Object key = new Object();
        Object val = new Object();
        c.putClientProperty(key, val);
        check(c.getClientProperty(key) == val, "object key client property");

        c.setPreferredSize(new Dimension(10, 20));
        check(new Dimension(10, 20).equals(c.getPreferredSize()), "preferred size");
        c.setMaximumSize(new Dimension(30, 40));
        check(new Dimension(30, 40).equals(c.getMaximumSize()), "maximum size");
        c.setMinimumSize(new Dimension(5, 6));
        check(new Dimension(5, 6).equals(c.getMinimumSize()), "minimum size");

        Border b = new EmptyBorder(1, 2, 3, 4);
        c.setBorder(b);
        check(c.getBorder() == b, "border set");

        c.setVisible(true);
        check(c.isVisible() == true, "visible on");
        c.setVisible(false);
        check(c.isVisible() == false, "visible off");

        Font font = new Font(Font.SERIF, Font.BOLD, 12);
        c.setFont(font);
        check(font.equals(c.getFont()), "font set");
        c.setForeground(Color.RED);
        check(Color.RED.equals(c.getForeground()), "foreground set");
        c.setBackground(Color.BLUE);
        check(Color.BLUE.equals(c.getBackground()), "background set");

        c.setOpaque(true);
        check(c.isOpaque() == true, "opaque on");
        c.setOpaque(false);
        check(c.isOpaque() == false, "opaque off");

        c.setAlignmentX(0.75f);
        check(c.getAlignmentX() == 0.75f, "alignment x");
        c.setAlignmentY(0.25f);
        check(c.getAlignmentY() == 0.25f, "alignment y");

        c.setAutoscrolls(true);
        check(c.getAutoscrolls() == true, "autoscrolls on");
        c.setAutoscrolls(false);
        check(c.getAutoscrolls() == false, "autoscrolls off");

        c.setInheritsPopupMenu(true);
        check(c.getInheritsPopupMenu() == true, "inherits popup on");
        JPopupMenu popup = new JPopupMenu();
        c.setComponentPopupMenu(popup);
        check(c.getComponentPopupMenu() == popup, "component popup menu");
    }
}
