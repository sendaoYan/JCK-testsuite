/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.DataLine conformance: white-box structure + black-box behavior.
 * @run main DataLineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class DataLineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.DataLine");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        for (String m : new String[]{"drain", "flush", "start", "stop", "isRunning", "isActive", "getFormat", "getBufferSize", "available", "getFramePosition", "getLongFramePosition", "getMicrosecondPosition", "getLevel"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        try {
            SourceDataLine sdl = AudioSystem.getSourceDataLine(fmt);
            DataLine dl = sdl;
            check(dl instanceof Line, "DataLine is a Line");
            check(dl.getFormat().matches(fmt), "getFormat matches");
            sdl.open(fmt, 1024);
            check(dl.getBufferSize() > 0, "getBufferSize positive");
            check(dl.available() >= 0, "available non-negative");
            check(dl.getFramePosition() >= 0, "getFramePosition non-negative");
            check(dl.getLongFramePosition() >= 0L, "getLongFramePosition non-negative");
            check(dl.getMicrosecondPosition() >= 0L, "getMicrosecondPosition non-negative");
            float level = dl.getLevel();
            check(level == -1.0f || (level >= 0.0f && level <= 1.0f), "getLevel in [-1,1]");
            dl.start();
            dl.isRunning();
            dl.isActive();
            dl.flush();
            dl.stop();
            dl.drain();
            dl.close();
            check(!dl.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No line available: degraded to enumeration paths.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Info ctor matrix / formats array / matching ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.DataLine.Info di = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, fmt);
        check(di.getLineClass() == javax.sound.sampled.Clip.class, "class");
        check(di.getFormats().length == 1 && di.getFormats()[0].getSampleRate() == 8000f, "formats passthrough");
        check(di.getMinBufferSize() == javax.sound.sampled.AudioSystem.NOT_SPECIFIED, "single-format min buffer NOT_SPECIFIED");
        javax.sound.sampled.DataLine.Info multi = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, new javax.sound.sampled.AudioFormat[] { fmt, new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, false) }, 1000, 4000);
        check(multi.getFormats().length == 2, "2 formats");
        check(multi.getMinBufferSize() == 1000 && multi.getMaxBufferSize() == 4000, "buffer bounds");
        check(!multi.matches(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, fmt)), "Info.matches is exact (subset false)");
        check(multi.matches(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, new javax.sound.sampled.AudioFormat[] { fmt, new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, false) }, 1000, 4000)), "identical Info matches");
        check(!multi.matches(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, fmt)), "class mismatch");
        javax.sound.sampled.DataLine.Info any = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat[0], 0, Integer.MAX_VALUE);
        check(any.getFormats().length == 0, "empty formats array legal");
        boolean hasFmtOverload = false;
        for (java.lang.reflect.Method m : javax.sound.sampled.DataLine.Info.class.getMethods()) if (m.getName().equals("isSupported") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == javax.sound.sampled.AudioFormat.class) hasFmtOverload = true;
        check(!hasFmtOverload, "no isSupported(AudioFormat) overload");
        check(javax.sound.sampled.DataLine.class.isInterface(), "interface");
        check(javax.sound.sampled.Line.class.isAssignableFrom(javax.sound.sampled.DataLine.class), "extends Line");
        // real data line read/write surface
        try {
            javax.sound.sampled.SourceDataLine sdl = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, fmt));
            sdl.open(fmt);
            check(sdl.available() >= 0, "available");
            sdl.close();
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
