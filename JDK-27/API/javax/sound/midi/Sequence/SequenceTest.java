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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: ctor matrix / track lifecycle / constants / tick math ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 24);
        check(s.getDivisionType() == 0.0f && s.getResolution() == 24, "PPQ ctor fields");
        check(s.getTracks().length == 0, "fresh sequence zero tracks");
        check(s.getMicrosecondLength() == 0, "empty micros 0");
        javax.sound.midi.Track t1 = s.createTrack();
        javax.sound.midi.Track t2 = s.createTrack();
        check(s.getTracks().length == 2 && s.getTracks()[0] == t1 && s.getTracks()[1] == t2, "tracks ordered by creation");
        t1.add(new javax.sound.midi.MidiEvent(new javax.sound.midi.ShortMessage(0x90, 60, 50), 24));
        check(s.getTickLength() == 24, "tickLength from longest track");
        check(s.getMicrosecondLength() == 500000, "micros = ticks * 60_000_000 / (res * bpm-default-120) = 0.5s");
        check(s.deleteTrack(t1), "deleteTrack true");
        check(!s.deleteTrack(t1), "double delete false");
        check(s.getTracks().length == 1, "one track remains");
        check(s.getPatchList().length == 0, "empty patch list");
        check(javax.sound.midi.Sequence.PPQ == 0.0f, "PPQ 0.0");
        check(javax.sound.midi.Sequence.SMPTE_24 == 24.0f && javax.sound.midi.Sequence.SMPTE_25 == 25.0f
           && javax.sound.midi.Sequence.SMPTE_30 == 30.0f && javax.sound.midi.Sequence.SMPTE_30DROP == 29.97f, "SMPTE constants");
        javax.sound.midi.Sequence s2 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.SMPTE_30, 240);
        check(s2.getDivisionType() == 30.0f, "SMPTE ctor");
        javax.sound.midi.Sequence lenient0 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 0);
        check(lenient0.getResolution() == 0, "resolution 0 accepted (no IAE)");
        javax.sound.midi.Sequence lenientNeg = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, -5);
        check(lenientNeg.getResolution() == -5, "negative resolution accepted (no IAE)");
        expectIMD("bad division", new Runnable() { public void run() { try { new javax.sound.midi.Sequence(999f, 24); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Unsupported division type: 999.0");
        javax.sound.midi.Sequence three = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96, 3);
        check(three.getTracks().length == 3, "numTracks ctor pre-allocates 3 empty tracks");
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
