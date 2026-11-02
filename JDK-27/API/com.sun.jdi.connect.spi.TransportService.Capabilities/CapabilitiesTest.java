/*
 * @test
 * @summary com.sun.jdi.connect.spi.TransportService.Capabilities conformance: abstract class shape and methods.
 * @run main CapabilitiesTest
 */

import java.lang.reflect.*;
import java.util.*;

public class CapabilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.spi.TransportService$Capabilities");
        check(Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = methodNames(c);
        check(dm.contains("supportsMultipleConnections"), "method supportsMultipleConnections");
        check(dm.contains("supportsAttachTimeout"), "method supportsAttachTimeout");
        check(dm.contains("supportsAcceptTimeout"), "method supportsAcceptTimeout");
        check(dm.contains("supportsHandshakeTimeout"), "method supportsHandshakeTimeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
