/*
 * @test
 * @summary javax.sound.sampled.Port conformance: interface hierarchy and methods.
 * @run main PortTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PortTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Port");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.sound.sampled.Line"), "extends javax.sound.sampled.Line");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
