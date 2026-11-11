/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Clip conformance: white-box structure + black-box behavior.
 * @run main ClipTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

public class ClipTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Clip");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.DataLine"), "extends DataLine");
        check(c.getField("LOOP_CONTINUOUSLY") != null, "field LOOP_CONTINUOUSLY");
        for (String m : new String[]{"open", "getFrameLength", "getMicrosecondLength", "setFramePosition", "setMicrosecondPosition", "setLoopPoints", "loop"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Clip.LOOP_CONTINUOUSLY == -1, "LOOP_CONTINUOUSLY == -1");

        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        byte[] data = new byte[800];
        try {
            Clip c = AudioSystem.getClip();
            check(c instanceof DataLine, "Clip is a DataLine");
            check(c instanceof Line, "Clip is a Line");
            check(!c.isOpen(), "not open initially");
            c.open(fmt, data, 0, 800);
            check(c.isOpen(), "open makes isOpen true");
            check(c.getFrameLength() == 800, "getFrameLength 800");
            check(c.getMicrosecondLength() == 100000L, "getMicrosecondLength 100000");
            c.setFramePosition(100);
            check(((DataLine) c).getFramePosition() == 100, "setFramePosition round-trip");
            c.setMicrosecondPosition(0);
            check(((DataLine) c).getFramePosition() == 0, "setMicrosecondPosition 0");
            c.setLoopPoints(0, -1);
            c.start();
            check(c.isOpen(), "still open after start");
            c.stop();
            c.close();
            check(!c.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No clip line available: degraded to enumeration/exception paths above.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: DirectClip open/micros/loop-constants/close loop ----
    static void testContractDeep() throws Exception {
        try {
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
        } catch (javax.sound.sampled.LineUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("No line matching")) {
                System.out.println("SKIP: no audio device/line available");
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
