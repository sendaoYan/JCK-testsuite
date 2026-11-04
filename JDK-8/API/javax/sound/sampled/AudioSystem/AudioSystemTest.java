/*
 * @test
 * @summary javax.sound.sampled.AudioSystem conformance: behavioral.
 * @run main AudioSystemTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFileFormat.Type[] types = javax.sound.sampled.AudioSystem.getAudioFileTypes();
        check(types != null && types.length > 0, "getAudioFileTypes non-empty");
        check(javax.sound.sampled.AudioSystem.isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type.WAVE), "WAVE supported");
        byte[] data = new byte[800];
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(data), fmt, data.length);
        java.io.File f = java.io.File.createTempFile("jck", ".wav");
        int written = javax.sound.sampled.AudioSystem.write(ais, javax.sound.sampled.AudioFileFormat.Type.WAVE, f);
        check(written > 0, "write wav");
        javax.sound.sampled.AudioFileFormat aff = javax.sound.sampled.AudioSystem.getAudioFileFormat(f);
        check(aff.getType().equals(javax.sound.sampled.AudioFileFormat.Type.WAVE), "getAudioFileFormat WAVE");
        check(aff.getFormat() != null, "format non-null");
        javax.sound.sampled.AudioInputStream back = javax.sound.sampled.AudioSystem.getAudioInputStream(f);
        check(back != null && back.getFrameLength() > 0, "getAudioInputStream");
        back.close();
        javax.sound.sampled.Mixer.Info[] mixers = javax.sound.sampled.AudioSystem.getMixerInfo();
        check(mixers != null, "getMixerInfo non-null");
        check(javax.sound.sampled.AudioSystem.NOT_SPECIFIED == -1, "NOT_SPECIFIED -1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
