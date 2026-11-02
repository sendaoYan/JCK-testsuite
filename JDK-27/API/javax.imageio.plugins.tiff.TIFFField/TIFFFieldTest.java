/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFField conformance: behavioral.
 * @run main TIFFFieldTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TIFFFieldTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.TIFFTag tag = javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance().getTag(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH);
        javax.imageio.plugins.tiff.TIFFField f = new javax.imageio.plugins.tiff.TIFFField(tag, javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG, 1, new long[]{100L});
        check(f.getTag() == tag, "getTag");
        check(f.getTagNumber() == javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH, "getTagNumber");
        check(f.getType() == javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG, "getType");
        check(f.getCount() == 1, "getCount");
        check(f.isIntegral(), "isIntegral");
        check(f.getAsLong(0) == 100L, "getAsLong");
        check(f.getValueAsString(0) != null, "valueAsString");
        javax.imageio.plugins.tiff.TIFFField f2 = new javax.imageio.plugins.tiff.TIFFField(tag, 1L);
        check(f2.getCount() == 1, "count2");
        check(javax.imageio.plugins.tiff.TIFFField.getTypeName(javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG) != null, "getTypeName");
        javax.imageio.plugins.tiff.TIFFField f3 = new javax.imageio.plugins.tiff.TIFFField(tag, javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG, 1);
        check(f3.getCount() == 1, "count3");
        javax.imageio.plugins.tiff.TIFFField f4 = f.clone();
        check(f4.getTagNumber() == f.getTagNumber(), "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
