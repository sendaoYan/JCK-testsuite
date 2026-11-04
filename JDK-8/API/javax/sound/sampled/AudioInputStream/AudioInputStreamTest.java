/*
 * @test
 * @summary javax.sound.sampled.AudioInputStream conformance: behavioral.
 * @run main AudioInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] data = new byte[400];
        javax.sound.sampled.AudioFormat f = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(data), f, 400);
        check(ais.getFormat() == f, "format");
        check(ais.getFrameLength() == 400, "frameLength");
        check(ais.read() != -1, "read");
        byte[] buf = new byte[100];
        check(ais.read(buf) > 0, "read buf");
        check(ais.available() >= 0, "available");
        check(ais.markSupported(), "markSupported");
        ais.mark(0);
        ais.reset();
        ais.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
