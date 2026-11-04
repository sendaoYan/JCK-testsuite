/*
 * @test
 * @summary java.awt.image.MultiPixelPackedSampleModel conformance: white-box structure plus black-box behavior.
 * @run main MultiPixelPackedSampleModelTest
 */

import java.awt.image.*;
import java.lang.reflect.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
