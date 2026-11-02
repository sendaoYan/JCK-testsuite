/*
 * @test
 * @summary javax.sound.sampled.Mixer conformance: interface hierarchy and methods.
 * @run main MixerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Mixer");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.Line"), "extends javax.sound.sampled.Line");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getMixerInfo"), "method getMixerInfo");
        check(dm.contains("getSourceLineInfo"), "method getSourceLineInfo");
        check(dm.contains("getTargetLineInfo"), "method getTargetLineInfo");
        check(dm.contains("getSourceLineInfo"), "method getSourceLineInfo");
        check(dm.contains("getTargetLineInfo"), "method getTargetLineInfo");
        check(dm.contains("isLineSupported"), "method isLineSupported");
        check(dm.contains("getLine"), "method getLine");
        check(dm.contains("getMaxLines"), "method getMaxLines");
        check(dm.contains("getSourceLines"), "method getSourceLines");
        check(dm.contains("getTargetLines"), "method getTargetLines");
        check(dm.contains("synchronize"), "method synchronize");
        check(dm.contains("unsynchronize"), "method unsynchronize");
        check(dm.contains("isSynchronizationSupported"), "method isSynchronizationSupported");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
