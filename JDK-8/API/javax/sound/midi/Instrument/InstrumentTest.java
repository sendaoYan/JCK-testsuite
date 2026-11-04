/*
 * @test
 * @summary javax.sound.midi.Instrument conformance: abstract hierarchy and methods.
 * @run main InstrumentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InstrumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Instrument");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "extends javax.sound.midi.SoundbankResource");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getPatch"), "method getPatch");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
