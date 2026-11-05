/*
 * @test
 * @summary javax.xml.bind.annotation.XmlSchema conformance: annotation interface and members.
 * @run main XmlSchemaTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;


public class XmlSchemaTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlSchema");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("xmlns"), "has method xmlns");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("elementFormDefault"), "has method elementFormDefault");
        check(names.contains("attributeFormDefault"), "has method attributeFormDefault");
        check(names.contains("location"), "has method location");
        testMeta();
        testDefaults();
        testPackageLevel();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<javax.xml.bind.annotation.XmlSchema> c = javax.xml.bind.annotation.XmlSchema.class;
        check("javax.xml.bind.annotation.XmlSchema".equals(c.getName()), "FQCN exact");
        check("XmlSchema".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        java.lang.reflect.Field[] fields = c.getDeclaredFields();
        check(fields.length == 1 && "NO_LOCATION".equals(fields[0].getName()),
                "single declared field NO_LOCATION (constants are retained on annotation types)");
    }

    static void testMeta() throws Exception {
        Class<javax.xml.bind.annotation.XmlSchema> c = javax.xml.bind.annotation.XmlSchema.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.PACKAGE, "@Target({PACKAGE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable (JAXB 2.2)");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 5, "exactly 5 members");
        check(declared.containsAll(Arrays.asList("xmlns", "namespace", "elementFormDefault", "attributeFormDefault", "location")), "member name set exact");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("elementFormDefault").getReturnType() == XmlNsForm.class, "elementFormDefault() returns XmlNsForm");
        check(c.getDeclaredMethod("attributeFormDefault").getReturnType() == XmlNsForm.class, "attributeFormDefault() returns XmlNsForm");
        check(c.getDeclaredMethod("location").getReturnType() == String.class, "location() returns String");
        check(c.getDeclaredMethod("xmlns").getReturnType() == XmlNs[].class, "xmlns() returns XmlNs[]");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
            check(Modifier.isPublic(m.getModifiers()), "member " + m.getName() + " is public");
        }
    }

    static void testDefaults() throws Exception {
        Class<javax.xml.bind.annotation.XmlSchema> c = javax.xml.bind.annotation.XmlSchema.class;
        check("".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default is empty string");
        check(c.getDeclaredMethod("elementFormDefault").getDefaultValue() == XmlNsForm.UNSET, "elementFormDefault() default UNSET");
        check(c.getDeclaredMethod("attributeFormDefault").getDefaultValue() == XmlNsForm.UNSET, "attributeFormDefault() default UNSET");
        Object xmlnsDefault = c.getDeclaredMethod("xmlns").getDefaultValue();
        check(xmlnsDefault instanceof XmlNs[] && ((XmlNs[]) xmlnsDefault).length == 0, "xmlns() default empty array");
        Object locDefault = c.getDeclaredMethod("location").getDefaultValue();
        check("##generate".equals(locDefault), "location() default \"##generate\"");
        check("##generate".equals(javax.xml.bind.annotation.XmlSchema.NO_LOCATION), "NO_LOCATION == \"##generate\"");
        check(javax.xml.bind.annotation.XmlSchema.NO_LOCATION.equals(locDefault), "NO_LOCATION equals location() default");
    }

    static void testPackageLevel() {
        // @XmlSchema is a package-level mapping annotation; in this RI the
        // javax.xml.bind.annotation package itself carries no schema mapping.
        Package p = Package.getPackage("javax.xml.bind.annotation");
        check(p != null, "javax.xml.bind.annotation package resolvable");
        check(p.getAnnotation(javax.xml.bind.annotation.XmlSchema.class) == null,
                "RI annotation package itself carries no @XmlSchema (quirk)");
        // non-package targets are rejected by the compiler; verify via @Target meta data
        Target tgt = javax.xml.bind.annotation.XmlSchema.class.getAnnotation(Target.class);
        boolean packageOnly = true;
        for (ElementType t : tgt.value()) {
            if (t != ElementType.PACKAGE) packageOnly = false;
        }
        check(packageOnly, "applicable to packages only");
    }
}
