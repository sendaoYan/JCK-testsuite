/*
 * @test
 * @summary javax.imageio.stream.FileImageInputStream conformance: behavioral.
 * @run main FileImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FileImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File f = java.io.File.createTempFile("jck", ".img");
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(new byte[]{10, 20, 30});
        fos.close();
        javax.imageio.stream.FileImageInputStream in = new javax.imageio.stream.FileImageInputStream(f);
        check(in.length() == 3, "length");
        check(in.read() == 10, "read");
        in.seek(0);
        check(in.read() == 10, "read after seek");
        in.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
