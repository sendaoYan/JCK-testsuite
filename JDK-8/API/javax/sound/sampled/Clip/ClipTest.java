/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Clip conformance: interface hierarchy and methods.
 * @run main ClipTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ClipTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Clip");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.DataLine"), "extends javax.sound.sampled.DataLine");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("open"), "method open");
        check(dm.contains("open"), "method open");
        check(dm.contains("getFrameLength"), "method getFrameLength");
        check(dm.contains("getMicrosecondLength"), "method getMicrosecondLength");
        check(dm.contains("setFramePosition"), "method setFramePosition");
        check(dm.contains("setMicrosecondPosition"), "method setMicrosecondPosition");
        check(dm.contains("setLoopPoints"), "method setLoopPoints");
        check(dm.contains("loop"), "method loop");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: DirectClip open/micros/loop-constants/close loop ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, fmt));
        check(!clip.isOpen(), "closed initially");
        clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), fmt, 2));
        check(clip.isOpen(), "open with AudioInputStream");
        check(clip.getFrameLength() == 2, "frame length 2");
        check(clip.getMicrosecondLength() == 250, "2 frames @8000Hz = 250 micros");
        check(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY == -1, "LOOP_CONTINUOUSLY -1");
        clip.setFramePosition(0);
        check(clip.getFramePosition() == 0, "position 0");
        clip.setMicrosecondPosition(0);
        clip.loop(0);
        clip.setLoopPoints(0, 1);
        check(true, "loop points set");
        clip.stop();
        clip.flush();
        clip.close();
        check(!clip.isOpen(), "closed");
        // reopen after close works
        clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), fmt, 2));
        check(clip.isOpen(), "reopen after close");
        clip.close();
        // byte-array open overload
        clip.open(fmt, new byte[] { 16, -16, 32, -32 }, 0, 4);
        check(clip.isOpen() && clip.getFrameLength() == 4, "byte-array open overload");
        clip.close();
        javax.sound.sampled.DataLine.Info bi = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, fmt);
        check(bi.getLineClass() == javax.sound.sampled.Clip.class, "info class");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // hand-built WAV: RIFF/WAVE, PCM 8000Hz 8-bit mono, samples {16,-16}, 44-byte header
    static byte[] wav8() throws java.io.IOException {
        java.io.ByteArrayOutputStream w = new java.io.ByteArrayOutputStream();
        byte[] pcm = { 0x10, (byte) 0xF0 };
        w.write('R'); w.write('I'); w.write('F'); w.write('F');
        int riffLen = 36 + pcm.length;
        w.write(riffLen & 0xFF); w.write((riffLen >> 8) & 0xFF); w.write((riffLen >> 16) & 0xFF); w.write((riffLen >> 24) & 0xFF);
        w.write('W'); w.write('A'); w.write('V'); w.write('E');
        w.write('f'); w.write('m'); w.write('t'); w.write(' ');
        w.write(16); w.write(0); w.write(0); w.write(0);
        w.write(1); w.write(0); w.write(1); w.write(0);
        int rate = 8000; w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(1); w.write(0); w.write(8); w.write(0);
        w.write('d'); w.write('a'); w.write('t'); w.write('a');
        w.write(pcm.length & 0xFF); w.write(0); w.write(0); w.write(0);
        w.write(pcm);
        return w.toByteArray();
    }
    // hand-built AU (.snd, big-endian header): encoding 1 = 8-bit mu-law, 8000Hz mono, 3 data bytes
    static byte[] au(int sizeField, int enc, int rate, int ch, byte[] data) throws java.io.IOException {
        java.io.ByteArrayOutputStream u = new java.io.ByteArrayOutputStream();
        u.write('.'); u.write('s'); u.write('n'); u.write('d');
        u.write((sizeField >> 24) & 0xFF); u.write((sizeField >> 16) & 0xFF); u.write((sizeField >> 8) & 0xFF); u.write(sizeField & 0xFF);
        u.write(0); u.write(0); u.write(0); u.write(24);
        u.write((enc >> 24) & 0xFF); u.write((enc >> 16) & 0xFF); u.write((enc >> 8) & 0xFF); u.write(enc & 0xFF);
        u.write((rate >> 24) & 0xFF); u.write((rate >> 16) & 0xFF); u.write((rate >> 8) & 0xFF); u.write(rate & 0xFF);
        u.write((ch >> 24) & 0xFF); u.write((ch >> 16) & 0xFF); u.write((ch >> 8) & 0xFF); u.write(ch & 0xFF);
        u.write(data);
        return u.toByteArray();
    }
    static byte[] auUlaw() throws java.io.IOException { return au(27, 1, 8000, 1, new byte[] { 0, (byte) 0xFF, 0x7F }); }

}
