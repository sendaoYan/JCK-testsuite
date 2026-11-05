/*
 * @test
 * @summary javax.xml.bind.annotation.XmlType.DEFAULT conformance: final marker used
 *          as the factoryClass() default sentinel.
 * @run main DEFAULTTest
 */

import javax.xml.bind.annotation.XmlType;


public class DEFAULTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlType
    public static class Plain { }

    @XmlType(factoryClass = XmlType.DEFAULT.class, factoryMethod = "nope")
    public static class Explicit { }

    public static void main(String[] args) throws Exception {
        javax.xml.bind.annotation.XmlType.DEFAULT d = new javax.xml.bind.annotation.XmlType.DEFAULT();
        check(d != null, "constructed");
        // --- appended deep coverage ------------------------------------------
        Class<?> c = XmlType.DEFAULT.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(c.getInterfaces().length == 0, "pure marker: implements nothing");
        check(c.getDeclaredMethods().length == 0, "declares no methods");
        check(c.getDeclaredFields().length == 0, "declares no fields");
        check(c.getConstructors().length == 1, "one public constructor");
        check(c.getDeclaringClass() == XmlType.class, "declared inside XmlType");
        check(c.getName().equals("javax.xml.bind.annotation.XmlType$DEFAULT"), "binary nested name");
        // identity equality (no equals override)
        javax.xml.bind.annotation.XmlType.DEFAULT other = new javax.xml.bind.annotation.XmlType.DEFAULT();
        check(!d.equals(other), "identity equality");
        check(d.equals(d), "reflexive");
        check(d.toString().contains("DEFAULT"), "toString mentions DEFAULT");
        // role: default of factoryClass()
        Object def = XmlType.class.getMethod("factoryClass").getDefaultValue();
        check(def == XmlType.DEFAULT.class, "factoryClass() default is the DEFAULT marker");
        // the annotation reflects the sentinel on plain classes
        XmlType ann = Plain.class.getAnnotation(XmlType.class);
        check(ann.factoryClass() == XmlType.DEFAULT.class, "plain class reads factoryClass == DEFAULT");
        // and on explicitly annotated classes
        XmlType explicit = Explicit.class.getAnnotation(XmlType.class);
        check(explicit.factoryClass() == XmlType.DEFAULT.class, "explicit DEFAULT reads back the same marker");
        check("nope".equals(explicit.factoryMethod()), "factoryMethod reads back alongside");
        // factoryMethod default is empty string (no marker)
        check("".equals(ann.factoryMethod()), "factoryMethod default is empty string");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
