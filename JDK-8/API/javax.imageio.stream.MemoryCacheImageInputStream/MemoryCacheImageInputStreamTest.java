/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageInputStream conformance: behavioral.
 * @run main MemoryCacheImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MemoryCacheImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] data = new byte[]{7, 8, 9};
        javax.imageio.stream.MemoryCacheImageInputStream in = new javax.imageio.stream.MemoryCacheImageInputStream(new java.io.ByteArrayInputStream(data));
        check(in.read() == 7, "read");
        check(in.read() == 8, "read2");
        check(in.isCached(), "isCached");
        check(in.isCachedMemory(), "isCachedMemory");
        check(!in.isCachedFile(), "not isCachedFile");
        in.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
