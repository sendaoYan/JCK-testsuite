/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Instrument conformance: abstract hierarchy and methods.
 * @run main InstrumentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InstrumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Instrument");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "extends javax.sound.midi.SoundbankResource");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getPatch"), "method getPatch");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real GM instrument surface ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Patch patch = new javax.sound.midi.Patch(1, 2);
        javax.sound.midi.Instrument rec = new javax.sound.midi.Instrument(null, patch, "RecInst", null) {
            public Object getData() { return "payload"; }
        };
        check("RecInst".equals(rec.getName()), "name passthrough");
        check(rec.getPatch().getBank() == 1 && rec.getPatch().getProgram() == 2, "patch passthrough");
        check(rec.getSoundbank() == null, "null soundbank allowed");
        check("payload".equals(rec.getData()), "custom data");
        javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
        syn.open();
        try {
            javax.sound.midi.Instrument piano = syn.getDefaultSoundbank().getInstruments()[0];
            check("Acoustic Grand Piano".equals(piano.getName()), "GM 0 name");
            check(piano.getSoundbank() == syn.getDefaultSoundbank(), "instrument knows soundbank");
            check(piano.toString().startsWith("Instrument: Acoustic Grand Piano"), "toString format [bank #0 preset #0]: " + piano.toString());
            check(piano.getDataClass() == null, "Gervill instrument dataClass null");
        } finally { syn.close(); }
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
