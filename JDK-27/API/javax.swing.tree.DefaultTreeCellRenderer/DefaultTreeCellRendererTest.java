/*
 * @test
 * @summary javax.swing.tree.DefaultTreeCellRenderer conformance: white-box structure + black-box behavior.
 * @run main DefaultTreeCellRendererTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.Modifier;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DefaultTreeCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class TestIcon implements Icon {
        final int w, h;
        TestIcon(int w, int h) { this.w = w; this.h = h; }
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeCellRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLabel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.tree.TreeCellRenderer"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();

        // Default icons come from the look and feel (Metal, headless-safe).
        check(r.getDefaultLeafIcon() != null, "default leaf icon");
        check(r.getDefaultOpenIcon() != null, "default open icon");
        check(r.getDefaultClosedIcon() != null, "default closed icon");

        // Icon set/get round-trips.
        Icon leaf = new TestIcon(16, 16);
        Icon open = new TestIcon(20, 20);
        Icon closed = new TestIcon(20, 20);
        r.setLeafIcon(leaf);
        r.setOpenIcon(open);
        r.setClosedIcon(closed);
        check(r.getLeafIcon() == leaf, "setLeafIcon/getLeafIcon");
        check(r.getOpenIcon() == open, "setOpenIcon/getOpenIcon");
        check(r.getClosedIcon() == closed, "setClosedIcon/getClosedIcon");

        // Color set/get round-trips.
        Color ts = new Color(1, 2, 3);
        Color tns = new Color(4, 5, 6);
        Color bs = new Color(7, 8, 9);
        Color bns = new Color(10, 11, 12);
        Color border = new Color(13, 14, 15);
        r.setTextSelectionColor(ts);
        r.setTextNonSelectionColor(tns);
        r.setBackgroundSelectionColor(bs);
        r.setBackgroundNonSelectionColor(bns);
        r.setBorderSelectionColor(border);
        check(ts.equals(r.getTextSelectionColor()), "text selection color");
        check(tns.equals(r.getTextNonSelectionColor()), "text non-selection color");
        check(bs.equals(r.getBackgroundSelectionColor()), "background selection color");
        check(bns.equals(r.getBackgroundNonSelectionColor()), "background non-selection color");
        check(border.equals(r.getBorderSelectionColor()), "border selection color");

        // Font round-trip (JLabel-backed, overridden setFont/getFont).
        Font f = new Font(Font.DIALOG, Font.BOLD, 12);
        r.setFont(f);
        check(f.equals(r.getFont()), "setFont/getFont");

        // getTreeCellRendererComponent returns a Component (headless-safe JTree).
        JTree tree = new JTree();
        Component c = r.getTreeCellRendererComponent(tree, "node", true, true, false, 0, true);
        check(c != null, "renderer component non-null");
        check(c == r, "renderer returns itself");
        check(c instanceof javax.swing.JLabel, "renderer is a JLabel");
        check("node".equals(((javax.swing.JLabel) c).getText()), "renderer text is value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
