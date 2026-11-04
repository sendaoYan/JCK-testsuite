/*
 * @test
 * @summary java.awt.image.VolatileImage conformance: white-box structure plus black-box behavior.
 * @run main VolatileImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;

public class VolatileImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.VolatileImage");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        check(Image.class.isAssignableFrom(c), "extends Image");
        check(Transparency.class.isAssignableFrom(c), "implements Transparency");
        check(c.getField("IMAGE_OK").getInt(null) != c.getField("IMAGE_RESTORED").getInt(null), "distinct status constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(VolatileImage.IMAGE_OK == 0 && VolatileImage.IMAGE_RESTORED == 1 && VolatileImage.IMAGE_INCOMPATIBLE == 2,
              "status constant values 0/1/2");

        VolatileImage vi = new VolatileImage() {
            public BufferedImage getSnapshot() { return new BufferedImage(4, 5, BufferedImage.TYPE_INT_RGB); }
            public int getWidth() { return 4; }
            public int getHeight() { return 5; }
            public int getWidth(ImageObserver observer) { return 4; }
            public int getHeight(ImageObserver observer) { return 5; }
            public Graphics2D createGraphics() { return null; }
            public int validate(GraphicsConfiguration gc) { return IMAGE_OK; }
            public boolean contentsLost() { return false; }
            public ImageCapabilities getCapabilities() { return new ImageCapabilities(false); }
            public Object getProperty(String name, ImageObserver observer) { return Image.UndefinedProperty; }
        };
        check(vi.getWidth() == 4 && vi.getHeight() == 5, "getWidth/getHeight");
        check(vi.getSnapshot().getWidth() == 4 && vi.getSnapshot().getHeight() == 5, "getSnapshot");
        check(!vi.contentsLost(), "contentsLost false");
        check(vi.validate(null) == VolatileImage.IMAGE_OK, "validate IMAGE_OK");
        check(vi.getCapabilities() != null, "getCapabilities non-null");
        check(vi.getTransparency() == Transparency.TRANSLUCENT, "TRANSLUCENT default transparency");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
