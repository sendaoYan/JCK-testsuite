/*
 * @test
 * @summary javax.imageio.plugins.tiff.BaselineTIFFTagSet conformance: behavioral.
 * @run main BaselineTIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BaselineTIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.BaselineTIFFTagSet ts = javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH) != null, "tag TAG_IMAGE_WIDTH");
        check(ts.getTag(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_LENGTH) != null, "tag TAG_IMAGE_LENGTH");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
