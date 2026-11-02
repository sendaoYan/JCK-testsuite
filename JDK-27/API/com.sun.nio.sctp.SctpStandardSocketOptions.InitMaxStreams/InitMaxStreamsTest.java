/*
 * @test
 * @summary com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams conformance: accessors.
 * @run main InitMaxStreamsTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InitMaxStreamsTest {
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
        com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams ims = com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams.create(5, 10);
        check(ims.maxInStreams() == 5, "maxInStreams");
        check(ims.maxOutStreams() == 10, "maxOutStreams");
        check(ims.toString() != null, "toString");
        check(ims.equals(com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams.create(5, 10)), "equals");
        check(ims.hashCode() == com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams.create(5, 10).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
