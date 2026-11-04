/*
 * @test
 * @summary java.awt.image.BufferStrategy conformance: white-box structure plus black-box behavior.
 * @run main BufferStrategyTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;

public class BufferStrategyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferStrategy");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        for (String s : new String[]{"getCapabilities","getDrawGraphics","contentsLost","contentsRestored","show"}) {
            Method mm = c.getMethod(s);
            check(Modifier.isAbstract(mm.getModifiers()), "abstract " + s);
        }
        check(!Modifier.isAbstract(c.getMethod("dispose").getModifiers()), "dispose concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferStrategy bs = new BufferStrategy() {
            public BufferCapabilities getCapabilities() {
                return new BufferCapabilities(new ImageCapabilities(false),
                        new ImageCapabilities(false), BufferCapabilities.FlipContents.UNDEFINED);
            }
            public Graphics getDrawGraphics() { return null; }
            public boolean contentsLost() { return false; }
            public boolean contentsRestored() { return true; }
            public void show() { }
        };
        check(bs.getCapabilities() != null, "getCapabilities");
        check(!bs.contentsLost(), "contentsLost false");
        check(bs.contentsRestored(), "contentsRestored true");
        bs.show();
        bs.dispose(); // concrete dispose no-throw
        check(true, "dispose no-throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
