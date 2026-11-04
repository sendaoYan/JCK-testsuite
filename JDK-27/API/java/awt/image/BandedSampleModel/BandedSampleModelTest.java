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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
