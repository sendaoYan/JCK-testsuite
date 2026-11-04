/*
 * @test
 * @summary javax.imageio.stream.FileImageOutputStream conformance: behavioral.
 * @run main FileImageOutputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FileImageOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File f = java.io.File.createTempFile("jck", ".img");
        javax.imageio.stream.FileImageOutputStream out = new javax.imageio.stream.FileImageOutputStream(f);
        out.write(1);
        out.write(new byte[]{2, 3}, 0, 2);
        check(out.length() == 3, "length");
        out.seek(0);
        check(out.read() == 1, "read after seek");
        out.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
