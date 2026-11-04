/*
 * @test
 * @summary javax.swing.ScrollPaneLayout conformance: white-box structure + black-box behavior.
 * @run main ScrollPaneLayoutTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;

public class ScrollPaneLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ScrollPaneLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.ScrollPaneConstants"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ScrollPaneLayout layout = new ScrollPaneLayout();
        layout.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        check(layout.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, "vsb policy");
        layout.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        check(layout.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, "hsb policy");

        JScrollPane sp = new JScrollPane();
        layout.syncWithScrollPane(sp);
        check(layout.getViewport() != null, "getViewport");
        check(layout.getHorizontalScrollBar() != null, "getHorizontalScrollBar");
        check(layout.getVerticalScrollBar() != null, "getVerticalScrollBar");
        check(layout.getRowHeader() == null, "row header null");
        check(layout.getColumnHeader() == null, "column header null");
        check(layout.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == null, "corner null");

        JScrollPane sp2 = new JScrollPane();
        layout.layoutContainer(sp2);
        check(layout.preferredLayoutSize(sp2) != null, "preferredLayoutSize");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
