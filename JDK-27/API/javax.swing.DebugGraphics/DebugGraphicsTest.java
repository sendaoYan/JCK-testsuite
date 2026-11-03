/*
 * @test
 * @summary javax.swing.DebugGraphics conformance: white-box structure + black-box behavior.
 * @run main DebugGraphicsTest
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.DebugGraphics;

public class DebugGraphicsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DebugGraphics");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Graphics"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        DebugGraphics dg = new DebugGraphics(g2);

        // color/round-trip + drawing smoke
        dg.setColor(Color.RED);
        check(Color.RED.equals(dg.getColor()), "setColor/getColor");
        dg.fillRect(0, 0, 8, 8);
        dg.drawRect(0, 0, 31, 31);
        dg.drawLine(0, 0, 10, 10);
        dg.drawOval(2, 2, 10, 10);
        dg.drawString("x", 4, 16);
        check(true, "drawing smoke");

        // debug options
        dg.setDebugOptions(DebugGraphics.LOG_OPTION | DebugGraphics.BUFFERED_OPTION);
        check((dg.getDebugOptions() & DebugGraphics.LOG_OPTION) != 0, "log option set");
        check((dg.getDebugOptions() & DebugGraphics.BUFFERED_OPTION) != 0, "buffered option set");

        // flash settings
        DebugGraphics.setFlashColor(Color.BLUE);
        check(Color.BLUE.equals(DebugGraphics.flashColor()), "flashColor");
        DebugGraphics.setFlashTime(123);
        check(DebugGraphics.flashTime() == 123, "flashTime");
        DebugGraphics.setFlashCount(7);
        check(DebugGraphics.flashCount() == 7, "flashCount");

        // create() returns a new graphics context
        Graphics child = dg.create();
        check(child != null, "create");
        child.dispose();
        dg.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
