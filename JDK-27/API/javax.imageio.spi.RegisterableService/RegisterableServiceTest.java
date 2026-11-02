/*
 * @test
 * @summary javax.imageio.spi.RegisterableService conformance: interface hierarchy and methods.
 * @run main RegisterableServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class RegisterableServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.RegisterableService");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("onRegistration"), "method onRegistration");
        check(dm.contains("onDeregistration"), "method onDeregistration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
