/*
 * @test
 * @summary javax.sound.midi.InvalidMidiDataException conformance: behavioral.
 * @run main InvalidMidiDataExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InvalidMidiDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.InvalidMidiDataException e = new javax.sound.midi.InvalidMidiDataException();
        check(e instanceof Exception, "is Exception");
        javax.sound.midi.InvalidMidiDataException e2 = new javax.sound.midi.InvalidMidiDataException("m");
        check("m".equals(e2.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
