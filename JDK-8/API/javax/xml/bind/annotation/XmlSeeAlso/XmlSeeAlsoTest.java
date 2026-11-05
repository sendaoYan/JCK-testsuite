/*
 * @test
 * @summary javax.xml.bind.annotation.XmlSeeAlso conformance: annotation interface and members.
 * @run main XmlSeeAlsoTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlSeeAlso;


public class XmlSeeAlsoTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlSeeAlso");
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
        Class<XmlSeeAlso> c = XmlSeeAlso.class;
        check("javax.xml.bind.annotation.XmlSeeAlso".equals(c.getName()), "FQCN exact");
        check("XmlSeeAlso".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(!Modifier.isStatic(v.getModifiers()), "value() not static");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        XmlSeeAlso three = RegistryBase.class.getAnnotation(XmlSeeAlso.class);
        check(!three.equals(EmptySeeAlso.class.getAnnotation(XmlSeeAlso.class)), "3-element not equal to 0-element");
        check(three.value() != SingleSeeAlso.class.getAnnotation(XmlSeeAlso.class).value(), "returned arrays are distinct objects");
    }

    static class Person {}

    interface Contact {}

    enum Kind {
        HOME, WORK
    }

    @XmlSeeAlso({Person.class, Contact.class, Kind.class})
    static class RegistryBase {}

    @XmlSeeAlso({})
    static class EmptySeeAlso {}

    @XmlSeeAlso(Person.class)
    static class SingleSeeAlso {}

    static class NoSeeAlso {}

    static void testMeta() throws Exception {
        Class<XmlSeeAlso> c = XmlSeeAlso.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.TYPE, "@Target({TYPE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly 1 member");
        check("value".equals(declared[0].getName()), "single member named value");
        check(declared[0].getReturnType() == Class[].class, "value() returns Class[]");
        check(declared[0].getDefaultValue() == null, "value() has no default (required)");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
        check(Modifier.isPublic(declared[0].getModifiers()), "value() is public");
    }

    static void testReadBack() {
        XmlSeeAlso sa = RegistryBase.class.getAnnotation(XmlSeeAlso.class);
        check(sa != null, "@XmlSeeAlso retrievable from class");
        check(sa.value().length == 3, "value() array length read back");
        check(sa.value()[0] == Person.class, "value()[0] identity and order preserved");
        check(sa.value()[1] == Contact.class, "value()[1] accepts interfaces");
        check(sa.value()[2] == Kind.class, "value()[2] accepts enums");
        check(sa.annotationType() == XmlSeeAlso.class, "annotationType() identity");
        check(sa.equals(RegistryBase.class.getAnnotation(XmlSeeAlso.class)), "repeated retrieval equal");
        check(sa.hashCode() == RegistryBase.class.getAnnotation(XmlSeeAlso.class).hashCode(), "hashCode consistent");
        check(EmptySeeAlso.class.getAnnotation(XmlSeeAlso.class).value().length == 0, "explicit empty value {} allowed");
        check(SingleSeeAlso.class.getAnnotation(XmlSeeAlso.class).value().length == 1, "single-element value allowed");
        check(SingleSeeAlso.class.getAnnotation(XmlSeeAlso.class).value()[0] == Person.class, "single element identity");
        check(NoSeeAlso.class.getAnnotation(XmlSeeAlso.class) == null, "unannotated class has no @XmlSeeAlso");
        check(!sa.equals(SingleSeeAlso.class.getAnnotation(XmlSeeAlso.class)), "different arrays not equal");
        check(sa.toString().startsWith("@javax.xml.bind.annotation.XmlSeeAlso"), "toString format");
    }
}
