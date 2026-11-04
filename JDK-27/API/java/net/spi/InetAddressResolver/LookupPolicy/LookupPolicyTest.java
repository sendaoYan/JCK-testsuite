/*
 * @test
 * @summary java.net.spi.InetAddressResolver.LookupPolicy conformance: API type verification.
 * @run main LookupPolicyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LookupPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final int IPV4 = InetAddressResolver0.IPV4;
    static final int IPV6 = InetAddressResolver0.IPV6;
    static final int IPV4_FIRST = InetAddressResolver0.IPV4_FIRST;
    static final int IPV6_FIRST = InetAddressResolver0.IPV6_FIRST;

    /** Helper reference to the enclosing interface kept separate to avoid import noise. */
    static class InetAddressResolver0 {
        static final int IPV4 = java.net.spi.InetAddressResolver.LookupPolicy.IPV4;
        static final int IPV6 = java.net.spi.InetAddressResolver.LookupPolicy.IPV6;
        static final int IPV4_FIRST = java.net.spi.InetAddressResolver.LookupPolicy.IPV4_FIRST;
        static final int IPV6_FIRST = java.net.spi.InetAddressResolver.LookupPolicy.IPV6_FIRST;
    }

    static Class<?> lpClass() throws Exception {
        return Class.forName("java.net.spi.InetAddressResolver$LookupPolicy");
    }

    /** Returns characteristics on success, or -1 together with the exact IAE message on failure. */
    static String outcome(int v) {
        try {
            int c = java.net.spi.InetAddressResolver.LookupPolicy.of(v).characteristics();
            return "OK:" + c;
        } catch (IllegalArgumentException e) {
            return "IAE:" + e.getMessage();
        }
    }

    // ---- Part 1: white-box type shape (probed on JDK-27 / jdk-27.0.1.0.2+1) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = lpClass();
        check(Modifier.isPublic(c.getModifiers()), "LookupPolicy is public");
        check(Modifier.isFinal(c.getModifiers()), "LookupPolicy is final");
        check(c.getDeclaringClass() == java.net.spi.InetAddressResolver.class,
                "nested in InetAddressResolver");
        check("LookupPolicy".equals(c.getSimpleName()), "simple name");
        check("java.net.spi.InetAddressResolver$LookupPolicy".equals(c.getName()),
                "binary name");

        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(ctors[0].getModifiers()), "constructor is private (factory-only)");
        check(ctors[0].getParameterCount() == 1
                && ctors[0].getParameterTypes()[0] == int.class, "private ctor takes int");

        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly two declared methods: of(int), characteristics()");
        Method of = c.getMethod("of", int.class);
        Method chars = c.getMethod("characteristics");
        check(Modifier.isStatic(of.getModifiers()), "of is static");
        check(Modifier.isPublic(of.getModifiers()), "of is public");
        check(of.getReturnType() == c, "of returns LookupPolicy");
        check(!Modifier.isStatic(chars.getModifiers()), "characteristics is instance method");
        check(chars.getReturnType() == int.class, "characteristics returns int");
        check(of.getExceptionTypes().length == 0, "of declares no checked exceptions");
        check(chars.getExceptionTypes().length == 0, "characteristics declares no exceptions");

        // Identity semantics: equals/hashCode/toString are NOT overridden (probed).
        try {
            c.getDeclaredMethod("equals", Object.class);
            check(false, "equals must not be declared");
        } catch (NoSuchMethodException expected) {
            check(true, "equals not overridden (identity semantics)");
        }
        try {
            c.getDeclaredMethod("hashCode");
            check(false, "hashCode must not be declared");
        } catch (NoSuchMethodException expected) {
            check(true, "hashCode not overridden");
        }
        try {
            c.getDeclaredMethod("toString");
            check(false, "toString must not be declared");
        } catch (NoSuchMethodException expected) {
            check(true, "toString not overridden");
        }

        // Fields: the four public static final int constants plus the private
        // final int characteristics instance field (probed shape).
        Field[] fs = c.getDeclaredFields();
        check(fs.length == 5, "five declared fields: 4 constants + characteristics instance field");
        Set<String> names = new HashSet<>();
        for (Field f : fs) {
            check(f.getType() == int.class, "field " + f.getName() + " is int");
            if (Modifier.isStatic(f.getModifiers())) {
                check(Modifier.isPublic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                        "static field " + f.getName() + " is public static final");
            } else {
                check(f.getName().equals("characteristics")
                        && Modifier.isPrivate(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                        "instance field is private final int characteristics");
            }
            names.add(f.getName());
        }
        check(names.contains("IPV4") && names.contains("IPV6")
                && names.contains("IPV4_FIRST") && names.contains("IPV6_FIRST"),
                "constant names are IPV4/IPV6/IPV4_FIRST/IPV6_FIRST");
    }

    // ---- Part 2: constant exact values (probed: 1,2,4,8) ----
    static void testConstants() throws Exception {
        check(IPV4 == 1, "IPV4 == 1");
        check(IPV6 == 2, "IPV6 == 2");
        check(IPV4_FIRST == 4, "IPV4_FIRST == 4");
        check(IPV6_FIRST == 8, "IPV6_FIRST == 8");
        check(IPV4 == (1 << 0) && IPV6 == (1 << 1) && IPV4_FIRST == (1 << 2) && IPV6_FIRST == (1 << 3),
                "constants are consecutive low bits 1<<0..1<<3");
        // The four known bits form the low nibble.
        check((IPV4 | IPV6 | IPV4_FIRST | IPV6_FIRST) == 0xF, "union of known bits is 0xF");
    }

    // ---- Part 3: legal combinations, full round-trip ----
    static void testLegalMatrix() {
        // All legal combinations of the four known bits (probed set):
        int[] legal = {IPV4, IPV6, IPV4 | IPV6, IPV4 | IPV4_FIRST, IPV4 | IPV6 | IPV4_FIRST,
                IPV6 | IPV6_FIRST, IPV4 | IPV6 | IPV6_FIRST};
        for (int v : legal) {
            java.net.spi.InetAddressResolver.LookupPolicy lp = java.net.spi.InetAddressResolver.LookupPolicy.of(v);
            check(lp != null, "of(" + v + ") non-null");
            check(lp.characteristics() == v, "characteristics round-trip for " + v);
            check(lp.toString() != null, "toString non-null for " + v);
        }
        // Named re-checks of the seven legal shapes.
        check(outcome(IPV4).equals("OK:1"), "IPV4 only");
        check(outcome(IPV6).equals("OK:2"), "IPV6 only");
        check(outcome(3).equals("OK:3"), "both families, system order");
        check(outcome(5).equals("OK:5"), "IPv4 only, IPv4 first");
        check(outcome(7).equals("OK:7"), "both families, IPv4 first");
        check(outcome(10).equals("OK:10"), "IPv6 only, IPv6 first");
        check(outcome(11).equals("OK:11"), "both families, IPv6 first");
    }

    // ---- Part 4: driven table for every 4-bit value 0..15 (probed classification) ----
    static void testFourBitTable() {
        // value -> "OK:c" | "IAE:<exact message>"
        String[] expected = new String[16];
        expected[0] = "IAE:No address type specified";              // 0: no family
        expected[1] = "OK:1";                                        // IPV4
        expected[2] = "OK:2";                                        // IPV6
        expected[3] = "OK:3";                                        // IPV4|IPV6
        expected[4] = "IAE:No address type specified";              // IPV4_FIRST alone
        expected[5] = "OK:5";                                        // IPV4|IPV4_FIRST
        expected[6] = "IAE:Addresses order and type do not match";   // IPV6|IPV4_FIRST
        expected[7] = "OK:7";                                        // IPV4|IPV6|IPV4_FIRST
        expected[8] = "IAE:No address type specified";              // IPV6_FIRST alone
        expected[9] = "IAE:Addresses order and type do not match";   // IPV4|IPV6_FIRST
        expected[10] = "OK:10";                                      // IPV6|IPV6_FIRST
        expected[11] = "OK:11";                                      // IPV4|IPV6|IPV6_FIRST
        expected[12] = "IAE:No address type specified";             // both FIRST, no family
        expected[13] = "IAE:Addresses order cannot be determined";   // IPV4|both FIRST
        expected[14] = "IAE:Addresses order cannot be determined";   // IPV6|both FIRST
        expected[15] = "IAE:Addresses order cannot be determined";   // all four bits
        for (int v = 0; v < 16; v++) {
            check(expected[v].equals(outcome(v)), "of(" + v + ") -> " + expected[v]
                    + ", got " + outcome(v));
        }
    }

    // ---- Part 5: extended / negative values with exact messages (probed) ----
    static void testExtendedNegatives() {
        check(outcome(-1).equals("IAE:Addresses order cannot be determined"),
                "of(-1): both FIRST bits set dominates");
        check(outcome(0xFFFFFFFF).equals("IAE:Addresses order cannot be determined"),
                "of(0xFFFFFFFF)");
        check(outcome(0xFFFFFFFE).equals("IAE:Addresses order cannot be determined"),
                "of(0xFFFFFFFE): both families + both FIRST");
        check(outcome(0x7FFFFFFF).equals("IAE:Addresses order cannot be determined"),
                "of(0x7FFFFFFF)");
        check(outcome(0x80000000).equals("IAE:No address type specified"),
                "of(Integer.MIN_VALUE bit alone): no family");
        check(outcome(Integer.MIN_VALUE).equals("IAE:No address type specified"),
                "of(Integer.MIN_VALUE)");
        check(outcome(0x10).equals("IAE:No address type specified"),
                "reserved bit 0x10 alone: treated as no family");
        check(outcome(0x20).equals("IAE:No address type specified"), "reserved bit 0x20 alone");
        check(outcome(0x10000).equals("IAE:No address type specified"), "high bit alone");

        // Probed laxness of the JDK-27 implementation: unknown/reserved bits are NOT
        // rejected when a valid known-bit combination is present; the value round-trips.
        check(outcome(IPV4 | 0x10).equals("OK:" + (IPV4 | 0x10)),
                "reserved bit 0x10 alongside IPV4 is accepted (implementation laxness, probed)");
        check(outcome(IPV6 | 0x80000000).equals("OK:" + (IPV6 | 0x80000000)),
                "sign bit alongside IPV6 accepted (probed)");
        check(outcome(0x80000001).equals("OK:" + 0x80000001),
                "IPV4|sign bit accepted; characteristics() keeps raw value");
        check(outcome(0x10002).equals("OK:" + 0x10002), "IPV6|bit 16 accepted (probed)");
        check(outcome(0x80000005).equals("OK:" + 0x80000005),
                "IPV4|IPV4_FIRST|sign bit accepted (probed)");

        // Exception type is exactly IllegalArgumentException for every bad input.
        for (int v : new int[]{0, 4, 6, 8, 9, 12, 13, 14, 15, -1, -2}) {
            try {
                java.net.spi.InetAddressResolver.LookupPolicy.of(v);
                check(false, "of(" + v + ") must throw IAE");
            } catch (IllegalArgumentException e) {
                check(true, "of(" + v + ") throws IAE");
            }
        }
    }

    // ---- Part 6: identity semantics (equals/hashCode/toString inherited from Object) ----
    static void testIdentitySemantics() {
        java.net.spi.InetAddressResolver.LookupPolicy a =
                java.net.spi.InetAddressResolver.LookupPolicy.of(IPV4);
        java.net.spi.InetAddressResolver.LookupPolicy b =
                java.net.spi.InetAddressResolver.LookupPolicy.of(IPV4);
        check(a != b, "of() creates a fresh instance each call (probed)");
        check(!a.equals(b), "equals not value-based: two policies with same bits differ (probed)");
        check(a.equals(a), "identity equals is reflexive");
        check(!a.equals(null), "equals(null) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        String ts = a.toString();
        check(ts != null && ts.contains("LookupPolicy"),
                "default toString contains simple class name");
        // Same characteristics value through different construction paths.
        check(a.characteristics() == b.characteristics(), "same bits, same characteristics()");
    }

    public static void main(String[] args) throws Exception {
        java.net.spi.InetAddressResolver.LookupPolicy lp = java.net.spi.InetAddressResolver.LookupPolicy.of(java.net.spi.InetAddressResolver.LookupPolicy.IPV4);
check(lp.characteristics() == java.net.spi.InetAddressResolver.LookupPolicy.IPV4, "characteristics");
        testWhiteBox();
        testConstants();
        testLegalMatrix();
        testFourBitTable();
        testExtendedNegatives();
        testIdentitySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
