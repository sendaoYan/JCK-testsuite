/*
 * @test
 * @summary javax.sound.sampled.DataLine.Info conformance: behavioral.
 * @run main InfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, new javax.sound.sampled.AudioFormat[]{fmt}, 1024, 4096);
        check(info.getLineClass() == javax.sound.sampled.SourceDataLine.class, "lineClass");
        check(info.getFormats().length == 1, "formats");
        check(info.isFormatSupported(fmt), "isFormatSupported");
        check(info.getMinBufferSize() == 1024, "minBufferSize");
        check(info.getMaxBufferSize() == 4096, "maxBufferSize");
        check(info.matches(info), "matches");
        check(info.toString() != null, "toString");
        javax.sound.sampled.DataLine.Info info2 = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, fmt);
        check(info2.getMinBufferSize() == javax.sound.sampled.AudioSystem.NOT_SPECIFIED, "min NOT_SPECIFIED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
