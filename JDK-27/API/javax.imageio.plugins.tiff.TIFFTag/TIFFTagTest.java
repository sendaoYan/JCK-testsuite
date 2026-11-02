/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFTag conformance: behavioral.
 * @run main TIFFTagTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TIFFTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.tiff.TIFFTag tag = new javax.imageio.plugins.tiff.TIFFTag("TestTag", 65000, 1 << javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG, 1);
        check("TestTag".equals(tag.getName()), "name");
        check(tag.getNumber() == 65000, "number");
        check(tag.getDataTypes() == (1 << javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG), "dataTypes");
        check(tag.getCount() == 1, "count");
        check(tag.isDataTypeOK(javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG), "isDataTypeOK");
        check(tag.getTagSet() == null, "tagSet null");
        check(!tag.isIFDPointer(), "not IFD pointer");
        check(!tag.hasValueNames(), "no value names");
        check(javax.imageio.plugins.tiff.TIFFTag.getSizeOfType(javax.imageio.plugins.tiff.TIFFTag.TIFF_LONG) == 4, "sizeOfType LONG");
        javax.imageio.plugins.tiff.TIFFTagSet tset = new javax.imageio.plugins.tiff.TIFFTagSet(new java.util.ArrayList<javax.imageio.plugins.tiff.TIFFTag>());
        javax.imageio.plugins.tiff.TIFFTag tag3 = new javax.imageio.plugins.tiff.TIFFTag("InSet", 65002, tset);
        check(tag3.getTagSet() == tset, "tagSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
