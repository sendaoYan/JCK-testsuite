/*
 * @test
 * @summary java.awt.image.BandedSampleModel conformance: white-box structure + black-box behavior.
 * @run main BandedSampleModelTest
 */

import java.awt.image.BandedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.lang.reflect.*;

public class BandedSampleModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BandedSampleModel", false, BandedSampleModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final class");
        check(java.awt.image.ComponentSampleModel.class.isAssignableFrom(c), "extends ComponentSampleModel");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel", int[].class), "createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "createDataBuffer()");
        check(hasMethod(c, "getSample", int.class, int.class, int.class, DataBuffer.class), "getSample");
        check(hasMethod(c, "setSample", int.class, int.class, int.class, int.class, DataBuffer.class), "setSample");
        check(hasMethod(c, "getPixel", int.class, int.class, int[].class, DataBuffer.class), "getPixel");
        check(hasMethod(c, "setPixel", int.class, int.class, int[].class, DataBuffer.class), "setPixel");
    }

    static void testBlackBox() {
        BandedSampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 4, 4, 3);
        check(sm.getWidth() == 4 && sm.getHeight() == 4, "dims");
        check(sm.getNumBands() == 3, "3 bands");
        check(sm.getNumDataElements() == 3, "3 data elements");
        check(sm.getDataType() == DataBuffer.TYPE_BYTE, "data type byte");
        check(sm.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type byte");
        int[] sizes = sm.getSampleSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8, "8-bit samples");
        check(sm.getSampleSize(0) == 8, "getSampleSize(0)");

        DataBufferByte db = (DataBufferByte) sm.createDataBuffer();
        check(db.getSize() == 16, "data buffer size 16");
        check(db.getNumBanks() == 3, "banded three banks");
        check(db.getOffsets().length == 3, "three bank offsets");

        sm.setSample(2, 1, 0, 200, db);
        sm.setSample(2, 1, 1, 100, db);
        sm.setSample(2, 1, 2, 50, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getSample band 0");
        check(sm.getSample(2, 1, 1, db) == 100, "getSample band 1");
        check(sm.getSample(2, 1, 2, db) == 50, "getSample band 2");

        int[] px = sm.getPixel(2, 1, (int[]) null, db);
        check(px.length == 3 && px[0] == 200 && px[1] == 100 && px[2] == 50, "getPixel");

        sm.setPixel(0, 0, new int[] { 9, 8, 7 }, db);
        int[] rp = sm.getPixel(0, 0, (int[]) null, db);
        check(rp[0] == 9 && rp[1] == 8 && rp[2] == 7, "setPixel/getPixel");

        Object de = sm.getDataElements(2, 1, null, db);
        sm.setDataElements(2, 1, de, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getDataElements/setDataElements");

        SampleModel comp = sm.createCompatibleSampleModel(2, 2);
        check(comp.getWidth() == 2 && comp.getHeight() == 2, "compatible dims");
        check(comp.getNumBands() == 3, "compatible bands");
        check(comp instanceof BandedSampleModel, "compatible class");

        SampleModel subset = sm.createSubsetSampleModel(new int[] { 1 });
        check(subset.getNumBands() == 1, "subset 1 band");
        check(subset.getWidth() == 4 && subset.getHeight() == 4, "subset dims");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += BandedSampleModelDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class BandedSampleModelDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.BandedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ComponentSampleModel"), "superclass java.awt.image.ComponentSampleModel");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "setSample"), "method setSample");
    }

    static void testLayout() {
        BandedSampleModel m = new BandedSampleModel(DataBuffer.TYPE_INT, 4, 3, 3);
        check(m.getWidth() == 4 && m.getHeight() == 3, "dims 4x3");
        check(m.getNumBands() == 3, "3 bands");
        check(m.getNumDataElements() == 3, "one data element per band (3 bands)");
        check(m.getPixelStride() == 1, "banded pixel stride is 1");
        check(m.getTransferType() == DataBuffer.TYPE_INT, "transfer type from ctor");
        check(m.getSampleSize(0) == 32, "int samples are 32 bits");
        // Banks: one per band, indices 0..n-1 by default.
        int[] banks = m.getBankIndices();
        check(banks.length == 3 && banks[0] == 0 && banks[1] == 1 && banks[2] == 2,
              "default bank indices per band");
        java.util.Arrays.fill(m.getBandOffsets(), 0);
        check(m.getBandOffsets().length == 3, "band offset per band");
        // createDataBuffer allocates one bank per band.
        DataBuffer db = m.createDataBuffer();
        check(db.getNumBanks() == 3, "data buffer bank per band");
        check(db.getDataType() == DataBuffer.TYPE_INT, "buffer type matches");
    }

    static void testSamples() {
        BandedSampleModel m = new BandedSampleModel(DataBuffer.TYPE_BYTE, 3, 2, 2);
        DataBuffer db = m.createDataBuffer();
        // Banded layout: each band lives in its own bank, consecutive pixels per row.
        m.setSample(0, 0, 0, 10, db);
        m.setSample(1, 0, 0, 20, db);
        m.setSample(0, 1, 0, 30, db);
        m.setSample(0, 0, 1, 40, db);
        check(m.getSample(0, 0, 0, db) == 10, "get/set round trip");
        check(m.getSample(1, 0, 0, db) == 20, "second column");
        check(m.getSample(0, 1, 0, db) == 30, "second row");
        check(m.getSample(0, 0, 1, db) == 40, "second band in separate bank");
        check(db.getElem(1, 0) == 40, "band 1 data lives in bank 1");
        // Offset formula: sample(x,y,b) at bank b, element y*scanlineStride + x + bandOffset.
        check(m.getOffset(1, 1) - m.getOffset(0, 1) == 1, "offset advances by 1 per pixel");
    }

    static void testSubset() {
        BandedSampleModel m = new BandedSampleModel(DataBuffer.TYPE_INT, 4, 4, 3);
        SampleModel sub = m.createSubsetSampleModel(new int[] { 1, 2 });
        check(sub.getNumBands() == 2, "subset keeps selected bands");
        check(sub.getWidth() == 4 && sub.getHeight() == 4, "subset keeps dims");
        check(sub instanceof BandedSampleModel, "subset stays a BandedSampleModel");
        // Null or out-of-range band lists are rejected.
        boolean threw = false;
        try { m.createSubsetSampleModel(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null band list -> NPE");
        threw = false;
        try { m.createSubsetSampleModel(new int[] { 0, 9 }); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "band index out of range -> AIOOBE");
    }

    static void testCtorErrors() {
        boolean threw = false;
        try { new BandedSampleModel(DataBuffer.TYPE_BYTE, 0, 2, 1); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero width -> IAE");
        // BandedSampleModel performs no data-type validation at construction.
        check(new BandedSampleModel(DataBuffer.TYPE_DOUBLE, 2, 2, 1).getTransferType()
              == DataBuffer.TYPE_DOUBLE, "double data type accepted");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BandedSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSamples"), "method setSamples");
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
