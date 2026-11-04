/*
 * @test
 * @summary java.awt.image.PixelInterleavedSampleModel conformance: white-box structure plus black-box behavior.
 * @run main PixelInterleavedSampleModelTest
 */

import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

public class PixelInterleavedSampleModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.PixelInterleavedSampleModel");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(ComponentSampleModel.class.isAssignableFrom(c), "extends ComponentSampleModel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // BGR order (band offsets 2/1/0), 3 bands, pixelStride 3, scanlineStride 12
        PixelInterleavedSampleModel pism = new PixelInterleavedSampleModel(
            DataBuffer.TYPE_BYTE, 4, 4, 3, 12, new int[]{2, 1, 0});
        check(pism.getPixelStride() == 3 && pism.getScanlineStride() == 12, "strides");
        int[] offsets = pism.getBandOffsets();
        check(offsets[0] == 2 && offsets[1] == 1 && offsets[2] == 0, "BGR band offsets");
        check(pism.getNumDataElements() == 3, "numDataElements 3");
        check(pism.getSampleSize(0) == 8, "sample size 8");

        DataBuffer db = pism.createDataBuffer();
        check(db.getSize() == 48, "data buffer size 48");
        pism.setSample(1, 2, 2, 123, db);
        check(pism.getSample(1, 2, 2, db) == 123, "get/setSample roundtrip");
        pism.setPixel(0, 0, new int[]{7, 8, 9}, db);
        int[] px = pism.getPixel(0, 0, (int[]) null, db);
        check(px[0] == 7 && px[1] == 8 && px[2] == 9, "get/setPixel roundtrip");

        SampleModel compat = pism.createCompatibleSampleModel(2, 2);
        check(compat instanceof PixelInterleavedSampleModel, "createCompatibleSampleModel type");
        check(pism.createSubsetSampleModel(new int[]{0, 1}) != null, "createSubsetSampleModel");

        PixelInterleavedSampleModel same = new PixelInterleavedSampleModel(
            DataBuffer.TYPE_BYTE, 4, 4, 3, 12, new int[]{2, 1, 0});
        check(pism.equals(same) && pism.hashCode() == same.hashCode(), "equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += PixelInterleavedSampleModelDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class PixelInterleavedSampleModelDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.PixelInterleavedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ComponentSampleModel"), "superclass java.awt.image.ComponentSampleModel");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
    }

    static void testLayout() {
        PixelInterleavedSampleModel m = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_BYTE, 6, 4, 3, 18, new int[] { 2, 1, 0 });
        check(m.getWidth() == 6 && m.getHeight() == 4, "dims 6x4");
        check(m.getNumBands() == 3, "3 bands");
        check(m.getPixelStride() == 3, "pixel stride 3");
        check(m.getScanlineStride() == 18, "scanline stride 18");
        check(m.getNumDataElements() == 3, "3 data elements per pixel");
        int[] off = m.getBandOffsets();
        check(off.length == 3 && off[0] == 2 && off[1] == 1 && off[2] == 0, "BGR band order kept");
        check(java.util.Arrays.equals(m.getBankIndices(), new int[] { 0, 0, 0 }),
              "single bank for all bands");
        DataBuffer db = m.createDataBuffer();
        check(db.getNumBanks() == 1, "single bank allocated");
        check(db.getSize() >= 18 * 4, "buffer sized for all rows");
    }

    static void testSamples() {
        PixelInterleavedSampleModel m = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_BYTE, 3, 2, 3, 9, new int[] { 0, 1, 2 });
        DataBuffer db = m.createDataBuffer();
        m.setPixel(1, 1, new int[] { 11, 22, 33 }, db);
        check(m.getPixel(1, 1, (int[]) null, db)[0] == 11
              && m.getPixel(1, 1, (int[]) null, db)[1] == 22
              && m.getPixel(1, 1, (int[]) null, db)[2] == 33,
              "pixel round trip through all bands");
        // Element position: row 1, column 1, band 2 -> 9 + 3 + 2.
        check(db.getElem(9 + 3 + 2) == 33, "band 2 stored at scanline + pixel + offset 2");
        // getDataElements hands back the raw interleaved bytes.
        Object elems = m.getDataElements(1, 1, null, db);
        check(elems instanceof byte[] && ((byte[]) elems).length == 3, "3 byte data elements");
        check(((byte[]) elems)[0] == 11, "data element band 0");
    }

    static void testSubset() {
        PixelInterleavedSampleModel m = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_INT, 4, 4, 3, 12, new int[] { 0, 1, 2 });
        SampleModel sub = m.createSubsetSampleModel(new int[] { 1 });
        check(sub instanceof PixelInterleavedSampleModel, "subset type preserved");
        check(sub.getNumBands() == 1, "subset band count 1");
        check(((PixelInterleavedSampleModel) sub).getPixelStride() == 3,
              "subset keeps the interleaved pixel stride");
    }

    static void testCtorErrors() {
        boolean threw = false;
        try { new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 2, 2, 3, 0,
                new int[] { 0, 1, 2 }); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero scanline stride -> IAE");
        threw = false;
        try { new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 2, 2, 3, 6, null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "null band offsets -> NPE");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.PixelInterleavedSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "hashCode"), "method hashCode");
    }



    static int run() throws Exception {
        testWhiteBox();
        testLayout();
        testSamples();
        testSubset();
        testCtorErrors();
        testOriginalSurface();
        return passed;
    }
}
