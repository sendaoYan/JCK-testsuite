/*
 * @test
 * @summary javax.sound.midi.MidiMessage conformance: abstract hierarchy and methods.
 * @run main MidiMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiMessage");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.Cloneable"), "implements java.lang.Cloneable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMessage"), "method getMessage");
        check(dm.contains("getStatus"), "method getStatus");
        check(dm.contains("getLength"), "method getLength");
        check(dm.contains("clone"), "method clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract class surface + subclass dispatch ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.MidiMessage.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.midi.MidiMessage.class.getDeclaredConstructors()[0].getModifiers()), "protected ctor");
        java.lang.reflect.Method gm = javax.sound.midi.MidiMessage.class.getMethod("getMessage");
        java.lang.reflect.Method gs = javax.sound.midi.MidiMessage.class.getMethod("getStatus");
        check(gm.getReturnType() == byte[].class && gs.getReturnType() == int.class, "abstract accessor signatures");
        javax.sound.midi.MidiMessage m = new javax.sound.midi.ShortMessage(0x90, 60, 64);
        check(m instanceof java.lang.Cloneable, "Cloneable");
        check(m.clone() != m, "clone distinct object");
        javax.sound.midi.MidiMessage meta = new javax.sound.midi.MetaMessage();
        check(meta.getStatus() == 0xFF, "MetaMessage default status 0xFF");
        javax.sound.midi.MidiMessage sysex = new javax.sound.midi.SysexMessage();
        check(sysex.getStatus() == 0xF0, "SysexMessage default status 0xF0");
        javax.sound.midi.MidiMessage sh = new javax.sound.midi.ShortMessage();
        check(sh.getStatus() == 0x90, "default ShortMessage status 0x90");
        check(sh.getLength() == 3, "default ShortMessage length 3");
        byte[] db = sh.getMessage();
        check((db[0] & 0xFF) == 0x90 && (db[1] & 0xFF) == 64 && (db[2] & 0xFF) == 127, "default message bytes NOTE_ON 64 127");
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
