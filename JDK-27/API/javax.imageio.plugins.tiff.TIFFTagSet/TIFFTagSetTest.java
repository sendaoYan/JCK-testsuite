/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFTagSet conformance: behavioral.
 * @run main TIFFTagSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TIFFTagSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.TIFFTag tag = new javax.imageio.plugins.tiff.TIFFTag("TagA", 65000, javax.imageio.plugins.tiff.TIFFTag.TIFF_SHORT, 1);
        java.util.List<javax.imageio.plugins.tiff.TIFFTag> list = new java.util.ArrayList<>();
        list.add(tag);
        javax.imageio.plugins.tiff.TIFFTagSet ts = new javax.imageio.plugins.tiff.TIFFTagSet(list);
        check(ts.getTag(65000) == tag, "getTag(int)");
        check(ts.getTag("TagA") == tag, "getTag(String)");
        check(ts.getTagNumbers().contains(65000), "tagNumbers");
        check(ts.getTagNames().contains("TagA"), "tagNames");
        check(ts.getTag(99999) == null, "getTag unknown null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
