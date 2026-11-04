/*
 * @test
 * @summary javax.sound.sampled.Line conformance: interface hierarchy and methods.
 * @run main LineTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Line");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getLineInfo"), "method getLineInfo");
        check(dm.contains("open"), "method open");
        check(dm.contains("close"), "method close");
        check(dm.contains("isOpen"), "method isOpen");
        check(dm.contains("getControls"), "method getControls");
        check(dm.contains("isControlSupported"), "method isControlSupported");
        check(dm.contains("getControl"), "method getControl");
        check(dm.contains("addLineListener"), "method addLineListener");
        check(dm.contains("removeLineListener"), "method removeLineListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
