/*
 * @test
 * @summary javax.sound.sampled.Control conformance: abstract hierarchy and methods.
 * @run main ControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Control");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getType"), "method getType");
        check(dm.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
