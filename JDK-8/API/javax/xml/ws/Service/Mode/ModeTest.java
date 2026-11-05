/*
 * @test
 * @summary javax.xml.ws.Service.Mode conformance: enum constants, ordinal contract,
 *          valueOf behavior, and Dispatch mode semantics.
 * @run main ModeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Service$Mode");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("MESSAGE"), "constant MESSAGE");
        check(cn.contains("PAYLOAD"), "constant PAYLOAD");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        // --- appended deep coverage ------------------------------------------
        javax.xml.ws.Service.Mode[] vals = javax.xml.ws.Service.Mode.values();
        check(vals.length == 2, "values() has exactly two entries");
        check(vals[0] == javax.xml.ws.Service.Mode.MESSAGE, "declared order: MESSAGE first");
        check(vals[1] == javax.xml.ws.Service.Mode.PAYLOAD, "declared order: PAYLOAD second");
        check(javax.xml.ws.Service.Mode.MESSAGE.ordinal() == 0, "MESSAGE ordinal 0");
        check(javax.xml.ws.Service.Mode.PAYLOAD.ordinal() == 1, "PAYLOAD ordinal 1");
        check("PAYLOAD".equals(javax.xml.ws.Service.Mode.PAYLOAD.name()), "PAYLOAD name");
        check("MESSAGE".equals(javax.xml.ws.Service.Mode.MESSAGE.name()), "MESSAGE name");
        // valueOf round-trips both spellings; exact-case only
        check(javax.xml.ws.Service.Mode.valueOf("PAYLOAD") == javax.xml.ws.Service.Mode.PAYLOAD, "valueOf PAYLOAD");
        check(javax.xml.ws.Service.Mode.valueOf("MESSAGE") == javax.xml.ws.Service.Mode.MESSAGE, "valueOf MESSAGE");
        try {
            javax.xml.ws.Service.Mode.valueOf("payload");
            check(false, "valueOf(lowercase) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf is case-sensitive");
        }
        try {
            javax.xml.ws.Service.Mode.valueOf("NOPE");
            check(false, "valueOf(unknown) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("NOPE"),
                  "valueOf(unknown) message names the constant: " + e.getMessage());
        }
        // compareTo / EnumSet interop
        check(javax.xml.ws.Service.Mode.MESSAGE.compareTo(javax.xml.ws.Service.Mode.PAYLOAD) < 0,
              "MESSAGE sorts before PAYLOAD");
        check(javax.xml.ws.Service.Mode.PAYLOAD.compareTo(javax.xml.ws.Service.Mode.MESSAGE) > 0,
              "PAYLOAD sorts after MESSAGE");
        java.util.EnumSet<javax.xml.ws.Service.Mode> set =
                java.util.EnumSet.allOf(javax.xml.ws.Service.Mode.class);
        check(set.size() == 2 && set.contains(javax.xml.ws.Service.Mode.PAYLOAD)
              && set.contains(javax.xml.ws.Service.Mode.MESSAGE), "EnumSet interop");
        // final class, no public constructor (enum invariant)
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "enum class is final");
        check(c.getConstructors().length == 0, "no public constructors");
        // toString == name (no custom toString)
        check("PAYLOAD".equals(javax.xml.ws.Service.Mode.PAYLOAD.toString()), "toString equals name");
        // enum constants are singletons
        check(javax.xml.ws.Service.Mode.values()[0] == javax.xml.ws.Service.Mode.MESSAGE
              && javax.xml.ws.Service.Mode.values()[1] == javax.xml.ws.Service.Mode.PAYLOAD,
              "values() returns the same singletons");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
