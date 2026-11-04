/*
 * @test
 * @summary javax.sound.midi.Synthesizer conformance: interface hierarchy and methods.
 * @run main SynthesizerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SynthesizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Synthesizer");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.midi.MidiDevice"), "extends javax.sound.midi.MidiDevice");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMaxPolyphony"), "method getMaxPolyphony");
        check(dm.contains("getLatency"), "method getLatency");
        check(dm.contains("getChannels"), "method getChannels");
        check(dm.contains("getVoiceStatus"), "method getVoiceStatus");
        check(dm.contains("isSoundbankSupported"), "method isSoundbankSupported");
        check(dm.contains("loadInstrument"), "method loadInstrument");
        check(dm.contains("unloadInstrument"), "method unloadInstrument");
        check(dm.contains("remapInstrument"), "method remapInstrument");
        check(dm.contains("getDefaultSoundbank"), "method getDefaultSoundbank");
        check(dm.contains("getAvailableInstruments"), "method getAvailableInstruments");
        check(dm.contains("getLoadedInstruments"), "method getLoadedInstruments");
        check(dm.contains("loadAllInstruments"), "method loadAllInstruments");
        check(dm.contains("unloadAllInstruments"), "method unloadAllInstruments");
        check(dm.contains("loadInstruments"), "method loadInstruments");
        check(dm.contains("unloadInstruments"), "method unloadInstruments");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
