/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageOutputStream conformance: behavioral.
 * @run main MemoryCacheImageOutputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MemoryCacheImageOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.imageio.stream.MemoryCacheImageOutputStream out = new javax.imageio.stream.MemoryCacheImageOutputStream(bos);
        out.write(1);
        out.write(new byte[]{2, 3}, 0, 2);
        out.flushBefore(out.length());
        out.close();
        check(bos.toByteArray().length == 3, "length 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
