/*
 * @test
 * @summary javax.sound.sampled.AudioFormat conformance: behavioral.
 * @run main AudioFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFormat f = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, "encoding");
        check(f.getSampleRate() == 44100f, "sampleRate");
        check(f.getSampleSizeInBits() == 16, "sampleSizeInBits");
        check(f.getChannels() == 2, "channels");
        check(f.getFrameSize() == 4, "frameSize");
        check(f.getFrameRate() == 44100f, "frameRate");
        check(!f.isBigEndian(), "bigEndian false");
        check(f.properties() != null, "properties");
        javax.sound.sampled.AudioFormat f2 = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, true);
        check(f2.getChannels() == 1 && f2.getSampleSizeInBits() == 8, "simple ctor");
        check(f.matches(f), "matches self");
        check(f2.matches(new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, true)), "matches equal");
        check(f.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
