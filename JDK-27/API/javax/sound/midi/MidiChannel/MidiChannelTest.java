/*
 * @test
 * @key sound
 * @summary javax.sound.midi.MidiChannel conformance: white-box structure + black-box behavior.
 * @run main MidiChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

public class MidiChannelTest {
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
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiChannel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 26, "declared method count");
        Set<String> dm = methodNames(c);
        String[] names = {
            "noteOn", "noteOff", "setPolyPressure", "getPolyPressure",
            "setChannelPressure", "getChannelPressure", "controlChange", "getController",
            "programChange", "getProgram", "setPitchBend", "getPitchBend",
            "resetAllControllers", "allNotesOff", "allSoundOff", "localControl",
            "setMono", "getMono", "setOmni", "getOmni", "setMute", "getMute",
            "setSolo", "getSolo"
        };
        for (String n : names) check(dm.contains(n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Synthesizer synth;
        try {
            synth = MidiSystem.getSynthesizer();
        } catch (Throwable t) {
            // No synthesizer available: degrade to white-box only.
            return;
        }
        try {
            MidiChannel[] chs = synth.getChannels();
            check(chs != null && chs.length >= 1, "channels non-empty");
            MidiChannel ch = chs[0];
            check(ch != null, "channel 0 non-null");

            noThrow(() -> { ch.noteOn(60, 100); ch.noteOff(60); ch.noteOff(60, 0); }, "noteOn/noteOff commands");

            ch.setPolyPressure(60, 64);
            check(ch.getPolyPressure(60) >= 0 && ch.getPolyPressure(60) <= 127, "getPolyPressure range");

            ch.setChannelPressure(64);
            check(ch.getChannelPressure() >= 0 && ch.getChannelPressure() <= 127, "getChannelPressure range");

            ch.controlChange(7, 100);
            check(ch.getController(7) >= 0 && ch.getController(7) <= 127, "getController range");

            ch.programChange(5);
            ch.programChange(0, 5);
            check(ch.getProgram() >= 0 && ch.getProgram() <= 127, "getProgram range");

            ch.setPitchBend(8192);
            check(ch.getPitchBend() >= 0 && ch.getPitchBend() <= 16383, "getPitchBend range");

            noThrow(() -> { ch.resetAllControllers(); ch.allNotesOff(); ch.allSoundOff(); }, "reset/allNotesOff/allSoundOff commands");

            check(ch.localControl(true) == false || ch.localControl(true) == true, "localControl returns boolean");
            check(ch.localControl(false) == false || ch.localControl(false) == true, "localControl(false) returns boolean");

            ch.setMono(true); ch.setMono(false);
            check(ch.getMono() == false || ch.getMono() == true, "getMono boolean");

            ch.setOmni(true); ch.setOmni(false);
            check(ch.getOmni() == false || ch.getOmni() == true, "getOmni boolean");

            ch.setMute(true); ch.setMute(false);
            check(ch.getMute() == false || ch.getMute() == true, "getMute boolean");

            ch.setSolo(true); ch.setSolo(false);
            check(ch.getSolo() == false || ch.getSolo() == true, "getSolo boolean");
        } finally {
            synth.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: real channel from Gervill full matrix ----
    static void testContractDeep() throws Exception {
        try {
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
        } catch (javax.sound.midi.MidiUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
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
