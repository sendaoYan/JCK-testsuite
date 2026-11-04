/*
 * @test
 * @summary java.net.StandardProtocolFamily conformance: white-box structure + black-box behavior.
 * @run main StandardProtocolFamilyTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class StandardProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.isEnum(), "enum");
        check(ProtocolFamily.class.isAssignableFrom(c), "implements ProtocolFamily");
    }

    static void testBlackBox() throws Exception {
        StandardProtocolFamily[] vals = StandardProtocolFamily.values();
        check(vals.length == 3, "3 constants");
        List<StandardProtocolFamily> list = Arrays.asList(vals);
        check(list.contains(StandardProtocolFamily.INET), "INET present");
        check(list.contains(StandardProtocolFamily.INET6), "INET6 present");
        check(list.contains(StandardProtocolFamily.UNIX), "UNIX present");

        check(StandardProtocolFamily.INET.name().equals("INET"), "INET name");
        check(StandardProtocolFamily.INET6.name().equals("INET6"), "INET6 name");
        check(StandardProtocolFamily.UNIX.name().equals("UNIX"), "UNIX name");

        check(StandardProtocolFamily.valueOf("INET") == StandardProtocolFamily.INET, "valueOf INET");
        check(StandardProtocolFamily.valueOf("UNIX") == StandardProtocolFamily.UNIX, "valueOf UNIX");

        // implements ProtocolFamily.name()
        ProtocolFamily pf = StandardProtocolFamily.INET;
        check(pf.name().equals("INET"), "ProtocolFamily.name");

        // enum identity: valueOf roundtrip and ordinal ordering
        check(StandardProtocolFamily.INET.ordinal() == 0, "INET ordinal 0");
        check(Arrays.asList(vals).indexOf(StandardProtocolFamily.INET6) == 1, "INET6 ordinal 1");

        fails(IllegalArgumentException.class, "valueOf bogus", () -> StandardProtocolFamily.valueOf("BOGUS"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
