/*
 * @test
 * @summary javax.imageio.event.IIOReadWarningListener conformance: interface hierarchy and methods.
 * @run main IIOReadWarningListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOReadWarningListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadWarningListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.util.EventListener"), "extends java.util.EventListener");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("warningOccurred"), "method warningOccurred");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
