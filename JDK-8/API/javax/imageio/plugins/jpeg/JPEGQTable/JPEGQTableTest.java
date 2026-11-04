/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGQTable conformance: behavioral.
 * @run main JPEGQTableTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class JPEGQTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        int[] q = new int[64];
        java.util.Arrays.fill(q, 16);
        javax.imageio.plugins.jpeg.JPEGQTable t = new javax.imageio.plugins.jpeg.JPEGQTable(q);
        check(t.getTable() != null && t.getTable().length == 64, "table 64");
        javax.imageio.plugins.jpeg.JPEGQTable s = t.getScaledInstance(0.5f, true);
        check(s != null && s.getTable().length == 64, "scaled");
        check(javax.imageio.plugins.jpeg.JPEGQTable.K1Luminance != null, "K1Luminance");
        check(javax.imageio.plugins.jpeg.JPEGQTable.K2Chrominance != null, "K2Chrominance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
