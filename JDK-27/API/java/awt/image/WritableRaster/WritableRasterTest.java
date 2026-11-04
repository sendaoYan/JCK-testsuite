/*
 * @test
 * @summary java.awt.image.WritableRaster conformance: white-box structure + black-box behavior.
 * @run main WritableRasterTest
 */

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;
import java.awt.Point;

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
        passed += WritableRasterDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class WritableRasterDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.WritableRaster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.Raster"), "superclass java.awt.image.Raster");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWritableParent"), "method getWritableParent");
        check(hasMethod(c, "createWritableTranslatedChild"), "method createWritableTranslatedChild");
        check(hasMethod(c, "createWritableChild"), "method createWritableChild");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setRect"), "method setRect");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
    }

    static WritableRaster mk() {
        return Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 4, 3, new Point(0, 0));
    }

    static void testSetters() {
        WritableRaster wr = mk();
        wr.setSample(0, 0, 0, 11);
        check(wr.getSample(0, 0, 0) == 11, "setSample(int) round-trip");
        wr.setSample(1, 0, 0, 3.9f);
        check(wr.getSample(1, 0, 0) == 3, "setSample(float) truncates");
        wr.setSample(2, 0, 0, 5.9);
        check(wr.getSample(2, 0, 0) == 5, "setSample(double) truncates");

        wr.setPixel(0, 1, new int[] { 1, 2, 3 });
        check(java.util.Arrays.equals(wr.getPixel(0, 1, (int[]) null), new int[] { 1, 2, 3 }),
              "setPixel(int[]) round-trip");
        wr.setPixel(1, 1, new float[] { 4.9f, 5.9f, 6.9f });
        check(java.util.Arrays.equals(wr.getPixel(1, 1, (int[]) null), new int[] { 4, 5, 6 }),
              "setPixel(float[]) truncates");
        wr.setPixel(2, 1, new double[] { 7.9, 8.9, 9.9 });
        check(java.util.Arrays.equals(wr.getPixel(2, 1, (int[]) null), new int[] { 7, 8, 9 }),
              "setPixel(double[]) truncates");

        wr.setPixels(0, 2, 2, 1, new int[] { 10, 11, 12, 13, 14, 15 });
        check(java.util.Arrays.equals(wr.getPixel(0, 2, (int[]) null), new int[] { 10, 11, 12 }),
              "setPixels writes rows");

        wr.setSamples(0, 3, 4, 1, 1, new int[] { 21, 22, 23, 24 });
        check(wr.getSample(0, 3, 1) == 21 && wr.getSample(3, 3, 1) == 24, "setSamples writes band");

        // Out-of-bounds setters.
        boolean threw = false;
        try { wr.setSample(9, 0, 0, 1); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setSample x OOB -> AIOOBE");
        threw = false;
        try { wr.setPixel(0, 9, new int[] { 1, 1, 1 }); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setPixel y OOB -> AIOOBE");
        threw = false;
        try { wr.setPixel(0, 0, new int[] { 1, 1 }); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setPixel short array -> AIOOBE");
    }

    static void testDataElementsAndRect() {
        WritableRaster src = mk();
        src.setPixel(1, 1, new int[] { 40, 50, 60 });
        WritableRaster dst = mk();
        // setDataElements(x,y,Raster) copies the source raster's rect starting at its origin.
        dst.setDataElements(0, 0, src);
        check(java.util.Arrays.equals(dst.getPixel(1, 1, (int[]) null), new int[] { 40, 50, 60 }),
              "setDataElements(Raster) copies pixels from source origin");
        check(java.util.Arrays.equals(dst.getPixel(0, 0, (int[]) null), new int[] { 0, 0, 0 }),
              "setDataElements(Raster) leaves untouched pixels zero");
        Object e = src.getDataElements(1, 1, (Object) null);
        dst.setDataElements(2, 2, e);
        check(java.util.Arrays.equals(dst.getPixel(2, 2, (int[]) null), new int[] { 40, 50, 60 }),
              "setDataElements(Object) copies pixel");
        // Region variant.
        dst.setDataElements(0, 3, 2, 1, src.getDataElements(1, 1, 2, 1, (Object) null));
        check(java.util.Arrays.equals(dst.getPixel(0, 3, (int[]) null), new int[] { 40, 50, 60 }),
              "setDataElements region copies");

        // setRect copies whole raster contents.
        WritableRaster d2 = mk();
        d2.setRect(src);
        check(java.util.Arrays.equals(d2.getPixel(1, 1, (int[]) null), new int[] { 40, 50, 60 }),
              "setRect(Raster) copies");
        // setRect with translation.
        WritableRaster d3 = mk();
        d3.setRect(1, 0, src);
        check(java.util.Arrays.equals(d3.getPixel(2, 1, (int[]) null), new int[] { 40, 50, 60 }),
              "setRect(dx,dy,Raster) offsets");
    }

    static void testChildren() {
        WritableRaster wr = mk();
        check(wr.getWritableParent() == null, "root has no writable parent");
        check(wr.getParent() == null, "root has no parent");
        WritableRaster t = wr.createWritableTranslatedChild(5, 6);
        check(t.getMinX() == 5 && t.getMinY() == 6, "writable translated child corner");
        check(t.getWritableParent() == wr, "writable translated child parent");
        // Writes through the child are visible in the parent (shared data).
        t.setSample(5, 6, 0, 77);
        check(wr.getSample(0, 0, 0) == 77, "child writes propagate to parent");
        WritableRaster ch = wr.createWritableChild(1, 1, 2, 2, 0, 0, null);
        check(ch.getWidth() == 2 && ch.getHeight() == 2, "writable child size");
        check(ch.getMinX() == 0 && ch.getMinY() == 0, "writable child translated");
        check(ch.getWritableParent() == wr, "writable child parent");
        ch.setSample(0, 0, 1, 88);
        check(wr.getSample(1, 1, 1) == 88, "writable child writes map into parent");
        boolean threw = false;
        try { wr.createWritableChild(3, 3, 5, 5, 0, 0, null); }
        catch (java.awt.image.RasterFormatException e) { threw = true; }
        check(threw, "createWritableChild overflow -> RasterFormatException");
    }



    static int run() throws Exception {
        testWhiteBox();
        testSetters();
        testDataElementsAndRect();
        testChildren();
        return passed;
    }
}
