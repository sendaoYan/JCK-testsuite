/*
 * @test
 * @summary com.sun.jdi.connect.spi.Connection conformance: abstract class shape and methods.
 * @run main ConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.spi.Connection");
        check(Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = methodNames(c);
        check(dm.contains("readPacket"), "method readPacket");
        check(dm.contains("writePacket"), "method writePacket");
        check(dm.contains("close"), "method close");
        check(dm.contains("isOpen"), "method isOpen");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
