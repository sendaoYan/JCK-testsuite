/*
 * @test
 * @summary javax.sound.midi.Sequencer conformance: interface hierarchy and methods.
 * @run main SequencerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SequencerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Sequencer");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.midi.MidiDevice"), "extends javax.sound.midi.MidiDevice");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setSequence"), "method setSequence");
        check(dm.contains("setSequence"), "method setSequence");
        check(dm.contains("getSequence"), "method getSequence");
        check(dm.contains("start"), "method start");
        check(dm.contains("stop"), "method stop");
        check(dm.contains("isRunning"), "method isRunning");
        check(dm.contains("startRecording"), "method startRecording");
        check(dm.contains("stopRecording"), "method stopRecording");
        check(dm.contains("isRecording"), "method isRecording");
        check(dm.contains("recordEnable"), "method recordEnable");
        check(dm.contains("recordDisable"), "method recordDisable");
        check(dm.contains("getTempoInBPM"), "method getTempoInBPM");
        check(dm.contains("setTempoInBPM"), "method setTempoInBPM");
        check(dm.contains("getTempoInMPQ"), "method getTempoInMPQ");
        check(dm.contains("setTempoInMPQ"), "method setTempoInMPQ");
        check(dm.contains("setTempoFactor"), "method setTempoFactor");
        check(dm.contains("getTempoFactor"), "method getTempoFactor");
        check(dm.contains("getTickLength"), "method getTickLength");
        check(dm.contains("getTickPosition"), "method getTickPosition");
        check(dm.contains("setTickPosition"), "method setTickPosition");
        check(dm.contains("getMicrosecondLength"), "method getMicrosecondLength");
        check(dm.contains("getMicrosecondPosition"), "method getMicrosecondPosition");
        check(dm.contains("setMicrosecondPosition"), "method setMicrosecondPosition");
        check(dm.contains("setMasterSyncMode"), "method setMasterSyncMode");
        check(dm.contains("getMasterSyncMode"), "method getMasterSyncMode");
        check(dm.contains("getMasterSyncModes"), "method getMasterSyncModes");
        check(dm.contains("setSlaveSyncMode"), "method setSlaveSyncMode");
        check(dm.contains("getSlaveSyncMode"), "method getSlaveSyncMode");
        check(dm.contains("getSlaveSyncModes"), "method getSlaveSyncModes");
        check(dm.contains("setTrackMute"), "method setTrackMute");
        check(dm.contains("getTrackMute"), "method getTrackMute");
        check(dm.contains("setTrackSolo"), "method setTrackSolo");
        check(dm.contains("getTrackSolo"), "method getTrackSolo");
        check(dm.contains("addMetaEventListener"), "method addMetaEventListener");
        check(dm.contains("removeMetaEventListener"), "method removeMetaEventListener");
        check(dm.contains("addControllerEventListener"), "method addControllerEventListener");
        check(dm.contains("removeControllerEventListener"), "method removeControllerEventListener");
        check(dm.contains("setLoopStartPoint"), "method setLoopStartPoint");
        check(dm.contains("getLoopStartPoint"), "method getLoopStartPoint");
        check(dm.contains("setLoopEndPoint"), "method setLoopEndPoint");
        check(dm.contains("getLoopEndPoint"), "method getLoopEndPoint");
        check(dm.contains("setLoopCount"), "method setLoopCount");
        check(dm.contains("getLoopCount"), "method getLoopCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
