/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAccessOrder conformance: enum constants and methods.
 * @run main XmlAccessOrderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAccessOrderTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAccessOrder");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("UNDEFINED"), "constant UNDEFINED");
        check(cn.contains("ALPHABETICAL"), "constant ALPHABETICAL");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        testEnumContract();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<javax.xml.bind.annotation.XmlAccessOrder> c = javax.xml.bind.annotation.XmlAccessOrder.class;
        check("javax.xml.bind.annotation.XmlAccessOrder".equals(c.getName()), "FQCN exact");
        check("XmlAccessOrder".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(c.asSubclass(java.lang.Enum.class) == c, "asSubclass(Enum.class) identity");
        java.util.EnumSet<javax.xml.bind.annotation.XmlAccessOrder> all =
                java.util.EnumSet.allOf(javax.xml.bind.annotation.XmlAccessOrder.class);
        check(all.size() == 2, "EnumSet.allOf size 2");
        check(all.contains(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED)
                && all.contains(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL), "EnumSet contains both constants");
        java.util.EnumMap<javax.xml.bind.annotation.XmlAccessOrder, Integer> m =
                new java.util.EnumMap<javax.xml.bind.annotation.XmlAccessOrder, Integer>(javax.xml.bind.annotation.XmlAccessOrder.class);
        m.put(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL, 1);
        check(Integer.valueOf(1).equals(m.get(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL)), "EnumMap round-trip");
        check(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED != javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL,
                "constants distinct");
    }

    static void testEnumContract() {
        javax.xml.bind.annotation.XmlAccessOrder[] vals = javax.xml.bind.annotation.XmlAccessOrder.values();
        check(vals.length == 2, "values().length == 2");
        check(vals[0] == javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED, "values()[0] == UNDEFINED (declaration order)");
        check(vals[1] == javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL, "values()[1] == ALPHABETICAL");
        check(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.ordinal() == 0, "UNDEFINED.ordinal() == 0");
        check(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL.ordinal() == 1, "ALPHABETICAL.ordinal() == 1");
        check("UNDEFINED".equals(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.name()), "UNDEFINED.name()");
        check("ALPHABETICAL".equals(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL.name()), "ALPHABETICAL.name()");
        check("UNDEFINED".equals(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.toString()), "UNDEFINED.toString()");
        check("ALPHABETICAL".equals(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL.toString()), "ALPHABETICAL.toString()");
        check(javax.xml.bind.annotation.XmlAccessOrder.valueOf("UNDEFINED") == javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED, "valueOf(\"UNDEFINED\") identity");
        check(javax.xml.bind.annotation.XmlAccessOrder.valueOf("ALPHABETICAL") == javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL, "valueOf(\"ALPHABETICAL\") identity");
        check(Enum.valueOf(javax.xml.bind.annotation.XmlAccessOrder.class, "UNDEFINED") == javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED, "Enum.valueOf round-trip");
        check(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.compareTo(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL) < 0, "UNDEFINED < ALPHABETICAL");
        check(javax.xml.bind.annotation.XmlAccessOrder.ALPHABETICAL.compareTo(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED) > 0, "ALPHABETICAL > UNDEFINED");
        check(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.compareTo(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED) == 0, "compareTo self == 0");
        check(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED.getDeclaringClass() == javax.xml.bind.annotation.XmlAccessOrder.class, "getDeclaringClass()");
        check(vals != javax.xml.bind.annotation.XmlAccessOrder.values(), "values() returns fresh array each call");
        Object[] ec = javax.xml.bind.annotation.XmlAccessOrder.class.getEnumConstants();
        check(ec.length == 2 && ec[0] == vals[0] && ec[1] == vals[1], "getEnumConstants() matches values()");
        try {
            javax.xml.bind.annotation.XmlAccessOrder.valueOf("bogus");
            check(false, "valueOf(bogus) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("bogus"), "valueOf(bogus) IAE mentions constant name");
        }
        try {
            javax.xml.bind.annotation.XmlAccessOrder.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        // used as the value type of @XmlAccessorOrder (cross-contract)
        try {
            java.lang.reflect.Method m = javax.xml.bind.annotation.XmlAccessorOrder.class.getDeclaredMethod("value");
            check(m.getReturnType() == javax.xml.bind.annotation.XmlAccessOrder.class, "XmlAccessorOrder.value() return type is XmlAccessOrder");
        } catch (NoSuchMethodException e) {
            check(false, "XmlAccessorOrder.value() missing: " + e);
        }
    }
}
