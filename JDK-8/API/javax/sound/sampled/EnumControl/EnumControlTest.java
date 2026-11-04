/*
 * @test
 * @summary javax.sound.sampled.EnumControl conformance: abstract hierarchy and methods.
 * @run main EnumControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class EnumControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.EnumControl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "extends javax.sound.sampled.Control");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
