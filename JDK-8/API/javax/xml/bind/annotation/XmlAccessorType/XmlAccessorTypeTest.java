/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAccessorType conformance: annotation interface and members.
 * @run main XmlAccessorTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


public class XmlAccessorTypeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
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

    static class DeepSub extends FieldAccessSub {}

    static void testClassFacts() throws Exception {
        Class<XmlAccessorType> c = XmlAccessorType.class;
        check("javax.xml.bind.annotation.XmlAccessorType".equals(c.getName()), "FQCN exact");
        check("XmlAccessorType".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(!Modifier.isStatic(v.getModifiers()), "value() not static");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        XmlAccessorType deep = DeepSub.class.getAnnotation(XmlAccessorType.class);
        check(deep != null && deep.value() == XmlAccessType.FIELD, "@Inherited propagates through two levels");
        check("@javax.xml.bind.annotation.XmlAccessorType(value=FIELD)".equals(
                FieldAccessBase.class.getAnnotation(XmlAccessorType.class).toString()), "toString exact for FIELD");
        check(FieldAccessBase.class.getAnnotation(XmlAccessorType.class).toString().contains("value=FIELD"),
                "toString contains value pair");
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class FieldAccessBase {}

    static class FieldAccessSub extends FieldAccessBase {}

    @XmlAccessorType(XmlAccessType.NONE)
    static class NoneAccess {}

    @XmlAccessorType(XmlAccessType.PROPERTY)
    static class PropertyAccess {}

    static class NoAccess {}

    static void testMeta() throws Exception {
        Class<XmlAccessorType> c = XmlAccessorType.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 2, "@Target has exactly 2 elements");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>(java.util.Arrays.asList(tgt.value()));
        check(ts.contains(ElementType.TYPE) && ts.contains(ElementType.PACKAGE), "@Target = {TYPE, PACKAGE}");
        check(c.isAnnotationPresent(Inherited.class), "@Inherited");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly 1 member");
        check("value".equals(declared[0].getName()), "single member named value");
        check(declared[0].getReturnType() == XmlAccessType.class, "value() returns XmlAccessType");
        check(declared[0].getDefaultValue() == XmlAccessType.PUBLIC_MEMBER, "value() default PUBLIC_MEMBER");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
    }

    static void testReadBack() {
        XmlAccessorType fa = FieldAccessBase.class.getAnnotation(XmlAccessorType.class);
        check(fa != null, "@XmlAccessorType retrievable from class");
        check(fa.value() == XmlAccessType.FIELD, "FIELD read back");
        check(fa.annotationType() == XmlAccessorType.class, "annotationType() identity");
        check(NoneAccess.class.getAnnotation(XmlAccessorType.class).value() == XmlAccessType.NONE, "NONE read back");
        check(PropertyAccess.class.getAnnotation(XmlAccessorType.class).value() == XmlAccessType.PROPERTY, "PROPERTY read back");
        check(NoAccess.class.getAnnotation(XmlAccessorType.class) == null, "unannotated class has no @XmlAccessorType");
        XmlAccessorType inherited = FieldAccessSub.class.getAnnotation(XmlAccessorType.class);
        check(inherited != null && inherited.value() == XmlAccessType.FIELD,
                "@Inherited: subclass sees superclass @XmlAccessorType");
        check(fa.equals(inherited), "inherited instance equals superclass instance");
        check(fa.hashCode() == inherited.hashCode(), "annotation hashCode consistent");
        check(!fa.equals(NoneAccess.class.getAnnotation(XmlAccessorType.class)), "different values not equal");
        check(fa.toString().startsWith("@javax.xml.bind.annotation.XmlAccessorType"), "toString format");
    }
}
