/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifInteroperabilityTagSet conformance: behavioral.
 * @run main ExifInteroperabilityTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExifInteroperabilityTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.ExifInteroperabilityTagSet ts = javax.imageio.plugins.tiff.ExifInteroperabilityTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.ExifInteroperabilityTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.ExifInteroperabilityTagSet.TAG_INTEROPERABILITY_INDEX) != null, "tag TAG_INTEROPERABILITY_INDEX");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
