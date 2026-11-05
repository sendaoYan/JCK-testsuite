/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAccessorOrder conformance: annotation interface and members.
 * @run main XmlAccessorOrderTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;


public class XmlAccessorOrderTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAccessorOrder");
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

    static class DeepSub extends AlphabeticalSub {}

    static void testClassFacts() throws Exception {
        Class<XmlAccessorOrder> c = XmlAccessorOrder.class;
        check("javax.xml.bind.annotation.XmlAccessorOrder".equals(c.getName()), "FQCN exact");
        check("XmlAccessorOrder".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented (JDK-8)");
        Method v = c.getDeclaredMethod("value");
        check(v.getParameterTypes().length == 0, "value() takes no parameters");
        check(v.getExceptionTypes().length == 0, "value() declares no exceptions");
        check(!Modifier.isStatic(v.getModifiers()), "value() not static");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        XmlAccessorOrder deep = DeepSub.class.getAnnotation(XmlAccessorOrder.class);
        check(deep != null && deep.value() == XmlAccessOrder.ALPHABETICAL,
                "@Inherited propagates through two levels");
        check("@javax.xml.bind.annotation.XmlAccessorOrder(value=ALPHABETICAL)".equals(alphaToString()),
                "toString exact for explicit value");
        check("@javax.xml.bind.annotation.XmlAccessorOrder(value=UNDEFINED)".equals(UndefinedOrder.class
                .getAnnotation(XmlAccessorOrder.class).toString()), "toString exact for explicit UNDEFINED");
    }

    static String alphaToString() {
        return AlphabeticalBase.class.getAnnotation(XmlAccessorOrder.class).toString();
    }

    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    static class AlphabeticalBase {}

    static class AlphabeticalSub extends AlphabeticalBase {}

    @XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
    static class UndefinedOrder {}

    static class NoOrder {}

    static void testMeta() throws Exception {
        Class<XmlAccessorOrder> c = XmlAccessorOrder.class;
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
        check(declared[0].getReturnType() == XmlAccessOrder.class, "value() returns XmlAccessOrder");
        check(declared[0].getDefaultValue() == XmlAccessOrder.UNDEFINED, "value() default UNDEFINED");
        check(Modifier.isAbstract(declared[0].getModifiers()), "value() is abstract");
    }

    static void testReadBack() {
        XmlAccessorOrder alpha = AlphabeticalBase.class.getAnnotation(XmlAccessorOrder.class);
        check(alpha != null, "@XmlAccessorOrder retrievable from class");
        check(alpha.value() == XmlAccessOrder.ALPHABETICAL, "ALPHABETICAL read back");
        check(alpha.annotationType() == XmlAccessorOrder.class, "annotationType() identity");
        XmlAccessorOrder undef = UndefinedOrder.class.getAnnotation(XmlAccessorOrder.class);
        check(undef != null && undef.value() == XmlAccessOrder.UNDEFINED, "explicit UNDEFINED read back");
        check(NoOrder.class.getAnnotation(XmlAccessorOrder.class) == null, "unannotated class has no @XmlAccessorOrder");
        XmlAccessorOrder inherited = AlphabeticalSub.class.getAnnotation(XmlAccessorOrder.class);
        check(inherited != null && inherited.value() == XmlAccessOrder.ALPHABETICAL,
                "@Inherited: subclass sees superclass @XmlAccessorOrder");
        check(alpha.equals(inherited), "inherited instance equals superclass instance");
        check(alpha.hashCode() == inherited.hashCode(), "annotation hashCode consistent");
        check(alpha.toString().startsWith("@javax.xml.bind.annotation.XmlAccessorOrder"), "toString format");
        check(!alpha.equals(undef), "different values not equal");
    }
}
