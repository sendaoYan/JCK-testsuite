/*
 * @test
 * @summary javax.sound.midi.Sequence conformance: behavioral.
 * @run main SequenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        check(s.getDivisionType() == javax.sound.midi.Sequence.PPQ, "divisionType");
        check(s.getResolution() == 96, "resolution");
        javax.sound.midi.Track t = s.createTrack();
        check(t != null, "createTrack");
        check(s.getTracks().length == 1, "tracks 1");
        check(s.getTickLength() == 0, "tickLength 0");
        check(s.getMicrosecondLength() == 0, "microsecondLength 0");
        check(s.getPatchList() != null, "patchList");
        javax.sound.midi.Sequence s2 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96, 2);
        check(s2.getTracks().length == 2, "tracks 2");
        check(s2.deleteTrack(s2.getTracks()[0]), "deleteTrack");
        check(s2.getTracks().length == 1, "tracks after delete");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: ctor matrix / track lifecycle / constants / tick math ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 24);
        check(s.getDivisionType() == 0.0f && s.getResolution() == 24, "PPQ ctor fields");
        check(s.getTracks().length == 0, "fresh sequence zero tracks");
        check(s.getMicrosecondLength() == 0, "empty micros 0");
        javax.sound.midi.Track t1 = s.createTrack();
        javax.sound.midi.Track t2 = s.createTrack();
        check(s.getTracks().length == 2 && s.getTracks()[0] == t1 && s.getTracks()[1] == t2, "tracks ordered by creation");
        t1.add(new javax.sound.midi.MidiEvent(new javax.sound.midi.ShortMessage(0x90, 60, 50), 24));
        check(s.getTickLength() == 24, "tickLength from longest track");
        check(s.getMicrosecondLength() == 500000, "micros = ticks * 60_000_000 / (res * bpm-default-120) = 0.5s");
        check(s.deleteTrack(t1), "deleteTrack true");
        check(!s.deleteTrack(t1), "double delete false");
        check(s.getTracks().length == 1, "one track remains");
        check(s.getPatchList().length == 0, "empty patch list");
        check(javax.sound.midi.Sequence.PPQ == 0.0f, "PPQ 0.0");
        check(javax.sound.midi.Sequence.SMPTE_24 == 24.0f && javax.sound.midi.Sequence.SMPTE_25 == 25.0f
           && javax.sound.midi.Sequence.SMPTE_30 == 30.0f && javax.sound.midi.Sequence.SMPTE_30DROP == 29.97f, "SMPTE constants");
        javax.sound.midi.Sequence s2 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.SMPTE_30, 240);
        check(s2.getDivisionType() == 30.0f, "SMPTE ctor");
        javax.sound.midi.Sequence lenient0 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 0);
        check(lenient0.getResolution() == 0, "resolution 0 accepted (no IAE)");
        javax.sound.midi.Sequence lenientNeg = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, -5);
        check(lenientNeg.getResolution() == -5, "negative resolution accepted (no IAE)");
        expectIMD("bad division", new Runnable() { public void run() { try { new javax.sound.midi.Sequence(999f, 24); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Unsupported division type: 999.0");
        javax.sound.midi.Sequence three = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96, 3);
        check(three.getTracks().length == 3, "numTracks ctor pre-allocates 3 empty tracks");
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
