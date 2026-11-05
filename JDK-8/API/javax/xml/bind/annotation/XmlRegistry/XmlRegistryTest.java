/*
 * @test
 * @summary javax.xml.bind.annotation.XmlRegistry conformance: annotation interface and members.
 * @run main XmlRegistryTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlRegistry;


public class XmlRegistryTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlRegistry");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testMeta();
        testReadBack();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<XmlRegistry> c = XmlRegistry.class;
        check("javax.xml.bind.annotation.XmlRegistry".equals(c.getName()), "FQCN exact");
        check("XmlRegistry".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation.XmlRegistry".equals(c.getCanonicalName()), "canonical name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        boolean hasRet = false, hasTgt = false;
        for (java.lang.annotation.Annotation an : c.getAnnotations()) {
            if (an instanceof Retention) hasRet = true;
            if (an instanceof Target) hasTgt = true;
        }
        check(hasRet, "getAnnotations() exposes Retention");
        check(hasTgt, "getAnnotations() exposes Target");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors (annotation)");
        // annotation types inherit equals/hashCode/toString/annotationType from java.lang.annotation.Annotation
        java.util.Set<String> pub = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) pub.add(m.getName());
        check(pub.contains("annotationType"), "exposes annotationType() from Annotation");
        check(pub.contains("equals"), "exposes equals() from Annotation");
        check(pub.contains("hashCode"), "exposes hashCode() from Annotation");
        check(pub.contains("toString"), "exposes toString() from Annotation");
        // cannot be applied to packages (TYPE-only target)
        check(Package.getPackage("javax.xml.bind.annotation").getAnnotation(XmlRegistry.class) == null,
                "TYPE-only target: package-level retrieval is null");
    }

    @XmlRegistry
    static class ObjectFactory {
        public String createPerson() {
            return "person";
        }
    }

    static class NotARegistry {}

    @XmlRegistry
    static class EmptyRegistry {}

    static void testMeta() throws Exception {
        Class<XmlRegistry> c = XmlRegistry.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.TYPE, "@Target({TYPE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
    }

    static void testReadBack() {
        XmlRegistry r = ObjectFactory.class.getAnnotation(XmlRegistry.class);
        check(r != null, "@XmlRegistry retrievable from class");
        check(r.annotationType() == XmlRegistry.class, "annotationType() identity");
        check("@javax.xml.bind.annotation.XmlRegistry()".equals(r.toString()), "marker annotation toString exact");
        check(r.equals(ObjectFactory.class.getAnnotation(XmlRegistry.class)), "repeated retrieval equal");
        check(r.hashCode() == ObjectFactory.class.getAnnotation(XmlRegistry.class).hashCode(), "hashCode consistent");
        check(r.equals(r), "equals self");
        check(!r.equals(null), "not equal to null");
        XmlRegistry e = EmptyRegistry.class.getAnnotation(XmlRegistry.class);
        check(e != null, "empty registry class also annotated");
        check(r.equals(e), "all marker instances with same type are equal");
        check(r.hashCode() == e.hashCode(), "marker hashCode uniform");
        check(NotARegistry.class.getAnnotation(XmlRegistry.class) == null, "unannotated class has no @XmlRegistry");
        // annotation not inherited
        check(NotARegistry.class.getAnnotation(XmlRegistry.class) == null, "no inheritance path for plain subclass");
        check(ObjectFactory.class.isAnnotationPresent(XmlRegistry.class), "isAnnotationPresent true");
    }
}
