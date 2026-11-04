/*
 * @test
 * @summary javax.sound.midi.Track conformance: behavioral.
 * @run main TrackTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TrackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        javax.sound.midi.Track t = s.createTrack();
        check(t.size() == 1, "size 1 (end-of-track)");
        check(t.ticks() == 0, "ticks 0");
        javax.sound.midi.ShortMessage sm = new javax.sound.midi.ShortMessage();
        sm.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(sm, 0L);
        check(t.add(ev), "add");
        check(t.size() == 2, "size 2");
        check(t.remove(ev), "remove");
        check(t.size() == 1, "size after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
