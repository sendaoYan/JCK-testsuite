/*
 * @test
 * @summary java.awt.image.WritableRenderedImage conformance: white-box structure plus black-box behavior.
 * @run main WritableRenderedImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;

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
        passed += WritableRenderedImageDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class WritableRenderedImageDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.WritableRenderedImage");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.RenderedImage"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addTileObserver"), "method addTileObserver");
        check(hasMethod(c, "removeTileObserver"), "method removeTileObserver");
        check(hasMethod(c, "getWritableTile"), "method getWritableTile");
        check(hasMethod(c, "releaseWritableTile"), "method releaseWritableTile");
        check(hasMethod(c, "isTileWritable"), "method isTileWritable");
        check(hasMethod(c, "getWritableTileIndices"), "method getWritableTileIndices");
        check(hasMethod(c, "hasTileWriters"), "method hasTileWriters");
        check(hasMethod(c, "setData"), "method setData");
        // BufferedImage implements WritableRenderedImage.
        check(WritableRenderedImage.class.isAssignableFrom(BufferedImage.class),
              "BufferedImage is a WritableRenderedImage");
    }

    static void testWritableTiles() {
        WritableRenderedImage wi = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        BufferedImage bi = (BufferedImage) wi;
        bi.setRGB(2, 1, 0xFF102030);
        // Checking out the single writable tile yields the live raster.
        WritableRaster wt = wi.getWritableTile(0, 0);
        check(wt != null, "getWritableTile returns raster");
        check(wi.isTileWritable(0, 0), "checked-out tile is writable");
        check(wi.hasTileWriters(), "hasTileWriters true while checked out");
        Point[] idx = wi.getWritableTileIndices();
        check(idx != null && idx.length >= 1 && idx[0].x == 0 && idx[0].y == 0,
              "writable tile indices list (0,0)");
        // Writing through the writable tile modifies the image.
        wt.setPixel(0, 0, new int[] { 5, 10, 15 });
        check(bi.getRGB(0, 0) == 0xFF050A0F, "write through writable tile visible");
        // Releasing the checkout is legal and repeatable; BufferedImage treats its
        // single tile as always writable, so isTileWritable stays true.
        wi.releaseWritableTile(0, 0);
        check(wi.isTileWritable(0, 0), "BufferedImage tile always writable");
        wi.releaseWritableTile(0, 0);
        check(true, "double release tolerated");
        // BufferedImage ignores the tile indices of its single tile.
        check(wi.getWritableTile(0, 0) == wt, "repeat checkout returns the same raster");
    }

    static void testSetData() {
        WritableRenderedImage wi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        BufferedImage bi = (BufferedImage) wi;
        // setData copies a source raster's samples into the image.
        WritableRaster src = Raster.createWritableRaster(bi.getSampleModel(), new Point(0, 0));
        src.setPixel(0, 0, new int[] { 1, 2, 3 });
        src.setPixel(1, 1, new int[] { 10, 11, 12 });
        wi.setData(src);
        check(bi.getRGB(0, 0) == 0xFF010203, "setData copies first pixel");
        check(bi.getRGB(1, 1) == 0xFF0A0B0C, "setData copies last pixel");
        check(bi.getRGB(1, 0) == 0xFF000000, "untouched pixels remain zero");
    }

    static void testTileObserver() {
        WritableRenderedImage wi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        TileObserver obs = new TileObserver() {
            public void tileUpdate(WritableRenderedImage src, int tx, int ty, boolean writable) { }
        };
        wi.addTileObserver(obs);
        check(true, "addTileObserver accepted");
        wi.removeTileObserver(obs);
        check(true, "removeTileObserver accepted");
        wi.removeTileObserver(obs);   // removing an unregistered observer is tolerated
        check(true, "removeTileObserver of unknown observer tolerated");
    }



    static int run() throws Exception {
        testWhiteBox();
        testWritableTiles();
        testSetData();
        testTileObserver();
        return passed;
    }
}
