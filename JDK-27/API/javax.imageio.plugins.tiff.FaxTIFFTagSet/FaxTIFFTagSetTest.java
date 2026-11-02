/*
 * @test
 * @summary javax.imageio.plugins.tiff.FaxTIFFTagSet conformance: behavioral.
 * @run main FaxTIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FaxTIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.FaxTIFFTagSet ts = javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance();
        check(ts != null, "getInstance non-null");
        check(javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance() == ts, "singleton");
        check(ts instanceof javax.imageio.plugins.tiff.TIFFTagSet, "instanceof TIFFTagSet");
        check(ts.getTag(javax.imageio.plugins.tiff.FaxTIFFTagSet.TAG_BAD_FAX_LINES) != null, "tag TAG_BAD_FAX_LINES");
        check(ts.getTag(javax.imageio.plugins.tiff.FaxTIFFTagSet.TAG_CLEAN_FAX_DATA) != null, "tag TAG_CLEAN_FAX_DATA");
        check(!ts.getTagNumbers().isEmpty(), "tagNumbers non-empty");
        check(!ts.getTagNames().isEmpty(), "tagNames non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
