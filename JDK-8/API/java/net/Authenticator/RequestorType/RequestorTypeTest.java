/*
 * @test
 * @summary java.net.Authenticator.RequestorType conformance: enum constants and members.
 * @run main RequestorTypeTest
 */

import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestorTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testEnumMatrix() {
        Authenticator.RequestorType[] vs = Authenticator.RequestorType.values();
        check(vs.length == 2, "values().length == 2");
        check(vs[0] == Authenticator.RequestorType.PROXY, "values()[0] PROXY");
        check(vs[1] == Authenticator.RequestorType.SERVER, "values()[1] SERVER");
        check(Authenticator.RequestorType.PROXY.name().equals("PROXY"), "PROXY name");
        check(Authenticator.RequestorType.SERVER.name().equals("SERVER"), "SERVER name");
        check(Authenticator.RequestorType.PROXY.ordinal() == 0, "PROXY ordinal 0");
        check(Authenticator.RequestorType.SERVER.ordinal() == 1, "SERVER ordinal 1");
        check(Authenticator.RequestorType.valueOf("PROXY") == Authenticator.RequestorType.PROXY, "valueOf PROXY");
        check(Authenticator.RequestorType.valueOf("SERVER") == Authenticator.RequestorType.SERVER, "valueOf SERVER");
        check(java.util.EnumSet.allOf(Authenticator.RequestorType.class).size() == 2, "EnumSet allOf 2");
        check(Authenticator.RequestorType.values() != Authenticator.RequestorType.values(), "values() fresh clone");
    }

    static void testEnumContract() {
        check(Authenticator.RequestorType.PROXY.compareTo(Authenticator.RequestorType.PROXY) == 0, "compareTo self");
        check(Authenticator.RequestorType.PROXY.compareTo(Authenticator.RequestorType.SERVER) < 0, "PROXY < SERVER");
        check(Authenticator.RequestorType.SERVER.compareTo(Authenticator.RequestorType.PROXY) > 0, "SERVER > PROXY");
        List<Authenticator.RequestorType> l = new ArrayList<Authenticator.RequestorType>();
        l.add(Authenticator.RequestorType.SERVER);
        l.add(Authenticator.RequestorType.PROXY);
        Collections.sort(l);
        check(l.get(0) == Authenticator.RequestorType.PROXY && l.get(1) == Authenticator.RequestorType.SERVER, "sorted order");
        check(Authenticator.RequestorType.PROXY.equals(Authenticator.RequestorType.PROXY), "equals self");
        check(!Authenticator.RequestorType.PROXY.equals(Authenticator.RequestorType.SERVER), "constants unequal");
        check(!Authenticator.RequestorType.PROXY.equals("PROXY"), "not equal to String");
        check(Authenticator.RequestorType.PROXY.getDeclaringClass() == Authenticator.RequestorType.class, "declaring class");
        check(Authenticator.RequestorType.PROXY instanceof Enum, "instanceof Enum");
        check(Authenticator.RequestorType.PROXY.toString().equals("PROXY"), "toString = name");
    }

    static void testEnumErrors() {
        try {
            Authenticator.RequestorType.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("BOGUS"), "message names constant");
        }
        try {
            Authenticator.RequestorType.valueOf("proxy");
            check(false, "valueOf case-sensitive");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            Authenticator.RequestorType.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator$RequestorType");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "enum final");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getField("PROXY").get(null) == Authenticator.RequestorType.PROXY, "field PROXY");
        check(c.getField("SERVER").get(null) == Authenticator.RequestorType.SERVER, "field SERVER");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("PROXY"), "constant PROXY");
        check(((java.lang.Enum<?>) ec[1]).name().equals("SERVER"), "constant SERVER");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator$RequestorType");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("PROXY"), "constant PROXY");
        check(((java.lang.Enum<?>) ec[1]).name().equals("SERVER"), "constant SERVER");
        testEnumMatrix();
        testEnumContract();
        testEnumErrors();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
