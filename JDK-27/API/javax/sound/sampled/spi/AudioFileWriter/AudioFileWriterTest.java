/*
 * @test
 * @summary javax.sound.sampled.spi.AudioFileWriter conformance: white-box structure + black-box behavior.
 * @run main AudioFileWriterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioFileWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.AudioFileWriter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("getAudioFileTypes").getModifiers()), "getAudioFileTypes() abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioFileTypes", AudioInputStream.class).getModifiers()), "getAudioFileTypes(stream) abstract");
        check(Modifier.isAbstract(c.getMethod("write", AudioInputStream.class, AudioFileFormat.Type.class, OutputStream.class).getModifiers()), "write(OutputStream) abstract");
        check(Modifier.isAbstract(c.getMethod("write", AudioInputStream.class, AudioFileFormat.Type.class, File.class).getModifiers()), "write(File) abstract");
        check(!Modifier.isAbstract(c.getMethod("isFileTypeSupported", AudioFileFormat.Type.class).getModifiers()), "isFileTypeSupported(Type) concrete");
        check(!Modifier.isAbstract(c.getMethod("isFileTypeSupported", AudioFileFormat.Type.class, AudioInputStream.class).getModifiers()), "isFileTypeSupported(Type,stream) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[800]), fmt, 800);

        // Concrete convenience methods on a trivial anonymous subclass.
        AudioFileWriterImpl w = new AudioFileWriterImpl();
        check(!w.isFileTypeSupported(AudioFileFormat.Type.WAVE), "empty writer: WAVE not supported");
        check(!w.isFileTypeSupported(AudioFileFormat.Type.WAVE, ais), "empty writer: WAVE not supported (stream)");

        // Real writers via AudioSystem.
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        check(types != null && types.length > 0, "getAudioFileTypes non-empty");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE), "WAVE supported");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE, ais), "WAVE supported (stream)");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, bos);
        check(written > 0, "write WAV bytes");
        check(bos.size() == written, "byte count matches written");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class AudioFileWriterImpl extends javax.sound.sampled.spi.AudioFileWriter {
    public AudioFileFormat.Type[] getAudioFileTypes() { return new AudioFileFormat.Type[0]; }
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream s) { return new AudioFileFormat.Type[0]; }
    public int write(AudioInputStream s, AudioFileFormat.Type t, OutputStream out) { return 0; }
    public int write(AudioInputStream s, AudioFileFormat.Type t, File f) { return 0; }
}
