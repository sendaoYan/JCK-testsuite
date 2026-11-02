/*
 * @test
 * @summary javax.sound.sampled.Clip conformance: interface hierarchy and methods.
 * @run main ClipTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ClipTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Clip");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.DataLine"), "extends javax.sound.sampled.DataLine");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("open"), "method open");
        check(dm.contains("open"), "method open");
        check(dm.contains("getFrameLength"), "method getFrameLength");
        check(dm.contains("getMicrosecondLength"), "method getMicrosecondLength");
        check(dm.contains("setFramePosition"), "method setFramePosition");
        check(dm.contains("setMicrosecondPosition"), "method setMicrosecondPosition");
        check(dm.contains("setLoopPoints"), "method setLoopPoints");
        check(dm.contains("loop"), "method loop");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
