/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters conformance: annotation interface and members.
 * @run main XmlJavaTypeAdaptersTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;


public class XmlJavaTypeAdaptersTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters");
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
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() throws Exception {
        Class<XmlJavaTypeAdapters> c = XmlJavaTypeAdapters.class;
        check("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters".equals(c.getName()), "FQCN exact");
        check("XmlJavaTypeAdapters".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation.adapters".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        boolean hasRet = false, hasTgt = false;
        for (java.lang.annotation.Annotation an : c.getAnnotations()) {
            if (an instanceof Retention) hasRet = true;
            if (an instanceof Target) hasTgt = true;
        }
        check(hasRet, "getAnnotations() exposes Retention");
        check(hasTgt, "getAnnotations() exposes Target");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(!Modifier.isStatic(v.getModifiers()), "value() not static");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        boolean fieldOk = false;
        for (ElementType t : c.getAnnotation(Target.class).value()) {
            if (t == ElementType.FIELD) fieldOk = true;
        }
        check(!fieldOk, "FIELD not among targets (package-level container only)");
        check(v.getReturnType().getComponentType() == XmlJavaTypeAdapter.class, "value() component type is XmlJavaTypeAdapter");
        check(c.isAssignableFrom(c), "isAssignableFrom self");
        check(Modifier.isPublic(c.getModifiers()), "public type");
        check(c.getTypeParameters().length == 0, "non-generic type");
        check(!c.isLocalClass() && !c.isMemberClass() && !c.isAnonymousClass(), "not a nested/local/anonymous class");
        check(!Package.getPackage("javax.xml.bind.annotation.adapters").isAnnotationPresent(XmlJavaTypeAdapters.class),
                "not present on its own package (needs explicit declaration)");
    }

    static void testMeta() throws Exception {
        Class<XmlJavaTypeAdapters> c = XmlJavaTypeAdapters.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.PACKAGE, "@Target({PACKAGE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "exactly 1 member");
        check("value".equals(declared[0].getName()), "single member named value");
        check(declared[0].getReturnType() == XmlJavaTypeAdapter[].class, "value() returns XmlJavaTypeAdapter[]");
        check(declared[0].getDefaultValue() == null, "value() has no default (required)");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
        check(Modifier.isPublic(declared[0].getModifiers()), "value() is public");
        // container pattern: @XmlJavaTypeAdapter itself is not @Repeatable in JAXB 2.2,
        // multiple package-level bindings must be wrapped in this container.
        check(!XmlJavaTypeAdapter.class.isAnnotationPresent(Repeatable.class), "contained @XmlJavaTypeAdapter not @Repeatable");
        check(XmlJavaTypeAdapter.class.isAnnotation(), "container holds annotation values only");
        check(c.isAnnotation(), "container is itself an annotation type");
        check(c.getDeclaredFields().length == 0, "no fields (pure annotation container)");
    }
}
