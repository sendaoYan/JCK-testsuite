/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Control conformance: abstract hierarchy and methods.
 * @library /lib
 * @run main ControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Control");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getType"), "method getType");
        check(dm.contains("toString"), "method toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Type surface + real controls from software Clip ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Control.Type t = new javax.sound.sampled.Control.Type("DeepType") {};
        check("DeepType".equals(t.toString()), "toString is the name");
        check(t.equals(t), "Type self-equals");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.sampled.Control.Type.class.getDeclaredConstructors()[0].getModifiers()), "Type ctor protected");
        boolean hasGetName = false;
        for (java.lang.reflect.Method m : javax.sound.sampled.Control.Type.class.getDeclaredMethods()) if (m.getName().equals("getName")) hasGetName = true;
        check(!hasGetName, "no public getName (toString only)");
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.sampled.Control.class.getModifiers()), "Control abstract");
        // real controls from software clip
        try {
            javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
            clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2));
            javax.sound.sampled.Control[] ctrls = clip.getControls();
            check(ctrls.length == 2, "2 controls (Master Gain, Mute)");
            boolean gain = false, mute = false;
            for (javax.sound.sampled.Control c : ctrls) {
                if (c.getType() == javax.sound.sampled.FloatControl.Type.MASTER_GAIN) gain = true;
                if (c.getType() == javax.sound.sampled.BooleanControl.Type.MUTE) mute = true;
            }
            check(gain && mute, "MASTER_GAIN + MUTE");
            check(clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN), "isControlSupported true");
            check(!clip.isControlSupported(new javax.sound.sampled.Control.Type("Nope") {}), "unknown control false");
            check(clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN) != null, "getControl");
            try { clip.getControl(new javax.sound.sampled.Control.Type("Nope") {}); check(false, "getControl unknown must fail"); }
            catch (IllegalArgumentException e) { passed++; }
            clip.close();
        } catch (javax.sound.sampled.LineUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("No line matching")) {
                throw new jtreg.SkippedException("no audio device/line available");
            } else {
                throw e;
            }
        }
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
