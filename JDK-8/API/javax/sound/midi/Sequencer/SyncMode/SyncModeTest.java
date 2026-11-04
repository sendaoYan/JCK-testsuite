/*
 * @test
 * @summary javax.sound.midi.Sequencer.SyncMode conformance: behavioral.
 * @run main SyncModeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SyncModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        SyncImpl s = new SyncImpl("Custom");
        check("Custom".equals(s.toString()), "toString");
        check(javax.sound.midi.Sequencer.SyncMode.INTERNAL_CLOCK != null, "INTERNAL_CLOCK");
        check(javax.sound.midi.Sequencer.SyncMode.MIDI_SYNC != null, "MIDI_SYNC");
        check(javax.sound.midi.Sequencer.SyncMode.MIDI_TIME_CODE != null, "MIDI_TIME_CODE");
        check(javax.sound.midi.Sequencer.SyncMode.NO_SYNC != null, "NO_SYNC");
        check(s.equals(s), "equals self");
        check(s.hashCode() == s.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class SyncImpl extends javax.sound.midi.Sequencer.SyncMode {
    SyncImpl(String n) { super(n); }
}
