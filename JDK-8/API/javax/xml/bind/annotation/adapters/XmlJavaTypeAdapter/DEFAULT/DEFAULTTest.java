/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT conformance:
 *          final marker used as the type() default sentinel.
 * @run main DEFAULTTest
 */

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
        @XmlJavaTypeAdapter(value = MyAdapter.class)
        public String field;
    }

    public static class MyAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, String> {
        public String marshal(String v) { return v; }
        public String unmarshal(String v) { return v; }
    }

    public static void main(String[] args) throws Exception {
        javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT d =
                new javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT();
        check(d != null, "constructed");
        // --- appended deep coverage ------------------------------------------
        Class<?> c = XmlJavaTypeAdapter.DEFAULT.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(c.getInterfaces().length == 0, "pure marker: implements nothing");
        check(c.getDeclaredMethods().length == 0, "declares no methods");
        check(c.getDeclaredFields().length == 0, "declares no fields");
        check(c.getConstructors().length == 1, "one public constructor");
        check(c.getDeclaringClass() == XmlJavaTypeAdapter.class, "declared inside XmlJavaTypeAdapter");
        check(c.getName().equals("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter$DEFAULT"),
              "binary nested name");
        // identity equality
        javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT other =
                new javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT();
        check(!d.equals(other), "identity equality");
        check(d.equals(d), "reflexive");
        check(d.toString().contains("DEFAULT"), "toString mentions DEFAULT");
        // role: default of type()
        Object def = XmlJavaTypeAdapter.class.getMethod("type").getDefaultValue();
        check(def == XmlJavaTypeAdapter.DEFAULT.class, "type() default is the DEFAULT marker");
        // field-level annotation reads back DEFAULT unless a concrete type is given
        XmlJavaTypeAdapter ann = Holder.class.getField("field").getAnnotation(XmlJavaTypeAdapter.class);
        check(ann.type() == XmlJavaTypeAdapter.DEFAULT.class, "field annotation type() reads back DEFAULT");
        check(ann.value() == MyAdapter.class, "value() reads back the concrete adapter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
