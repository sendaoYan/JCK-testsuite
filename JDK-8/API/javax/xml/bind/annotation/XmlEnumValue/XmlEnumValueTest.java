/*
 * @test
 * @summary javax.xml.bind.annotation.XmlEnumValue conformance: annotation interface and members.
 * @run main XmlEnumValueTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


public class XmlEnumValueTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlEnumValue");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        testMeta();
        testReadBack();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() throws Exception {
        Class<XmlEnumValue> c = XmlEnumValue.class;
        check("javax.xml.bind.annotation.XmlEnumValue".equals(c.getName()), "FQCN exact");
        check("XmlEnumValue".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(!Modifier.isStatic(v.getModifiers()), "value() not static");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        java.lang.reflect.Field two = MappedEnum.class.getDeclaredField("TWO");
        check(Modifier.isPublic(two.getModifiers()) && Modifier.isStatic(two.getModifiers()) && Modifier.isFinal(two.getModifiers()),
                "enum constant field is public static final");
        check(MappedEnum.values().length == 3, "@XmlEnumValue does not disturb enum constants");
        XmlEnumValue one = MappedEnum.class.getDeclaredField("ONE").getAnnotation(XmlEnumValue.class);
        check("@javax.xml.bind.annotation.XmlEnumValue(value=1)".equals(one.toString()), "toString exact");
    }

    @XmlEnum
    enum MappedEnum {
        @XmlEnumValue("1")
        ONE,
        @XmlEnumValue("2")
        TWO,
        THREE
    }

    static void testMeta() throws Exception {
        Class<XmlEnumValue> c = XmlEnumValue.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.FIELD, "@Target({FIELD}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly 1 member");
        check("value".equals(declared[0].getName()), "single member named value");
        check(declared[0].getReturnType() == String.class, "value() returns String");
        check(declared[0].getDefaultValue() == null, "value() has no default (required)");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
        check(Modifier.isPublic(declared[0].getModifiers()), "value() is public");
    }

    static void testReadBack() throws Exception {
        java.lang.reflect.Field one = MappedEnum.class.getDeclaredField("ONE");
        XmlEnumValue v1 = one.getAnnotation(XmlEnumValue.class);
        check(v1 != null, "@XmlEnumValue retrievable from enum constant");
        check("1".equals(v1.value()), "value() \"1\" read back");
        check(v1.annotationType() == XmlEnumValue.class, "annotationType() identity");
        XmlEnumValue v2 = MappedEnum.class.getDeclaredField("TWO").getAnnotation(XmlEnumValue.class);
        check("2".equals(v2.value()), "value() \"2\" read back");
        check(!v1.equals(v2), "different constants' annotations not equal");
        check(v1.hashCode() == MappedEnum.class.getDeclaredField("ONE").getAnnotation(XmlEnumValue.class).hashCode(),
                "hashCode consistent across retrievals");
        check(MappedEnum.class.getDeclaredField("THREE").getAnnotation(XmlEnumValue.class) == null,
                "unannotated constant has no @XmlEnumValue");
        check(v1.toString().startsWith("@javax.xml.bind.annotation.XmlEnumValue"), "toString format");
        // composition: @XmlEnum on the type + @XmlEnumValue on constants is the documented mapping pair
        check(MappedEnum.class.getAnnotation(XmlEnum.class) != null, "mapped enum carries @XmlEnum too");
        check(MappedEnum.class.getAnnotation(XmlEnum.class).value() == String.class, "@XmlEnum default String binding");
        // enum constant fields are static; annotation is per-constant
        check(java.lang.reflect.Modifier.isStatic(one.getModifiers()), "enum constant accessor is a static field");
    }
}
