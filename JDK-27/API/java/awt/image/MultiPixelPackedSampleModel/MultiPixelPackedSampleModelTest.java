/*
 * @test
 * @summary java.awt.image.MultiPixelPackedSampleModel conformance: white-box structure plus black-box behavior.
 * @run main MultiPixelPackedSampleModelTest
 */

import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;

public class MultiPixelPackedSampleModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.MultiPixelPackedSampleModel");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(SampleModel.class.isAssignableFrom(c), "extends SampleModel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 8 pixels wide, 1 bit per pixel
        MultiPixelPackedSampleModel mppsm = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, 8, 1, 1);
        check(mppsm.getNumDataElements() == 1, "numDataElements 1");
        check(mppsm.getSampleSize(0) == 1, "sample size 1 bit");
        check(mppsm.getSampleSize()[0] == 1, "getSampleSize array");
        check(mppsm.getPixelBitStride() == 1, "pixel bit stride 1");
        check(mppsm.getDataBitOffset() == 0, "data bit offset 0");
        check(mppsm.getBitOffset(0) == 0, "bit offset 0");
        check(mppsm.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type BYTE");
        check(mppsm.getScanlineStride() == 1, "scanline stride 1 byte");

        DataBuffer db = mppsm.createDataBuffer();
        check(db.getSize() == 1, "data buffer size 1");
        mppsm.setSample(0, 0, 0, 1, db);
        mppsm.setSample(3, 0, 0, 1, db);
        check(mppsm.getSample(0, 0, 0, db) == 1, "set/get sample bit 0");
        check(mppsm.getSample(3, 0, 0, db) == 1, "set/get sample bit 3");
        check(mppsm.getSample(1, 0, 0, db) == 0, "unset bit 1 remains 0");

        int[] px = mppsm.getPixel(0, 0, (int[]) null, db);
        check(px[0] == 1, "getPixel bit 0");
        check(mppsm.createCompatibleSampleModel(4, 2) != null, "createCompatibleSampleModel");
        check(mppsm.createSubsetSampleModel(new int[]{0}) != null, "createSubsetSampleModel");
        check(mppsm.getDataElements(0, 0, null, db) != null, "getDataElements non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += MultiPixelPackedSampleModelDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class MultiPixelPackedSampleModelDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.MultiPixelPackedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.SampleModel"), "superclass java.awt.image.SampleModel");
        check(hasMethod(c, "getPixelBitStride"), "method getPixelBitStride");
        check(hasMethod(c, "getScanlineStride"), "method getScanlineStride");
        check(hasMethod(c, "getDataBitOffset"), "method getDataBitOffset");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
    }

    static void testLayout() {
        // 4-bit pixels: two per byte.
        MultiPixelPackedSampleModel m = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 4, 2, 4);
        check(m.getWidth() == 4 && m.getHeight() == 2, "dims 4x2");
        check(m.getNumBands() == 1, "single band");
        check(m.getNumDataElements() == 1, "one byte element per pixel group");
        check(m.getPixelBitStride() == 4, "4 bits per pixel");
        check(m.getDataBitOffset() == 0, "data bit offset 0");
        check(m.getSampleSize(0) == 4, "sample size 4 bits");
        check(m.getTransferType() == DataBuffer.TYPE_BYTE, "byte transfer");
        // 8-bit pixels: one per byte.
        MultiPixelPackedSampleModel m8 = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 4, 2, 8);
        check(m8.getPixelBitStride() == 8, "8 bits per pixel");
        // 1-bit pixels: eight per byte; a 5-wide row spans two bytes.
        MultiPixelPackedSampleModel m1 = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 5, 1, 1);
        check(m1.getScanlineStride() == 1, "5 one-bit pixels fit one byte");
        MultiPixelPackedSampleModel m9 = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 9, 1, 1);
        check(m9.getScanlineStride() == 2, "9 one-bit pixels span two bytes");
    }

    static void testPacking() {
        // 4-bit pixels pack two per byte, high nibble first.
        MultiPixelPackedSampleModel m = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 2, 1, 4);
        DataBuffer db = m.createDataBuffer();
        m.setPixel(0, 0, new int[] { 5 }, db);
        m.setPixel(1, 0, new int[] { 9 }, db);
        check(db.getElem(0) == ((5 << 4) | 9), "two nibbles packed per byte");
        check(m.getPixel(0, 0, (int[]) null, db)[0] == 5, "high nibble read back");
        check(m.getPixel(1, 0, (int[]) null, db)[0] == 9, "low nibble read back");
        // Byte-level and pixel-level access agree.
        check(m.getSample(0, 0, 0, db) == 5 && m.getSample(1, 0, 0, db) == 9,
              "sample access unpacks");
    }

    static void testOffsetFormula() {
        MultiPixelPackedSampleModel m = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 8, 2, 1);
        check(m.getOffset(0, 0) == 0, "origin offset 0");
        check(m.getOffset(0, 1) == 1, "row stride for 8 one-bit pixels");
        check(m.getOffset(1, 0) == 0, "pixel 1 shares byte 0");
    }

    static void testSubsetAndErrors() {
        MultiPixelPackedSampleModel m = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, 8, 2, 4);
        SampleModel sub = m.createSubsetSampleModel(new int[] { 0 });
        check(sub instanceof MultiPixelPackedSampleModel, "subset type preserved");
        check(sub.getNumBands() == 1, "subset single band");
        boolean threw = false;
        try { new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, 4, 2, 0); }
        catch (java.awt.image.RasterFormatException e) { threw = true; }
        check(threw, "zero bits per pixel -> RasterFormatException (JDK-27)");
        threw = false;
        try { new MultiPixelPackedSampleModel(DataBuffer.TYPE_SHORT, 4, 2, 4); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "non-byte data type -> IAE");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.MultiPixelPackedSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getBitOffset"), "method getBitOffset");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setSample"), "method setSample");
    }



    static int run() throws Exception {
        testWhiteBox();
        testLayout();
        testPacking();
        testOffsetFormula();
        testSubsetAndErrors();
        testOriginalSurface();
        return passed;
    }
}
