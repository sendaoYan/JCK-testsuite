/*
 * @test
 * @summary javax.swing.CellRendererPane conformance: white-box structure + black-box behavior.
 * @run main CellRendererPaneTest
 */

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.CellRendererPane;
import javax.swing.JLabel;

public class CellRendererPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.CellRendererPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CellRendererPane pane = new CellRendererPane();
        JLabel label = new JLabel("cell");
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setClip(0, 0, 16, 16);

        // paint a renderer component headlessly into an offscreen buffer
        pane.paintComponent(g, label, null, 0, 0, 16, 16, false);
        pane.paintComponent(g, label, null, 0, 0, 16, 16);
        pane.paintComponent(g, label, null, new java.awt.Rectangle(0, 0, 16, 16));
        pane.paint(g);
        pane.update(g);
        g.dispose();

        // invalidate is a no-op that must not throw; accessible context exists
        pane.invalidate();
        check(pane.getAccessibleContext() != null, "accessible context");
        check(true, "paint smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
