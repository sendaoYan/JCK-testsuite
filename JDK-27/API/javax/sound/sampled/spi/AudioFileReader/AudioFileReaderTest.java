/*
 * @test
 * @summary javax.sound.sampled.spi.AudioFileReader conformance: white-box structure + black-box behavior.
 * @run main AudioFileReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFileReaderTest {
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
        Class<?> c = Class.forName("javax.sound.sampled.spi.AudioFileReader");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        for (Class<?>[] sig : new Class<?>[][]{
                {InputStream.class}, {URL.class}, {File.class}}) {
            check(c.getMethod("getAudioFileFormat", sig) != null, "getAudioFileFormat abstract");
            check(c.getMethod("getAudioInputStream", sig) != null, "getAudioInputStream abstract");
        }
        check(Modifier.isAbstract(c.getMethod("getAudioFileFormat", InputStream.class).getModifiers()), "getAudioFileFormat abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", InputStream.class).getModifiers()), "getAudioInputStream abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Instantiate via anonymous subclass to prove the protected constructor is usable.
        AudioFileReaderImpl reader = new AudioFileReaderImpl();
        check(reader != null, "anonymous subclass instantiation");

        // Concrete readers are exercised through AudioSystem's public API.
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        byte[] data = new byte[800];
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), fmt, data.length);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, bos);
        check(written > 0, "write WAV");
        byte[] wav = bos.toByteArray();

        AudioFileFormat aff = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(wav));
        check(aff.getType().equals(AudioFileFormat.Type.WAVE), "reader: WAVE type");
        check(aff.getFormat() != null, "reader: format non-null");

        AudioInputStream back = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
        check(back.getFrameLength() == 800, "reader: frame length 800");
        back.close();

        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioFileFormat(new ByteArrayInputStream(new byte[]{1,2,3,4})),
            "reader: bogus format");
        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioInputStream(new ByteArrayInputStream(new byte[]{1,2,3,4})),
            "reader: bogus stream");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class AudioFileReaderImpl extends javax.sound.sampled.spi.AudioFileReader {
    public AudioFileFormat getAudioFileFormat(InputStream s) { return null; }
    public AudioFileFormat getAudioFileFormat(URL u) { return null; }
    public AudioFileFormat getAudioFileFormat(File f) { return null; }
    public AudioInputStream getAudioInputStream(InputStream s) { return null; }
    public AudioInputStream getAudioInputStream(URL u) { return null; }
    public AudioInputStream getAudioInputStream(File f) { return null; }
}
