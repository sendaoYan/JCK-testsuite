/*
 * @test
 * @summary javax.xml.bind.annotation.XmlSchemaType.DEFAULT conformance: final marker used
 *          as the type() default sentinel.
 * @run main DEFAULTTest
 */

import javax.xml.bind.annotation.XmlSchemaType;


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

    public static class Holder {
        @XmlSchemaType(name = "date")
        public java.util.Calendar when;
    }

    public static void main(String[] args) throws Exception {
        javax.xml.bind.annotation.XmlSchemaType.DEFAULT d = new javax.xml.bind.annotation.XmlSchemaType.DEFAULT();
        check(d != null, "constructed");
        // --- appended deep coverage ------------------------------------------
        Class<?> c = XmlSchemaType.DEFAULT.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(c.getInterfaces().length == 0, "pure marker: implements nothing");
        check(c.getDeclaredMethods().length == 0, "declares no methods");
        check(c.getDeclaredFields().length == 0, "declares no fields");
        check(c.getConstructors().length == 1, "one public constructor");
        check(c.getDeclaringClass() == XmlSchemaType.class, "declared inside XmlSchemaType");
        check(c.getName().equals("javax.xml.bind.annotation.XmlSchemaType$DEFAULT"), "binary nested name");
        // identity equality
        javax.xml.bind.annotation.XmlSchemaType.DEFAULT other = new javax.xml.bind.annotation.XmlSchemaType.DEFAULT();
        check(!d.equals(other), "identity equality");
        check(d.equals(d), "reflexive");
        check(d.toString().contains("DEFAULT"), "toString mentions DEFAULT");
        // role: default of type()
        Object def = XmlSchemaType.class.getMethod("type").getDefaultValue();
        check(def == XmlSchemaType.DEFAULT.class, "type() default is the DEFAULT marker");
        // a field-level annotation reads back DEFAULT unless a concrete type is given
        XmlSchemaType ann = Holder.class.getField("when").getAnnotation(XmlSchemaType.class);
        check(ann.type() == XmlSchemaType.DEFAULT.class, "field annotation type() reads back DEFAULT");
        check("date".equals(ann.name()), "name() reads back the explicit value");
        // namespace default
        check("http://www.w3.org/2001/XMLSchema".equals(ann.namespace()),
              "namespace() default is the XSD namespace (measured)");
        // the marker on package-level annotation default too
        Object pkgDef = ann.annotationType().getMethod("type").getDefaultValue();
        check(pkgDef == XmlSchemaType.DEFAULT.class, "annotation default object is the same marker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
