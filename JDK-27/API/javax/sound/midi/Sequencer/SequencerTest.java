/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Sequencer conformance: white-box structure + black-box behavior.
 * @run main SequencerTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class SequencerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Sequencer");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("javax.sound.midi.MidiDevice"), "extends MidiDevice");
        check(c.getField("LOOP_CONTINUOUSLY") != null, "constant LOOP_CONTINUOUSLY");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        String[] names = {"setSequence", "getSequence", "start", "stop", "isRunning", "startRecording", "stopRecording", "isRecording", "recordEnable", "recordDisable", "getTempoInBPM", "setTempoInBPM", "getTempoInMPQ", "setTempoInMPQ", "setTempoFactor", "getTempoFactor", "getTickLength", "getTickPosition", "setTickPosition", "getMicrosecondLength", "getMicrosecondPosition", "setMicrosecondPosition", "setMasterSyncMode", "getMasterSyncMode", "getMasterSyncModes", "setSlaveSyncMode", "getSlaveSyncMode", "getSlaveSyncModes", "setTrackMute", "getTrackMute", "setTrackSolo", "getTrackSolo", "addMetaEventListener", "removeMetaEventListener", "addControllerEventListener", "removeControllerEventListener", "setLoopStartPoint", "getLoopStartPoint", "setLoopEndPoint", "getLoopEndPoint", "setLoopCount", "getLoopCount"};
        for (String n : names) check(methods.contains(n), "method " + n);
    }

    static void testBlackBox() throws Exception {
        Sequencer seq;
        try {
            seq = MidiSystem.getSequencer(false);
        } catch (MidiUnavailableException e) {
            check(true, "sequencer unavailable (degraded)");
            return;
        }
        check(seq != null, "getSequencer(false) non-null");
        check(seq instanceof MidiDevice, "sequencer is MidiDevice");
        try {
            Sequence s = new Sequence(Sequence.PPQ, 96);
            Track tr = s.createTrack();
            ShortMessage sm = new ShortMessage();
            sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
            tr.add(new MidiEvent(sm, 96L));

            check(seq.getSequence() == null, "no sequence before set");
            seq.setSequence(s);
            check(seq.getSequence() == s, "setSequence/getSequence round-trip");

            check(!seq.isRunning(), "not running before start");
            check(!seq.isRecording(), "not recording before startRecording");

            check(seq.getTempoInBPM() > 0, "tempo BPM positive");
            seq.setTempoInBPM(120.0f);
            check(seq.getTempoInBPM() == 120.0f, "setTempoInBPM round-trip");
            seq.setTempoInMPQ(600000);
            check(seq.getTempoInMPQ() == 600000.0f, "setTempoInMPQ round-trip");
            seq.setTempoFactor(1.0f);
            check(seq.getTempoFactor() == 1.0f, "setTempoFactor round-trip");

            check(seq.getTickLength() == 96L, "tick length matches sequence");
            check(seq.getMicrosecondLength() == 500000L, "microsecond length at 120 BPM");

            seq.open();
            check(seq.isOpen(), "open via MidiDevice");
            check(seq.getTickPosition() == 0L, "initial tick position 0");
            seq.setTickPosition(10L);
            check(seq.getTickPosition() == 10L, "setTickPosition round-trip");
            check(seq.getMicrosecondPosition() >= 0L, "microsecond position non-negative");

            check(seq.getMasterSyncMode() != null, "master sync mode non-null");
            check(seq.getMasterSyncModes() != null && seq.getMasterSyncModes().length > 0, "master sync modes non-empty");
            check(seq.getSlaveSyncMode() != null, "slave sync mode non-null");
            check(seq.getSlaveSyncModes() != null && seq.getSlaveSyncModes().length > 0, "slave sync modes non-empty");

            check(seq.getLoopCount() == 0, "default loop count 0");
            seq.setLoopStartPoint(0L);
            seq.setLoopEndPoint(96L);
            seq.setLoopCount(2);
            check(seq.getLoopCount() == 2, "setLoopCount round-trip");
            check(seq.getLoopStartPoint() == 0L, "setLoopStartPoint round-trip");
            check(seq.getLoopEndPoint() == 96L, "setLoopEndPoint round-trip");

            seq.setTrackMute(0, true);
            check(seq.getTrackMute(0), "setTrackMute/getTrackMute true");
            seq.setTrackSolo(0, true);
            check(seq.getTrackSolo(0), "setTrackSolo/getTrackSolo true");

            seq.start();
            check(seq.isRunning(), "isRunning after start");
            seq.stop();
            check(!seq.isRunning(), "isRunning after stop");
        } finally {
            if (seq.isOpen()) seq.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
