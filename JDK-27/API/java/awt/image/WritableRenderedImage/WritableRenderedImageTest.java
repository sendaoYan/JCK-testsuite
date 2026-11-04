/*
 * @test
 * @summary java.awt.image.WritableRenderedImage conformance: white-box structure plus black-box behavior.
 * @run main WritableRenderedImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;

public class WritableRenderedImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethodNamed(Class<?> c, String n) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.WritableRenderedImage");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(RenderedImage.class.isAssignableFrom(c), "extends RenderedImage");
        for (String s : new String[]{"addTileObserver","removeTileObserver","getWritableTile","releaseWritableTile",
                "isTileWritable","getWritableTileIndices","hasTileWriters","setData"}) {
            check(hasMethodNamed(c, s), "method " + s);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // BufferedImage implements WritableRenderedImage
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        WritableRenderedImage wri = img;

        WritableRaster tile = wri.getWritableTile(0, 0);
        check(tile != null && tile.getWidth() == 4, "getWritableTile");
        check(wri.isTileWritable(0, 0), "isTileWritable true");
        check(wri.hasTileWriters(), "hasTileWriters");
        Point[] indices = wri.getWritableTileIndices();
        check(indices != null && indices.length >= 1, "getWritableTileIndices");
        wri.releaseWritableTile(0, 0);
        check(wri.hasTileWriters(), "BufferedImage hasTileWriters always true");

        Raster src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB).getRaster();
        wri.setData(src);
        check(wri.getData().getSample(0, 0, 0) == src.getSample(0, 0, 0), "setData updates image");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
