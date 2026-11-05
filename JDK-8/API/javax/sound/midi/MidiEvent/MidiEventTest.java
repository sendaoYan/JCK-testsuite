/*
 * @test
 * @summary javax.sound.midi.MidiEvent conformance: behavioral.
 * @run main MidiEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.ShortMessage sm = new javax.sound.midi.ShortMessage();
        sm.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(sm, 10L);
        check(ev.getMessage() == sm, "getMessage");
        check(ev.getTick() == 10L, "getTick");
        ev.setTick(20L);
        check(ev.getTick() == 20L, "setTick");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: tick identity/mutation/negative ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.ShortMessage msg = new javax.sound.midi.ShortMessage(0x90, 30, 40);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(msg, 123);
        check(ev.getTick() == 123, "tick round-trip");
        check(ev.getMessage() == msg, "message same reference");
        ev.setTick(999);
        check(ev.getTick() == 999, "setTick");
        javax.sound.midi.MidiEvent neg = new javax.sound.midi.MidiEvent(msg, -5);
        check(neg.getTick() == -5, "negative tick accepted");
        javax.sound.midi.MidiEvent zero = new javax.sound.midi.MidiEvent(msg, 0);
        check(zero.getTick() == 0, "zero tick");
        check(javax.sound.midi.MidiEvent.class.getConstructors().length == 1, "single public ctor");
        javax.sound.midi.MidiEvent nullMsg = new javax.sound.midi.MidiEvent(null, 0);
        check(nullMsg.getMessage() == null && nullMsg.getTick() == 0, "null message accepted (no validation), getMessage null");
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
