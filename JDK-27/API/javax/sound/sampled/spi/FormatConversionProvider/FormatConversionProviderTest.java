/*
 * @test
 * @summary javax.sound.sampled.spi.FormatConversionProvider conformance: white-box structure + black-box behavior.
 * @run main FormatConversionProviderTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class FormatConversionProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.FormatConversionProvider");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("getSourceEncodings").getModifiers()), "getSourceEncodings abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetEncodings").getModifiers()), "getTargetEncodings() abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetEncodings", AudioFormat.class).getModifiers()), "getTargetEncodings(format) abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetFormats", AudioFormat.Encoding.class, AudioFormat.class).getModifiers()), "getTargetFormats abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", AudioFormat.Encoding.class, AudioInputStream.class).getModifiers()), "getAudioInputStream(encoding) abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", AudioFormat.class, AudioInputStream.class).getModifiers()), "getAudioInputStream(format) abstract");
        check(!Modifier.isAbstract(c.getMethod("isSourceEncodingSupported", AudioFormat.Encoding.class).getModifiers()), "isSourceEncodingSupported concrete");
        check(!Modifier.isAbstract(c.getMethod("isTargetEncodingSupported", AudioFormat.Encoding.class).getModifiers()), "isTargetEncodingSupported concrete");
        check(!Modifier.isAbstract(c.getMethod("isConversionSupported", AudioFormat.Encoding.class, AudioFormat.class).getModifiers()), "isConversionSupported(encoding,format) concrete");
        check(!Modifier.isAbstract(c.getMethod("isConversionSupported", AudioFormat.class, AudioFormat.class).getModifiers()), "isConversionSupported(format,format) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);

        // Concrete convenience methods on an anonymous subclass with no encodings.
        FormatConversionProviderImpl p = new FormatConversionProviderImpl();
        check(!p.isSourceEncodingSupported(AudioFormat.Encoding.PCM_SIGNED), "empty provider: source encoding unsupported");
        check(!p.isTargetEncodingSupported(AudioFormat.Encoding.PCM_SIGNED), "empty provider: target encoding unsupported");
        check(!p.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, fmt), "empty provider: conversion (encoding,format) unsupported");
        check(!p.isConversionSupported(fmt, fmt), "empty provider: conversion (format,format) unsupported");

        // Real conversion providers via AudioSystem public API.
        check(AudioSystem.getTargetEncodings(AudioFormat.Encoding.PCM_SIGNED) != null, "getTargetEncodings(encoding) non-null");
        check(AudioSystem.getTargetEncodings(fmt) != null, "getTargetEncodings(format) non-null");
        AudioFormat[] targetFormats = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, fmt);
        check(targetFormats != null, "getTargetFormats non-null");
        boolean conversionSupported = AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, fmt);
        check(conversionSupported == (targetFormats.length > 0), "isConversionSupported consistent with getTargetFormats");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

class FormatConversionProviderImpl extends javax.sound.sampled.spi.FormatConversionProvider {
    public AudioFormat.Encoding[] getSourceEncodings() { return new AudioFormat.Encoding[0]; }
    public AudioFormat.Encoding[] getTargetEncodings() { return new AudioFormat.Encoding[0]; }
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat f) { return new AudioFormat.Encoding[0]; }
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding e, AudioFormat f) { return new AudioFormat[0]; }
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding e, AudioInputStream s) { return null; }
    public AudioInputStream getAudioInputStream(AudioFormat f, AudioInputStream s) { return null; }
}
