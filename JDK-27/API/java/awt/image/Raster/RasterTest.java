/*
 * @test
 * @summary java.awt.image.Raster conformance: white-box structure + black-box behavior.
 * @run main RasterTest
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.lang.reflect.*;

public class RasterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.Raster", false, RasterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "createInterleavedRaster", int.class, int.class, int.class, int.class, Point.class), "createInterleavedRaster");
        check(hasMethod(c, "createBandedRaster", int.class, int.class, int.class, int.class, Point.class), "createBandedRaster");
        check(hasMethod(c, "createPackedRaster", int.class, int.class, int.class, int[].class, Point.class), "createPackedRaster");
        check(hasMethod(c, "getPixel", int.class, int.class, int[].class), "getPixel(int,int,int[])");
        check(hasMethod(c, "getPixels", int.class, int.class, int.class, int.class, int[].class), "getPixels");
        check(hasMethod(c, "getSample", int.class, int.class, int.class), "getSample");
        check(hasMethod(c, "getSamples", int.class, int.class, int.class, int.class, int.class, int[].class), "getSamples");
        check(hasMethod(c, "getDataBuffer"), "getDataBuffer()");
        check(hasMethod(c, "getSampleModel"), "getSampleModel()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getNumBands"), "getNumBands()");
        check(hasMethod(c, "getBounds"), "getBounds()");
        check(hasMethod(c, "createCompatibleWritableRaster", int.class, int.class), "createCompatibleWritableRaster");
        check(hasMethod(c, "createTranslatedChild", int.class, int.class), "createTranslatedChild");
        check(hasMethod(c, "createChild", int.class, int.class, int.class, int.class, int.class, int.class, int[].class), "createChild");
    }

    static void testBlackBox() {
        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 3, 3, null);
        check(wr.getWidth() == 4, "width 4");
        check(wr.getHeight() == 3, "height 3");
        check(wr.getNumBands() == 3, "3 bands");
        check(wr.getNumDataElements() == 3, "3 data elements");
        check(wr.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type byte");
        check(wr.getMinX() == 0 && wr.getMinY() == 0, "min x/y 0");
        check(wr.getBounds().equals(new Rectangle(0, 0, 4, 3)), "bounds (0,0,4,3)");

        wr.setSample(2, 1, 0, 200);
        wr.setSample(2, 1, 1, 100);
        wr.setSample(2, 1, 2, 50);
        check(wr.getSample(2, 1, 0) == 200, "getSample band 0");
        check(wr.getSample(2, 1, 1) == 100, "getSample band 1");
        check(wr.getSample(2, 1, 2) == 50, "getSample band 2");
        check(wr.getSample(0, 0, 0) == 0, "untouched sample 0");

        int[] px = wr.getPixel(2, 1, (int[]) null);
        check(px.length == 3 && px[0] == 200 && px[1] == 100 && px[2] == 50, "getPixel");
        int[] samples = wr.getSamples(2, 1, 1, 1, 0, (int[]) null);
        check(samples.length == 1 && samples[0] == 200, "getSamples band 0");

        DataBuffer db = wr.getDataBuffer();
        check(db != null && db.getSize() >= 12, "data buffer sized");
        SampleModel sm = wr.getSampleModel();
        check(sm.getWidth() == 4 && sm.getHeight() == 3 && sm.getNumBands() == 3, "sample model");

        WritableRaster comp = wr.createCompatibleWritableRaster();
        check(comp.getWidth() == 4 && comp.getHeight() == 3 && comp.getNumBands() == 3, "compatible raster");
        WritableRaster comp2 = wr.createCompatibleWritableRaster(2, 2);
        check(comp2.getWidth() == 2 && comp2.getHeight() == 2, "compatible raster(w,h)");
        WritableRaster comp3 = wr.createCompatibleWritableRaster(new Rectangle(1, 1, 2, 2));
        check(comp3.getWidth() == 2 && comp3.getHeight() == 2, "compatible raster(rect)");

        Raster child = wr.createTranslatedChild(1, 2);
        check(child.getMinX() == 1 && child.getMinY() == 2, "translated child min");
        check(child.getParent() == wr, "translated child parent");

        Raster sub = wr.createChild(1, 1, 2, 2, 0, 0, null);
        check(sub.getWidth() == 2 && sub.getHeight() == 2, "child dims");
        check(sub.getMinX() == 0 && sub.getMinY() == 0, "child min");
        check(sub.getSample(1, 0, 0) == 200, "child sample reflects parent");

        WritableRaster banded = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, 3, 2, 2, null);
        check(banded.getNumBands() == 2, "banded 2 bands");
        WritableRaster packed = Raster.createPackedRaster(DataBuffer.TYPE_INT, 4, 4, new int[] { 0xFF0000, 0xFF00, 0xFF }, null);
        check(packed.getNumBands() == 3, "packed 3 bands");
        check(packed.getNumDataElements() == 1, "packed 1 data element");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
