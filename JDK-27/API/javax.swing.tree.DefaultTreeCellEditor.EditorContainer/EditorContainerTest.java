/*
 * @test
 * @summary javax.swing.tree.DefaultTreeCellEditor.EditorContainer conformance: white-box structure + black-box behavior.
 * @run main EditorContainerTest
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

public class EditorContainerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeCellEditor$EditorContainer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTreeCellEditor editor = new DefaultTreeCellEditor(new JTree(), new DefaultTreeCellRenderer());
        DefaultTreeCellEditor.EditorContainer container = editor.new EditorContainer();

        check(container != null, "constructed");
        check(container instanceof java.awt.Container, "is a Container");
        check(container.getComponentCount() == 0, "starts with no children");
        check(container.getLayout() == null, "null layout");

        // getPreferredSize is 0x0 when no editing component is set.
        Dimension d = container.getPreferredSize();
        check(d != null && d.width == 0 && d.height == 0, "getPreferredSize (0,0) when idle");

        // doLayout is a no-op with no editing component.
        container.setSize(100, 30);
        container.doLayout();

        // paint is drawable on a headless BufferedImage graphics.
        BufferedImage img = new BufferedImage(120, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        try {
            container.paint(g);
            passed++;
        } finally {
            g.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
