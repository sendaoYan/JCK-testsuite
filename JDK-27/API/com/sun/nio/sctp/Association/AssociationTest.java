/*
 * @test
 * @summary com.sun.nio.sctp.Association conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.Association");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");

        Constructor<?> ctor = c.getDeclaredConstructor(int.class, int.class, int.class);
        check(Modifier.isProtected(ctor.getModifiers()), "protected (int,int,int) constructor");

        Set<String> m = methodNames(c);
        check(m.contains("associationID"), "associationID");
        check(m.contains("maxInboundStreams"), "maxInboundStreams");
        check(m.contains("maxOutboundStreams"), "maxOutboundStreams");

        for (String n : new String[]{"associationID", "maxInboundStreams", "maxOutboundStreams"}) {
            Method mm = c.getDeclaredMethod(n);
            check(Modifier.isFinal(mm.getModifiers()), n + " is final");
            check(mm.getReturnType() == int.class, n + " returns int");
        }

        Set<String> f = fieldNames(c);
        check(f.contains("associationID"), "field associationID");
        check(f.contains("maxInStreams"), "field maxInStreams");
        check(f.contains("maxOutStreams"), "field maxOutStreams");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.Association a = assoc(1, 2, 3);
        check(a.associationID() == 1, "associationID 1");
        check(a.maxInboundStreams() == 2, "maxInboundStreams 2");
        check(a.maxOutboundStreams() == 3, "maxOutboundStreams 3");

        com.sun.nio.sctp.Association b = assoc(42, 100, 200);
        check(b.associationID() == 42, "associationID 42");
        check(b.maxInboundStreams() == 100, "maxInboundStreams 100");
        check(b.maxOutboundStreams() == 200, "maxOutboundStreams 200");

        // boundary values: accessors are plain final field readers, must round-trip exactly
        com.sun.nio.sctp.Association zero = assoc(0, 0, 0);
        check(zero.associationID() == 0 && zero.maxInboundStreams() == 0 && zero.maxOutboundStreams() == 0,
              "zero values round-trip");

        com.sun.nio.sctp.Association big = assoc(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        check(big.associationID() == Integer.MAX_VALUE, "associationID Integer.MAX_VALUE");
        check(big.maxInboundStreams() == Integer.MAX_VALUE, "maxInboundStreams Integer.MAX_VALUE");
        check(big.maxOutboundStreams() == Integer.MAX_VALUE, "maxOutboundStreams Integer.MAX_VALUE");

        com.sun.nio.sctp.Association neg = assoc(-1, -2, -3);
        check(neg.associationID() == -1 && neg.maxInboundStreams() == -2 && neg.maxOutboundStreams() == -3,
              "negative values round-trip (no validation in constructor)");
    }

    static com.sun.nio.sctp.Association assoc(int id, int in, int out) {
        // constructor is protected; accessed via an anonymous subclass
        return new com.sun.nio.sctp.Association(id, in, out) {};
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
