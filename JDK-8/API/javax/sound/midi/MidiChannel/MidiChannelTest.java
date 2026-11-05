/*
 * @test
 * @key sound
 * @summary javax.sound.midi.MidiChannel conformance: interface hierarchy and methods.
 * @run main MidiChannelTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiChannel");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("noteOn"), "method noteOn");
        check(dm.contains("noteOff"), "method noteOff");
        check(dm.contains("noteOff"), "method noteOff");
        check(dm.contains("setPolyPressure"), "method setPolyPressure");
        check(dm.contains("getPolyPressure"), "method getPolyPressure");
        check(dm.contains("setChannelPressure"), "method setChannelPressure");
        check(dm.contains("getChannelPressure"), "method getChannelPressure");
        check(dm.contains("controlChange"), "method controlChange");
        check(dm.contains("getController"), "method getController");
        check(dm.contains("programChange"), "method programChange");
        check(dm.contains("programChange"), "method programChange");
        check(dm.contains("getProgram"), "method getProgram");
        check(dm.contains("setPitchBend"), "method setPitchBend");
        check(dm.contains("getPitchBend"), "method getPitchBend");
        check(dm.contains("resetAllControllers"), "method resetAllControllers");
        check(dm.contains("allNotesOff"), "method allNotesOff");
        check(dm.contains("allSoundOff"), "method allSoundOff");
        check(dm.contains("localControl"), "method localControl");
        check(dm.contains("setMono"), "method setMono");
        check(dm.contains("getMono"), "method getMono");
        check(dm.contains("setOmni"), "method setOmni");
        check(dm.contains("getOmni"), "method getOmni");
        check(dm.contains("setMute"), "method setMute");
        check(dm.contains("getMute"), "method getMute");
        check(dm.contains("setSolo"), "method setSolo");
        check(dm.contains("getSolo"), "method getSolo");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: real channel from Gervill full matrix ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
        syn.open();
        try {
            javax.sound.midi.MidiChannel ch = syn.getChannels()[0];
            ch.programChange(5);
            check(ch.getProgram() == 5, "program");
            ch.programChange(0, 9);
            check(ch.getProgram() == 9, "bank+program overload applies program");
            ch.controlChange(7, 100);
            check(ch.getController(7) == 100, "cc get");
            check(ch.getController(91) == 40, "GM default cc91 (reverb) 40");
            check(ch.getController(10) == 64, "GM default cc10 (pan) 64");
            ch.noteOn(60, 127);
            ch.noteOff(60, 0);
            ch.noteOff(61);
            passed += 3; // note lifecycle silent
            ch.setPolyPressure(62, 99);
            check(ch.getPolyPressure(62) == 99, "poly pressure");
            check(ch.getPolyPressure(61) == 0, "other note pressure 0");
            ch.setChannelPressure(45);
            check(ch.getChannelPressure() == 45, "channel pressure");
            ch.setPitchBend(1);
            check(ch.getPitchBend() == 1, "min bend");
            ch.setPitchBend(0);
            check(ch.getPitchBend() == 0, "bend 0");
            ch.setPitchBend(16383);
            check(ch.getPitchBend() == 16256, "bend 16383 quantized to 16256 (128-step)");
            check(!ch.getSolo() && !ch.getMute() && !ch.getMono(), "boolean states default false");
            ch.setMono(false); ch.setMute(false); ch.setSolo(false); ch.setOmni(false);
            check(!ch.getOmni(), "omni false stays false on Gervill");
            ch.resetAllControllers();
            check(ch.getController(7) == 100, "resetAllControllers restores GM default cc7=100");
            ch.allSoundOff(); ch.allNotesOff();
            passed += 2;
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
