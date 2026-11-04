/*
 * @test
 * @summary java.awt.Canvas conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CanvasTest
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class CanvasTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Canvas");
        check(c != null, "type loads: java.awt.Canvas");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"paint", "update", "createBufferStrategy", "getBufferStrategy"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Canvas c = new Canvas();
        check(c.getBufferStrategy() == null, "buffer strategy null before peer");

        // paint/update should run against an in-memory graphics without throwing
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        c.setSize(50, 50);
        c.paint(g);
        c.update(g);
        g.dispose();

        Frame f = new Frame("CanvasTest");
        f.setLayout(null);
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            f.add(c);
            check(c.isDisplayable(), "displayable after add");
            check(c.isShowing(), "showing after add");
            c.setBounds(0, 0, 100, 80);
            check(c.getBounds().equals(new Rectangle(0, 0, 100, 80)), "setBounds/getBounds");
            c.setBackground(Color.CYAN);
            check(Color.CYAN.equals(c.getBackground()), "setBackground/getBackground");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
