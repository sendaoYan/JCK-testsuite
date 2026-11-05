/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter conformance: annotation interface and members.
 * @run main XmlJavaTypeAdapterTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


public class XmlJavaTypeAdapterTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        check(names.contains("type"), "has method type");
        testMeta();
        testReadBack();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class Holder {
        @XmlJavaTypeAdapter(value = HexBinaryAdapter.class, type = byte[].class)
        byte[] hex;

        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        String collapsed;

        @XmlJavaTypeAdapter(value = HexBinaryAdapter.class, type = byte[].class)
        byte[] getHex() {
            return hex;
        }
    }

    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    static class AdaptedType {}

    @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class, type = String.class)
    interface AdaptedInterface {}

    static void testMeta() throws Exception {
        Class<XmlJavaTypeAdapter> c = XmlJavaTypeAdapter.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 5, "@Target has exactly 5 elements");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>(java.util.Arrays.asList(tgt.value()));
        check(ts.contains(ElementType.PACKAGE) && ts.contains(ElementType.FIELD) && ts.contains(ElementType.METHOD)
                && ts.contains(ElementType.TYPE) && ts.contains(ElementType.PARAMETER), "@Target = {PACKAGE, FIELD, METHOD, TYPE, PARAMETER}");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(Repeatable.class), "not @Repeatable (multiple bindings need @XmlJavaTypeAdapters)");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 2, "exactly 2 members");
        check(declared.contains("value") && declared.contains("type"), "member name set exact");
        check(c.getDeclaredMethod("value").getReturnType() == Class.class, "value() returns Class");
        check(c.getDeclaredMethod("type").getReturnType() == Class.class, "type() returns Class");
        check(c.getDeclaredMethod("value").getDefaultValue() == null, "value() has no default (required)");
        check(c.getDeclaredMethod("type").getDefaultValue() == XmlJavaTypeAdapter.DEFAULT.class,
                "type() default is XmlJavaTypeAdapter.DEFAULT.class");
        Class<?> d = Class.forName("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter$DEFAULT");
        check(Modifier.isPublic(d.getModifiers()) && Modifier.isStatic(d.getModifiers()) && Modifier.isFinal(d.getModifiers()),
                "DEFAULT is a public static final nested class");
        check(d.getEnclosingClass() == XmlJavaTypeAdapter.class, "DEFAULT enclosed by XmlJavaTypeAdapter");
    }

    static void testReadBack() throws Exception {
        XmlJavaTypeAdapter hexField = Holder.class.getDeclaredField("hex").getAnnotation(XmlJavaTypeAdapter.class);
        check(hexField != null, "field-level @XmlJavaTypeAdapter retrievable");
        check(hexField.value() == HexBinaryAdapter.class, "explicit adapter class read back");
        check(hexField.type() == byte[].class, "explicit type read back");
        check(hexField.annotationType() == XmlJavaTypeAdapter.class, "annotationType() identity");
        XmlJavaTypeAdapter colField = Holder.class.getDeclaredField("collapsed").getAnnotation(XmlJavaTypeAdapter.class);
        check(colField.value() == CollapsedStringAdapter.class, "single-element form: value implied");
        check(colField.type() == XmlJavaTypeAdapter.DEFAULT.class, "omitted type() defaults to DEFAULT.class");
        XmlJavaTypeAdapter onMethod = Holder.class.getDeclaredMethod("getHex").getAnnotation(XmlJavaTypeAdapter.class);
        check(onMethod != null, "method-level @XmlJavaTypeAdapter retrievable");
        check(onMethod.value() == HexBinaryAdapter.class, "method-level adapter read back");
        XmlJavaTypeAdapter onType = AdaptedType.class.getAnnotation(XmlJavaTypeAdapter.class);
        check(onType != null, "type-level @XmlJavaTypeAdapter retrievable");
        check(onType.value() == HexBinaryAdapter.class, "type-level adapter read back");
        check(onType.type() == XmlJavaTypeAdapter.DEFAULT.class, "type-level omitted type defaults to DEFAULT");
        XmlJavaTypeAdapter onIface = AdaptedInterface.class.getAnnotation(XmlJavaTypeAdapter.class);
        check(onIface != null && onIface.type() == String.class, "interface-level explicit type read back");
        check(hexField.equals(Holder.class.getDeclaredField("hex").getAnnotation(XmlJavaTypeAdapter.class)), "repeated retrieval equal");
        check(!hexField.equals(colField), "different adapters not equal");
        check(hexField.toString().startsWith("@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter"), "toString format");
    }
}
