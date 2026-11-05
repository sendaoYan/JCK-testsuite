/*
 * @test
 * @summary javax.xml.bind.annotation.XmlType conformance: annotation interface and members.
 * @run main XmlTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlType;


public class XmlTypeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlType");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("propOrder"), "has method propOrder");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("factoryClass"), "has method factoryClass");
        check(names.contains("factoryMethod"), "has method factoryMethod");
        testMeta();
        testDefaults();
        testReadBack();
        System.out.println("PASS (" + passed + " assertions)");
    }

    @XmlType(name = "CustomName", propOrder = {"a", "b"}, namespace = "urn:t")
    static class CustomType {}

    @XmlType
    static class DefaultType {}

    @XmlType(propOrder = {})
    static class EmptyPropOrderType {}

    static class NotAnnotatedType {}

    static class FactoryClass {}

    @XmlType(factoryClass = FactoryClass.class, factoryMethod = "create")
    static class FactoryType {}

    static void testMeta() throws Exception {
        Class<XmlType> c = XmlType.class;
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.TYPE, "@Target({TYPE}) exactly");
        check(!c.isAnnotationPresent(Inherited.class), "not @Inherited");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 5, "exactly 5 members");
        check(declared.containsAll(java.util.Arrays.asList("name", "propOrder", "namespace", "factoryClass", "factoryMethod")),
                "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("propOrder").getReturnType() == String[].class, "propOrder() returns String[]");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("factoryClass").getReturnType() == Class.class, "factoryClass() returns Class");
        check(c.getDeclaredMethod("factoryMethod").getReturnType() == String.class, "factoryMethod() returns String");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<XmlType> c = XmlType.class;
        check("##default".equals(c.getDeclaredMethod("name").getDefaultValue()), "name() default \"##default\"");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
        check("".equals(c.getDeclaredMethod("factoryMethod").getDefaultValue()), "factoryMethod() default empty string");
        Object po = c.getDeclaredMethod("propOrder").getDefaultValue();
        check(po instanceof String[] && ((String[]) po).length == 1 && "".equals(((String[]) po)[0]),
                "propOrder() default is {\"\"} (single empty string, not empty array)");
        check(c.getDeclaredMethod("factoryClass").getDefaultValue() == XmlType.DEFAULT.class,
                "factoryClass() default is XmlType.DEFAULT.class");
        Class<?> d = Class.forName("javax.xml.bind.annotation.XmlType$DEFAULT");
        check(Modifier.isPublic(d.getModifiers()) && Modifier.isStatic(d.getModifiers()) && Modifier.isFinal(d.getModifiers()),
                "DEFAULT is a public static final nested class");
        check(d.getEnclosingClass() == XmlType.class, "DEFAULT enclosed by XmlType");
    }

    static void testReadBack() {
        XmlType t = CustomType.class.getAnnotation(XmlType.class);
        check(t != null, "@XmlType retrievable from class");
        check("CustomName".equals(t.name()), "explicit name read back");
        check("urn:t".equals(t.namespace()), "explicit namespace read back");
        check(t.propOrder().length == 2 && "a".equals(t.propOrder()[0]) && "b".equals(t.propOrder()[1]),
                "propOrder read back with declaration order preserved");
        check(t.factoryClass() == XmlType.DEFAULT.class, "factoryClass default applied on read-back");
        check("".equals(t.factoryMethod()), "factoryMethod default applied on read-back");
        check(t.annotationType() == XmlType.class, "annotationType() identity");
        XmlType d = DefaultType.class.getAnnotation(XmlType.class);
        check("##default".equals(d.name()), "bare @XmlType name is \"##default\"");
        check("##default".equals(d.namespace()), "bare @XmlType namespace is \"##default\"");
        check(d.propOrder().length == 1 && "".equals(d.propOrder()[0]), "bare @XmlType propOrder is {\"\"}");
        XmlType e = EmptyPropOrderType.class.getAnnotation(XmlType.class);
        check(e.propOrder().length == 0, "explicit empty propOrder {} differs from default {\"\"}");
        check(NotAnnotatedType.class.getAnnotation(XmlType.class) == null, "unannotated class has no @XmlType");
        XmlType f = FactoryType.class.getAnnotation(XmlType.class);
        check(f.factoryClass() == FactoryClass.class, "explicit factoryClass read back");
        check("create".equals(f.factoryMethod()), "explicit factoryMethod read back");
        check(f.name().equals(d.name()), "factory-style type still has default name");
        XmlType again = CustomType.class.getAnnotation(XmlType.class);
        check(t.equals(again), "repeated retrieval yields equal instance");
        check(t.hashCode() == again.hashCode(), "hashCode consistent across retrievals");
    }
}
