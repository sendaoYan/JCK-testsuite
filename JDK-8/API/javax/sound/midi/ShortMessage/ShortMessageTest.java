/*
 * @test
 * @summary javax.sound.midi.ShortMessage conformance: behavioral.
 * @run main ShortMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ShortMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.ShortMessage m = new javax.sound.midi.ShortMessage();
        m.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        check(m.getStatus() == javax.sound.midi.ShortMessage.NOTE_ON, "status");
        check(m.getCommand() == javax.sound.midi.ShortMessage.NOTE_ON, "command");
        check(m.getChannel() == 0, "channel");
        check(m.getData1() == 60, "data1");
        check(m.getData2() == 100, "data2");
        check(m.getLength() == 3, "length");
        javax.sound.midi.ShortMessage m2 = new javax.sound.midi.ShortMessage(javax.sound.midi.ShortMessage.PROGRAM_CHANGE | 1, 5, 0);
        check(m2.getCommand() == javax.sound.midi.ShortMessage.PROGRAM_CHANGE, "programChange command");
        check(m2.getChannel() == 1, "channel 1");
        check(m2.getData1() == 5, "program");
        javax.sound.midi.ShortMessage m3 = (javax.sound.midi.ShortMessage) m.clone();
        check(m3.getCommand() == javax.sound.midi.ShortMessage.NOTE_ON, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
