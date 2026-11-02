/*
 * @test
 * @summary javax.sound.midi.MidiDevice conformance: interface hierarchy and methods.
 * @run main MidiDeviceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiDeviceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDevice");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getDeviceInfo"), "method getDeviceInfo");
        check(dm.contains("open"), "method open");
        check(dm.contains("close"), "method close");
        check(dm.contains("isOpen"), "method isOpen");
        check(dm.contains("getMicrosecondPosition"), "method getMicrosecondPosition");
        check(dm.contains("getMaxReceivers"), "method getMaxReceivers");
        check(dm.contains("getMaxTransmitters"), "method getMaxTransmitters");
        check(dm.contains("getReceiver"), "method getReceiver");
        check(dm.contains("getReceivers"), "method getReceivers");
        check(dm.contains("getTransmitter"), "method getTransmitter");
        check(dm.contains("getTransmitters"), "method getTransmitters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
