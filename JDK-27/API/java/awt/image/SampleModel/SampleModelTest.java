/*
 * @test
 * @summary java.awt.image.SampleModel conformance: white-box structure + black-box behavior.
 * @run main SampleModelTest
 */

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;

public class SampleModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.SampleModel", false, SampleModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getNumDataElements"), "getNumDataElements()");
        check(hasMethod(c, "getSampleSize", int.class), "getSampleSize(int)");
        check(hasMethod(c, "getSampleSize"), "getSampleSize()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getNumBands"), "getNumBands()");
        check(hasMethod(c, "getDataType"), "getDataType()");
        check(hasMethod(c, "getTransferType"), "getTransferType()");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel", int[].class), "createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "createDataBuffer()");
        check(hasMethod(c, "getPixel", int.class, int.class, int[].class, DataBuffer.class), "getPixel");
        check(hasMethod(c, "setPixel", int.class, int.class, int[].class, DataBuffer.class), "setPixel");
        check(hasMethod(c, "getSample", int.class, int.class, int.class, DataBuffer.class), "getSample");
        check(hasMethod(c, "setSample", int.class, int.class, int.class, int.class, DataBuffer.class), "setSample");
        check(hasMethod(c, "getDataElements", int.class, int.class, Object.class, DataBuffer.class), "getDataElements");
    }

    static void testBlackBox() {
        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 3, 3, null);
        SampleModel sm = wr.getSampleModel();
        check(sm.getWidth() == 4 && sm.getHeight() == 3, "dims");
        check(sm.getNumBands() == 3, "3 bands");
        check(sm.getDataType() == DataBuffer.TYPE_BYTE, "data type byte");
        check(sm.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type byte");
        check(sm.getNumDataElements() == 3, "3 data elements");
        int[] sizes = sm.getSampleSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8, "8-bit sample sizes");
        check(sm.getSampleSize(0) == 8 && sm.getSampleSize(2) == 8, "getSampleSize per band");

        DataBuffer db = wr.getDataBuffer();
        sm.setSample(2, 1, 0, 200, db);
        sm.setSample(2, 1, 1, 100, db);
        sm.setSample(2, 1, 2, 50, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getSample band 0");
        check(sm.getSample(2, 1, 1, db) == 100, "getSample band 1");
        check(sm.getSample(2, 1, 2, db) == 50, "getSample band 2");

        int[] px = sm.getPixel(2, 1, (int[]) null, db);
        check(px.length == 3 && px[0] == 200 && px[1] == 100 && px[2] == 50, "getPixel");

        int[] np = new int[] { 7, 8, 9 };
        sm.setPixel(0, 0, np, db);
        int[] rp = sm.getPixel(0, 0, (int[]) null, db);
        check(rp[0] == 7 && rp[1] == 8 && rp[2] == 9, "setPixel/getPixel");

        DataBuffer db2 = sm.createDataBuffer();
        check(db2.getDataType() == DataBuffer.TYPE_BYTE, "createDataBuffer type");
        check(db2.getSize() >= 12, "createDataBuffer sized");

        SampleModel comp = sm.createCompatibleSampleModel(2, 2);
        check(comp.getWidth() == 2 && comp.getHeight() == 2, "compatible sample model dims");
        check(comp.getNumBands() == 3, "compatible sample model bands");
        check(comp.getClass() == sm.getClass(), "compatible same class");

        SampleModel subset = sm.createSubsetSampleModel(new int[] { 0, 2 });
        check(subset.getNumBands() == 2, "subset sample model 2 bands");
        check(subset.getWidth() == 4 && subset.getHeight() == 3, "subset sample model dims");

        // getDataElements / setDataElements round-trip
        Object de = sm.getDataElements(2, 1, null, db);
        sm.setDataElements(2, 1, de, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getDataElements/setDataElements round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
