/*
 * @test
 * @summary javax.sound.midi.MidiFileFormat conformance: behavioral.
 * @run main MidiFileFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiFileFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.MidiFileFormat ff = new javax.sound.midi.MidiFileFormat(1, javax.sound.midi.Sequence.PPQ, 480, 100, 5000L);
        check(ff.getType() == 1, "type");
        check(ff.getDivisionType() == javax.sound.midi.Sequence.PPQ, "divisionType");
        check(ff.getResolution() == 480, "resolution");
        check(ff.getByteLength() == 100, "byteLength");
        check(ff.getMicrosecondLength() == 5000L, "microsecondLength");
        check(javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH");
        check(ff.properties() != null, "properties");
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("k", "v");
        javax.sound.midi.MidiFileFormat ff2 = new javax.sound.midi.MidiFileFormat(0, javax.sound.midi.Sequence.PPQ, 96, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, props);
        check("v".equals(ff2.getProperty("k")), "getProperty");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: both ctor forms / properties map immutability / UNKNOWN ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.MidiFileFormat ff = new javax.sound.midi.MidiFileFormat(0, javax.sound.midi.Sequence.PPQ, 96, 340, 250000L);
        check(ff.getType() == 0 && ff.getDivisionType() == 0.0f && ff.getResolution() == 96, "5-arg ctor fields");
        check(ff.getByteLength() == 340, "byte length");
        check(ff.getMicrosecondLength() == 250000L, "microsecond length");
        java.util.HashMap<String, Object> props = new java.util.HashMap<String, Object>();
        props.put("title", "deep");
        javax.sound.midi.MidiFileFormat ff2 = new javax.sound.midi.MidiFileFormat(1, javax.sound.midi.Sequence.SMPTE_30, 240, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, 1000L, props);
        check(ff2.getType() == 1 && ff2.getDivisionType() == 30.0f, "6-arg ctor fields");
        check(ff2.getByteLength() == -1, "UNKNOWN_LENGTH passthrough");
        check("deep".equals(ff2.getProperty("title")), "property get");
        check(ff2.getProperty("missing") == null, "missing property null");
        java.util.Map<String, Object> view = ff2.properties();
        check("deep".equals(view.get("title")), "properties map view");
        boolean immutable = false;
        try { view.put("x", "y"); } catch (UnsupportedOperationException e) { immutable = true; }
        check(immutable, "properties map unmodifiable");
        check(javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH -1");
        javax.sound.midi.MidiFileFormat negType = new javax.sound.midi.MidiFileFormat(-1, javax.sound.midi.Sequence.PPQ, 96, 1, 1L);
        check(negType.getType() == -1, "negative type accepted without validation");
        javax.sound.midi.MidiFileFormat parsed = javax.sound.midi.MidiSystem.getMidiFileFormat(new java.io.ByteArrayInputStream(smf0()));
        check(parsed.getType() == 0 && parsed.getResolution() == 96, "parsed format from hand-built SMF");
        check(parsed.getDivisionType() == 0.0f, "parsed division PPQ");
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
