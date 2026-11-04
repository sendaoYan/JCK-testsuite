/*
 * @test
 * @summary javax.imageio.ImageReadParam conformance: behavioral.
 * @run main ImageReadParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageReadParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.ImageReadParam p = new javax.imageio.ImageReadParam();
        check(p.getSourceRegion() == null, "sourceRegion null");
        java.awt.Rectangle r = new java.awt.Rectangle(0, 0, 10, 10);
        p.setSourceRegion(r);
        check(p.getSourceRegion().equals(r), "setSourceRegion");
        p.setSourceSubsampling(2, 3, 1, 2);
        check(p.getSourceXSubsampling() == 2, "xSub");
        check(p.getSourceYSubsampling() == 3, "ySub");
        check(p.getSubsamplingXOffset() == 1, "xOff");
        check(p.getSubsamplingYOffset() == 2, "yOff");
        p.setSourceBands(new int[]{0});
        check(p.getSourceBands().length == 1, "sourceBands");
        check(p.getDestination() == null, "destination null");
        check(!p.canSetSourceRenderSize(), "canSetSourceRenderSize false");
        p.setSourceProgressivePasses(1, 5);
        check(p.getSourceMinProgressivePass() == 1, "minPass");
        check(p.getSourceNumProgressivePasses() == 5, "numPass");
        check(!p.hasController(), "hasController false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
