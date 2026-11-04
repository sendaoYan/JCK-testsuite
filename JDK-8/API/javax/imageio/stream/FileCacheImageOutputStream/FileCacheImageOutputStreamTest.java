/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageOutputStream conformance: behavioral.
 * @run main FileCacheImageOutputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FileCacheImageOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File cache = new java.io.File(System.getProperty("java.io.tmpdir"), "jckcache" + System.nanoTime());
        cache.mkdir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.imageio.stream.FileCacheImageOutputStream out = new javax.imageio.stream.FileCacheImageOutputStream(bos, cache);
        out.write(65);
        out.write(new byte[]{66, 67}, 0, 2);
        out.writeInt(0x01020304);
        out.close();
        check(bos.toByteArray().length == 7, "wrote 7 bytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
