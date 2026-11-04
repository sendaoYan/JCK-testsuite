/*
 * @test
 * @summary java.net.Proxy.Type conformance: enum constants and members.
 * @run main TypeTest
 */

import java.net.Proxy;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original assertions (preserved verbatim) ----
    static void testOriginalAssertions() {
        check(Proxy.Type.valueOf("SOCKS") == Proxy.Type.SOCKS, "valueOf");
        check(Proxy.Type.values().length == 3, "3 values");
        check(Proxy.Type.DIRECT.name().equals("DIRECT"), "DIRECT");
    }

    // ---- enum value matrix (probed on JDK-27) ----
    static void testEnumMatrix() {
        Proxy.Type[] vs = Proxy.Type.values();
        check(vs.length == 3, "values().length == 3");
        check(vs[0] == Proxy.Type.DIRECT, "values()[0] DIRECT");
        check(vs[1] == Proxy.Type.HTTP, "values()[1] HTTP");
        check(vs[2] == Proxy.Type.SOCKS, "values()[2] SOCKS");
        check(Proxy.Type.DIRECT.name().equals("DIRECT"), "DIRECT name");
        check(Proxy.Type.HTTP.name().equals("HTTP"), "HTTP name");
        check(Proxy.Type.SOCKS.name().equals("SOCKS"), "SOCKS name");
        check(Proxy.Type.DIRECT.ordinal() == 0, "DIRECT ordinal 0");
        check(Proxy.Type.HTTP.ordinal() == 1, "HTTP ordinal 1");
        check(Proxy.Type.SOCKS.ordinal() == 2, "SOCKS ordinal 2");
        check(Proxy.Type.valueOf("DIRECT") == Proxy.Type.DIRECT, "valueOf DIRECT");
        check(Proxy.Type.valueOf("HTTP") == Proxy.Type.HTTP, "valueOf HTTP");
        check(Proxy.Type.valueOf("SOCKS") == Proxy.Type.SOCKS, "valueOf SOCKS");
        check(java.util.EnumSet.allOf(Proxy.Type.class).size() == 3, "EnumSet allOf 3");
        check(Proxy.Type.values() != Proxy.Type.values(), "values() fresh clone each call");
    }

    // ---- enum contract: compareTo / equals / identity ----
    static void testEnumContract() {
        check(Proxy.Type.DIRECT.compareTo(Proxy.Type.DIRECT) == 0, "DIRECT compareTo self");
        check(Proxy.Type.DIRECT.compareTo(Proxy.Type.HTTP) < 0, "DIRECT < HTTP");
        check(Proxy.Type.HTTP.compareTo(Proxy.Type.DIRECT) > 0, "HTTP > DIRECT");
        check(Proxy.Type.HTTP.compareTo(Proxy.Type.SOCKS) < 0, "HTTP < SOCKS");
        check(Proxy.Type.SOCKS.compareTo(Proxy.Type.HTTP) > 0, "SOCKS > HTTP");
        java.util.List<Proxy.Type> sorted = new java.util.ArrayList<Proxy.Type>();
        sorted.add(Proxy.Type.SOCKS);
        sorted.add(Proxy.Type.DIRECT);
        sorted.add(Proxy.Type.HTTP);
        java.util.Collections.sort(sorted);
        check(sorted.get(0) == Proxy.Type.DIRECT && sorted.get(1) == Proxy.Type.HTTP
            && sorted.get(2) == Proxy.Type.SOCKS, "natural order sort");
        check(Proxy.Type.DIRECT.equals(Proxy.Type.DIRECT), "equals self");
        check(!Proxy.Type.DIRECT.equals(Proxy.Type.HTTP), "distinct constants unequal");
        check(!Proxy.Type.DIRECT.equals("DIRECT"), "not equal to name String");
        check(Proxy.Type.DIRECT != null, "constant non-null");
        check(Proxy.Type.DIRECT.getDeclaringClass() == Proxy.Type.class, "declaring class");
        check(Proxy.Type.DIRECT instanceof Enum, "instanceof Enum");
    }

    // ---- negative paths (probed: standard enum behavior) ----
    static void testEnumErrors() {
        try {
            Proxy.Type.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("BOGUS"), "valueOf(BOGUS) message names constant");
        }
        try {
            Proxy.Type.valueOf("direct");
            check(false, "valueOf is case-sensitive");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            Proxy.Type.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    // ---- white-box structure (javap java.net.Proxy$Type, JDK-27) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Proxy$Type");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "enum final");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "enum public");
        check(java.lang.reflect.Modifier.isStatic(c.getModifiers()), "nested static");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("DIRECT"), "constant DIRECT");
        check(((java.lang.Enum<?>) ec[1]).name().equals("HTTP"), "constant HTTP");
        check(((java.lang.Enum<?>) ec[2]).name().equals("SOCKS"), "constant SOCKS");
        check(c.getField("DIRECT").get(null) == Proxy.Type.DIRECT, "field DIRECT");
        check(c.getField("HTTP").get(null) == Proxy.Type.HTTP, "field HTTP");
        check(c.getField("SOCKS").get(null) == Proxy.Type.SOCKS, "field SOCKS");
        check(Proxy.Type.valueOf("HTTP") == (Proxy.Type) Enum.valueOf(Proxy.Type.class, "HTTP"),
            "Enum.valueOf static generic");
    }

    public static void main(String[] args) throws Exception {
        testOriginalAssertions();
        testEnumMatrix();
        testEnumContract();
        testEnumErrors();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
