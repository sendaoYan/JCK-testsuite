/*
 * @test
 * @summary javax.sound.midi.MidiFileFormat conformance: behavioral.
 * @run main MidiFileFormatTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiFileFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.MidiFileFormat ff = new javax.sound.midi.MidiFileFormat(1, javax.sound.midi.Sequence.PPQ, 480, 100, 5000L);
        check(ff.getType() == 1, "type");
        check(ff.getDivisionType() == javax.sound.midi.Sequence.PPQ, "divisionType");
        check(ff.getResolution() == 480, "resolution");
        check(ff.getByteLength() == 100, "byteLength");
        check(ff.getMicrosecondLength() == 5000L, "microsecondLength");
        check(javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH");
        check(ff.properties() != null, "properties");
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("k", "v");
        javax.sound.midi.MidiFileFormat ff2 = new javax.sound.midi.MidiFileFormat(0, javax.sound.midi.Sequence.PPQ, 96, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, props);
        check("v".equals(ff2.getProperty("k")), "getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
