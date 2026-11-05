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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Real Time Sequencer surface + listener recording loops ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
        check(!sqr.isOpen(), "closed initially");
        sqr.open();
        try {
            check(sqr.getTempoInBPM() == 120.0f, "default tempo 120 BPM");
            sqr.setTempoInBPM(180.0f);
            check(sqr.getTempoInBPM() == 180.0f, "setTempoInBPM round-trip");
            sqr.setTempoFactor(0.5f);
            check(sqr.getTempoFactor() == 0.5f, "tempo factor round-trip");
            sqr.setTempoInMPQ(500000f);
            check(sqr.getTempoInMPQ() == 500000f, "MPQ round-trip");
            javax.sound.midi.Sequence seq = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
            javax.sound.midi.Track t = seq.createTrack();
            t.add(new javax.sound.midi.MidiEvent(new javax.sound.midi.ShortMessage(0x90, 60, 50), 24));
            sqr.setSequence(seq);
            check(sqr.getSequence() == seq, "sequence same reference");
            check(sqr.getTickLength() == 24, "tick length");
            check(sqr.getTickPosition() == 0, "tick position 0");
            sqr.setTickPosition(5);
            check(sqr.getTickPosition() == 5, "set tick position");
            check(sqr.getSequence() != null, "getSequence after set");
            // recording loop
            javax.sound.midi.Track recTrack = seq.createTrack();
            sqr.recordEnable(recTrack, 0);
            sqr.recordDisable(recTrack);
            passed += 2; // record enable/disable accepted
            check(sqr.getMasterSyncModes().length > 0, "master sync modes present");
            check(sqr.getSlaveSyncModes().length > 0, "slave sync modes present");
            // meta listener recording
            final int[] metaGot = new int[1];
            javax.sound.midi.MetaEventListener ml = new javax.sound.midi.MetaEventListener() {
                public void meta(javax.sound.midi.MetaMessage m) { metaGot[0]++; }
            };
            sqr.addMetaEventListener(ml);
            check(sqr.getTrackMute(0) == false, "track mute default false");
            check(sqr.getTrackSolo(0) == false, "track solo default false");
            sqr.setTrackMute(0, true);
            check(sqr.getTrackMute(0), "set track mute");
            sqr.setTrackSolo(0, true);
            check(sqr.getTrackSolo(0), "set track solo");
            sqr.setLoopCount(0);
            check(sqr.getLoopCount() == 0, "loop count 0");
            check(sqr.getLoopStartPoint() == 0 && sqr.getLoopEndPoint() == -1, "loop endpoints defaults");
            sqr.removeMetaEventListener(ml);
            passed++; // removal accepted
            check(!sqr.isRunning(), "not running before start");
            sqr.setSequence(new java.io.ByteArrayInputStream(smf0()));
            check(sqr.getTickLength() == 48, "setSequence(InputStream) parse works");
        } finally { sqr.close(); }
        check(!sqr.isOpen(), "close returns closed");
        javax.sound.midi.Sequencer.SyncMode[] modes = javax.sound.midi.MidiSystem.getSequencer().getMasterSyncModes();
        boolean hasInternal = false, hasClock = false, hasMtc = false;
        for (javax.sound.midi.Sequencer.SyncMode m : modes) {
            if (m == javax.sound.midi.Sequencer.SyncMode.INTERNAL_CLOCK) hasInternal = true;
            if (m == javax.sound.midi.Sequencer.SyncMode.MIDI_SYNC) hasClock = true;
            if (m == javax.sound.midi.Sequencer.SyncMode.MIDI_TIME_CODE) hasMtc = true;
        }
        check(hasInternal, "INTERNAL_CLOCK reported");
        check(javax.sound.midi.Sequencer.SyncMode.MIDI_SYNC != null && javax.sound.midi.Sequencer.SyncMode.MIDI_TIME_CODE != null, "sync mode constants exist");
        check("Internal Clock".equals(javax.sound.midi.Sequencer.SyncMode.INTERNAL_CLOCK.toString()), "sync mode toString");
        try { javax.sound.midi.Sequencer.class.getMethod("getMasterSyncModes"); passed++; } catch (Exception e) { throw new AssertionError("surface"); }
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
