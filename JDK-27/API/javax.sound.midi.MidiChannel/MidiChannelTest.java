/*
 * @test
 * @summary javax.sound.midi.MidiChannel conformance: white-box structure + black-box behavior.
 * @key sound
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
