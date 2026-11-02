/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifGPSTagSet conformance: behavioral.
 * @run main ExifGPSTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExifGPSTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.ExifGPSTagSet ts = javax.imageio.plugins.tiff.ExifGPSTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.ExifGPSTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifGPSTagSet.TAG_GPS_LATITUDE) != null, "tag TAG_GPS_LATITUDE");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifGPSTagSet.TAG_GPS_LONGITUDE) != null, "tag TAG_GPS_LONGITUDE");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
