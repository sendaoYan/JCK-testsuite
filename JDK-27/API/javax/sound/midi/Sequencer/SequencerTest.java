/*
 * @test
 * @summary javax.sound.midi.Sequencer conformance: white-box structure + black-box behavior.
 * @key sound
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
