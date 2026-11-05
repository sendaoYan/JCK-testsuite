/*
 * @test
 * @summary javax.sound.sampled.spi.FormatConversionProvider conformance: abstract hierarchy and methods.
 * @run main FormatConversionProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FormatConversionProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.FormatConversionProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getSourceEncodings"), "method getSourceEncodings");
        check(dm.contains("getTargetEncodings"), "method getTargetEncodings");
        check(dm.contains("isSourceEncodingSupported"), "method isSourceEncodingSupported");
        check(dm.contains("isTargetEncodingSupported"), "method isTargetEncodingSupported");
        check(dm.contains("getTargetEncodings"), "method getTargetEncodings");
        check(dm.contains("isConversionSupported"), "method isConversionSupported");
        check(dm.contains("getTargetFormats"), "method getTargetFormats");
        check(dm.contains("isConversionSupported"), "method isConversionSupported");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.sampled.spi.FormatConversionProvider.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.sampled.spi.FormatConversionProvider.class.getModifiers()), "public");
        
        javax.sound.sampled.AudioFormat src = new javax.sound.sampled.AudioFormat(8000f, 16, 1, true, false);
        javax.sound.sampled.AudioFormat ulaw = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000f, 8, 1, 1, 8000f, false);
        RecConv c = new RecConv();
        javax.sound.sampled.AudioFormat.Encoding[] encs = c.getTargetEncodings(src);
        check(encs.length == 1 && encs[0] == javax.sound.sampled.AudioFormat.Encoding.ULAW, "recording converter encodings");
        check(c.isConversionSupported(javax.sound.sampled.AudioFormat.Encoding.ULAW, src), "conversion supported");
        javax.sound.sampled.AudioFormat[] tf = c.getTargetFormats(javax.sound.sampled.AudioFormat.Encoding.ULAW, src);
        check(tf.length == 1 && tf[0].getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW, "target formats");
        byte[] pcm = new byte[2048];
        javax.sound.sampled.AudioInputStream conv = c.getAudioInputStream(ulaw, new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm), src, 1024));
        check(conv.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW, "converted stream");
    }
    static class RecConv extends javax.sound.sampled.spi.FormatConversionProvider {
        final javax.sound.sampled.AudioFormat ulaw = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000f, 8, 1, 1, 8000f, false);
        public javax.sound.sampled.AudioFormat.Encoding[] getSourceEncodings() { return new javax.sound.sampled.AudioFormat.Encoding[] { javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED }; }
        public javax.sound.sampled.AudioFormat.Encoding[] getTargetEncodings() { return new javax.sound.sampled.AudioFormat.Encoding[] { javax.sound.sampled.AudioFormat.Encoding.ULAW }; }
        public javax.sound.sampled.AudioFormat.Encoding[] getTargetEncodings(javax.sound.sampled.AudioFormat src) { return new javax.sound.sampled.AudioFormat.Encoding[] { javax.sound.sampled.AudioFormat.Encoding.ULAW }; }
        public javax.sound.sampled.AudioFormat[] getTargetFormats(javax.sound.sampled.AudioFormat.Encoding enc, javax.sound.sampled.AudioFormat src) { return new javax.sound.sampled.AudioFormat[] { ulaw }; }
        public boolean isConversionSupported(javax.sound.sampled.AudioFormat.Encoding enc, javax.sound.sampled.AudioFormat src) { return enc == javax.sound.sampled.AudioFormat.Encoding.ULAW; }
        public boolean isConversionSupported(javax.sound.sampled.AudioFormat tgt, javax.sound.sampled.AudioFormat src) { return tgt.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW; }
        public javax.sound.sampled.AudioInputStream getAudioInputStream(javax.sound.sampled.AudioFormat tgt, javax.sound.sampled.AudioInputStream src) { return javax.sound.sampled.AudioSystem.getAudioInputStream(tgt, src); }
        public javax.sound.sampled.AudioInputStream getAudioInputStream(javax.sound.sampled.AudioFormat.Encoding enc, javax.sound.sampled.AudioInputStream src) { return javax.sound.sampled.AudioSystem.getAudioInputStream(new javax.sound.sampled.AudioFormat(enc, 8000f, 8, 1, 1, 8000f, false), src); }
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
