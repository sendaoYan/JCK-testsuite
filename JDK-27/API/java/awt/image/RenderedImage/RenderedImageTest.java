/*
 * @test
 * @summary java.awt.image.RenderedImage conformance: white-box structure plus black-box behavior.
 * @run main RenderedImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;

public class RenderedImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethodNamed(Class<?> c, String n) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RenderedImage");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        for (String s : new String[]{"getSources","getColorModel","getSampleModel","getWidth","getHeight",
                "getMinX","getMinY","getTile","getData","copyData"}) {
            check(hasMethodNamed(c, s), "method " + s);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // BufferedImage implements RenderedImage
        BufferedImage img = new BufferedImage(6, 4, BufferedImage.TYPE_INT_ARGB);
        RenderedImage ri = img;
        check(ri.getWidth() == 6 && ri.getHeight() == 4, "getWidth/getHeight");
        check(ri.getMinX() == 0 && ri.getMinY() == 0, "min x/y origin");
        check(ri.getColorModel() != null, "getColorModel non-null");
        check(ri.getSampleModel() != null, "getSampleModel non-null");
        check(ri.getSources() == null, "getSources null (no sources)");
        check(ri.getNumXTiles() >= 1 && ri.getNumYTiles() >= 1, "tile counts positive");
        check(ri.getMinTileX() == 0 && ri.getMinTileY() == 0, "min tile 0");
        check(ri.getTileWidth() > 0 && ri.getTileHeight() > 0, "tile dims positive");
        Raster tile = ri.getTile(ri.getMinTileX(), ri.getMinTileY());
        check(tile != null && tile.getWidth() == ri.getTileWidth(), "getTile");
        check(ri.getData() != null && ri.getData().getWidth() == 6, "getData");
        check(ri.getData(new Rectangle(0, 0, 3, 2)).getWidth() == 3, "getData(Rectangle)");
        WritableRaster copy = ri.copyData(null);
        check(copy != null && copy.getWidth() == 6 && copy.getHeight() == 4, "copyData");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
