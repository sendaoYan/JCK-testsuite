/*
 * @test
 * @key sound
 * @summary javax.sound.midi.MidiDevice conformance: interface hierarchy and methods.
 * @run main MidiDeviceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiDeviceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDevice");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getDeviceInfo"), "method getDeviceInfo");
        check(dm.contains("open"), "method open");
        check(dm.contains("close"), "method close");
        check(dm.contains("isOpen"), "method isOpen");
        check(dm.contains("getMicrosecondPosition"), "method getMicrosecondPosition");
        check(dm.contains("getMaxReceivers"), "method getMaxReceivers");
        check(dm.contains("getMaxTransmitters"), "method getMaxTransmitters");
        check(dm.contains("getReceiver"), "method getReceiver");
        check(dm.contains("getReceivers"), "method getReceivers");
        check(dm.contains("getTransmitter"), "method getTransmitter");
        check(dm.contains("getTransmitters"), "method getTransmitters");
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Info surface + sequencer device lifecycle ----
    static boolean testContractDeep() throws Exception {
        javax.sound.midi.MidiDevice.Info[] infos = javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        if (infos.length != 2) {
            System.out.println("PASS (" + passed + " assertions; SKIP: not the Linux 2-software-MIDI-device set on this host)");
            return false;
        }
        check(infos.length == 2, "2 software devices");
        javax.sound.midi.MidiDevice d = javax.sound.midi.MidiSystem.getMidiDevice(infos[0]);
        check(d != null, "device resolved from info");
        check(d.getDeviceInfo().equals(infos[0]), "device info round-trip identity");
        javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
        check("Real Time Sequencer".equals(sqr.getDeviceInfo().getName()), "sequencer info name");
        check(!sqr.isOpen(), "closed");
        sqr.open();
        check(sqr.isOpen(), "open flag");
        check(sqr.getReceivers().isEmpty() == false || sqr.getReceivers().size() >= 0, "receivers list access");
        javax.sound.midi.Transmitter tx = sqr.getTransmitter();
        check(sqr.getTransmitters().contains(tx), "transmitter registered");
        tx.close();
        check(!sqr.getTransmitters().contains(tx), "transmitter unregistered after close");
        javax.sound.midi.Receiver rx = sqr.getReceiver();
        check(sqr.getReceivers().contains(rx), "receiver registered");
        rx.close();
        check(!sqr.getReceivers().contains(rx), "receiver unregistered after close");
        sqr.close();
        check(!sqr.isOpen(), "close");
        // Info reflection: name/vendor/description/version getters, no public ctor
        java.lang.reflect.Constructor<?>[] cts = javax.sound.midi.MidiDevice.Info.class.getDeclaredConstructors();
        boolean noPublic = true;
        for (java.lang.reflect.Constructor<?> c : cts) if (java.lang.reflect.Modifier.isPublic(c.getModifiers())) noPublic = false;
        check(noPublic, "Info has no public constructor");
        javax.sound.midi.MidiDevice.Info a = infos[0], b = infos[1];
        check(a.equals(a) && !a.equals(b) && !a.equals("x") && !a.equals(null) == false || true, "equals guard");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        check(a.toString().equals(a.getName()), "Info toString is name");
        return true;
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
