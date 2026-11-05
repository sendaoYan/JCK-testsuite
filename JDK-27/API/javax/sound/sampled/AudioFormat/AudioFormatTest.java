/*
 * @test
 * @summary javax.sound.sampled.AudioFormat conformance: white-box structure + black-box behavior.
 * @run main AudioFormatTest
 */

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 3, "constructor count 3");

        Class<?> e = Class.forName("javax.sound.sampled.AudioFormat$Encoding");
        check(e != null && e.getEnclosingClass() == c, "Encoding nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 7-arg explicit constructor
        AudioFormat f = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, "encoding");
        check(f.getSampleRate() == 44100f, "sampleRate");
        check(f.getSampleSizeInBits() == 16, "sampleSizeInBits");
        check(f.getChannels() == 2, "channels");
        check(f.getFrameSize() == 4, "frameSize");
        check(f.getFrameRate() == 44100f, "frameRate");
        check(!f.isBigEndian(), "bigEndian false");
        check(f.properties().isEmpty(), "default properties empty");
        check(f.getProperty("bitrate") == null, "default property null");

        // PCM convenience constructor derives frameSize/frameRate/encoding
        AudioFormat pcm = new AudioFormat(22050f, 16, 1, true, true);
        check(pcm.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, "PCM signed encoding");
        check(pcm.getSampleRate() == 22050f, "PCM sampleRate");
        check(pcm.getChannels() == 1, "PCM channels");
        check(pcm.getFrameSize() == 2, "PCM frameSize = ceil(16/8)*1");
        check(pcm.getFrameRate() == 22050f, "PCM frameRate == sampleRate");
        check(pcm.isBigEndian(), "PCM bigEndian true");

        AudioFormat upcm = new AudioFormat(8000f, 8, 1, false, false);
        check(upcm.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED, "PCM unsigned encoding");
        check(upcm.getFrameSize() == 1, "8-bit frameSize 1");

        // 8-arg constructor with properties
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("bitrate", Integer.valueOf(128000));
        AudioFormat fp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false, props);
        check(Integer.valueOf(128000).equals(fp.getProperty("bitrate")), "property bitrate");
        check(fp.properties().size() == 1, "properties size 1");

        // matches: equal formats match, properties are ignored
        AudioFormat same = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.matches(same), "identical formats match");
        AudioFormat sameProps = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false, props);
        check(f.matches(sameProps), "properties ignored in matches");

        // NOT_SPECIFIED wildcards match concrete values
        AudioFormat wild = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
        check(f.matches(wild), "NOT_SPECIFIED wildcard matches");

        // differing concrete values do not match
        AudioFormat otherRate = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050f, 16, 2, 4, 44100f, false);
        check(!f.matches(otherRate), "different sampleRate not match");
        AudioFormat otherEnc = new AudioFormat(AudioFormat.Encoding.ULAW, 44100f, 16, 2, 4, 44100f, false);
        check(!f.matches(otherEnc), "different encoding not match");
        // big-endian differs and sample size > 8 -> no match
        AudioFormat otherEndian = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, true);
        check(!f.matches(otherEndian), "byte order differs (16-bit) not match");

        // toString is non-empty and carries the encoding name
        String s = f.toString();
        check(s != null && s.contains("PCM_SIGNED"), "toString has encoding");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
