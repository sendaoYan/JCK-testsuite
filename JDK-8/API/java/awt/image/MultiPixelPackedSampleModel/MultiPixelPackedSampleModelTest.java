/*
 * @test
 * @summary java.awt.image.MultiPixelPackedSampleModel conformance: class reflection + bit packing contract.
 * @run main MultiPixelPackedSampleModelTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;

public class MultiPixelPackedSampleModelTest {
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
        catch (ArithmeticException e) { threw = true; }
        check(threw, "zero bits per pixel -> ArithmeticException (JDK-8)");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOriginalSurface();
        testLayout();
        testPacking();
        testOffsetFormula();
        testSubsetAndErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
