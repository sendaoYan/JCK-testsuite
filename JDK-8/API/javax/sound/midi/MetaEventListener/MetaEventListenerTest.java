/*
 * @test
 * @summary javax.sound.midi.MetaEventListener conformance: interface hierarchy and methods.
 * @run main MetaEventListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MetaEventListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MetaEventListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.util.EventListener"), "extends java.util.EventListener");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("meta"), "method meta");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
