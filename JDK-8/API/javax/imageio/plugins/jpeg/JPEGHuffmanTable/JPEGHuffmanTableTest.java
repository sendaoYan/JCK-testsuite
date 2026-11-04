/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGHuffmanTable conformance: behavioral.
 * @run main JPEGHuffmanTableTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class JPEGHuffmanTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        short[] lengths = new short[16]; lengths[0] = 2;
        short[] values = new short[]{0, 1};
        javax.imageio.plugins.jpeg.JPEGHuffmanTable t = new javax.imageio.plugins.jpeg.JPEGHuffmanTable(lengths, values);
        check(t.getLengths() != null && t.getLengths().length == 16, "lengths 16");
        check(t.getValues() != null, "values non-null");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCLuminance != null, "StdDCLuminance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCChrominance != null, "StdDCChrominance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACLuminance != null, "StdACLuminance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACChrominance != null, "StdACChrominance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
