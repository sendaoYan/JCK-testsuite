/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileWriter conformance: white-box structure + black-box behavior.
 * @run main MidiFileWriterTest
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.midi.Sequence;
import javax.sound.midi.spi.MidiFileWriter;

public class MidiFileWriterTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileWriter");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getMidiFileTypes"), "method getMidiFileTypes");
        check(methods.contains("isFileTypeSupported"), "method isFileTypeSupported");
        check(methods.contains("write"), "method write");
    }

    static void testBlackBox() throws Exception {
        Sequence seq = new Sequence(Sequence.PPQ, 96);
        seq.createTrack();
        FakeWriter w = new FakeWriter();
        check(w.getMidiFileTypes() != null, "getMidiFileTypes non-null");
        check(w.getMidiFileTypes().length == 2, "getMidiFileTypes length");
        check(w.isFileTypeSupported(0), "isFileTypeSupported(0) true");
        check(w.isFileTypeSupported(1), "isFileTypeSupported(1) true");
        check(!w.isFileTypeSupported(2), "isFileTypeSupported(2) false");
        check(w.getMidiFileTypes(seq) != null, "getMidiFileTypes(seq) non-null");
        check(w.getMidiFileTypes(seq).length == 1, "getMidiFileTypes(seq) length");
        check(w.isFileTypeSupported(0, seq), "isFileTypeSupported(0,seq) true");
        check(!w.isFileTypeSupported(1, seq), "isFileTypeSupported(1,seq) false");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(w.write(seq, 0, bos) == 42, "write(OutputStream) returns bytes written");
        Path dir = Files.createTempDirectory("jck");
        try {
            File out = dir.resolve("out.mid").toFile();
            check(w.write(seq, 0, out) == 42, "write(File) returns bytes written");
        } finally {
            try { Files.deleteIfExists(dir.resolve("out.mid")); } catch (Exception ignore) {}
            try { Files.deleteIfExists(dir); } catch (Exception ignore) {}
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class FakeWriter extends MidiFileWriter {
    public int[] getMidiFileTypes() { return new int[]{0, 1}; }
    public int[] getMidiFileTypes(Sequence sequence) { return new int[]{0}; }
    public int write(Sequence in, int fileType, OutputStream out) { return 42; }
    public int write(Sequence in, int fileType, File out) { return 42; }
}
