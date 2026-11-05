/*
 * @test
 * @summary javax.sound.midi.spi.MidiDeviceProvider conformance: abstract hierarchy and methods.
 * @run main MidiDeviceProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiDeviceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiDeviceProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isDeviceSupported"), "method isDeviceSupported");
        check(dm.contains("getDeviceInfo"), "method getDeviceInfo");
        check(dm.contains("getDevice"), "method getDevice");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.spi.MidiDeviceProvider.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.midi.spi.MidiDeviceProvider.class.getModifiers()), "public");
        
        RecProvider p = new RecProvider();
        javax.sound.midi.MidiDevice.Info[] infos = p.getDeviceInfo();
        check(infos.length == 1 && infos[0].getName().equals("Fake"), "recording provider info");
        check(p.isDeviceSupported(infos[0]), "supports own info");
        try { p.isDeviceSupported(null); check(false, "null info must NPE"); }
        catch (NullPointerException e) { passed++; }
        javax.sound.midi.MidiDevice dev = p.getDevice(infos[0]);
        check(dev.getDeviceInfo() == infos[0], "device bound to info");
        // real providers surface indirectly through MidiSystem (2 devices on this host)
        check(javax.sound.midi.MidiSystem.getMidiDeviceInfo().length == 2, "JDK providers registered");
    }
    static class RecProvider extends javax.sound.midi.spi.MidiDeviceProvider {
        static final javax.sound.midi.MidiDevice.Info INFO = new javax.sound.midi.MidiDevice.Info("Fake", "deep", "recording", "1") {};
        public javax.sound.midi.MidiDevice.Info[] getDeviceInfo() { return new javax.sound.midi.MidiDevice.Info[] { INFO }; }
        public javax.sound.midi.MidiDevice getDevice(javax.sound.midi.MidiDevice.Info info) {
            if (!isDeviceSupported(info)) return null;
            return new javax.sound.midi.MidiDevice() {
                public javax.sound.midi.MidiDevice.Info getDeviceInfo() { return INFO; }
                public void open() {} public void close() {} public boolean isOpen() { return false; }
                public long getMicrosecondPosition() { return 0; }
                public int getMaxReceivers() { return -1; } public int getMaxTransmitters() { return -1; }
                public javax.sound.midi.Receiver getReceiver() { return null; }
                public java.util.List<javax.sound.midi.Receiver> getReceivers() { return new java.util.ArrayList<javax.sound.midi.Receiver>(); }
                public javax.sound.midi.Transmitter getTransmitter() { return null; }
                public java.util.List<javax.sound.midi.Transmitter> getTransmitters() { return new java.util.ArrayList<javax.sound.midi.Transmitter>(); }
            };
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
