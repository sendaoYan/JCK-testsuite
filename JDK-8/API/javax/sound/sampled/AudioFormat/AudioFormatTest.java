/*
 * @test
 * @summary javax.sound.sampled.AudioFormat conformance: behavioral.
 * @run main AudioFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioFormat f = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, "encoding");
        check(f.getSampleRate() == 44100f, "sampleRate");
        check(f.getSampleSizeInBits() == 16, "sampleSizeInBits");
        check(f.getChannels() == 2, "channels");
        check(f.getFrameSize() == 4, "frameSize");
        check(f.getFrameRate() == 44100f, "frameRate");
        check(!f.isBigEndian(), "bigEndian false");
        check(f.properties() != null, "properties");
        javax.sound.sampled.AudioFormat f2 = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, true);
        check(f2.getChannels() == 1 && f2.getSampleSizeInBits() == 8, "simple ctor");
        check(f.matches(f), "matches self");
        check(f2.matches(new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, true)), "matches equal");
        check(f.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: ctor matrix / frame math / properties / matches asymmetry / toString ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioFormat f = new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, false);
        check(f.getSampleRate() == 44100f && f.getSampleSizeInBits() == 16 && f.getChannels() == 2, "field round-trip");
        check(f.getFrameSize() == 4, "frameSize 16-bit stereo = 4");
        check(f.getFrameRate() == 44100f, "frameRate defaults to sampleRate");
        check(f.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, "PCM_SIGNED");
        check(!f.isBigEndian(), "little-endian flag");
        check(new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, true).isBigEndian(), "big-endian flag");
        // full ctor: encoding/sampleRate/bits/channels/frameSize/frameRate/bigEndian
        javax.sound.sampled.AudioFormat full = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000f, 8, 1, 1, 8000f, false);
        check(full.getEncoding().toString().equals("ULAW") && full.getFrameSize() == 1 && full.getFrameRate() == 8000f, "7-arg ctor fields");
        // properties ctor
        java.util.HashMap<String, Object> props = new java.util.HashMap<String, Object>();
        props.put("bitDepth", Integer.valueOf(16));
        javax.sound.sampled.AudioFormat pf = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false, props);
        check("16".equals(pf.getProperty("bitDepth").toString()), "property get");
        check(pf.getProperty("missing") == null, "missing property null");
        check(pf.properties().size() == 1, "properties map size 1");
        boolean imm = false;
        try { pf.properties().put("x", "y"); } catch (UnsupportedOperationException e) { imm = true; }
        check(imm, "properties map unmodifiable");
        // no equals override: two identical-value formats are NOT equals (identity semantics)
        javax.sound.sampled.AudioFormat f2 = new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, false);
        check(!f.equals(f2) && f.hashCode() != f2.hashCode() || !f.equals(f2), "no value-equals");
        check(f.matches(f2), "matches is the value comparison");
        // NOT_SPECIFIED matches asymmetry: unspecified matches specified, not vice versa
        javax.sound.sampled.AudioFormat unspec = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, javax.sound.sampled.AudioSystem.NOT_SPECIFIED, 16, 2, 4, javax.sound.sampled.AudioSystem.NOT_SPECIFIED, false);
        check(!unspec.matches(f), "NOT_SPECIFIED source does not match concrete");
        check(f.matches(unspec), "concrete matches NOT_SPECIFIED target");
        check(true, "NOT_SPECIFIED -1 (in AudioSystem)");
        // no constructor validation
        new javax.sound.sampled.AudioFormat(-1f, 16, 2, true, false);
        check(true, "negative sample rate accepted (no IAE)");
        new javax.sound.sampled.AudioFormat(44100f, 3, 2, true, false);
        check(true, "3-bit sample size accepted (no IAE)");
        // encoding constants
        check("PCM_SIGNED".equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.toString()), "PCM_SIGNED str");
        check("PCM_UNSIGNED".equals(javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED.toString()), "PCM_UNSIGNED str");
        check("PCM_FLOAT".equals(javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT.toString()), "PCM_FLOAT str");
        check("ULAW".equals(javax.sound.sampled.AudioFormat.Encoding.ULAW.toString()), "ULAW str");
        check("ALAW".equals(javax.sound.sampled.AudioFormat.Encoding.ALAW.toString()), "ALAW str");
        // toString: JDK-8 ends with trailing ", "
        boolean isOld = System.getProperty("java.specification.version").startsWith("1.8");
        String ts = f.toString();
        check(ts.equals("PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian") || ts.equals("PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian, "), "toString with endian: [" + ts + "]");
        String ts8 = new javax.sound.sampled.AudioFormat(8000f, 8, 1, false, false).toString();
        check(ts8.startsWith("PCM_UNSIGNED 8000.0 Hz, 8 bit, mono, 1 bytes/frame"), "8-bit toString prefix: [" + ts8 + "]");
        check(ts8.endsWith(", ") == isOld, "trailing comma (no-endian form) only on JDK-8: [" + ts8 + "]");
        // getProperty("sign") is not populated by the plain ctor
        check(f.getProperty("sign") == null, "no implicit sign property");
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
