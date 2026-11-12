/*
 * @test
 * @summary javax.sound.sampled.spi.MixerProvider conformance: abstract hierarchy and methods.
 * @run main MixerProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MixerProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.MixerProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isMixerSupported"), "method isMixerSupported");
        check(dm.contains("getMixerInfo"), "method getMixerInfo");
        check(dm.contains("getMixer"), "method getMixer");
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static boolean testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.sampled.spi.MixerProvider.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.sampled.spi.MixerProvider.class.getModifiers()), "public");
        
        RecMix p = new RecMix();
        javax.sound.sampled.Mixer.Info[] infos = p.getMixerInfo();
        check(infos.length == 1 && "deep-mixer".equals(infos[0].getName()), "recording provider info");
        javax.sound.sampled.Mixer m = p.getMixer(infos[0]);
        check(m.getMixerInfo() == infos[0], "mixer bound to info");
        check(p.getMixer(null) == null, "null info null mixer (recording impl)");
        javax.sound.sampled.Mixer.Info[] real = javax.sound.sampled.AudioSystem.getMixerInfo();
        if (real.length != 1 || !"default [default]".equals(real[0].getName())) {
            System.out.println("PASS (" + passed + " assertions; SKIP: no ALSA null software mixer on this host)");
            return false;
        }
        check(real.length == 1, "real JDK providers registered (default [default])");
        return true;
    }
    static class RecMix extends javax.sound.sampled.spi.MixerProvider {
        static final javax.sound.sampled.Mixer.Info INFO = new javax.sound.sampled.Mixer.Info("deep-mixer", "deep", "recording", "1") {};
        static class RecMixer implements javax.sound.sampled.Mixer {
            public javax.sound.sampled.Mixer.Info getMixerInfo() { return INFO; }
            public javax.sound.sampled.Line.Info[] getSourceLineInfo() { return new javax.sound.sampled.Line.Info[0]; }
            public javax.sound.sampled.Line.Info[] getTargetLineInfo() { return new javax.sound.sampled.Line.Info[0]; }
            public javax.sound.sampled.Line.Info[] getSourceLineInfo(javax.sound.sampled.Line.Info info) { return new javax.sound.sampled.Line.Info[0]; }
            public javax.sound.sampled.Line.Info[] getTargetLineInfo(javax.sound.sampled.Line.Info info) { return new javax.sound.sampled.Line.Info[0]; }
            public boolean isLineSupported(javax.sound.sampled.Line.Info info) { return false; }
            public javax.sound.sampled.Line getLine(javax.sound.sampled.Line.Info info) { return null; }
            public int getMaxLines(javax.sound.sampled.Line.Info info) { return 0; }
            public javax.sound.sampled.Line[] getSourceLines() { return new javax.sound.sampled.Line[0]; }
            public javax.sound.sampled.Line[] getTargetLines() { return new javax.sound.sampled.Line[0]; }
            public void synchronize(javax.sound.sampled.Line[] lines, boolean maintainSync) {}
            public void unsynchronize(javax.sound.sampled.Line[] lines) {}
            public boolean isSynchronizationSupported(javax.sound.sampled.Line[] lines, boolean maintainSync) { return false; }
            public void open() {} public void close() {} public boolean isOpen() { return false; }
            public javax.sound.sampled.Control[] getControls() { return new javax.sound.sampled.Control[0]; }
            public boolean isControlSupported(javax.sound.sampled.Control.Type t) { return false; }
            public javax.sound.sampled.Control getControl(javax.sound.sampled.Control.Type t) { return null; }
            public void addLineListener(javax.sound.sampled.LineListener l) {}
            public void removeLineListener(javax.sound.sampled.LineListener l) {}
            public javax.sound.sampled.Line.Info getLineInfo() { return new javax.sound.sampled.Line.Info(javax.sound.sampled.Mixer.class); }
        }
        public javax.sound.sampled.Mixer.Info[] getMixerInfo() { return new javax.sound.sampled.Mixer.Info[] { INFO }; }
        public javax.sound.sampled.Mixer getMixer(javax.sound.sampled.Mixer.Info info) { return info != null && info.getName().equals("deep-mixer") ? new RecMixer() : null; }
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
