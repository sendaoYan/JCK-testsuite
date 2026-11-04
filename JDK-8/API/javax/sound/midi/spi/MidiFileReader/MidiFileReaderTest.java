/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileReader conformance: abstract hierarchy and methods.
 * @run main MidiFileReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiFileReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileReader");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(dm.contains("getSequence"), "method getSequence");
        check(dm.contains("getSequence"), "method getSequence");
        check(dm.contains("getSequence"), "method getSequence");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
