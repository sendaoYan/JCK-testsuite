/*
 * @test
 * @summary com.sun.nio.sctp.Association conformance: accessors.
 * @run main AssociationTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AssociationTest {
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
        com.sun.nio.sctp.Association assoc = new com.sun.nio.sctp.Association(1, 2, 3) {};
        check(assoc.associationID() == 1, "associationID");
        check(assoc.maxInboundStreams() == 2, "maxInboundStreams");
        check(assoc.maxOutboundStreams() == 3, "maxOutboundStreams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
