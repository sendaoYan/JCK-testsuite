/*
 * @test
 * @summary javax.imageio.ImageWriteParam conformance: behavioral.
 * @run main ImageWriteParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageWriteParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.util.Locale loc = java.util.Locale.US;
        javax.imageio.ImageWriteParam p = new javax.imageio.ImageWriteParam(loc);
        check(p.getLocale() == loc, "locale");
        check(!p.canWriteTiles(), "canWriteTiles false");
        check(!p.canWriteCompressed(), "canWriteCompressed false");
        check(!p.canWriteProgressive(), "canWriteProgressive false");
        check(!p.canOffsetTiles(), "canOffsetTiles false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
