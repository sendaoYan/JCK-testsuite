/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageReadParam conformance: behavioral.
 * @run main JPEGImageReadParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class JPEGImageReadParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.jpeg.JPEGImageReadParam p = new javax.imageio.plugins.jpeg.JPEGImageReadParam();
        check(!p.areTablesSet(), "areTablesSet false");
        javax.imageio.plugins.jpeg.JPEGQTable[] q = new javax.imageio.plugins.jpeg.JPEGQTable[]{
            javax.imageio.plugins.jpeg.JPEGQTable.K1Luminance,
            javax.imageio.plugins.jpeg.JPEGQTable.K1Div2Luminance,
            javax.imageio.plugins.jpeg.JPEGQTable.K2Chrominance,
            javax.imageio.plugins.jpeg.JPEGQTable.K2Div2Chrominance};
        javax.imageio.plugins.jpeg.JPEGHuffmanTable[] dc = new javax.imageio.plugins.jpeg.JPEGHuffmanTable[]{
            javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCLuminance,
            javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCChrominance};
        javax.imageio.plugins.jpeg.JPEGHuffmanTable[] ac = new javax.imageio.plugins.jpeg.JPEGHuffmanTable[]{
            javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACLuminance,
            javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACChrominance};
        p.setDecodeTables(q, dc, ac);
        check(p.areTablesSet(), "areTablesSet after setDecodeTables");
        check(p.getQTables().length == 4, "getQTables length");
        check(p.getDCHuffmanTables().length == 2, "getDCHuffmanTables length");
        check(p.getACHuffmanTables().length == 2, "getACHuffmanTables length");
        p.unsetDecodeTables();
        check(!p.areTablesSet(), "unsetDecodeTables");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
