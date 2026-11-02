/*
 * @test
 * @summary javax.sound.sampled.AudioFileFormat conformance: behavioral.
 * @run main AudioFileFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioFileFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 16, 1, true, false);
        javax.sound.sampled.AudioFileFormat aff = new javax.sound.sampled.AudioFileFormat(javax.sound.sampled.AudioFileFormat.Type.WAVE, fmt, 100);
        check(aff.getType().equals(javax.sound.sampled.AudioFileFormat.Type.WAVE), "type");
        check(aff.getFormat() == fmt, "format");
        check(aff.getFrameLength() == 100, "frameLength");
        check(aff.getByteLength() == javax.sound.sampled.AudioSystem.NOT_SPECIFIED, "byteLength NOT_SPECIFIED");
        check(aff.properties() != null, "properties");
        check(aff.toString() != null, "toString");
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("k", "v");
        javax.sound.sampled.AudioFileFormat aff2 = new javax.sound.sampled.AudioFileFormat(javax.sound.sampled.AudioFileFormat.Type.WAVE, fmt, 100, props);
        check("v".equals(aff2.getProperty("k")), "getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
