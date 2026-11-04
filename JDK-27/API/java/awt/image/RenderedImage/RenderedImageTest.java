/*
 * @test
 * @summary java.awt.image.RenderedImage conformance: white-box structure plus black-box behavior.
 * @run main RenderedImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

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
        passed += RenderedImageDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class RenderedImageDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RenderedImage");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSources"), "method getSources");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getPropertyNames"), "method getPropertyNames");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getSampleModel"), "method getSampleModel");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "getNumXTiles"), "method getNumXTiles");
        check(hasMethod(c, "getNumYTiles"), "method getNumYTiles");
        check(hasMethod(c, "getMinTileX"), "method getMinTileX");
        check(hasMethod(c, "getMinTileY"), "method getMinTileY");
        check(hasMethod(c, "getTileWidth"), "method getTileWidth");
        check(hasMethod(c, "getTileHeight"), "method getTileHeight");
        check(hasMethod(c, "getTileGridXOffset"), "method getTileGridXOffset");
        check(hasMethod(c, "getTileGridYOffset"), "method getTileGridYOffset");
        check(hasMethod(c, "getTile"), "method getTile");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "copyData"), "method copyData");
        // BufferedImage implements RenderedImage (through WritableRenderedImage).
        check(RenderedImage.class.isAssignableFrom(BufferedImage.class),
              "BufferedImage is a RenderedImage");
    }

    static void testGeometry() {
        RenderedImage ri = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        check(ri.getWidth() == 4 && ri.getHeight() == 3, "width/height from constructor");
        check(ri.getMinX() == 0 && ri.getMinY() == 0, "origin at (0,0)");
        // A BufferedImage is a single-tile image the size of the whole image.
        check(ri.getNumXTiles() == 1 && ri.getNumYTiles() == 1, "single tile");
        check(ri.getMinTileX() == 0 && ri.getMinTileY() == 0, "tile indices start at 0");
        check(ri.getTileWidth() == 4 && ri.getTileHeight() == 3, "tile covers whole image");
        check(ri.getTileGridXOffset() == 0 && ri.getTileGridYOffset() == 0, "tile grid origin 0");
        check(ri.getColorModel() != null && ri.getSampleModel() != null,
              "color model and sample model available");
        check(ri.getSampleModel().getWidth() == 4 && ri.getSampleModel().getHeight() == 3,
              "sample model matches image dims");
    }

    static void testProperties() {
        RenderedImage ri = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        check(ri.getProperty("no-such-key") == Image.UndefinedProperty,
              "unknown property -> Image.UndefinedProperty");
        check(ri.getPropertyNames() == null, "no properties -> getPropertyNames null");
        check(ri.getSources() == null, "direct image has no sources");
    }

    static void testTilesAndData() {
        BufferedImage bi = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        RenderedImage ri = bi;
        bi.setRGB(1, 1, 0xFF336699);
        // getTile(0,0) exposes the live raster: writes through it are visible.
        Raster tile = ri.getTile(0, 0);
        check(tile.getWidth() == 4 && tile.getHeight() == 3, "tile raster covers image");
        check(tile.getSample(1, 1, 0) == 0x33, "tile reads image pixels");
        // Tile indices outside [minTileX, minTileX+numXTiles) -> AIOOBE.
        boolean threw = false;
        try { ri.getTile(1, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getTile out of range -> AIOOBE");
        threw = false;
        try { ri.getTile(0, -1); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "negative tile index -> AIOOBE");
        // getData() returns a snapshot copy.
        Raster snap = ri.getData();
        check(snap.getWidth() == 4 && snap.getHeight() == 3, "getData covers image");
        check(snap.getSample(1, 1, 0) == 0x33, "getData reads pixels");
        // copyData(null) allocates; copyData(raster) fills and returns the argument.
        WritableRaster into = Raster.createWritableRaster(bi.getSampleModel(), new Point(0, 0));
        Raster ret = ri.copyData(into);
        check(ret == into, "copyData returns the provided raster");
        check(into.getPixel(1, 1, (int[]) null)[0] == 0x33
              && into.getPixel(1, 1, (int[]) null)[1] == 0x66
              && into.getPixel(1, 1, (int[]) null)[2] == 0x99,
              "copyData copies exact band values");
        check(ri.copyData(null) != null, "copyData(null) allocates a destination");
    }



    static int run() throws Exception {
        testWhiteBox();
        testGeometry();
        testProperties();
        testTilesAndData();
        return passed;
    }
}
