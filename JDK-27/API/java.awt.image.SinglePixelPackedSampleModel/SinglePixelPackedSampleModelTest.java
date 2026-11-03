/*
 * @test
 * @summary java.awt.image.SinglePixelPackedSampleModel conformance: white-box structure + black-box behavior.
 * @run main SinglePixelPackedSampleModelTest
 */

import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.lang.reflect.*;

public class SinglePixelPackedSampleModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.SinglePixelPackedSampleModel", false, SinglePixelPackedSampleModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == SampleModel.class, "extends SampleModel");
        check(hasMethod(c, "getNumDataElements"), "getNumDataElements()");
        check(hasMethod(c, "getSampleSize"), "getSampleSize()");
        check(hasMethod(c, "getSampleSize", int.class), "getSampleSize(int)");
        check(hasMethod(c, "getBitMasks"), "getBitMasks()");
        check(hasMethod(c, "getBitOffsets"), "getBitOffsets()");
        check(hasMethod(c, "getOffset", int.class, int.class), "getOffset");
        check(hasMethod(c, "getScanlineStride"), "getScanlineStride()");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel", int[].class), "createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "createDataBuffer()");
        check(hasMethod(c, "getSample", int.class, int.class, int.class, DataBuffer.class), "getSample");
        check(hasMethod(c, "setSample", int.class, int.class, int.class, int.class, DataBuffer.class), "setSample");
    }

    static void testBlackBox() {
        SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 4, 3, new int[] { 0xFF0000, 0x00FF00, 0x0000FF });
        check(sm.getWidth() == 4 && sm.getHeight() == 3, "dims");
        check(sm.getNumBands() == 3, "3 bands");
        check(sm.getNumDataElements() == 1, "1 data element");
        check(sm.getDataType() == DataBuffer.TYPE_INT, "data type INT");
        check(sm.getTransferType() == DataBuffer.TYPE_INT, "transfer type INT");
        check(sm.getScanlineStride() == 4, "scanline stride 4");
        check(sm.getOffset(0, 0) == 0, "offset(0,0) 0");
        check(sm.getOffset(2, 1) == 6, "offset(2,1) 6");

        int[] masks = sm.getBitMasks();
        check(masks.length == 3 && masks[0] == 0xFF0000 && masks[1] == 0x00FF00 && masks[2] == 0x0000FF, "bit masks");
        int[] offs = sm.getBitOffsets();
        check(offs.length == 3 && offs[0] == 16 && offs[1] == 8 && offs[2] == 0, "bit offsets");
        int[] sizes = sm.getSampleSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8, "sample sizes 8");
        check(sm.getSampleSize(0) == 8, "getSampleSize(0)");

        DataBufferInt db = new DataBufferInt(12);
        sm.setSample(2, 1, 0, 0x11, db);
        sm.setSample(2, 1, 1, 0x22, db);
        sm.setSample(2, 1, 2, 0x33, db);
        check(sm.getSample(2, 1, 0, db) == 0x11, "getSample red");
        check(sm.getSample(2, 1, 1, db) == 0x22, "getSample green");
        check(sm.getSample(2, 1, 2, db) == 0x33, "getSample blue");
        check(db.getElem(6) == 0x00112233, "packed int pixel");

        int[] px = sm.getPixel(2, 1, (int[]) null, db);
        check(px[0] == 0x11 && px[1] == 0x22 && px[2] == 0x33, "getPixel");

        int[] np = new int[] { 0x40, 0x50, 0x60 };
        sm.setPixel(0, 0, np, db);
        int[] rp = sm.getPixel(0, 0, (int[]) null, db);
        check(rp[0] == 0x40 && rp[1] == 0x50 && rp[2] == 0x60, "setPixel/getPixel");

        DataBuffer db2 = sm.createDataBuffer();
        check(db2 instanceof DataBufferInt, "createDataBuffer DataBufferInt");
        check(db2.getSize() == 12, "createDataBuffer size 12");

        SampleModel comp = sm.createCompatibleSampleModel(2, 2);
        check(comp.getWidth() == 2 && comp.getHeight() == 2, "compatible dims");
        check(comp.getNumBands() == 3, "compatible bands");
        check(comp instanceof SinglePixelPackedSampleModel, "compatible class");

        SampleModel subset = sm.createSubsetSampleModel(new int[] { 0, 2 });
        check(subset.getNumBands() == 2, "subset 2 bands");

        // data elements round trip (single int)
        Object de = sm.getDataElements(2, 1, null, db);
        check(de instanceof int[], "data elements int[]");
        sm.setDataElements(2, 1, de, db);
        check(sm.getSample(2, 1, 0, db) == 0x11, "data elements round-trip red");

        SinglePixelPackedSampleModel same = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 4, 3, new int[] { 0xFF0000, 0x00FF00, 0x0000FF });
        check(sm.equals(same), "equals symmetric");
        check(sm.hashCode() == same.hashCode(), "hashCode symmetric");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
