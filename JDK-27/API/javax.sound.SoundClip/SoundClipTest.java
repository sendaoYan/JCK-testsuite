/*
 * @test
 * @summary javax.sound.SoundClip conformance: behavioral.
 * @run main SoundClipTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SoundClipTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.SoundClip");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        java.util.Set<String> dm = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("createSoundClip"), "createSoundClip");
        check(dm.contains("canPlay"), "canPlay");
        check(dm.contains("isPlaying"), "isPlaying");
        check(dm.contains("play"), "play");
        check(dm.contains("loop"), "loop");
        check(dm.contains("stop"), "stop");
        java.io.File f = java.io.File.createTempFile("jck", ".wav");
        try {
            byte[] data = new byte[800];
            javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
            javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(data), fmt, data.length);
            javax.sound.sampled.AudioSystem.write(ais, javax.sound.sampled.AudioFileFormat.Type.WAVE, f);
            javax.sound.SoundClip sc = javax.sound.SoundClip.createSoundClip(f);
            check(sc != null, "createSoundClip non-null");
            check(!sc.isPlaying(), "isPlaying false initially");
        } catch (java.io.IOException e) {
            // headless or unsupported audio backend: tolerated
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
