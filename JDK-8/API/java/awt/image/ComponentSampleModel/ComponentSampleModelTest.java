/*
 * @test
 * @summary java.awt.image.ComponentSampleModel conformance: class reflection + component layout contract.
 * @run main ComponentSampleModelTest
 */

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

public class ComponentSampleModelTest {
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
        Class<?> c = Class.forName("java.awt.image.ComponentSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.SampleModel"), "superclass java.awt.image.SampleModel");
        check(hasMethod(c, "getPixelStride"), "method getPixelStride");
        check(hasMethod(c, "getScanlineStride"), "method getScanlineStride");
        check(hasMethod(c, "getBandOffsets"), "method getBandOffsets");
        check(hasMethod(c, "getBankIndices"), "method getBankIndices");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "setSample"), "method setSample");
        // Known concrete subclasses.
        check(PixelInterleavedSampleModel.class.getSuperclass() == ComponentSampleModel.class,
              "PixelInterleavedSampleModel extends ComponentSampleModel");
        check(java.awt.image.BandedSampleModel.class.getSuperclass() == ComponentSampleModel.class,
              "BandedSampleModel extends ComponentSampleModel");
    }

    static void testLayout() {
        ComponentSampleModel m = new ComponentSampleModel(DataBuffer.TYPE_SHORT,
                5, 4, 3, 15, new int[] { 0, 1, 2 });
        check(m.getWidth() == 5 && m.getHeight() == 4, "dims 5x4");
        check(m.getNumBands() == 3, "3 bands");
        check(m.getPixelStride() == 3, "pixel stride from ctor");
        check(m.getScanlineStride() == 15, "scanline stride from ctor");
        check(m.getTransferType() == DataBuffer.TYPE_SHORT, "transfer type short");
        check(m.getSampleSize(0) == 16, "short samples are 16 bits");
        int[] off = m.getBandOffsets();
        check(off.length == 3 && off[0] == 0 && off[1] == 1 && off[2] == 2, "band offsets exact");
        // getBandOffsets returns a defensive copy.
        off[0] = 99;
        check(m.getBandOffsets()[0] == 0, "band offsets defensively copied");
        int[] banks = m.getBankIndices();
        check(banks.length == 3 && banks[0] == 0, "single bank by default");
        banks[0] = 99;
        check(m.getBankIndices()[0] == 0, "bank indices defensively copied");
        // Explicit multi-bank layout.
        ComponentSampleModel mb = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
                3, 2, 1, 3, new int[] { 1, 0, 5 }, new int[] { 10, 20, 30 });
        check(mb.getBankIndices()[1] == 0 && mb.getBankIndices()[2] == 5, "bank indices exact");
        check(mb.getBandOffsets()[0] == 10 && mb.getBandOffsets()[2] == 30, "band offsets exact");
        check(mb.createDataBuffer().getNumBanks() == 6, "one bank per distinct index");
    }

    static void testSamples() {
        ComponentSampleModel m = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
                3, 2, 3, 9, new int[] { 0, 1, 2 });
        DataBuffer db = m.createDataBuffer();
        // Interleaved formula: elem = y*scanline + x*pixelStride + bandOffset.
        m.setSample(2, 1, 1, 77, db);
        check(db.getElem(1 * 9 + 2 * 3 + 1) == 77, "interleaved sample placement");
        check(m.getSample(2, 1, 1, db) == 77, "sample round trip");
        check(m.getOffset(2, 1) == 1 * 9 + 2 * 3, "offset formula");
        check(m.getOffset(2, 1, 2) == 1 * 9 + 2 * 3 + 2, "offset formula with band");
    }

    static void testSubset() {
        ComponentSampleModel m = new ComponentSampleModel(DataBuffer.TYPE_INT,
                4, 4, 3, 12, new int[] { 0, 1, 2 });
        SampleModel sub = m.createSubsetSampleModel(new int[] { 0 });
        check(sub.getNumBands() == 1, "subset band count");
        check(sub instanceof ComponentSampleModel, "subset type preserved");
        boolean threw = false;
        try { m.createSubsetSampleModel(new int[0]); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "empty band list -> IAE");
    }

    static void testCtorErrors() {
        // ComponentSampleModel does not validate strides at construction; both are stored.
        check(new ComponentSampleModel(DataBuffer.TYPE_BYTE, 2, 2, 0, 6, new int[] { 0 }).getPixelStride() == 0,
              "zero pixel stride stored as-is");
        check(new ComponentSampleModel(DataBuffer.TYPE_BYTE, 2, 2, 3, 0, new int[] { 0, 1, 2 })
                .getScanlineStride() == 0, "zero scanline stride stored as-is");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ComponentSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
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
