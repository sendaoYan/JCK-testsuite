/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageWriteParam conformance: behavioral.
 * @run main JPEGImageWriteParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class JPEGImageWriteParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.jpeg.JPEGImageWriteParam p = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(java.util.Locale.US);
        check(p.getLocale() == java.util.Locale.US, "locale");
        String[] ct = p.getCompressionTypes();
        check(ct != null && ct.length >= 1 && "JPEG".equals(ct[0]), "compressionTypes JPEG");
        p.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionMode() == javax.imageio.ImageWriteParam.MODE_EXPLICIT, "compressionMode");
        check(p.getCompressionQualityDescriptions().length > 0, "qualityDescriptions");
        check(p.getCompressionQualityValues().length > 0, "qualityValues");
        p.setCompressionQuality(0.75f);
        check(p.getCompressionQuality() == 0.75f, "quality");
        p.setOptimizeHuffmanTables(true);
        check(p.getOptimizeHuffmanTables(), "optimize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
