/*
 * @test
 * @summary javax.sound.midi.MidiMessage conformance: abstract hierarchy and methods.
 * @run main MidiMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiMessage");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.Cloneable"), "implements java.lang.Cloneable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMessage"), "method getMessage");
        check(dm.contains("getStatus"), "method getStatus");
        check(dm.contains("getLength"), "method getLength");
        check(dm.contains("clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
