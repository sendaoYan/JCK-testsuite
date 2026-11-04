/*
 * @test
 * @summary java.awt.image.WritableRaster conformance: white-box structure + black-box behavior.
 * @run main WritableRasterTest
 */

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;

public class WritableRasterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.WritableRaster", false, WritableRasterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == Raster.class, "extends Raster");
        check(hasMethod(c, "setPixel", int.class, int.class, int[].class), "setPixel(int,int,int[])");
        check(hasMethod(c, "setPixel", int.class, int.class, float[].class), "setPixel(int,int,float[])");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, int[].class), "setPixels");
        check(hasMethod(c, "setSample", int.class, int.class, int.class, int.class), "setSample(int,int,int,int)");
        check(hasMethod(c, "setSamples", int.class, int.class, int.class, int.class, int.class, int[].class), "setSamples");
        check(hasMethod(c, "setDataElements", int.class, int.class, Object.class), "setDataElements");
        check(hasMethod(c, "setRect", Raster.class), "setRect(Raster)");
        check(hasMethod(c, "setRect", int.class, int.class, Raster.class), "setRect(int,int,Raster)");
        check(hasMethod(c, "getWritableParent"), "getWritableParent()");
        check(hasMethod(c, "createWritableTranslatedChild", int.class, int.class), "createWritableTranslatedChild");
        check(hasMethod(c, "createWritableChild", int.class, int.class, int.class, int.class, int.class, int.class, int[].class), "createWritableChild");
    }

    static void testBlackBox() {
        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 4, 3, null);
        check(wr.getWidth() == 4 && wr.getHeight() == 4, "dims");
        check(wr.getNumBands() == 3, "3 bands");

        wr.setPixel(1, 2, new int[] { 10, 20, 30 });
        int[] px = wr.getPixel(1, 2, (int[]) null);
        check(px[0] == 10 && px[1] == 20 && px[2] == 30, "setPixel/getPixel round-trip");

        wr.setSample(3, 3, 1, 77);
        check(wr.getSample(3, 3, 1) == 77, "setSample/getSample");

        wr.setPixels(0, 0, 2, 1, new int[] { 1, 2, 3, 4, 5, 6 });
        check(wr.getSample(0, 0, 0) == 1 && wr.getSample(1, 0, 2) == 6, "setPixels fills region");

        wr.setSamples(0, 3, 2, 1, 2, new int[] { 11, 22 });
        check(wr.getSample(0, 3, 2) == 11 && wr.getSample(1, 3, 2) == 22, "setSamples");

        // setDataElements with a byte[] packs bands in order
        byte[] de = new byte[] { 9, 8, 7 };
        wr.setDataElements(2, 2, de);
        check(wr.getSample(2, 2, 0) == 9 && wr.getSample(2, 2, 1) == 8 && wr.getSample(2, 2, 2) == 7, "setDataElements");

        // setRect copies a region from another raster
        WritableRaster src = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 2, 2, 3, null);
        src.setPixel(0, 0, new int[] { 40, 41, 42 });
        src.setPixel(1, 0, new int[] { 43, 44, 45 });
        src.setPixel(0, 1, new int[] { 46, 47, 48 });
        src.setPixel(1, 1, new int[] { 49, 50, 51 });
        wr.setRect(0, 0, src);
        check(wr.getSample(0, 0, 0) == 40 && wr.getSample(1, 1, 2) == 51, "setRect copies region");

        WritableRaster parent = wr.getWritableParent();
        check(parent == null, "writable parent null for root raster");
        WritableRaster child = wr.createWritableTranslatedChild(1, 1);
        check(child.getMinX() == 1 && child.getMinY() == 1, "writable translated child");
        child.setSample(1, 1, 0, 123);
        check(wr.getSample(0, 0, 0) == 123, "writable child writes to parent");

        WritableRaster wchild = wr.createWritableChild(1, 1, 2, 2, 0, 0, null);
        check(wchild.getWidth() == 2 && wchild.getHeight() == 2, "writable child dims");
        wchild.setSample(0, 0, 1, 250);
        check(wr.getSample(1, 1, 1) == 250, "writable child shares data");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
