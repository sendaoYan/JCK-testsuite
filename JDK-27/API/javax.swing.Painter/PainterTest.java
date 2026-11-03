/*
 * @test
 * @summary javax.swing.Painter conformance: white-box structure + black-box behavior.
 * @run main PainterTest
 */

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.Painter;

public class PainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Painter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final int[] calls = { 0 };
        Painter<String> painter = new Painter<String>() {
            public void paint(Graphics2D g, String object, int width, int height) {
                calls[0]++;
            }
        };
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        painter.paint(img.createGraphics(), "obj", 10, 10);
        check(calls[0] == 1, "paint invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
