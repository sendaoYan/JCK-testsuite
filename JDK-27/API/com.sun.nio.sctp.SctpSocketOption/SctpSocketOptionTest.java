/*
 * @test
 * @summary com.sun.nio.sctp.SctpSocketOption conformance: socket option interface.
 * @run main SctpSocketOptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SctpSocketOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.nio.sctp.SctpSocketOption");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.net.SocketOption"), "extends SocketOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
