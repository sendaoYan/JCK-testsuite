/*
 * @test
 * @summary javax.sound.sampled.DataLine conformance: interface hierarchy and methods.
 * @run main DataLineTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class DataLineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.DataLine");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.Line"), "extends javax.sound.sampled.Line");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("drain"), "method drain");
        check(dm.contains("flush"), "method flush");
        check(dm.contains("start"), "method start");
        check(dm.contains("stop"), "method stop");
        check(dm.contains("isRunning"), "method isRunning");
        check(dm.contains("isActive"), "method isActive");
        check(dm.contains("getFormat"), "method getFormat");
        check(dm.contains("getBufferSize"), "method getBufferSize");
        check(dm.contains("available"), "method available");
        check(dm.contains("getFramePosition"), "method getFramePosition");
        check(dm.contains("getLongFramePosition"), "method getLongFramePosition");
        check(dm.contains("getMicrosecondPosition"), "method getMicrosecondPosition");
        check(dm.contains("getLevel"), "method getLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
