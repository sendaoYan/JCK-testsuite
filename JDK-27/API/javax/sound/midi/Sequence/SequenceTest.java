/*
 * @test
 * @summary javax.sound.midi.Sequence conformance: white-box structure + black-box behavior.
 * @run main SequenceTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class SequenceTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()), msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Sequence");
        check(!c.isInterface(), "is not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "is not abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> fs = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fs.add(f.getName());
        check(fs.contains("divisionType"), "field divisionType");
        check(fs.contains("resolution"), "field resolution");
        check(fs.contains("tracks"), "field tracks");
    }

    static void testBlackBox() throws Exception {
        check(Sequence.PPQ == 0.0f, "PPQ == 0.0f");
        check(Sequence.SMPTE_24 == 24.0f, "SMPTE_24 == 24.0f");
        check(Sequence.SMPTE_25 == 25.0f, "SMPTE_25 == 25.0f");
        check(Sequence.SMPTE_30DROP == 29.97f, "SMPTE_30DROP == 29.97f");
        check(Sequence.SMPTE_30 == 30.0f, "SMPTE_30 == 30.0f");

        Sequence s = new Sequence(Sequence.PPQ, 96);
        check(s.getDivisionType() == Sequence.PPQ, "divisionType PPQ");
        check(s.getResolution() == 96, "resolution 96");
        check(s.getTracks().length == 0, "initial track count 0");
        check(s.getTickLength() == 0L, "tick length 0 initially");
        check(s.getMicrosecondLength() == 0L, "microsecond length 0 initially");
        check(s.getPatchList() != null && s.getPatchList().length == 0, "patch list empty");

        Track t = s.createTrack();
        check(s.getTracks().length == 1, "createTrack adds track");
        check(s.getTracks()[0] == t, "getTracks[0] is created track");

        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
        t.add(new MidiEvent(sm, 96L));
        check(s.getTickLength() == 96L, "tick length 96 after event");
        check(s.getMicrosecondLength() == 500000L, "microsecond length 96 ticks at default 120 BPM");

        Sequence s2 = new Sequence(Sequence.PPQ, 96, 3);
        check(s2.getTracks().length == 3, "constructor with numTracks creates 3 tracks");
        Track foreign = new Sequence(Sequence.PPQ, 96).createTrack();
        check(!s2.deleteTrack(foreign), "deleteTrack non-member returns false");
        check(s2.deleteTrack(s2.getTracks()[0]), "deleteTrack member returns true");
        check(s2.getTracks().length == 2, "track count after delete");

        Sequence smpte = new Sequence(Sequence.SMPTE_25, 40);
        Track st = smpte.createTrack();
        ShortMessage sm2 = new ShortMessage();
        sm2.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
        st.add(new MidiEvent(sm2, 1000L));
        check(smpte.getTickLength() == 1000L, "SMPTE tick length");
        check(smpte.getMicrosecondLength() == 1000000L, "SMPTE microsecond length (1000 ticks / 25fps * 40)");

        expectThrows(InvalidMidiDataException.class, () -> new Sequence(1.5f, 96), "invalid division type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
