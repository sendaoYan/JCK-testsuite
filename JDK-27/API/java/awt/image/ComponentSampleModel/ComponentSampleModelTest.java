/*
 * @test
 * @summary java.awt.image.ComponentSampleModel conformance: white-box structure plus black-box behavior.
 * @run main ComponentSampleModelTest
 */

import java.awt.image.*;
import java.lang.reflect.*;

public class ComponentSampleModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ComponentSampleModel");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(SampleModel.class.isAssignableFrom(c), "extends SampleModel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 3 bands, pixelStride 3, scanlineStride 12 (4 px/row), band offsets 0/1/2
        ComponentSampleModel csm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, 4, 4, 3, 12, new int[]{0, 1, 2});
        check(csm.getNumDataElements() == 3, "numDataElements == pixelStride");
        check(csm.getSampleSize(0) == 8 && csm.getSampleSize(2) == 8, "sample size 8 bits");
        int[] sizes = csm.getSampleSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[2] == 8, "getSampleSize array");
        int[] banks = csm.getBankIndices();
        check(banks.length == 3 && banks[0] == 0, "bank indices length 3");
        int[] offsets = csm.getBandOffsets();
        check(offsets[0] == 0 && offsets[1] == 1 && offsets[2] == 2, "band offsets 0/1/2");
        check(csm.getScanlineStride() == 12 && csm.getPixelStride() == 3, "strides");
        check(csm.getOffset(1, 2) == 2 * 12 + 1 * 3, "getOffset(x,y)");
        check(csm.getOffset(1, 2, 1) == 2 * 12 + 1 * 3 + 1, "getOffset(x,y,b)");

        DataBuffer db = csm.createDataBuffer();
        check(db.getSize() == 48, "data buffer size 4*4*3");
        csm.setSample(1, 2, 1, 200, db);
        check(csm.getSample(1, 2, 1, db) == 200, "get/setSample roundtrip");
        csm.setPixel(0, 0, new int[]{10, 20, 30}, db);
        int[] px = csm.getPixel(0, 0, (int[]) null, db);
        check(px[0] == 10 && px[1] == 20 && px[2] == 30, "get/setPixel roundtrip");

        SampleModel compat = csm.createCompatibleSampleModel(8, 8);
        check(compat != null && compat.getWidth() == 8, "createCompatibleSampleModel");
        check(csm.createSubsetSampleModel(new int[]{1}) != null, "createSubsetSampleModel");
        check(csm.getDataElements(0, 0, null, db) != null, "getDataElements non-null");

        ComponentSampleModel same = new ComponentSampleModel(DataBuffer.TYPE_BYTE, 4, 4, 3, 12, new int[]{0, 1, 2});
        check(csm.equals(same) && csm.hashCode() == same.hashCode(), "equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
