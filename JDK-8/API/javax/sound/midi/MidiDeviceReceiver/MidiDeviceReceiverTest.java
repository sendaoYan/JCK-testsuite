/*
 * @test
 * @summary javax.sound.midi.MidiDeviceReceiver conformance: interface hierarchy and methods.
 * @run main MidiDeviceReceiverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiDeviceReceiverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDeviceReceiver");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.midi.Receiver"), "extends javax.sound.midi.Receiver");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMidiDevice"), "method getMidiDevice");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: spi-aware subinterfaces extend base interfaces ----
    static void testContractDeep() throws Exception {
        check(javax.sound.midi.Receiver.class.isAssignableFrom(javax.sound.midi.MidiDeviceReceiver.class), "MidiDeviceReceiver extends Receiver");
        check(javax.sound.midi.Transmitter.class.isAssignableFrom(javax.sound.midi.MidiDeviceTransmitter.class), "MidiDeviceTransmitter extends Transmitter");
        javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
        sqr.open();
        try {
            javax.sound.midi.Transmitter tx = sqr.getTransmitter();
            javax.sound.midi.MidiDeviceTransmitter mdt = (javax.sound.midi.MidiDeviceTransmitter) tx;
            check(mdt.getMidiDevice() == sqr, "transmitter reports owning device");
            javax.sound.midi.Receiver rx = sqr.getReceiver();
            javax.sound.midi.MidiDeviceReceiver mdr = (javax.sound.midi.MidiDeviceReceiver) rx;
            check(mdr.getMidiDevice() == sqr, "receiver reports owning device");
        } finally { sqr.close(); }
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
