/*
 * @test
 * @summary javax.sound.midi.MidiUnavailableException conformance: behavioral.
 * @run main MidiUnavailableExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiUnavailableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.MidiUnavailableException e = new javax.sound.midi.MidiUnavailableException();
        check(e instanceof Exception, "is Exception");
        javax.sound.midi.MidiUnavailableException e2 = new javax.sound.midi.MidiUnavailableException("m");
        check("m".equals(e2.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
