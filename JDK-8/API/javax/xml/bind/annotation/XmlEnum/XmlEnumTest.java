/*
 * @test
 * @summary javax.xml.bind.annotation.XmlEnum conformance: annotation interface and members.
 * @run main XmlEnumTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlEnum;


public class XmlEnumTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlEnum");
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

    @XmlEnum(String.class)
    enum ExplicitStringEnum {
        S
    }

    static void testClassFacts() throws Exception {
        Class<XmlEnum> c = XmlEnum.class;
        check("javax.xml.bind.annotation.XmlEnum".equals(c.getName()), "FQCN exact");
        check("XmlEnum".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        // explicit String.class equals the default binding
        XmlEnum es = ExplicitStringEnum.class.getAnnotation(XmlEnum.class);
        check(es.value() == String.class, "explicit String.class read back");
        check(es.equals(DefaultBoundEnum.class.getAnnotation(XmlEnum.class)),
                "explicit String binding equals default binding instance");
        check(es.hashCode() == DefaultBoundEnum.class.getAnnotation(XmlEnum.class).hashCode(), "hashCode consistent");
        check("@javax.xml.bind.annotation.XmlEnum(value=class java.lang.String)".equals(es.toString()),
                "toString exact for String binding");
        check(IntBoundEnum.values().length == 2, "@XmlEnum does not disturb enum constants");
        check(PlainEnum.values().length == 2, "unannotated enum constants untouched");
    }

    @XmlEnum(Integer.class)
    enum IntBoundEnum {
        ONE, TWO
    }

    @XmlEnum
    enum DefaultBoundEnum {
        A, B
    }

    enum PlainEnum {
        X, Y
    }

    @XmlEnum(Long.class)
    enum LongBoundEnum {
        L
    }

    static void testMeta() throws Exception {
        Class<XmlEnum> c = XmlEnum.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.TYPE, "@Target({TYPE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly 1 member");
        check("value".equals(declared[0].getName()), "single member named value");
        check(declared[0].getReturnType() == Class.class, "value() returns Class");
        check(declared[0].getDefaultValue() == String.class, "value() default is String.class");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
        check(Modifier.isPublic(declared[0].getModifiers()), "value() is public");
    }

    static void testReadBack() {
        XmlEnum ie = IntBoundEnum.class.getAnnotation(XmlEnum.class);
        check(ie != null, "@XmlEnum retrievable from enum type");
        check(ie.value() == Integer.class, "explicit Integer bound read back");
        check(ie.annotationType() == XmlEnum.class, "annotationType() identity");
        XmlEnum de = DefaultBoundEnum.class.getAnnotation(XmlEnum.class);
        check(de != null && de.value() == String.class, "omitted value() defaults to String.class on read-back");
        check(PlainEnum.class.getAnnotation(XmlEnum.class) == null, "unannotated enum has no @XmlEnum");
        XmlEnum le = LongBoundEnum.class.getAnnotation(XmlEnum.class);
        check(le.value() == Long.class, "explicit Long bound read back");
        check(ie.equals(IntBoundEnum.class.getAnnotation(XmlEnum.class)), "repeated retrieval equal");
        check(!ie.equals(le), "different bindings not equal");
        check(ie.toString().startsWith("@javax.xml.bind.annotation.XmlEnum"), "toString format");
        // @XmlEnum is only meaningful on enum types; the JDK annotates no JDK enum with it here
        check(XmlEnum.class.getAnnotation(XmlEnum.class) == null, "annotation type itself unannotated");
    }
}
