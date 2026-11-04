/*
 * @test
 * @summary javax.sound.sampled.AudioSystem conformance: white-box structure + black-box behavior.
 * @key sound
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class BogusMixerInfo extends Mixer.Info {
    BogusMixerInfo(String a, String b, String c, String d) { super(a, b, c, d); }
}
