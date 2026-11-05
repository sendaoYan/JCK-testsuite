/*
 * @test
 * @summary javax.xml.bind.annotation.XmlSchemaType conformance: annotation interface and members.
 * @run main XmlSchemaTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlSchemaType;


public class XmlSchemaTypeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlSchemaType");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("type"), "has method type");
        testMeta();
        testDefaults();
        testReadBack();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() throws Exception {
        Class<XmlSchemaType> c = XmlSchemaType.class;
        check("javax.xml.bind.annotation.XmlSchemaType".equals(c.getName()), "FQCN exact");
        check("XmlSchemaType".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        for (Method m : c.getDeclaredMethods()) {
            check(m.getParameterTypes().length == 0, m.getName() + " takes no parameters");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
        }
        check(XmlSchemaTypeTest.class.isAnnotationPresent(XmlSchemaType.class) == false,
                "TYPE not a target: class-level retrieval is null");
        XmlSchemaType onField = XmlSchemaTypeTest.class.getDeclaredField("dateField").getAnnotation(XmlSchemaType.class);
        check(onField.toString().contains("name=date"), "toString contains name pair");
    }

    @XmlSchemaType(name = "date")
    static String dateField;

    @XmlSchemaType(name = "gYear", namespace = "urn:custom", type = XmlSchemaType.DEFAULT.class)
    static int gYearField;

    @XmlSchemaType(name = "time")
    static String getTime() {
        return null;
    }

    static void testMeta() throws Exception {
        Class<XmlSchemaType> c = XmlSchemaType.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 3, "@Target has exactly 3 elements");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>(java.util.Arrays.asList(tgt.value()));
        check(ts.contains(ElementType.FIELD) && ts.contains(ElementType.METHOD) && ts.contains(ElementType.PACKAGE),
                "@Target = {FIELD, METHOD, PACKAGE}");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(Repeatable.class), "not @Repeatable (multiple declarations need @XmlSchemaTypes container)");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 3, "exactly 3 members");
        check(declared.contains("name") && declared.contains("namespace") && declared.contains("type"), "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("type").getReturnType() == Class.class, "type() returns Class");
    }

    static void testDefaults() throws Exception {
        Class<XmlSchemaType> c = XmlSchemaType.class;
        check(c.getDeclaredMethod("name").getDefaultValue() == null, "name() has no default (required)");
        check("http://www.w3.org/2001/XMLSchema".equals(c.getDeclaredMethod("namespace").getDefaultValue()),
                "namespace() default is the XSD namespace");
        check(c.getDeclaredMethod("type").getDefaultValue() == XmlSchemaType.DEFAULT.class,
                "type() default is XmlSchemaType.DEFAULT.class");
        Class<?> d = Class.forName("javax.xml.bind.annotation.XmlSchemaType$DEFAULT");
        check(Modifier.isPublic(d.getModifiers()) && Modifier.isStatic(d.getModifiers()) && Modifier.isFinal(d.getModifiers()),
                "DEFAULT is a public static final nested class");
        check(d.getEnclosingClass() == XmlSchemaType.class, "DEFAULT enclosed by XmlSchemaType");
    }

    static void testReadBack() throws Exception {
        XmlSchemaType onField = XmlSchemaTypeTest.class.getDeclaredField("dateField").getAnnotation(XmlSchemaType.class);
        check(onField != null, "@XmlSchemaType on field retrievable");
        check("date".equals(onField.name()), "name() read back");
        check("http://www.w3.org/2001/XMLSchema".equals(onField.namespace()), "namespace() default applied on read-back");
        check(onField.type() == XmlSchemaType.DEFAULT.class, "type() default applied on read-back");
        XmlSchemaType full = XmlSchemaTypeTest.class.getDeclaredField("gYearField").getAnnotation(XmlSchemaType.class);
        check("gYear".equals(full.name()), "explicit name read back");
        check("urn:custom".equals(full.namespace()), "explicit namespace read back");
        check(full.type() == XmlSchemaType.DEFAULT.class, "explicit DEFAULT type read back");
        XmlSchemaType onMethod = XmlSchemaTypeTest.class.getDeclaredMethod("getTime").getAnnotation(XmlSchemaType.class);
        check(onMethod != null, "@XmlSchemaType on method retrievable (METHOD target)");
        check("time".equals(onMethod.name()), "method-level name read back");
    }
}
