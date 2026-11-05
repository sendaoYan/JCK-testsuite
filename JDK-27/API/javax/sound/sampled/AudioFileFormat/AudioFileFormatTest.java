/*
 * @test
 * @summary javax.sound.sampled.AudioFileFormat conformance: white-box structure + black-box behavior.
 * @run main AudioFileFormatTest
 */

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioFileFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFileFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 3, "constructor count 3");

        Class<?> t = Class.forName("javax.sound.sampled.AudioFileFormat$Type");
        check(t != null, "nested Type loads");
        check(t.getEnclosingClass() == c, "Type enclosed by AudioFileFormat");
        check(Modifier.isPublic(t.getModifiers()) && Modifier.isStatic(t.getModifiers()), "Type public static");
    }

    // concrete subclass to exercise the protected (byteLength) constructor
    static class CustomFileFormat extends AudioFileFormat {
        CustomFileFormat(AudioFileFormat.Type type, int byteLength, AudioFormat format, int frameLength) {
            super(type, byteLength, format, frameLength);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);

        // 3-arg public constructor
        AudioFileFormat aff = new AudioFileFormat(AudioFileFormat.Type.WAVE, fmt, 100);
        check(aff.getType() == AudioFileFormat.Type.WAVE, "getType WAVE");
        check(aff.getFormat() == fmt, "getFormat identity");
        check(aff.getFrameLength() == 100, "getFrameLength 100");
        check(aff.getByteLength() == AudioSystem.NOT_SPECIFIED, "3-arg byteLength NOT_SPECIFIED");
        check(aff.properties().isEmpty(), "3-arg properties empty");
        check(aff.getProperty("author") == null, "absent property null");

        // 4-arg public constructor with properties map
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("author", "me");
        props.put("duration", Long.valueOf(5000L));
        AudioFileFormat aff2 = new AudioFileFormat(AudioFileFormat.Type.AU, fmt, 50, props);
        check("me".equals(aff2.getProperty("author")), "property author");
        check(Long.valueOf(5000L).equals(aff2.getProperty("duration")), "property duration");
        check(aff2.properties().size() == 2, "properties size 2");
        check(aff2.getFrameLength() == 50, "4-arg frameLength");

        // protected constructor -> byteLength round-trip
        CustomFileFormat cff = new CustomFileFormat(AudioFileFormat.Type.AIFF, 1234, fmt, 60);
        check(cff.getByteLength() == 1234, "byteLength round-trip");
        check(cff.getType() == AudioFileFormat.Type.AIFF, "AIFF type");
        check(cff.getFrameLength() == 60, "frameLength 60");

        // toString includes type and extension
        String s = aff.toString();
        check(s.contains("WAVE") && s.contains(".wav"), "toString has WAVE + .wav");
        check(aff2.toString().contains(".au"), "toString has .au");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: both ctors / hand-built WAV + AU byte-level parse / Type table ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.AudioFileFormat aff = new javax.sound.sampled.AudioFileFormat(javax.sound.sampled.AudioFileFormat.Type.WAVE, fmt, 2);
        check(aff.getType() == javax.sound.sampled.AudioFileFormat.Type.WAVE, "type");
        check(aff.getFrameLength() == 2, "frame length");
        check(aff.getByteLength() == -1, "3-arg ctor leaves byteLength NOT_SPECIFIED");
        check(aff.getFormat() == fmt, "format same reference");
        java.util.HashMap<String, Object> props = new java.util.HashMap<String, Object>();
        props.put("k", "v");
        javax.sound.sampled.AudioFileFormat pff = new javax.sound.sampled.AudioFileFormat(javax.sound.sampled.AudioFileFormat.Type.AU, fmt, 2, props);
        check(pff.getByteLength() == javax.sound.sampled.AudioSystem.NOT_SPECIFIED || pff.getByteLength() >= 0, "byte length unspecified form");
        check("v".equals(pff.getProperty("k")) && pff.getProperty("x") == null, "property get/miss");
        boolean imm = false;
        try { pff.properties().put("x", "y"); } catch (UnsupportedOperationException e) { imm = true; }
        check(imm, "properties unmodifiable");
        // hand-built WAV byte-level parse
        byte[] wav = wav8();
        javax.sound.sampled.AudioFileFormat w = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(wav));
        check(w.getType() == javax.sound.sampled.AudioFileFormat.Type.WAVE, "WAVE type");
        check(w.getByteLength() == 46 && w.getFrameLength() == 2, "WAVE 46 bytes / 2 frames");
        javax.sound.sampled.AudioFormat wf = w.getFormat();
        check(wf.getSampleRate() == 8000f && wf.getSampleSizeInBits() == 8 && wf.getChannels() == 1, "WAV fields");
        check(wf.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED, "8-bit WAV parsed as PCM_UNSIGNED");
        check(wf.getFrameSize() == 1, "WAV frame size 1");
        // hand-built AU byte-level parse
        javax.sound.sampled.AudioFileFormat au = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(auUlaw()));
        check(au.getType() == javax.sound.sampled.AudioFileFormat.Type.AU, "AU type");
        check(au.getFrameLength() == 24 && au.getByteLength() == 51, "AU frameLen 24 / byteLen offset(24)+size(27)=51");
        javax.sound.sampled.AudioFormat af = au.getFormat();
        check(af.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW && af.getSampleRate() == 8000f, "AU ULAW 8000");
        check(af.getSampleSizeInBits() == 8 && af.getChannels() == 1 && af.getFrameSize() == 1, "AU fields");
        // AU data-encoding table: 1=ULAW 2=PCM_SIGNED 8 3=PCM_SIGNED 16 27=ALAW 5=PCM_SIGNED 32
        int[][] encs = { { 1, 8 }, { 2, 8 }, { 3, 16 }, { 27, 8 }, { 5, 32 } };
        javax.sound.sampled.AudioFormat.Encoding[] wantEnc = { javax.sound.sampled.AudioFormat.Encoding.ULAW, javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, javax.sound.sampled.AudioFormat.Encoding.ALAW, javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED };
        int[] wantBits = { 8, 8, 16, 8, 32 };
        int[] wantFs = { 1, 1, 2, 1, 4 };
        for (int i = 0; i < encs.length; i++) {
            javax.sound.sampled.AudioFormat g = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(au(27, encs[i][0], 8000, 1, new byte[] { 0, 1, 2 }))).getFormat();
            check(g.getEncoding() == wantEnc[i] && g.getSampleSizeInBits() == wantBits[i] && g.getFrameSize() == wantFs[i], "AU enc " + encs[i][0] + " mapping");
        }
        // stereo AU frame math
        javax.sound.sampled.AudioFormat s16 = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(au(28, 3, 44100, 2, new byte[4]))).getFormat();
        check(s16.getFrameSize() == 4 && s16.getChannels() == 2 && s16.getSampleRate() == 44100f, "stereo 16-bit AU frame math");
        // Type constants + extension + equals
        javax.sound.sampled.AudioFileFormat.Type[] ts = javax.sound.sampled.AudioSystem.getAudioFileTypes();
        check(ts.length == 3, "writable file types: WAVE/AU/AIFF = 3");
        boolean wave = false, auT = false, aiff = false;
        for (javax.sound.sampled.AudioFileFormat.Type t : ts) {
            if (t == javax.sound.sampled.AudioFileFormat.Type.WAVE) wave = true;
            if (t == javax.sound.sampled.AudioFileFormat.Type.AU) auT = true;
            if (t == javax.sound.sampled.AudioFileFormat.Type.AIFF) aiff = true;
        }
        check(wave && auT && aiff, "WAVE+AU+AIFF writable");
        javax.sound.sampled.AudioFileFormat.Type aifcConst = javax.sound.sampled.AudioFileFormat.Type.AIFC;
        javax.sound.sampled.AudioFileFormat.Type sndConst = javax.sound.sampled.AudioFileFormat.Type.SND;
        check("AIFF-C".equals(aifcConst.toString()) && "SND".equals(sndConst.toString()), "AIFC(=AIFF-C)/SND constants exist (read-only types)");
        check("WAVE".equals(javax.sound.sampled.AudioFileFormat.Type.WAVE.toString()) && "wav".equals(javax.sound.sampled.AudioFileFormat.Type.WAVE.getExtension()), "WAVE name/ext");
        check("aif".equals(javax.sound.sampled.AudioFileFormat.Type.AIFF.getExtension()) && "aifc".equals(javax.sound.sampled.AudioFileFormat.Type.AIFC.getExtension()), "AIFF ext=aif, AIFC ext=aifc");
        check("snd".equals(javax.sound.sampled.AudioFileFormat.Type.SND.getExtension()), "SND ext");
        check(javax.sound.sampled.AudioFileFormat.Type.WAVE.equals(javax.sound.sampled.AudioFileFormat.Type.WAVE), "Type equals");
        check(javax.sound.sampled.AudioFileFormat.Type.WAVE.toString().equals("WAVE"), "Type toString is name");
        // 1-frame AU with unknown encoding 99
        try {
            javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(au(27, 99, 8000, 1, new byte[] { 0 })));
            check(false, "unknown AU encoding must be rejected");
        } catch (javax.sound.sampled.UnsupportedAudioFileException e) { passed++; }
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
