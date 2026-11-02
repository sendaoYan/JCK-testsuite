/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFImageReadParam conformance: behavioral.
 * @run main TIFFImageReadParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TIFFImageReadParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.TIFFImageReadParam p = new javax.imageio.plugins.tiff.TIFFImageReadParam();
        check(!p.getReadUnknownTags(), "readUnknownTags default false");
        p.setReadUnknownTags(true);
        check(p.getReadUnknownTags(), "setReadUnknownTags");
        check(p.getAllowedTagSets() != null, "allowedTagSets non-null");
        javax.imageio.plugins.tiff.TIFFTagSet custom = new javax.imageio.plugins.tiff.TIFFTagSet(new java.util.ArrayList<>());
        int n = p.getAllowedTagSets().size();
        p.addAllowedTagSet(custom);
        check(p.getAllowedTagSets().size() == n + 1, "addAllowedTagSet");
        p.removeAllowedTagSet(custom);
        check(p.getAllowedTagSets().size() == n, "removeAllowedTagSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
