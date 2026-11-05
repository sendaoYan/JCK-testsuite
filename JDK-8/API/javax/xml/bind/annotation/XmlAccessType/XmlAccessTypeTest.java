/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAccessType conformance: enum constants and methods.
 * @run main XmlAccessTypeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAccessTypeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAccessType");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 4, "4 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("PROPERTY"), "constant PROPERTY");
        check(cn.contains("FIELD"), "constant FIELD");
        check(cn.contains("PUBLIC_MEMBER"), "constant PUBLIC_MEMBER");
        check(cn.contains("NONE"), "constant NONE");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        testEnumContract();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<javax.xml.bind.annotation.XmlAccessType> c = javax.xml.bind.annotation.XmlAccessType.class;
        check("javax.xml.bind.annotation.XmlAccessType".equals(c.getName()), "FQCN exact");
        check("XmlAccessType".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(c.asSubclass(java.lang.Enum.class) == c, "asSubclass(Enum.class) identity");
        java.util.EnumSet<javax.xml.bind.annotation.XmlAccessType> all =
                java.util.EnumSet.allOf(javax.xml.bind.annotation.XmlAccessType.class);
        check(all.size() == 4, "EnumSet.allOf size 4");
        check(all.contains(javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER)
                && all.contains(javax.xml.bind.annotation.XmlAccessType.NONE), "EnumSet contains default and NONE");
        java.util.EnumMap<javax.xml.bind.annotation.XmlAccessType, Integer> m =
                new java.util.EnumMap<javax.xml.bind.annotation.XmlAccessType, Integer>(javax.xml.bind.annotation.XmlAccessType.class);
        m.put(javax.xml.bind.annotation.XmlAccessType.FIELD, 1);
        check(Integer.valueOf(1).equals(m.get(javax.xml.bind.annotation.XmlAccessType.FIELD)), "EnumMap round-trip");
        check(m.get(javax.xml.bind.annotation.XmlAccessType.PROPERTY) == null, "EnumMap miss returns null");
    }

    static void testEnumContract() {
        javax.xml.bind.annotation.XmlAccessType[] vals = javax.xml.bind.annotation.XmlAccessType.values();
        check(vals.length == 4, "values().length == 4");
        check(vals[0] == javax.xml.bind.annotation.XmlAccessType.PROPERTY, "values()[0] == PROPERTY (declaration order)");
        check(vals[1] == javax.xml.bind.annotation.XmlAccessType.FIELD, "values()[1] == FIELD");
        check(vals[2] == javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER, "values()[2] == PUBLIC_MEMBER");
        check(vals[3] == javax.xml.bind.annotation.XmlAccessType.NONE, "values()[3] == NONE");
        check(javax.xml.bind.annotation.XmlAccessType.PROPERTY.ordinal() == 0, "PROPERTY.ordinal() == 0");
        check(javax.xml.bind.annotation.XmlAccessType.FIELD.ordinal() == 1, "FIELD.ordinal() == 1");
        check(javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER.ordinal() == 2, "PUBLIC_MEMBER.ordinal() == 2");
        check(javax.xml.bind.annotation.XmlAccessType.NONE.ordinal() == 3, "NONE.ordinal() == 3");
        for (javax.xml.bind.annotation.XmlAccessType t : vals) {
            check(javax.xml.bind.annotation.XmlAccessType.valueOf(t.name()) == t, "valueOf round-trip " + t.name());
        }
        check("PUBLIC_MEMBER".equals(javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER.name()), "PUBLIC_MEMBER.name()");
        check(javax.xml.bind.annotation.XmlAccessType.FIELD.compareTo(javax.xml.bind.annotation.XmlAccessType.PROPERTY) > 0, "FIELD > PROPERTY");
        check(javax.xml.bind.annotation.XmlAccessType.NONE.compareTo(javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER) > 0, "NONE > PUBLIC_MEMBER");
        check(javax.xml.bind.annotation.XmlAccessType.NONE.compareTo(javax.xml.bind.annotation.XmlAccessType.NONE) == 0, "compareTo self == 0");
        check(vals != javax.xml.bind.annotation.XmlAccessType.values(), "values() returns fresh array each call");
        Object[] ec = javax.xml.bind.annotation.XmlAccessType.class.getEnumConstants();
        check(ec.length == 4 && ec[2] == vals[2], "getEnumConstants() matches values()");
        try {
            javax.xml.bind.annotation.XmlAccessType.valueOf("bogus");
            check(false, "valueOf(bogus) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("javax.xml.bind.annotation.XmlAccessType"), "valueOf(bogus) IAE mentions enum class");
        }
        try {
            javax.xml.bind.annotation.XmlAccessType.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        try {
            java.lang.reflect.Method m = javax.xml.bind.annotation.XmlAccessorType.class.getDeclaredMethod("value");
            check(m.getReturnType() == javax.xml.bind.annotation.XmlAccessType.class, "XmlAccessorType.value() return type is XmlAccessType");
        } catch (NoSuchMethodException e) {
            check(false, "XmlAccessorType.value() missing: " + e);
        }
    }
}
