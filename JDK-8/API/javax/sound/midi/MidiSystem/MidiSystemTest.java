/*
 * @test
 * @summary javax.sound.midi.MidiSystem conformance: behavioral.
 * @run main MidiSystemTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.MidiDevice.Info[] infos = javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        check(infos != null, "getMidiDeviceInfo non-null");
        int[] types = javax.sound.midi.MidiSystem.getMidiFileTypes();
        check(types != null && types.length > 0, "getMidiFileTypes non-empty");
        check(javax.sound.midi.MidiSystem.isFileTypeSupported(types[0]), "isFileTypeSupported");
        javax.sound.midi.Sequence seq = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        seq.createTrack();
        check(javax.sound.midi.MidiSystem.isFileTypeSupported(types[0], seq), "isFileTypeSupported(seq)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int written = javax.sound.midi.MidiSystem.write(seq, types[0], bos);
        check(written > 0, "write");
        javax.sound.midi.Sequence back = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getResolution() == 96, "getSequence roundtrip");
        javax.sound.midi.MidiFileFormat fmt = javax.sound.midi.MidiSystem.getMidiFileFormat(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(fmt.getType() == types[0], "getMidiFileFormat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
