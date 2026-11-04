/*
 * @test
 * @summary javax.imageio.spi.IIOServiceProvider conformance: abstract hierarchy and methods.
 * @run main IIOServiceProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.IIOServiceProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.spi.RegisterableService"), "implements javax.imageio.spi.RegisterableService");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("onRegistration"), "method onRegistration");
        check(dm.contains("onDeregistration"), "method onDeregistration");
        check(dm.contains("getVendorName"), "method getVendorName");
        check(dm.contains("getVersion"), "method getVersion");
        check(dm.contains("getDescription"), "method getDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
