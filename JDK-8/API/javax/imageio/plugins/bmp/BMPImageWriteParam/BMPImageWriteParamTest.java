/*
 * @test
 * @summary javax.imageio.plugins.bmp.BMPImageWriteParam conformance: behavioral.
 * @run main BMPImageWriteParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BMPImageWriteParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.bmp.BMPImageWriteParam p = new javax.imageio.plugins.bmp.BMPImageWriteParam();
        check(!p.isTopDown(), "isTopDown default false");
        p.setTopDown(true);
        check(p.isTopDown(), "setTopDown true");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
