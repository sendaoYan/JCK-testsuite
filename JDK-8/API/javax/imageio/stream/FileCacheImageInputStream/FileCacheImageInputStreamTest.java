/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageInputStream conformance: behavioral.
 * @run main FileCacheImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FileCacheImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File cache = new java.io.File(System.getProperty("java.io.tmpdir"), "jckcache" + System.nanoTime());
        cache.mkdir();
        byte[] data = new byte[]{1, 2, 3, 4};
        javax.imageio.stream.FileCacheImageInputStream in = new javax.imageio.stream.FileCacheImageInputStream(new java.io.ByteArrayInputStream(data), cache);
        check(in.read() == 1, "read");
        byte[] buf = new byte[3];
        check(in.read(buf, 0, 3) == 3, "read buf");
        check(buf[0] == 2 && buf[2] == 4, "buf values");
        in.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
