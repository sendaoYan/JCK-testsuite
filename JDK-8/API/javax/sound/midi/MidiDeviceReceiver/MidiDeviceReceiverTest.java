/*
 * @test
 * @summary javax.sound.midi.MidiDeviceReceiver conformance: interface hierarchy and methods.
 * @run main MidiDeviceReceiverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiDeviceReceiverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDeviceReceiver");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.midi.Receiver"), "extends javax.sound.midi.Receiver");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMidiDevice"), "method getMidiDevice");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
