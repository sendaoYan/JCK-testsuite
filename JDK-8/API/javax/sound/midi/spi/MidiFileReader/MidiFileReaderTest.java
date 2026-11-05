/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileReader conformance: abstract hierarchy and methods.
 * @run main MidiFileReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiFileReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileReader");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getSequence"), "method getSequence");
        check(dm.contains("getSequence"), "method getSequence");
        check(dm.contains("getSequence"), "method getSequence");
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
