/*
 * @test
 * @summary javax.sound.midi.Sequence conformance: behavioral.
 * @run main SequenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.Sequence s = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        check(s.getDivisionType() == javax.sound.midi.Sequence.PPQ, "divisionType");
        check(s.getResolution() == 96, "resolution");
        javax.sound.midi.Track t = s.createTrack();
        check(t != null, "createTrack");
        check(s.getTracks().length == 1, "tracks 1");
        check(s.getTickLength() == 0, "tickLength 0");
        check(s.getMicrosecondLength() == 0, "microsecondLength 0");
        check(s.getPatchList() != null, "patchList");
        javax.sound.midi.Sequence s2 = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96, 2);
        check(s2.getTracks().length == 2, "tracks 2");
        check(s2.deleteTrack(s2.getTracks()[0]), "deleteTrack");
        check(s2.getTracks().length == 1, "tracks after delete");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
