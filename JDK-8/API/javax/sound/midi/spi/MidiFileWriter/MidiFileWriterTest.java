/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileWriter conformance: abstract hierarchy and methods.
 * @run main MidiFileWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiFileWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileWriter");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMidiFileTypes"), "method getMidiFileTypes");
        check(dm.contains("getMidiFileTypes"), "method getMidiFileTypes");
        check(dm.contains("isFileTypeSupported"), "method isFileTypeSupported");
        check(dm.contains("isFileTypeSupported"), "method isFileTypeSupported");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
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
