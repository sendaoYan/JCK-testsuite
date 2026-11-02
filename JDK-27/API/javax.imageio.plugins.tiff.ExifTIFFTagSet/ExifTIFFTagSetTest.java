/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifTIFFTagSet conformance: behavioral.
 * @run main ExifTIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExifTIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.ExifTIFFTagSet ts = javax.imageio.plugins.tiff.ExifTIFFTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.ExifTIFFTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifTIFFTagSet.TAG_EXPOSURE_TIME) != null, "tag TAG_EXPOSURE_TIME");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifTIFFTagSet.TAG_F_NUMBER) != null, "tag TAG_F_NUMBER");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
