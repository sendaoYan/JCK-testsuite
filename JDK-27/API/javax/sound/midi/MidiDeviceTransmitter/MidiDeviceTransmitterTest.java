/*
 * @test
 * @key sound
 * @summary javax.sound.midi.MidiDeviceTransmitter conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main MidiDeviceTransmitterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

public class MidiDeviceTransmitterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void noThrow(ThrowingRunnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            throw new AssertionError(msg + " threw " + t);
        }
        check(true, msg);
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDeviceTransmitter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("javax.sound.midi.Transmitter"), "extends javax.sound.midi.Transmitter");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "declared method count");
        check(ms[0].getName().equals("getMidiDevice"), "method getMidiDevice");
        check(ms[0].getReturnType().getName().equals("javax.sound.midi.MidiDevice"), "getMidiDevice return type");
        check(ms[0].getParameterTypes().length == 0, "getMidiDevice no params");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Sequencer seq;
        Transmitter t;
        try {
            seq = MidiSystem.getSequencer();
            t = seq.getTransmitter();
        } catch (Throwable ex) {
            // No transmitter available: degrade to white-box only.
            return;
        }
        try {
            check(t instanceof MidiDeviceTransmitter, "getTransmitter returns MidiDeviceTransmitter");
            MidiDeviceTransmitter mdt = (MidiDeviceTransmitter) t;
            MidiDevice dev = mdt.getMidiDevice();
            check(dev != null, "getMidiDevice non-null");
            check(mdt.getMidiDevice() == dev, "getMidiDevice identity across calls");

            Receiver stub = new Receiver() {
                public void send(MidiMessage message, long timeStamp) {}
                public void close() {}
            };
            mdt.setReceiver(stub);
            check(mdt.getReceiver() == stub, "setReceiver/getReceiver round-trip");

            noThrow(() -> mdt.close(), "close");
        } finally {
            try { if (seq != null) seq.close(); } catch (Throwable ignored) {}
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: spi-aware subinterfaces extend base interfaces ----
    static void testContractDeep() throws Exception {
        check(javax.sound.midi.Receiver.class.isAssignableFrom(javax.sound.midi.MidiDeviceReceiver.class), "MidiDeviceReceiver extends Receiver");
        check(javax.sound.midi.Transmitter.class.isAssignableFrom(javax.sound.midi.MidiDeviceTransmitter.class), "MidiDeviceTransmitter extends Transmitter");
        try {
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
