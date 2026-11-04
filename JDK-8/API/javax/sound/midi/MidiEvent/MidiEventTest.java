/*
 * @test
 * @summary javax.sound.midi.MidiEvent conformance: behavioral.
 * @run main MidiEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.ShortMessage sm = new javax.sound.midi.ShortMessage();
        sm.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(sm, 10L);
        check(ev.getMessage() == sm, "getMessage");
        check(ev.getTick() == 10L, "getTick");
        ev.setTick(20L);
        check(ev.getTick() == 20L, "setTick");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
