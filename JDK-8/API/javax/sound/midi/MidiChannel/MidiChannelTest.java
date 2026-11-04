/*
 * @test
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
