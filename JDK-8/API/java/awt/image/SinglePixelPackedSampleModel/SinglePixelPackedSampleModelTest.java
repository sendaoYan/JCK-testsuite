/*
 * @test
 * @summary java.awt.image.SinglePixelPackedSampleModel conformance: class reflection + packed layout contract.
 * @run main SinglePixelPackedSampleModelTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

public class SinglePixelPackedSampleModelTest {
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
        Class<?> c = Class.forName("java.awt.image.SinglePixelPackedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.SampleModel"), "superclass java.awt.image.SampleModel");
        check(hasMethod(c, "getBitMasks"), "method getBitMasks");
        check(hasMethod(c, "getBitOffsets"), "method getBitOffsets");
        check(hasMethod(c, "getScanlineStride"), "method getScanlineStride");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "getSample"), "method getSample");
    }

    static int[] rgbMasks() {
        return new int[] { 0xFF0000, 0xFF00, 0xFF };
    }

    static void testLayout() {
        SinglePixelPackedSampleModel m = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 5, 3, 8, rgbMasks());
        check(m.getWidth() == 5 && m.getHeight() == 3, "dims 5x3");
        check(m.getNumBands() == 3, "3 bands from 3 masks");
        check(m.getNumDataElements() == 1, "one int data element per pixel");
        check(m.getScanlineStride() == 8, "scanline stride from ctor");
        check(m.getTransferType() == DataBuffer.TYPE_INT, "int transfer type");
        check(java.util.Arrays.equals(m.getBitMasks(), rgbMasks()), "bit masks exact");
        check(java.util.Arrays.equals(m.getBitOffsets(), new int[] { 16, 8, 0 }),
              "bit offsets derived from masks");
        check(m.getSampleSize(0) == 8, "8 bits per mask band");
        DataBuffer db = m.createDataBuffer();
        check(db instanceof DataBuffer && db.getDataType() == DataBuffer.TYPE_INT,
              "int data buffer");
    }

    static void testSamples() {
        SinglePixelPackedSampleModel m = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 3, 2, 3, rgbMasks());
        DataBuffer db = m.createDataBuffer();
        m.setPixel(2, 1, new int[] { 0x12, 0x34, 0x56 }, db);
        check(db.getElem(1 * 3 + 2) == 0x123456, "packed int stored at y*stride + x");
        check(m.getPixel(2, 1, (int[]) null, db)[0] == 0x12
              && m.getPixel(2, 1, (int[]) null, db)[1] == 0x34
              && m.getPixel(2, 1, (int[]) null, db)[2] == 0x56,
              "bands unpacked from packed int");
        m.setSample(0, 0, 1, 0xAB, db);
        check(m.getSample(0, 0, 1, db) == 0xAB, "single band sample round trip");
        check(db.getElem(0) == 0x00AB00, "setSample writes only its field");
    }

    static void testSubset() {
        SinglePixelPackedSampleModel m = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 4, 4, 4, new int[] { 0xFF000000, 0xFF0000, 0xFF00, 0xFF });
        SampleModel sub = m.createSubsetSampleModel(new int[] { 0, 2 });
        check(sub instanceof SinglePixelPackedSampleModel, "subset type preserved");
        check(sub.getNumBands() == 2, "subset keeps 2 masked bands");
        check(java.util.Arrays.equals(((SinglePixelPackedSampleModel) sub).getBitMasks(),
                new int[] { 0xFF000000, 0xFF00 }), "subset masks follow selection");
    }

    static void testCtorErrors() {
        // SinglePixelPackedSampleModel does not validate masks or strides at construction.
        check(new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, 2, 2, 2, rgbMasks())
                .getTransferType() == DataBuffer.TYPE_BYTE, "byte data type accepted");
        check(java.util.Arrays.equals(
                new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 2, 2, 2,
                        new int[] { 0xF, 0xF }).getBitMasks(),
                new int[] { 0xF, 0xF }), "duplicate masks stored as-is");
        check(new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 2, 2, 0, rgbMasks())
                .getScanlineStride() == 0, "zero scanline stride stored as-is");
        boolean threw = false;
        try { new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 0, 2, 2, rgbMasks()); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero width -> IAE");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.SinglePixelPackedSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOriginalSurface();
        testLayout();
        testSamples();
        testSubset();
        testCtorErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
