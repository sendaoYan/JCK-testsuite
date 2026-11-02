/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFDirectory conformance: behavioral.
 * @run main TIFFDirectoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TIFFDirectoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.TIFFTagSet[] tagSets = new javax.imageio.plugins.tiff.TIFFTagSet[]{ javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance() };
        javax.imageio.plugins.tiff.TIFFTag tag = javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance().getTag(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH);
        javax.imageio.plugins.tiff.TIFFDirectory dir = new javax.imageio.plugins.tiff.TIFFDirectory(tagSets, tag);
        check(dir.getTagSets().length == 1, "tagSets");
        check(dir.getParentTag() == tag, "parentTag");
        check(dir.getNumTIFFFields() == 0, "numFields 0");
        javax.imageio.plugins.tiff.TIFFField f = new javax.imageio.plugins.tiff.TIFFField(tag, javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG, 1, new long[]{100L});
        dir.addTIFFField(f);
        check(dir.getNumTIFFFields() == 1, "addTIFFField");
        check(dir.containsTIFFField(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH), "containsTIFFField");
        check(dir.getTIFFField(javax.imageio.plugins.tiff.BaselineTIFFTagSet.TAG_IMAGE_WIDTH) == f, "getTIFFField");
        check(dir.getAsMetadata() != null, "getAsMetadata");
        javax.imageio.plugins.tiff.TIFFDirectory c = dir.clone();
        check(c.getNumTIFFFields() == 1, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
