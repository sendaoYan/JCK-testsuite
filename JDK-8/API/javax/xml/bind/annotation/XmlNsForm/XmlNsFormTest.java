/*
 * @test
 * @summary javax.xml.bind.annotation.XmlNsForm conformance: enum constants and methods.
 * @run main XmlNsFormTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlNsFormTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlNsForm");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("UNQUALIFIED"), "constant UNQUALIFIED");
        check(cn.contains("QUALIFIED"), "constant QUALIFIED");
        check(cn.contains("UNSET"), "constant UNSET");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        testEnumContract();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<javax.xml.bind.annotation.XmlNsForm> c = javax.xml.bind.annotation.XmlNsForm.class;
        check("javax.xml.bind.annotation.XmlNsForm".equals(c.getName()), "FQCN exact");
        check("XmlNsForm".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(c.asSubclass(java.lang.Enum.class) == c, "asSubclass(Enum.class) identity");
        check(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED != javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
                "constants pairwise distinct (1)");
        check(javax.xml.bind.annotation.XmlNsForm.QUALIFIED != javax.xml.bind.annotation.XmlNsForm.UNSET,
                "constants pairwise distinct (2)");
        check(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED != javax.xml.bind.annotation.XmlNsForm.UNSET,
                "constants pairwise distinct (3)");
        java.util.EnumSet<javax.xml.bind.annotation.XmlNsForm> all =
                java.util.EnumSet.allOf(javax.xml.bind.annotation.XmlNsForm.class);
        check(all.size() == 3, "EnumSet.allOf size 3");
        check(all.contains(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED)
                && all.contains(javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
                && all.contains(javax.xml.bind.annotation.XmlNsForm.UNSET), "EnumSet contains all constants");
        java.util.EnumMap<javax.xml.bind.annotation.XmlNsForm, String> m =
                new java.util.EnumMap<javax.xml.bind.annotation.XmlNsForm, String>(javax.xml.bind.annotation.XmlNsForm.class);
        m.put(javax.xml.bind.annotation.XmlNsForm.UNSET, "u");
        check("u".equals(m.get(javax.xml.bind.annotation.XmlNsForm.UNSET)), "EnumMap round-trip");
        check(m.get(javax.xml.bind.annotation.XmlNsForm.QUALIFIED) == null, "EnumMap miss returns null");
    }

    static void testEnumContract() {
        javax.xml.bind.annotation.XmlNsForm[] vals = javax.xml.bind.annotation.XmlNsForm.values();
        check(vals.length == 3, "values().length == 3");
        check(vals[0] == javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED, "values()[0] == UNQUALIFIED (declaration order)");
        check(vals[1] == javax.xml.bind.annotation.XmlNsForm.QUALIFIED, "values()[1] == QUALIFIED");
        check(vals[2] == javax.xml.bind.annotation.XmlNsForm.UNSET, "values()[2] == UNSET");
        check(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED.ordinal() == 0, "UNQUALIFIED.ordinal() == 0");
        check(javax.xml.bind.annotation.XmlNsForm.QUALIFIED.ordinal() == 1, "QUALIFIED.ordinal() == 1");
        check(javax.xml.bind.annotation.XmlNsForm.UNSET.ordinal() == 2, "UNSET.ordinal() == 2");
        for (javax.xml.bind.annotation.XmlNsForm f : vals) {
            check(javax.xml.bind.annotation.XmlNsForm.valueOf(f.name()) == f, "valueOf round-trip " + f.name());
        }
        check("UNQUALIFIED".equals(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED.toString()), "UNQUALIFIED.toString()");
        check(javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED.compareTo(javax.xml.bind.annotation.XmlNsForm.QUALIFIED) < 0, "UNQUALIFIED < QUALIFIED");
        check(javax.xml.bind.annotation.XmlNsForm.QUALIFIED.compareTo(javax.xml.bind.annotation.XmlNsForm.UNSET) < 0, "QUALIFIED < UNSET");
        check(javax.xml.bind.annotation.XmlNsForm.UNSET.compareTo(javax.xml.bind.annotation.XmlNsForm.UNSET) == 0, "compareTo self == 0");
        check(vals != javax.xml.bind.annotation.XmlNsForm.values(), "values() returns fresh array each call");
        try {
            javax.xml.bind.annotation.XmlNsForm.valueOf("bogus");
            check(false, "valueOf(bogus) should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("bogus"), "valueOf(bogus) IAE mentions constant name");
        }
        try {
            javax.xml.bind.annotation.XmlNsForm.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        // used as the value type of @XmlSchema elementFormDefault/attributeFormDefault (cross-contract)
        try {
            java.lang.reflect.Method efd = javax.xml.bind.annotation.XmlSchema.class.getDeclaredMethod("elementFormDefault");
            check(efd.getReturnType() == javax.xml.bind.annotation.XmlNsForm.class, "XmlSchema.elementFormDefault() return type is XmlNsForm");
            java.lang.reflect.Method afd = javax.xml.bind.annotation.XmlSchema.class.getDeclaredMethod("attributeFormDefault");
            check(afd.getReturnType() == javax.xml.bind.annotation.XmlNsForm.class, "XmlSchema.attributeFormDefault() return type is XmlNsForm");
        } catch (NoSuchMethodException e) {
            check(false, "XmlSchema form member missing: " + e);
        }
    }
}
