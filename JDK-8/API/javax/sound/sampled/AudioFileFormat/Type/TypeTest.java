/*
 * @test
 * @summary javax.sound.sampled.AudioFileFormat.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFileFormat.Type t = new javax.sound.sampled.AudioFileFormat.Type("TEST", "tst");
        check("tst".equals(t.getExtension()), "getExtension");
        check("TEST".equals(t.toString()), "toString");
        check(javax.sound.sampled.AudioFileFormat.Type.WAVE != null, "WAVE");
        check(javax.sound.sampled.AudioFileFormat.Type.AU != null, "AU");
        check(javax.sound.sampled.AudioFileFormat.Type.AIFF != null, "AIFF");
        check(javax.sound.sampled.AudioFileFormat.Type.AIFC != null, "AIFC");
        check(javax.sound.sampled.AudioFileFormat.Type.SND != null, "SND");
        check(t.equals(t), "equals");
        check(t.hashCode() == t.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
