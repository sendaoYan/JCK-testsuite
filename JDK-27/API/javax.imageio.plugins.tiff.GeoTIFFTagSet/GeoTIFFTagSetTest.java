/*
 * @test
 * @summary javax.imageio.plugins.tiff.GeoTIFFTagSet conformance: behavioral.
 * @run main GeoTIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class GeoTIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.GeoTIFFTagSet ts = javax.imageio.plugins.tiff.GeoTIFFTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.GeoTIFFTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE) != null, "tag TAG_MODEL_PIXEL_SCALE");
        check(ts.getTag(javax.imageio.plugins.tiff.GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION) != null, "tag TAG_MODEL_TRANSFORMATION");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
