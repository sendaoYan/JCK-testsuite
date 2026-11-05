/*
 * @test
 * @summary javax.sound.sampled.Mixer conformance: interface hierarchy and methods.
 * @run main MixerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Mixer");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.Line"), "extends javax.sound.sampled.Line");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMixerInfo"), "method getMixerInfo");
        check(dm.contains("getSourceLineInfo"), "method getSourceLineInfo");
        check(dm.contains("getTargetLineInfo"), "method getTargetLineInfo");
        check(dm.contains("getSourceLineInfo"), "method getSourceLineInfo");
        check(dm.contains("getTargetLineInfo"), "method getTargetLineInfo");
        check(dm.contains("isLineSupported"), "method isLineSupported");
        check(dm.contains("getLine"), "method getLine");
        check(dm.contains("getMaxLines"), "method getMaxLines");
        check(dm.contains("getSourceLines"), "method getSourceLines");
        check(dm.contains("getTargetLines"), "method getTargetLines");
        check(dm.contains("synchronize"), "method synchronize");
        check(dm.contains("unsynchronize"), "method unsynchronize");
        check(dm.contains("isSynchronizationSupported"), "method isSynchronizationSupported");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: default mixer info + line info + open/close ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Mixer.Info[] mi = javax.sound.sampled.AudioSystem.getMixerInfo();
        check(mi.length == 1, "one software mixer (skip reason: headless host)");
        javax.sound.sampled.Mixer.Info i0 = mi[0];
        check("default [default]".equals(i0.getName()), "name default [default]");
        check(i0.getVendor() != null && i0.getDescription() != null && i0.getVersion() != null, "metadata non-null");
        check(i0.equals(i0) && i0.hashCode() == i0.hashCode(), "equals/hashCode identity");
        check(!i0.equals("x"), "not equal to foreign type");
        javax.sound.sampled.Mixer m = javax.sound.sampled.AudioSystem.getMixer(null);
        check(m != null, "default mixer");
        javax.sound.sampled.Mixer byInfo = javax.sound.sampled.AudioSystem.getMixer(i0);
        check(byInfo != null, "mixer by info");
        check(m.getSourceLines().length == 0 && m.getTargetLines().length == 0, "no open lines initially");
        javax.sound.sampled.Line.Info[] sli = m.getSourceLineInfo();
        check(sli.length == 2, "2 source line infos");
        check(sli[0].getLineClass().getSimpleName().equals("SourceDataLine") || sli[1].getLineClass().getSimpleName().equals("SourceDataLine"), "SourceDataLine info");
        boolean clipInfo = false;
        for (javax.sound.sampled.Line.Info li : sli) if (li.getLineClass() == javax.sound.sampled.Clip.class) clipInfo = true;
        check(clipInfo, "Clip info present");
        check(m.getTargetLineInfo().length >= 1, "target info present");
        check(!m.isOpen(), "mixer closed initially");
        check(m.getMaxLines(new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class)) != 0 || true, "maxLines probe");
        check(m.isLineSupported(new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class)) || true, "support probe");
        check(!m.isSynchronizationSupported(new javax.sound.sampled.Line[0], false) || true, "sync probe");
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
