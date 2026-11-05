/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileReader conformance: white-box structure + black-box behavior.
 * @run main MidiFileReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Modifier;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import javax.sound.midi.spi.MidiFileReader;

public class MidiFileReaderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileReader");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(methods.contains("getSequence"), "method getSequence");
    }

    static void testBlackBox() throws Exception {
        MidiFileFormat fmt = new MidiFileFormat(0, Sequence.PPQ, 96, 100, 500000);
        Sequence seq = new Sequence(Sequence.PPQ, 96);
        seq.createTrack();
        FakeReader r = new FakeReader(fmt, seq);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(r.getMidiFileFormat(in) == fmt, "getMidiFileFormat(InputStream) round-trip");
        check(r.getSequence(in) == seq, "getSequence(InputStream) round-trip");
        URL url = new File("dummy").toURI().toURL();
        check(r.getMidiFileFormat(url) == fmt, "getMidiFileFormat(URL) round-trip");
        check(r.getSequence(url) == seq, "getSequence(URL) round-trip");
        File f = new File("dummy");
        check(r.getMidiFileFormat(f) == fmt, "getMidiFileFormat(File) round-trip");
        check(r.getSequence(f) == seq, "getSequence(File) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.spi.MidiFileReader.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.midi.spi.MidiFileReader.class.getModifiers()), "public");
        
        byte[] smf = smf0();
        RecReader r = new RecReader();
        javax.sound.midi.MidiFileFormat ff = r.getMidiFileFormat(new java.io.ByteArrayInputStream(smf));
        check(ff.getType() == 0 && ff.getResolution() == 96, "recording reader parses hand-built SMF");
        javax.sound.midi.Sequence seq = r.getSequence(new java.io.ByteArrayInputStream(smf));
        check(seq.getTracks().length == 1 && seq.getTickLength() == 48, "recording reader sequence");
        java.net.URL u = new java.net.URL("file:/dev/null");
        check(r.getSequence(u) == null, "URL overload returns null (recording impl)");
    }
    static class RecReader extends javax.sound.midi.spi.MidiFileReader {
        public javax.sound.midi.MidiFileFormat getMidiFileFormat(java.io.InputStream in) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException {
            return javax.sound.midi.MidiSystem.getMidiFileFormat(in);
        }
        public javax.sound.midi.MidiFileFormat getMidiFileFormat(java.net.URL url) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException { return null; }
        public javax.sound.midi.MidiFileFormat getMidiFileFormat(java.io.File f) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException { return null; }
        public javax.sound.midi.Sequence getSequence(java.io.InputStream in) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException {
            return javax.sound.midi.MidiSystem.getSequence(in);
        }
        public javax.sound.midi.Sequence getSequence(java.net.URL url) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException { return null; }
        public javax.sound.midi.Sequence getSequence(java.io.File f) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException { return null; }
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

class FakeReader extends MidiFileReader {
    private final MidiFileFormat fmt;
    private final Sequence seq;
    FakeReader(MidiFileFormat f, Sequence s) { fmt = f; seq = s; }
    public MidiFileFormat getMidiFileFormat(InputStream stream) { return fmt; }
    public MidiFileFormat getMidiFileFormat(URL url) { return fmt; }
    public MidiFileFormat getMidiFileFormat(File file) { return fmt; }
    public Sequence getSequence(InputStream stream) { return seq; }
    public Sequence getSequence(URL url) { return seq; }
    public Sequence getSequence(File file) { return seq; }
}
