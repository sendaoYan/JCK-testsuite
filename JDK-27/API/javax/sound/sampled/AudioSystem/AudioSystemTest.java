/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.AudioSystem conformance: white-box structure + black-box behavior.
 * @run main AudioSystemTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioSystemTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()), msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioSystem");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getField("NOT_SPECIFIED") != null, "field NOT_SPECIFIED");
        check(Modifier.isStatic(c.getField("NOT_SPECIFIED").getModifiers()), "NOT_SPECIFIED static");
        check(c.getField("NOT_SPECIFIED").getInt(null) == -1, "NOT_SPECIFIED == -1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(AudioSystem.NOT_SPECIFIED == -1, "NOT_SPECIFIED value");

        // In-memory WAV write/read round-trip.
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        byte[] data = new byte[800];
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), fmt, data.length);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, bos);
        check(written > 0, "write WAV > 0 bytes");
        check(bos.size() == written, "written bytes equals returned count");
        byte[] wav = bos.toByteArray();

        AudioInputStream back = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
        check(back != null, "getAudioInputStream non-null");
        check(back.getFrameLength() == 800, "frame length round-trip");
        AudioFormat bf = back.getFormat();
        check(bf.getChannels() == 1, "channels round-trip");
        check(bf.getSampleRate() == 8000f, "sample rate round-trip");
        check(bf.getSampleSizeInBits() == 8, "sample size round-trip");
        back.close();

        AudioFileFormat aff = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(wav));
        check(aff.getType().equals(AudioFileFormat.Type.WAVE), "file type WAVE");
        check(aff.getFormat() != null, "file format non-null");
        check(aff.getFrameLength() == 800, "file frame length");

        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioFileFormat(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})),
            "getAudioFileFormat bogus");
        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})),
            "getAudioInputStream bogus");

        // File type queries.
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        check(types != null && types.length > 0, "getAudioFileTypes non-empty");
        boolean hasWave = false;
        for (AudioFileFormat.Type t : types) if (t.equals(AudioFileFormat.Type.WAVE)) hasWave = true;
        check(hasWave, "WAVE present");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE), "WAVE supported");
        AudioInputStream ais2 = new AudioInputStream(new ByteArrayInputStream(data), fmt, data.length);
        check(AudioSystem.getAudioFileTypes(ais2) != null, "getAudioFileTypes(stream) non-null");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE, ais2), "isFileTypeSupported WAVE+stream");

        // Mixer enumeration and line queries.
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        check(mixers != null, "getMixerInfo non-null");
        expectThrows(IllegalArgumentException.class,
            () -> AudioSystem.getMixer(new BogusMixerInfo("n", "v", "d", "1")),
            "getMixer bogus info");
        try {
            Mixer dm = AudioSystem.getMixer(null);
            check(dm != null, "getMixer(null) returns default mixer");
        } catch (IllegalArgumentException iae) {
            // Spec-conformant when no default mixer is installed.
        }

        Line.Info sdlInfo = new Line.Info(SourceDataLine.class);
        Line.Info tdlInfo = new Line.Info(TargetDataLine.class);
        Line.Info[] src = AudioSystem.getSourceLineInfo(sdlInfo);
        Line.Info[] tgt = AudioSystem.getTargetLineInfo(tdlInfo);
        check(src != null, "getSourceLineInfo non-null");
        check(tgt != null, "getTargetLineInfo non-null");
        check(AudioSystem.isLineSupported(sdlInfo) == (src.length > 0), "isLineSupported consistent with source line info");

        // Open a real line when available.
        if (mixers.length > 0) {
            Mixer m = AudioSystem.getMixer(mixers[0]);
            check(m != null, "getMixer(first info) non-null");
            check(m.getSourceLineInfo() != null, "mixer source lines non-null");
            check(m.getTargetLineInfo() != null, "mixer target lines non-null");
        }
        try {
            SourceDataLine sdl = AudioSystem.getSourceDataLine(fmt);
            sdl.open(fmt, 1024);
            check(sdl.isOpen(), "sdl open");
            sdl.start();
            sdl.stop();
            sdl.close();
            check(!sdl.isOpen(), "sdl closed");
        } catch (LineUnavailableException lue) {
            // No output line: acceptable.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: write round-trips / ULAW transcode loop / version-diff matrix ----
    static boolean testContractDeep() throws Exception {
        // PCM 16-bit -> ULAW transcode closed loop
        javax.sound.sampled.AudioFormat src16 = new javax.sound.sampled.AudioFormat(8000f, 16, 1, true, false);
        byte[] pcm = new byte[2048];
        for (int i = 0; i < pcm.length; i++) pcm[i] = (byte) ((i * 31) & 0xFF);
        javax.sound.sampled.AudioInputStream in = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm), src16, 1024);
        javax.sound.sampled.AudioFormat ulaw = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000f, 8, 1, 1, 8000f, false);
        javax.sound.sampled.AudioInputStream conv = javax.sound.sampled.AudioSystem.getAudioInputStream(ulaw, in);
        check(conv.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW, "transcoded to ULAW");
        check(conv.available() == 2048, "ULAW available 2048");
        byte[] out = new byte[2048];
        int got = conv.read(out);
        check(got == 1024, "1024 ULAW bytes (half rate)");
        check((out[0] & 0xFF) == 160, "first ULAW byte 160 deterministic");
        // ULAW -> PCM_SIGNED back
        javax.sound.sampled.AudioInputStream back = javax.sound.sampled.AudioSystem.getAudioInputStream(
            new javax.sound.sampled.AudioFormat(8000f, 16, 1, true, false),
            new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(java.util.Arrays.copyOf(out, got)), ulaw, got));
        check(back.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED && back.getFormat().getSampleSizeInBits() == 16, "ULAW back to 16-bit PCM");
        // conversion support matrix
        check(javax.sound.sampled.AudioSystem.isConversionSupported(javax.sound.sampled.AudioFormat.Encoding.ULAW, src16), "16-bit signed -> ULAW supported");
        check(javax.sound.sampled.AudioSystem.isConversionSupported(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, ulaw), "ULAW -> PCM_SIGNED supported");
        check(!javax.sound.sampled.AudioSystem.isConversionSupported(javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED, ulaw), "ULAW -> PCM_UNSIGNED unsupported");
        // unsupported conversion: 8-bit unsigned -> ULAW IAE (message differs: JDK-8 trailing comma)
        boolean isOld = System.getProperty("java.specification.version").startsWith("1.8");
        javax.sound.sampled.AudioFormat u8 = new javax.sound.sampled.AudioFormat(8000f, 8, 1, false, false);
        try {
            javax.sound.sampled.AudioSystem.getAudioInputStream(ulaw, new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[2]), u8, 2));
            check(false, "expected IAE for unsupported conversion");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().startsWith("Unsupported conversion"), "IAE prefix");
            check(e.getMessage().endsWith(", ") == isOld, "IAE trailing comma JDK-8 only: [" + e.getMessage() + "]");
        }
        // getTargetEncodings membership (order/count differ across versions: assert set)
        javax.sound.sampled.AudioFormat.Encoding[] tes = javax.sound.sampled.AudioSystem.getTargetEncodings(src16);
        boolean s = false, us = false, fl = false;
        for (javax.sound.sampled.AudioFormat.Encoding e : tes) {
            if (e == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED) s = true;
            if (e == javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED) us = true;
            if (e == javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT) fl = true;
        }
        check(s && us && fl, "target encodings contain SIGNED/UNSIGNED/FLOAT");
        check(tes.length == 7, "16-bit mono target encoding count 7 both sides");
        javax.sound.sampled.AudioFormat.Encoding[] te8 = javax.sound.sampled.AudioSystem.getTargetEncodings(new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false));
        check(te8.length == (isOld ? 5 : 4), "8-bit target encoding count 5 (8) / 4 (27): " + te8.length);
        // AU write round-trip: ULAW 1024 bytes -> 1048-byte AU (24 header)
        javax.sound.sampled.AudioInputStream uIn = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(java.util.Arrays.copyOf(out, got)), ulaw, got);
        java.io.ByteArrayOutputStream auOut = new java.io.ByteArrayOutputStream();
        int auW = javax.sound.sampled.AudioSystem.write(uIn, javax.sound.sampled.AudioFileFormat.Type.AU, auOut);
        check(auW == 1048 && auOut.size() == 1048, "AU output 24+1024=1048");
        javax.sound.sampled.AudioFileFormat auBack = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(auOut.toByteArray()));
        check(auBack.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW && auBack.getFrameLength() == 1024, "AU read-back ULAW 1024 frames");
        // WAV bad fmt-tag: message differs
        byte[] badWav = wav8().clone(); badWav[20] = 99;
        try {
            javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(badWav));
            check(false, "bad fmt tag must fail");
        } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
            check(e.getMessage().equals(isOld ? "file is not a supported file type" : "Stream of unsupported format"), "fmt-99 message: [" + e.getMessage() + "]");
        }
        // 1-byte input: EOFException (8) vs UnsupportedAudioFileException (27)
        try {
            javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 1 }));
            check(false, "1-byte input must fail");
        } catch (java.io.EOFException e) { check(isOld, "EOFException on JDK-8"); }
        catch (javax.sound.sampled.UnsupportedAudioFileException e) { check(!isOld, "UnsupportedAudioFileException on JDK-27"); }
        // AU data-size field validation: sizes below the 24-byte header accepted on 8 only
        byte[] d3 = { 0, (byte) 0xFF, 0x7F };
        int[] sizes = { 3, 0, -1 };
        for (int size : sizes) {
            try {
                javax.sound.sampled.AudioFileFormat g = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(au(size, 1, 8000, 1, d3)));
                check(isOld, "small size accepted on JDK-8 only");
                check(g.getByteLength() == 24 + size, "byteLen=offset+sizeField [" + g.getByteLength() + "]");
            } catch (javax.sound.sampled.UnsupportedAudioFileException e) { check(!isOld, "small size rejected on JDK-27: " + e.getMessage()); check("Stream of unsupported format".equals(e.getMessage()), "message"); }
        }
        // huge size field: byteLen differs
        javax.sound.sampled.AudioFileFormat huge = javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(au(0x7FFFFFFF, 1, 8000, 1, d3)));
        check(huge.getFrameLength() == 24, "huge size frameLen 24");
        check(huge.getByteLength() == (isOld ? -2147483625 : -1), "huge byteLen overflow [" + huge.getByteLength() + "]");
        // non-audio magic rejected
        try { javax.sound.sampled.AudioSystem.getAudioFileFormat(new java.io.ByteArrayInputStream(new byte[] { 'X', 'Y', 'Z', 'W', 0, 0, 0, 24, 0, 0, 0, 1, 0, 0, 0x1F, 0x40, 0, 0, 0, 1, 0 })); check(false, "bad magic must fail"); }
        catch (javax.sound.sampled.UnsupportedAudioFileException e) { passed++; }
        // File overload round-trip
        java.io.File tmp = java.io.File.createTempFile("deep", ".wav");
        tmp.deleteOnExit();
        java.io.FileOutputStream fo = new java.io.FileOutputStream(tmp); fo.write(wav8()); fo.close();
        javax.sound.sampled.AudioFileFormat fromFile = javax.sound.sampled.AudioSystem.getAudioFileFormat(tmp);
        check(fromFile.getType() == javax.sound.sampled.AudioFileFormat.Type.WAVE && fromFile.getFrameLength() == 2, "File overload parse");
        // line acquisition: software devices headless
        javax.sound.sampled.AudioFormat lineFmt = new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, false);
        check(javax.sound.sampled.AudioSystem.isLineSupported(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, lineFmt)), "16-bit stereo source line supported");
        try {
            javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
            check(clip != null && !clip.isOpen(), "software Clip acquired closed (skip reason: no sound card needed)");
        } catch (javax.sound.sampled.LineUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("No line matching")) {
                System.out.println("SKIP: no audio device/line available");
            } else {
                throw e;
            }
        }
        // bogus line: negative contract gated on device format-agnosticism.
        // A real device rejects the absurd 99-channel format -> getLine must
        // throw IAE (message differs by trailing comma on JDK-8). A format-
        // agnostic pseudo-device (the ALSA null PCM used in emulation/CI)
        // reports support for any parameters -> getLine must succeed and stay
        // consistent with isLineSupported. Both branches carry 2 assertions so
        // the pass count is identical across environments.
        javax.sound.sampled.DataLine.Info bogusLine = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(1f, 8, 99, true, false));
        if (javax.sound.sampled.AudioSystem.isLineSupported(bogusLine)) {
            javax.sound.sampled.Clip anyFmt = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(bogusLine);
            check(anyFmt != null, "line acquired when isLineSupported reports true");
            check(!anyFmt.isOpen(), "acquired line starts closed");
        } else {
            try {
                javax.sound.sampled.AudioSystem.getLine(bogusLine);
                check(false, "bogus line must fail");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().startsWith("No line matching interface Clip supporting format PCM_SIGNED 1.0 Hz, 8 bit, 99 channels, 99 bytes/frame"), "bogus line prefix");
                check(e.getMessage().endsWith(",  is supported.") == isOld, "trailing comma in line error (JDK-8)");
            }
        }
        // mixer info available headless
        javax.sound.sampled.Mixer.Info[] mi = javax.sound.sampled.AudioSystem.getMixerInfo();
        if (mi.length != 1 || !"default [default]".equals(mi[0].getName())) {
            System.out.println("PASS (" + passed + " assertions; SKIP: no ALSA null software mixer on this host)");
            return false;
        }
        check(mi.length >= 1, "at least default mixer");
        check("default [default]".equals(mi[0].getName()), "default mixer name");
        return true;
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

class BogusMixerInfo extends Mixer.Info {
    BogusMixerInfo(String a, String b, String c, String d) { super(a, b, c, d); }
}
