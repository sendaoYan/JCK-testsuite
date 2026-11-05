/*
 * @test
 * @summary javax.sound.midi.Track conformance: behavioral.
 * @run main TrackTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TrackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        javax.sound.midi.Track t = s.createTrack();
        check(t.size() == 1, "size 1 (end-of-track)");
        check(t.ticks() == 0, "ticks 0");
        javax.sound.midi.ShortMessage sm = new javax.sound.midi.ShortMessage();
        sm.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(sm, 0L);
        check(t.add(ev), "add");
        check(t.size() == 2, "size 2");
        check(t.remove(ev), "remove");
        check(t.size() == 1, "size after remove");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: add/remove/ticks matrix on real parsed track ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Sequence seq = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        javax.sound.midi.Track tr = seq.createTrack();
        check(seq.getTracks().length == 1 && seq.getTracks()[0] == tr, "createTrack registers track");
        check(tr.size() == 1, "new track holds implicit EndOfTrack (size 1)");
        check(tr.ticks() == 0, "fresh track ticks 0 (EOT at 0)");
        javax.sound.midi.MidiEvent e1 = new javax.sound.midi.MidiEvent(new javax.sound.midi.ShortMessage(0x90, 60, 50), 5);
        check(tr.add(e1), "first add true");
        check(tr.size() == 2, "EOT retained after add");
        check(tr.ticks() == 5, "ticks follows last event");
        check(!tr.add(e1), "re-adding identical instance false (dedup)");
        check(tr.size() == 2, "size unchanged after dup add");
        javax.sound.midi.MidiEvent e2 = new javax.sound.midi.MidiEvent(new javax.sound.midi.ShortMessage(0x80, 60, 0), 9);
        check(tr.add(e2), "second add true");
        check(tr.get(0).getTick() <= tr.get(1).getTick() && tr.get(1).getTick() <= tr.get(2).getTick(), "events sorted by tick");
        check(tr.get(0) == e1 && tr.get(1) == e2, "tick order 5 then 9");
        check(tr.get(2).getMessage().getStatus() == 0xFF, "implicit EOT repositioned to end");
        check(tr.remove(e1), "remove true");
        check(!tr.remove(e1), "double remove false");
        check(tr.ticks() == 9, "ticks after removal");
        javax.sound.midi.Track fromFile = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(smf0())).getTracks()[0];
        check(fromFile.size() == 3, "parsed track: 2 written + EOT");
        check(fromFile.ticks() == 48, "parsed track ticks 48");
        javax.sound.midi.MidiEvent last = fromFile.get(fromFile.size() - 1);
        check(last.getMessage().getStatus() == 0xFF, "implicit EOT is meta status 0xFF");
        check(fromFile.remove(last), "implicit EOT IS removable on a parsed track");
        check(fromFile.size() == 2, "size 2 after EOT removal");
        check(fromFile.ticks() == 48, "ticks unchanged by EOT removal");
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
