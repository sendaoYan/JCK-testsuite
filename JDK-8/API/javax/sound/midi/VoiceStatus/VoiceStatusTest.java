/*
 * @test
 * @key sound
 * @summary javax.sound.midi.VoiceStatus conformance: behavioral.
 * @run main VoiceStatusTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class VoiceStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.VoiceStatus v = new javax.sound.midi.VoiceStatus();
        check(!v.active, "active default false");
        check(v.channel == 0, "channel 0");
        check(v.note == 0, "note 0");
        v.active = true; v.channel = 1; v.bank = 2; v.program = 3; v.note = 60; v.volume = 100;
        check(v.active && v.channel == 1 && v.note == 60 && v.volume == 100, "fields");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: public fields + real voice array ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.VoiceStatus vs = new javax.sound.midi.VoiceStatus();
        check(vs.active == false && vs.channel == 0 && vs.bank == 0, "default fields false/0");
        check(vs.program == 0 && vs.note == 0 && vs.volume == 0, "default numeric fields");
        vs.active = true; vs.channel = 3; vs.bank = 1; vs.program = 40; vs.note = 60; vs.volume = 100;
        check(vs.active && vs.channel == 3 && vs.bank == 1 && vs.program == 40 && vs.note == 60 && vs.volume == 100, "all six public fields writable");
        javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
        syn.open();
        try {
            javax.sound.midi.VoiceStatus[] all = syn.getVoiceStatus();
            check(all.length == 64, "64 voices on Gervill");
            for (javax.sound.midi.VoiceStatus v : all) check(!v.active, "all inactive before notes");
            javax.sound.midi.MidiChannel ch = syn.getChannels()[0];
            ch.noteOn(60, 100);
            all = syn.getVoiceStatus(); // array is a fresh snapshot each call
            int activeCount = 0;
            for (javax.sound.midi.VoiceStatus v : all) if (v.active) activeCount++;
            check(activeCount == 2, "two voices active for one noteOn (main+release) [" + activeCount + "]");
            javax.sound.midi.VoiceStatus av = null;
            for (javax.sound.midi.VoiceStatus v : all) if (v.active) av = v;
            check(av.note == 60 && av.channel == 0, "active voice reports note+channel");
            ch.noteOff(60);
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
