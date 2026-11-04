/*
 * @test
 * @summary com.sun.nio.sctp.SctpStandardSocketOptions conformance: standard option constants.
 * @run main SctpStandardSocketOptionsTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SctpStandardSocketOptionsTest {
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
        Class<?> c = load("com.sun.nio.sctp.SctpStandardSocketOptions");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_DISABLE_FRAGMENTS != null, "SCTP_DISABLE_FRAGMENTS");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_EXPLICIT_COMPLETE != null, "SCTP_EXPLICIT_COMPLETE");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_FRAGMENT_INTERLEAVE != null, "SCTP_FRAGMENT_INTERLEAVE");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS != null, "SCTP_INIT_MAXSTREAMS");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_NODELAY != null, "SCTP_NODELAY");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_PRIMARY_ADDR != null, "SCTP_PRIMARY_ADDR");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SCTP_SET_PEER_PRIMARY_ADDR != null, "SCTP_SET_PEER_PRIMARY_ADDR");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SO_SNDBUF != null, "SO_SNDBUF");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SO_RCVBUF != null, "SO_RCVBUF");
        check(com.sun.nio.sctp.SctpStandardSocketOptions.SO_LINGER != null, "SO_LINGER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
