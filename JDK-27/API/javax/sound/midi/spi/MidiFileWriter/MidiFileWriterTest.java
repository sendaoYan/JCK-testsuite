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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.spi.MidiFileWriter.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.midi.spi.MidiFileWriter.class.getModifiers()), "public");
        
        javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(smf0()));
        RecWriter w = new RecWriter();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int n = w.write(seq, 0, bos);
        check(n == 34 && bos.size() == 34, "recording writer emits 34 bytes");
        check(w.getMidiFileTypes().length >= 2, "recording writer file types");
        check(w.isFileTypeSupported(0), "supports type 0");
        check(!w.isFileTypeSupported(99), "bogus type false");
    }
    static class RecWriter extends javax.sound.midi.spi.MidiFileWriter {
        public int[] getMidiFileTypes() { return new int[] { 0, 1 }; }
        public int[] getMidiFileTypes(javax.sound.midi.Sequence seq) { return new int[] { 0, 1 }; }
        public boolean isFileTypeSupported(int type) { return type == 0 || type == 1; }
        public boolean isFileTypeSupported(int type, javax.sound.midi.Sequence seq) { return type == 0 || type == 1; }
        public int write(javax.sound.midi.Sequence in, int type, java.io.OutputStream out) throws java.io.IOException { return javax.sound.midi.MidiSystem.write(in, type, out); }
        public int write(javax.sound.midi.Sequence in, int type, java.io.File out) throws java.io.IOException { return 0; }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIMD(String what, Runnable r, String msg) {
        RuntimeException caught = null;
        try { r.run(); }
        catch (RuntimeException e) { caught = e; }
        if (caught == null) throw new AssertionError("expected InvalidMidiDataException: " + what);
        Throwable t = caught.getCause() != null ? caught.getCause() : caught;
        check(t instanceof javax.sound.midi.InvalidMidiDataException, "IMD type for " + what + " got " + t);
        if (t instanceof javax.sound.midi.InvalidMidiDataException) {
            javax.sound.midi.InvalidMidiDataException imd = (javax.sound.midi.InvalidMidiDataException) t;
            check(msg == null || msg.equals(imd.getMessage()), "IMD msg " + what + " [" + imd.getMessage() + "]");
        }
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }
    // minimal hand-built SMF (format 0, 1 track, PPQ 96): NOTE_ON tick 0, NOTE_OFF tick 48, EOT
    static byte[] smf0() throws java.io.IOException {
        java.io.ByteArrayOutputStream f = new java.io.ByteArrayOutputStream();
        f.write('M'); f.write('T'); f.write('h'); f.write('d');
        f.write(0); f.write(0); f.write(0); f.write(6);
        f.write(0); f.write(0); f.write(0); f.write(1); f.write(0); f.write(96);
        java.io.ByteArrayOutputStream t = new java.io.ByteArrayOutputStream();
        t.write(0); t.write(0x90); t.write(60); t.write(100);
        t.write(0x30); t.write(0x80); t.write(60); t.write(0);
        t.write(0); t.write(0xFF); t.write(0x2F); t.write(0);
        byte[] tb = t.toByteArray();
        f.write('M'); f.write('T'); f.write('r'); f.write('k');
        f.write((tb.length >> 24) & 0xFF); f.write((tb.length >> 16) & 0xFF); f.write((tb.length >> 8) & 0xFF); f.write(tb.length & 0xFF);
        f.write(tb);
        return f.toByteArray();
    }

}

class FakeWriter extends MidiFileWriter {
    public int[] getMidiFileTypes() { return new int[]{0, 1}; }
    public int[] getMidiFileTypes(Sequence sequence) { return new int[]{0}; }
    public int write(Sequence in, int fileType, OutputStream out) { return 42; }
    public int write(Sequence in, int fileType, File out) { return 42; }
}
