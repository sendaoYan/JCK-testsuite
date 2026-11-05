/*
 * @test
 * @summary javax.xml.bind.annotation.XmlNs conformance: annotation interface and members.
 * @run main XmlNsTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlNsTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlNs");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("prefix"), "has method prefix");
        check(names.contains("namespaceURI"), "has method namespaceURI");
        testMeta();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() throws Exception {
        Class<javax.xml.bind.annotation.XmlNs> c = javax.xml.bind.annotation.XmlNs.class;
        check("javax.xml.bind.annotation.XmlNs".equals(c.getName()), "FQCN exact");
        check("XmlNs".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation.XmlNs".equals(c.getCanonicalName()), "canonical name exact");
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
        for (Method m : c.getDeclaredMethods()) {
            check(m.getParameterTypes().length == 0, m.getName() + " takes no parameters");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
        }
        check(c.getDeclaredFields().length == 0, "no declared fields");
        // empty @Target means the annotation can never be applied directly anywhere
        check(!XmlNsTest.class.isAnnotationPresent(javax.xml.bind.annotation.XmlNs.class),
                "cannot be present on this class (empty @Target enforced)");
        check(javax.xml.bind.annotation.XmlSchema.class.isAnnotationPresent(javax.xml.bind.annotation.XmlNs.class) == false,
                "cannot be present on another annotation type");
    }

    static void testMeta() throws Exception {
        Class<javax.xml.bind.annotation.XmlNs> c = javax.xml.bind.annotation.XmlNs.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 0, "@Target is empty: cannot be applied standalone (nested in @XmlSchema only)");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 2, "exactly 2 members");
        check(declared.contains("prefix") && declared.contains("namespaceURI"), "member name set exact");
        check(c.getDeclaredMethod("prefix").getReturnType() == String.class, "prefix() returns String");
        check(c.getDeclaredMethod("namespaceURI").getReturnType() == String.class, "namespaceURI() returns String");
        check(c.getDeclaredMethod("prefix").getDefaultValue() == null, "prefix() has no default (required)");
        check(c.getDeclaredMethod("namespaceURI").getDefaultValue() == null, "namespaceURI() has no default (required)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
            check(Modifier.isPublic(m.getModifiers()), "member " + m.getName() + " is public");
        }
        // cross-contract: @XmlSchema.xmlns() is the only nesting point for @XmlNs
        java.lang.reflect.Method xmlns = javax.xml.bind.annotation.XmlSchema.class.getDeclaredMethod("xmlns");
        check(xmlns.getReturnType() == javax.xml.bind.annotation.XmlNs[].class, "@XmlSchema.xmlns() component type is XmlNs");
        check(xmlns.getReturnType().getComponentType() == javax.xml.bind.annotation.XmlNs.class, "component type identity");
    }
}
