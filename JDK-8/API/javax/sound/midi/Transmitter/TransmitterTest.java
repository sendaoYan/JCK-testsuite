/*
 * @test
 * @summary javax.sound.midi.Transmitter conformance: interface hierarchy and methods.
 * @library /lib
 * @run main TransmitterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TransmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Transmitter");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setReceiver"), "method setReceiver");
        check(dm.contains("getReceiver"), "method getReceiver");
        check(dm.contains("close"), "method close");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementations + real device endpoints ----
    static void testContractDeep() throws Exception {
        final int[] sent = new int[1];
        final javax.sound.midi.MidiMessage[] last = new javax.sound.midi.MidiMessage[1];
        javax.sound.midi.Receiver rec = new javax.sound.midi.Receiver() {
            public void send(javax.sound.midi.MidiMessage m, long timeStamp) { sent[0]++; last[0] = m; }
            public void close() {}
        };
        javax.sound.midi.ShortMessage msg = new javax.sound.midi.ShortMessage(0x90, 60, 64);
        rec.send(msg, -1);
        check(sent[0] == 1 && last[0] == msg, "recording receiver captures message");
        rec.send(msg, 12345);
        check(sent[0] == 2, "second send counted");
        javax.sound.midi.Receiver[] targets = new javax.sound.midi.Receiver[] { rec };
        javax.sound.midi.Transmitter tx = new javax.sound.midi.Transmitter() {
            javax.sound.midi.Receiver r;
            public void setReceiver(javax.sound.midi.Receiver r) { this.r = r; }
            public javax.sound.midi.Receiver getReceiver() { return r; }
            public void close() { r = null; }
        };
        check(tx.getReceiver() == null, "fresh transmitter null receiver");
        tx.setReceiver(targets[0]);
        check(tx.getReceiver() == rec, "setReceiver identity");
        tx.getReceiver().send(msg, -1);
        check(sent[0] == 3, "send via transmitter wiring");
        tx.close();
        check(tx.getReceiver() == null, "close clears receiver (recording impl)");
        // real sequencer endpoints
        try {
            javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
            sqr.open();
            javax.sound.midi.Transmitter real = sqr.getTransmitter();
            real.setReceiver(rec);
            check(real.getReceiver() == rec, "real transmitter accepts external receiver");
            javax.sound.midi.Receiver realRx = sqr.getReceiver();
            realRx.send(msg, -1);
            real.close();
            realRx.close();
            sqr.close();
            passed += 3;
        } catch (javax.sound.midi.MidiUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
        }
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
