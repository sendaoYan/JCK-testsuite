/*
 * @test
 * @summary javax.sound.midi.Transmitter conformance: interface hierarchy and methods.
 * @run main TransmitterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TransmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Transmitter");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setReceiver"), "method setReceiver");
        check(dm.contains("getReceiver"), "method getReceiver");
        check(dm.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
