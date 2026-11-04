/*
 * @test
 * @summary javax.sound.midi.VoiceStatus conformance: behavioral.
 * @run main VoiceStatusTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class VoiceStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.VoiceStatus v = new javax.sound.midi.VoiceStatus();
        check(!v.active, "active default false");
        check(v.channel == 0, "channel 0");
        check(v.note == 0, "note 0");
        v.active = true; v.channel = 1; v.bank = 2; v.program = 3; v.note = 60; v.volume = 100;
        check(v.active && v.channel == 1 && v.note == 60 && v.volume == 100, "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
