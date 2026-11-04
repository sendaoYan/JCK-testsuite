/*
 * @test
 * @summary java.awt.image.PixelInterleavedSampleModel conformance: white-box structure plus black-box behavior.
 * @run main PixelInterleavedSampleModelTest
 */

import java.awt.image.*;
import java.lang.reflect.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
