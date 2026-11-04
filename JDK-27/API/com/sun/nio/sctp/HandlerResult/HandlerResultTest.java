/*
 * @test
 * @summary com.sun.nio.sctp.HandlerResult conformance: white-box structure + black-box behavior.
 * @run main HandlerResultTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HandlerResultTest {
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
        Class<?> c = load("com.sun.nio.sctp.HandlerResult");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends java.lang.Enum");
        Set<String> m = methodNames(c);
        check(m.contains("values"), "values");
        check(m.contains("valueOf"), "valueOf");

        com.sun.nio.sctp.HandlerResult[] v = com.sun.nio.sctp.HandlerResult.values();
        check(v.length == 2, "2 constants");
        check(com.sun.nio.sctp.HandlerResult.CONTINUE.name().equals("CONTINUE"), "CONTINUE name");
        check(com.sun.nio.sctp.HandlerResult.RETURN.name().equals("RETURN"), "RETURN name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.HandlerResult c = com.sun.nio.sctp.HandlerResult.CONTINUE;
        com.sun.nio.sctp.HandlerResult r = com.sun.nio.sctp.HandlerResult.RETURN;

        // ordinal order is the declaration order CONTINUE, RETURN
        check(c.ordinal() == 0 && r.ordinal() == 1, "ordinals CONTINUE=0 RETURN=1");
        check(com.sun.nio.sctp.HandlerResult.valueOf("CONTINUE") == c, "valueOf CONTINUE");
        check(com.sun.nio.sctp.HandlerResult.valueOf("RETURN") == r, "valueOf RETURN");
        check(com.sun.nio.sctp.HandlerResult.values()[0] == c, "values()[0] == CONTINUE");
        check(com.sun.nio.sctp.HandlerResult.values()[1] == r, "values()[1] == RETURN");

        // round-trip through name()
        check(com.sun.nio.sctp.HandlerResult.valueOf(c.name()) == c, "valueOf(name) round-trip CONTINUE");
        check(com.sun.nio.sctp.HandlerResult.valueOf(r.name()) == r, "valueOf(name) round-trip RETURN");

        // invalid constant name
        try {
            com.sun.nio.sctp.HandlerResult.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf(BOGUS) throws IllegalArgumentException");
        }

        // switch dispatch works with both constants
        check(pick(c) == 0, "switch CONTINUE -> 0");
        check(pick(r) == 1, "switch RETURN -> 1");

        // toString follows the enum constant name
        check(c.toString().equals("CONTINUE"), "toString CONTINUE");
        check(r.toString().equals("RETURN"), "toString RETURN");
    }

    static int pick(com.sun.nio.sctp.HandlerResult h) {
        switch (h) {
            case CONTINUE: return 0;
            case RETURN:   return 1;
            default:       return -1;
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
