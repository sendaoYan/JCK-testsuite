/*
 * @test
 * @summary javax.sound.sampled.AudioFormat.Encoding conformance: behavioral.
 * @run main EncodingTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class EncodingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFormat.Encoding e = new javax.sound.sampled.AudioFormat.Encoding("CUSTOM");
        check("CUSTOM".equals(e.toString()), "toString");
        check(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED != null, "PCM_SIGNED");
        check(javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED != null, "PCM_UNSIGNED");
        check(javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT != null, "PCM_FLOAT");
        check(javax.sound.sampled.AudioFormat.Encoding.ULAW != null, "ULAW");
        check(javax.sound.sampled.AudioFormat.Encoding.ALAW != null, "ALAW");
        check(e.equals(e), "equals");
        check(e.hashCode() == e.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
