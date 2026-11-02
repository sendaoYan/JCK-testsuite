/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifParentTIFFTagSet conformance: behavioral.
 * @run main ExifParentTIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExifParentTIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.ExifParentTIFFTagSet ts = javax.imageio.plugins.tiff.ExifParentTIFFTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.ExifParentTIFFTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifParentTIFFTagSet.TAG_EXIF_IFD_POINTER) != null, "tag TAG_EXIF_IFD_POINTER");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER) != null, "tag TAG_GPS_INFO_IFD_POINTER");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
